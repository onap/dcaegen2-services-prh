/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.services.prh.integration;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.ClassLoader.getSystemResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.prh.MainApp;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.tasks.DmaapConsumerTaskImpl;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasks;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasksRunner;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

/**
 * End-to-end integration tests for the PRH workflow (default/prod profile).
 *
 * <p>These tests verify ALL external HTTP communication of the PRH service:
 * <ul>
 *   <li>A&amp;AI – query PNF (GET), update PNF (PATCH), service-instance lookup (GET)</li>
 *   <li>A&amp;AI – BBS logical-link CRUD (GET / PUT / DELETE)</li>
 *   <li>DMaaP Message Router – publish (mocked at the SDK boundary since
 *       the DMaaP SDK internally forces Kafka transport when JAAS_CONFIG is set)</li>
 * </ul>
 *
 * <p>Events are injected directly into the {@link DmaapConsumerTaskImpl} message
 * buffer via {@code onMessage()}, bypassing the Kafka listener container
 * (which is disabled via {@code setAutoStartup(false)} in the test config).
 * The publisher is still mocked at the SDK boundary (Phase 2 will replace it).
 */
@SpringBootTest(properties = {"spring.kafka.listener.auto-startup=false"})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles(value = "prod")
class PrhWorkflowEndToEndIntegrationTest {

    @Autowired
    private ScheduledTasks scheduledTasks;

    @SpyBean
    private CbsConfiguration cbsConfiguration;

    @MockBean
    private ScheduledTasksRunner scheduledTasksRunner;  // disable auto-scheduling

    @Autowired
    private DmaapConsumerTaskImpl dmaapConsumerTaskImpl;

    private MessageRouterPublisher publisher;

    private ArgumentCaptor<MessageRouterPublishRequest> publishRequestCaptor;

    @Configuration
    @Import(MainApp.class)
    static class TestConfig {

        @Value("http://localhost:${wiremock.server.port}")
        private String wiremockServerAddress;

        @Bean
        public CbsConfiguration cbsConfiguration() throws Exception {
            JsonObject cbsConfigJson = new Gson().fromJson(
                    getResourceContent("configurationFromCbs.json")
                            .replaceAll("https?://dmaap-mr[\\w.]*:\\d+", wiremockServerAddress)
                            .replaceAll("https?://aai[\\w.]*:\\d+", wiremockServerAddress),
                    JsonObject.class);

            CbsConfiguration cbsConfiguration = new CbsConfiguration();
            withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
                    .and("BOOTSTRAP_SERVERS", "localhost:9092")
                    .execute(() -> cbsConfiguration.parseCBSConfig(cbsConfigJson));
            return cbsConfiguration;
        }
    }

    @BeforeEach
    void setup() {
        WireMock.reset();
        dmaapConsumerTaskImpl.execute();  // drain any leftover events
        publisher = Mockito.mock(MessageRouterPublisher.class);
        publishRequestCaptor = ArgumentCaptor.forClass(MessageRouterPublishRequest.class);
        when(cbsConfiguration.getMessageRouterPublisher()).thenReturn(publisher);
        when(publisher.put(any(MessageRouterPublishRequest.class), any()))
                .thenReturn(Flux.just(ImmutableMessageRouterPublishResponse.builder().build()));
    }

    // ==================== Scenario 1: Empty DMaaP response ====================

    @Test
    void whenDmaapReturnsNoEvents_noAaiOrPublishCallsShouldBeMade() {
        // No events injected — buffer is empty

        scheduledTasks.scheduleMainPrhEventTask();

        // No AAI calls should be made
        verify(0, getRequestedFor(urlPathMatching("/aai.*")));
        verify(0, patchRequestedFor(urlPathMatching("/aai.*")));

        // No DMaaP publish should occur
        Mockito.verify(publisher, never()).put(any(), any());
    }

    // ==================== Scenario 2: First registration (no service-instance relation) ====================

