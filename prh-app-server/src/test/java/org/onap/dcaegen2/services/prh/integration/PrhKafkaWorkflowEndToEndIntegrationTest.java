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
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static java.lang.ClassLoader.getSystemResource;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.vavr.collection.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.dcaegen2.services.prh.MainApp;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.configuration.CbsConfigurationForAutoCommitDisabledMode;
import org.onap.dcaegen2.services.prh.adapter.kafka.ImmutableKafkaConfiguration;
import org.onap.dcaegen2.services.prh.adapter.kafka.KafkaConfiguration;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.prh.tasks.commit.KafkaConsumerTaskImpl;
import org.onap.dcaegen2.services.prh.tasks.commit.ScheduledTasksRunnerWithCommit;
import org.onap.dcaegen2.services.prh.tasks.commit.ScheduledTasksWithCommit;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * End-to-end integration tests for the PRH workflow (autoCommitDisabled profile / Kafka mode).
 *
 * In this mode, the Kafka consumer is wired via Spring {@code @KafkaListener}. Since the
 * consumer stores events internally and the workflow polls them, we mock the KafkaConsumerTaskImpl
 * to inject events (simulating what Kafka would deliver), while all downstream communication
 * (A&AI, DMaaP publish) is tested via WireMock.
 *
 * This verifies:
 * <ul>
 *   <li>A&AI PNF existence check (findPnfinAAI) – extra step in autoCommitDisabled mode</li>
 *   <li>A&AI query, patch, service-instance lookup</li>
 *   <li>DMaaP publish to PNF_READY or PNF_UPDATE</li>
 *   <li>BBS logical-link operations</li>
 *   <li>Offset commit behaviour</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles(value = "autoCommitDisabled")
class PrhKafkaWorkflowEndToEndIntegrationTest {

    @Autowired
    private ScheduledTasksWithCommit scheduledTasksWithCommit;

    @MockBean
    private ScheduledTasksRunnerWithCommit scheduledTasksRunnerWithCommit;

    @MockBean
    private KafkaConsumerTaskImpl kafkaConsumerTaskImpl;

    @Autowired
    private DmaapConsumerJsonParser dmaapConsumerJsonParser;

    @SpyBean
    CbsConfiguration cbsConfiguration;

    @Mock
    MessageRouterPublisher publisher;

    @Configuration
    @Import(MainApp.class)
    static class TestConfig {

        @Value("http://localhost:${wiremock.server.port}")
        private String wiremockServerAddress;

        protected KafkaConfiguration kafkaConfiguration;

        @Bean
        public CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode() {
            JsonObject cbsConfigJson = new Gson()
                    .fromJson(getResourceContent("autoCommitDisabledConfigurationFromCbs2.json")
                                    .replaceAll("https?://dmaap-mr[\\w.]*:\\d+", wiremockServerAddress)
                                    .replaceAll("https?://aai[\\w.]*:\\d+", wiremockServerAddress),
                            JsonObject.class);

            CbsConfigurationForAutoCommitDisabledMode config =
                    new CbsConfigurationForAutoCommitDisabledMode();

            try {
                withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
                        .and("BOOTSTRAP_SERVERS", "localhost:9092")
                        .execute(() -> config.parseCBSConfig(cbsConfigJson));
            } catch (Exception e) {
                if ("kafkaJaasConfig".equals(e.getMessage())) {
                    kafkaConfiguration = new ImmutableKafkaConfiguration.Builder()
                            .kafkaBoostrapServerConfig("0.0.0.0")
                            .groupIdConfig("CG1")
                            .kafkaSaslMechanism("SASL_MECHANISM")
                            .kafkaSecurityProtocol("SEC-PROTOCOL")
                            .kafkaJaasConfig("JAAS_CONFIG")
                            .build();
                    config.setKafkaConfiguration(kafkaConfiguration);
                }
            }
            return config;
        }
    }

    @BeforeEach
    void resetWireMock() {
        WireMock.reset();
    }

    // ==================== Scenario 1: No events from Kafka ====================

    @Test
    void whenKafkaHasNoEvents_noAaiOrPublishCallsShouldBeMade() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config").execute(() -> {
            when(kafkaConsumerTaskImpl.execute()).thenReturn(Flux.empty());

            scheduledTasksWithCommit.scheduleKafkaPrhEventTask();