    @Test
    void whenPnfHasNoServiceRelation_shouldPatchAaiAndPublishPnfReady() {
        String event = getResourceContent("integration/event.json");
        String pnfName = "NOK6061ZW8";

        injectEvents(event);

        // Stub A&AI GET PNF – return PNF without relationship-list (first registration)
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"pnf-name\":\"" + pnfName + "\"}")));

        // Stub A&AI PATCH PNF
        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok()));

        scheduledTasks.scheduleMainPrhEventTask();

        // Verify: AAI GET PNF was called
        verify(1, getRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));

        // Verify: AAI PATCH was made with the correct PNF data
        verify(1, patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .withHeader("Content-Type", equalTo("application/merge-patch+json"))
                .withRequestBody(matchingJsonPath("$.correlationId", equalTo(pnfName)))
                .withRequestBody(matchingJsonPath("$.['ipaddress-v4-oam']", equalTo("val3")))
                .withRequestBody(matchingJsonPath("$.['ipaddress-v6-oam']", equalTo("val4")))
                .withRequestBody(matchingJsonPath("$.['serial-number']", equalTo("6061ZW9")))
                .withRequestBody(matchingJsonPath("$.['equip-vendor']", equalTo("Nokia")))
                .withRequestBody(matchingJsonPath("$.['equip-model']", equalTo("val6")))
                .withRequestBody(matchingJsonPath("$.['equip-type']", equalTo("val8")))
                .withRequestBody(matchingJsonPath("$.['sw-version']", equalTo("val7"))));

        // Verify: published to PNF_READY (not PNF_UPDATE)
        Mockito.verify(publisher, times(1)).put(publishRequestCaptor.capture(), any());
        assertThat(publishRequestCaptor.getValue().sinkDefinition().topicUrl())
                .contains("PNF_READY");
    }

    // ==================== Scenario 3: Re-registration (active service-instance) ====================

    @Test
    void whenPnfHasActiveServiceInstance_shouldPatchAaiAndPublishPnfUpdate() {
        String event = getResourceContent("integration/event.json");
        String pnfName = "NOK6061ZW8";

        injectEvents(event);

        // Stub A&AI GET PNF – PNF with service-instance relationship
        String pnfWithServiceRelation = "{"
                + "\"pnf-name\":\"" + pnfName + "\","
                + "\"relationship-list\":{\"relationship\":[{"
                + "  \"related-to\":\"service-instance\","
                + "  \"relationship-data\":["
                + "    {\"relationship-key\":\"customer.global-customer-id\",\"relationship-value\":\"Demonstration\"},"
                + "    {\"relationship-key\":\"service-subscription.service-type\",\"relationship-value\":\"vFWCL\"},"
                + "    {\"relationship-key\":\"service-instance.service-instance-id\",\"relationship-value\":\"service-1\"}"
                + "  ]"
                + "}]}"
                + "}";
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody(pnfWithServiceRelation)));

        // Stub A&AI GET service-instance – return Active status
        String serviceInstancePath = "/aai/v23/business/customers/customer/Demonstration"
                + "/service-subscriptions/service-subscription/vFWCL"
                + "/service-instances/service-instance/service-1";
        stubFor(get(urlEqualTo(serviceInstancePath))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"orchestration-status\":\"Active\"}")));

        // Stub A&AI PATCH PNF
        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok()));

        scheduledTasks.scheduleMainPrhEventTask();

        // Verify: AAI GET PNF was queried
        verify(1, getRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));

        // Verify: service-instance was queried
        verify(1, getRequestedFor(urlEqualTo(serviceInstancePath)));

        // Verify: AAI PATCH was made
        verify(1, patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));

        // Verify: published to PNF_UPDATE (not PNF_READY) because it's a re-registration
        Mockito.verify(publisher, times(1)).put(publishRequestCaptor.capture(), any());
        assertThat(publishRequestCaptor.getValue().sinkDefinition().topicUrl())
                .contains("PNF_UPDATE");
    }

    // ==================== Scenario 4: First reg with inactive service-instance ====================

    @Test
    void whenPnfHasInactiveServiceInstance_shouldPublishToPnfReady() {
        String event = getResourceContent("integration/event.json");
        String pnfName = "NOK6061ZW8";

        injectEvents(event);

        // PNF with service-instance relationship but Inactive
        String pnfWithServiceRelation = "{"
                + "\"pnf-name\":\"" + pnfName + "\","
                + "\"relationship-list\":{\"relationship\":[{"
                + "  \"related-to\":\"service-instance\","
                + "  \"relationship-data\":["
                + "    {\"relationship-key\":\"customer.global-customer-id\",\"relationship-value\":\"Cust1\"},"
                + "    {\"relationship-key\":\"service-subscription.service-type\",\"relationship-value\":\"Sub1\"},"
                + "    {\"relationship-key\":\"service-instance.service-instance-id\",\"relationship-value\":\"si-1\"}"
                + "  ]"
                + "}]}"
                + "}";
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody(pnfWithServiceRelation)));

        String serviceInstancePath = "/aai/v23/business/customers/customer/Cust1"
                + "/service-subscriptions/service-subscription/Sub1"
                + "/service-instances/service-instance/si-1";
        stubFor(get(urlEqualTo(serviceInstancePath))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"orchestration-status\":\"Inactive\"}")));

        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok()));

        scheduledTasks.scheduleMainPrhEventTask();

        // Inactive means not a re-registration → PNF_READY
        Mockito.verify(publisher, times(1)).put(publishRequestCaptor.capture(), any());
        assertThat(publishRequestCaptor.getValue().sinkDefinition().topicUrl())
                .contains("PNF_READY");
    }

    // ==================== Scenario 5: BBS attachment-point handling ====================

    @Test
    void whenEventHasAttachmentPoint_shouldDeleteOldAndCreateNewLogicalLink() {
        String event = getResourceContent("integration/event-with-attachment-point.json");
        String pnfName = "NOK6061ZW3";

        injectEvents(event);

        // Stub A&AI GET PNF – return PNF with an existing logical-link relationship
        // (no service-instance → first registration → BBS actions will run)
        String pnfWithLogicalLink = "{"
                + "\"pnf-name\":\"" + pnfName + "\","
                + "\"relationship-list\":{\"relationship\":[{"
                + "  \"related-to\":\"logical-link\","
                + "  \"relationship-label\":\"org.onap.relationships.inventory.BridgedTo\","
                + "  \"related-link\":\"/aai/v23/network/logical-links/logical-link/old-link\","
                + "  \"relationship-data\":[{\"relationship-key\":\"logical-link.link-name\",\"relationship-value\":\"old-link\"}]"
                + "}]}"
                + "}";
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody(pnfWithLogicalLink)));

        // Stub A&AI PATCH PNF
        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok()));

        // Stub GET for old logical-link
        String oldLogicalLink = "{"
                + "\"link-name\":\"old-link\","
                + "\"link-type\":\"attachment-point\","
                + "\"resource-version\":\"1560171816043\""
                + "}";
        stubFor(get(urlEqualTo("/aai/v23/network/logical-links/logical-link/old-link"))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody(oldLogicalLink)));

        // Stub DELETE old logical-link
        stubFor(delete(urlEqualTo("/aai/v23/network/logical-links/logical-link/old-link?resource-version=1560171816043"))
                .willReturn(ok()));

        // Stub PUT new logical-link
        stubFor(put(urlEqualTo("/aai/v23/network/logical-links/logical-link/olt-bbs-cpe-1"))
                .willReturn(aResponse().withStatus(201)));

        scheduledTasks.scheduleMainPrhEventTask();

        // Verify: AAI PATCH PNF was called
        verify(1, patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));

        // Verify: BBS old logical-link was fetched
        verify(1, getRequestedFor(urlEqualTo("/aai/v23/network/logical-links/logical-link/old-link")));

        // Verify: BBS old logical-link was deleted
        verify(1, deleteRequestedFor(
                urlEqualTo("/aai/v23/network/logical-links/logical-link/old-link?resource-version=1560171816043")));

        // Verify: BBS new logical-link was created with correct body
        verify(1, putRequestedFor(urlEqualTo("/aai/v23/network/logical-links/logical-link/olt-bbs-cpe-1"))
                .withRequestBody(matchingJsonPath("$.['link-name']", equalTo("olt-bbs-cpe-1")))
                .withRequestBody(matchingJsonPath("$.['link-type']", equalTo("attachment-point"))));

        // Verify: published to PNF_READY (first registration)
        Mockito.verify(publisher, times(1)).put(publishRequestCaptor.capture(), any());
        assertThat(publishRequestCaptor.getValue().sinkDefinition().topicUrl())
                .contains("PNF_READY");
    }

    // ==================== Scenario 6: Re-registration skips BBS actions ====================

    @Test
    void whenReRegistration_bbsLogicalLinksShouldNotBeUpdated() {
        String event = getResourceContent("integration/event-with-attachment-point.json");
        String pnfName = "NOK6061ZW3";

        injectEvents(event);

        // PNF with both logical-link AND active service-instance (re-registration)
        String pnfBody = "{"
                + "\"pnf-name\":\"" + pnfName + "\","
                + "\"relationship-list\":{\"relationship\":["
                + "  {\"related-to\":\"logical-link\","
                + "   \"relationship-data\":[{\"relationship-key\":\"logical-link.link-name\",\"relationship-value\":\"old-link\"}]},"
                + "  {\"related-to\":\"service-instance\","
                + "   \"relationship-data\":["
                + "     {\"relationship-key\":\"customer.global-customer-id\",\"relationship-value\":\"Cust1\"},"
                + "     {\"relationship-key\":\"service-subscription.service-type\",\"relationship-value\":\"Sub1\"},"
                + "     {\"relationship-key\":\"service-instance.service-instance-id\",\"relationship-value\":\"si-1\"}"
                + "   ]}"
                + "]}}";
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(pnfBody)));

        String serviceInstancePath = "/aai/v23/business/customers/customer/Cust1"
                + "/service-subscriptions/service-subscription/Sub1"
                + "/service-instances/service-instance/si-1";
        stubFor(get(urlEqualTo(serviceInstancePath))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"orchestration-status\":\"Active\"}")));

        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok()));

        scheduledTasks.scheduleMainPrhEventTask();

        // Verify: no logical-link operations occurred (BBS skipped for re-registration)
        verify(0, getRequestedFor(urlPathMatching("/aai/v23/network/logical-links/.*")));
        verify(0, putRequestedFor(urlPathMatching("/aai/v23/network/logical-links/.*")));
        verify(0, deleteRequestedFor(urlPathMatching("/aai/v23/network/logical-links/.*")));

        // Verify: published to PNF_UPDATE
        Mockito.verify(publisher, times(1)).put(publishRequestCaptor.capture(), any());
        assertThat(publishRequestCaptor.getValue().sinkDefinition().topicUrl())
                .contains("PNF_UPDATE");
    }

    // ==================== Scenario 7: Event without attachment-point, no BBS ====================

    @Test
    void whenEventHasNoAttachmentPoint_noLogicalLinkActionsShouldOccur() {
        String event = getResourceContent("integration/event.json");
        String pnfName = "NOK6061ZW8";

        injectEvents(event);

        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"pnf-name\":\"" + pnfName + "\"}")));

        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok()));

        scheduledTasks.scheduleMainPrhEventTask();

        // No logical-link operations when there's no attachment-point
        verify(0, getRequestedFor(urlPathMatching("/aai/v23/network/logical-links/.*")));
        verify(0, putRequestedFor(urlPathMatching("/aai/v23/network/logical-links/.*")));
        verify(0, deleteRequestedFor(urlPathMatching("/aai/v23/network/logical-links/.*")));

        Mockito.verify(publisher, times(1)).put(publishRequestCaptor.capture(), any());
        assertThat(publishRequestCaptor.getValue().sinkDefinition().topicUrl())
                .contains("PNF_READY");
    }

    // ==================== Scenario 8: AAI headers are sent correctly ====================

    @Test
    void allAaiRequestsShouldContainRequiredHeaders() {
        String event = getResourceContent("integration/event.json");
        String pnfName = "NOK6061ZW8";

        injectEvents(event);

        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"pnf-name\":\"" + pnfName + "\"}")));

        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok()));

        scheduledTasks.scheduleMainPrhEventTask();

        // Verify: AAI GET contains required headers
        verify(getRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .withHeader("X-FromAppId", equalTo("prh"))
                .withHeader("X-TransactionId", equalTo("9999"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Authorization", equalTo("Basic QUFJOkFBSQ==")));

        // Verify: AAI PATCH contains merge-patch content type and AAI headers
        verify(patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .withHeader("Content-Type", equalTo("application/merge-patch+json"))
                .withHeader("X-FromAppId", equalTo("prh"))
                .withHeader("Authorization", equalTo("Basic QUFJOkFBSQ==")));
    }

    // ==================== Scenario 9: DMaaP publish message structure ====================

    @Test
    void pnfReadyMessageShouldContainCorrelationIdAndAdditionalFields() {
        String event = getResourceContent("integration/event-with-attachment-point.json");
        String pnfName = "NOK6061ZW3";

        injectEvents(event);

        // PNF without logical-link relationships (no old link to delete)
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"pnf-name\":\"" + pnfName + "\"}")));

        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok()));

        // BBS creates new logical-link
        stubFor(put(urlEqualTo("/aai/v23/network/logical-links/logical-link/olt-bbs-cpe-1"))
                .willReturn(aResponse().withStatus(201)));

        scheduledTasks.scheduleMainPrhEventTask();

        // Verify publisher was called with the PNF_READY topic
        Mockito.verify(publisher, times(1)).put(publishRequestCaptor.capture(), any());
        assertThat(publishRequestCaptor.getValue().sinkDefinition().topicUrl())
                .contains("PNF_READY");
    }

    // ==================== Scenario 10: Multiple events in single batch ====================

    @Test
    void whenMultipleEventsInDmaapBatch_allShouldBeProcessed() {
        String event1 = getResourceContent("integration/event.json");
        String event2 = event1.replace("NOK6061ZW8", "NOK6061ZW9");

        injectEvents(event1, event2);

        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/NOK6061ZW8"))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"pnf-name\":\"NOK6061ZW8\"}")));
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/NOK6061ZW9"))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"pnf-name\":\"NOK6061ZW9\"}")));

        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/NOK6061ZW8")).willReturn(ok()));
        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/NOK6061ZW9")).willReturn(ok()));

        scheduledTasks.scheduleMainPrhEventTask();

        // Both PNFs should be patched in AAI
        verify(1, patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/NOK6061ZW8")));
        verify(1, patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/NOK6061ZW9")));

        // Both should trigger DMaaP publish to PNF_READY
        Mockito.verify(publisher, times(2)).put(any(), any());
    }


    // ==================== Helpers ====================

    private void injectEvents(String... events) {
        List<ConsumerRecord<String, String>> records = new ArrayList<>();
        for (String event : events) {
            records.add(new ConsumerRecord<>("test-topic", 0, 0, null, event));
        }
        dmaapConsumerTaskImpl.onMessage(records, Mockito.mock(Acknowledgment.class));
    }

    private static String getResourceContent(String resourceName) {
        try {
            return new String(Files.readAllBytes(Paths.get(getSystemResource(resourceName).toURI())));
        } catch (Exception e) {
            throw new RuntimeException("failed loading content of '" + resourceName + "'", e);
        }
    }
}