            com.github.tomakehurst.wiremock.client.WireMock.verify(
                    0, anyRequestedFor(urlPathMatching("/aai.*")));
            com.github.tomakehurst.wiremock.client.WireMock.verify(
                    0, postRequestedFor(urlPathMatching("/events.*")));
        });
    }

    // ==================== Scenario 2: First registration via Kafka ====================

    @Test
    void whenKafkaEventReceived_shouldQueryAaiPatchAndPublishPnfReady() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config").execute(() -> {
            String event = getResourceContent("integration/event.json");
            String pnfName = "NOK6061ZW8";

            java.util.List<String> eventList = new ArrayList<>();
            eventList.add(event);
            Flux<ConsumerDmaapModel> fluxList = dmaapConsumerJsonParser
                    .getConsumerDmaapModelFromKafkaConsumerRecord(eventList);

            // Stub A&AI GET PNF (for findPnfinAAI + queryAaiForConfiguration)
            stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                    .willReturn(ok().withHeader("Content-Type", "application/json")
                            .withBody("{\"pnf-name\":\"" + pnfName + "\"}")));

            // Stub A&AI PATCH PNF
            stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                    .willReturn(ok()));

            // Stub DMaaP publish
            stubFor(post(urlEqualTo("/events/unauthenticated.PNF_READY"))
                    .willReturn(ok().withBody("[1]")));

            when(kafkaConsumerTaskImpl.execute()).thenReturn(fluxList);

            // Mock the DMaaP publisher since it's used via SDK
            List<String> expectedItems = List.of(event);
            Flux<MessageRouterPublishResponse> pubresp = Flux.just(
                    ImmutableMessageRouterPublishResponse.builder()
                            .items(expectedItems.map(JsonPrimitive::new))
                            .build());
            when(cbsConfiguration.getMessageRouterPublisher()).thenReturn(publisher);
            when(publisher.put(any(MessageRouterPublishRequest.class), any())).thenReturn(pubresp);

            scheduledTasksWithCommit.scheduleKafkaPrhEventTask();

            // Verify: AAI GET PNF called (findPnfinAAI + queryAaiForConfiguration = 2 calls)
            com.github.tomakehurst.wiremock.client.WireMock.verify(
                    2, getRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));

            // Verify: AAI PATCH called
            com.github.tomakehurst.wiremock.client.WireMock.verify(
                    1, patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                            .withHeader("Content-Type", equalTo("application/merge-patch+json"))
                            .withRequestBody(matchingJsonPath("$.correlationId", equalTo(pnfName))));

            // Verify: DMaaP publisher was called (PNF_READY)
            verify(publisher, times(1)).put(any(MessageRouterPublishRequest.class), any());
        });
    }

    // ==================== Scenario 3: Re-registration via Kafka ====================

    @Test
    void whenKafkaEventForReRegistration_shouldPublishPnfUpdate() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config").execute(() -> {
            String event = getResourceContent("integration/event.json");
            String pnfName = "NOK6061ZW8";

            java.util.List<String> eventList = new ArrayList<>();
            eventList.add(event);
            Flux<ConsumerDmaapModel> fluxList = dmaapConsumerJsonParser
                    .getConsumerDmaapModelFromKafkaConsumerRecord(eventList);

            // PNF with active service-instance relationship
            String pnfBody = "{"
                    + "\"pnf-name\":\"" + pnfName + "\","
                    + "\"relationship-list\":{\"relationship\":[{"
                    + "  \"related-to\":\"service-instance\","
                    + "  \"relationship-data\":["
                    + "    {\"relationship-key\":\"customer.global-customer-id\",\"relationship-value\":\"Demo\"},"
                    + "    {\"relationship-key\":\"service-subscription.service-type\",\"relationship-value\":\"vFW\"},"
                    + "    {\"relationship-key\":\"service-instance.service-instance-id\",\"relationship-value\":\"si-1\"}"
                    + "  ]"
                    + "}]}"
                    + "}";
            stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                    .willReturn(ok().withHeader("Content-Type", "application/json").withBody(pnfBody)));

            String serviceInstancePath = "/aai/v23/business/customers/customer/Demo"
                    + "/service-subscriptions/service-subscription/vFW"
                    + "/service-instances/service-instance/si-1";
            stubFor(get(urlEqualTo(serviceInstancePath))
                    .willReturn(ok().withHeader("Content-Type", "application/json")
                            .withBody("{\"orchestration-status\":\"Active\"}")));

            stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                    .willReturn(ok()));

            when(kafkaConsumerTaskImpl.execute()).thenReturn(fluxList);

            List<String> expectedItems = List.of(event);
            Flux<MessageRouterPublishResponse> pubresp = Flux.just(
                    ImmutableMessageRouterPublishResponse.builder()
                            .items(expectedItems.map(JsonPrimitive::new))
                            .build());
            when(cbsConfiguration.getMessageRouterPublisher()).thenReturn(publisher);
            when(publisher.put(any(MessageRouterPublishRequest.class), any())).thenReturn(pubresp);

            scheduledTasksWithCommit.scheduleKafkaPrhEventTask();

            // Verify: service instance was queried
            com.github.tomakehurst.wiremock.client.WireMock.verify(
                    1, getRequestedFor(urlEqualTo(serviceInstancePath)));

            // Verify: PNF was patched
            com.github.tomakehurst.wiremock.client.WireMock.verify(
                    1, patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));

            // Verify: DMaaP publisher was invoked
            verify(publisher, times(1)).put(any(MessageRouterPublishRequest.class), any());
        });
    }

    // ==================== Scenario 4: PNF not found in AAI – offset not committed ====================

    @Test
    void whenPnfNotFoundInAai_offsetShouldNotBeCommitted() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config").execute(() -> {
            String event = getResourceContent("integration/event.json");
            String pnfName = "NOK6061ZW8";

            java.util.List<String> eventList = new ArrayList<>();
            eventList.add(event);
            Flux<ConsumerDmaapModel> fluxList = dmaapConsumerJsonParser
                    .getConsumerDmaapModelFromKafkaConsumerRecord(eventList);

            // PNF not found in AAI – return 404
            stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                    .willReturn(aResponse().withStatus(404)));

            when(kafkaConsumerTaskImpl.execute()).thenReturn(fluxList);

            scheduledTasksWithCommit.scheduleKafkaPrhEventTask();

            // Verify: no PATCH was attempted (workflow should abort after findPnfinAAI fails)
            com.github.tomakehurst.wiremock.client.WireMock.verify(
                    0, patchRequestedFor(urlPathMatching("/aai.*")));

            // Verify: commitOffset was NOT called (offset should not be committed)
            verify(kafkaConsumerTaskImpl, times(0)).commitOffset();
        });
    }

    // ==================== Scenario 5: Kafka event with attachment-point ====================

    @Test
    void whenKafkaEventHasAttachmentPoint_shouldHandleBbsLogicalLinks() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config").execute(() -> {
            String event = getResourceContent("integration/event-with-attachment-point.json");
            String pnfName = "NOK6061ZW3";

            java.util.List<String> eventList = new ArrayList<>();
            eventList.add(event);
            Flux<ConsumerDmaapModel> fluxList = dmaapConsumerJsonParser
                    .getConsumerDmaapModelFromKafkaConsumerRecord(eventList);

            // PNF exists but with no service-instance (first registration with attachment-point)
            stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                    .willReturn(ok().withHeader("Content-Type", "application/json")
                            .withBody("{\"pnf-name\":\"" + pnfName + "\"}")));

            stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                    .willReturn(ok()));

            // BBS: new logical-link creation
            stubFor(com.github.tomakehurst.wiremock.client.WireMock.put(
                    urlEqualTo("/aai/v23/network/logical-links/logical-link/olt-bbs-cpe-1"))
                    .willReturn(aResponse().withStatus(201)));

            when(kafkaConsumerTaskImpl.execute()).thenReturn(fluxList);

            List<String> expectedItems = List.of(event);
            Flux<MessageRouterPublishResponse> pubresp = Flux.just(
                    ImmutableMessageRouterPublishResponse.builder()
                            .items(expectedItems.map(JsonPrimitive::new))
                            .build());
            when(cbsConfiguration.getMessageRouterPublisher()).thenReturn(publisher);
            when(publisher.put(any(MessageRouterPublishRequest.class), any())).thenReturn(pubresp);

            scheduledTasksWithCommit.scheduleKafkaPrhEventTask();

            // Verify: BBS logical-link was created
            com.github.tomakehurst.wiremock.client.WireMock.verify(
                    1, com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor(
                            urlEqualTo("/aai/v23/network/logical-links/logical-link/olt-bbs-cpe-1"))
                            .withRequestBody(matchingJsonPath("$.['link-name']", equalTo("olt-bbs-cpe-1")))
                            .withRequestBody(matchingJsonPath("$.['link-type']", equalTo("attachment-point"))));

            // Verify: DMaaP publisher was invoked
            verify(publisher, times(1)).put(any(MessageRouterPublishRequest.class), any());
        });
    }


    private static String getResourceContent(String resourceName) {
        try {
            return new String(Files.readAllBytes(Paths.get(getSystemResource(resourceName).toURI())));
        } catch (Exception e) {
            throw new RuntimeException("failed loading content of '" + resourceName + "'", e);
        }
    }
}
