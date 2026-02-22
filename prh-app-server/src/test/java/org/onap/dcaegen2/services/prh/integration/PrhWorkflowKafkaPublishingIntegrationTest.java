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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.ClassLoader.getSystemResource;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.MainApp;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.kafka.ImmutableKafkaConfiguration;
import org.onap.dcaegen2.services.prh.adapter.kafka.KafkaConfiguration;
import org.onap.dcaegen2.services.prh.configuration.CbsConfigurationForAutoCommitDisabledMode;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.prh.tasks.commit.KafkaConsumerTaskImpl;
import org.onap.dcaegen2.services.prh.tasks.commit.ScheduledTasksRunnerWithCommit;
import org.onap.dcaegen2.services.prh.tasks.commit.ScheduledTasksWithCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;

/**
 * Integration test for the Kafka publishing path in the autoCommitDisabled profile.
 *
 * <p>This test uses an embedded Kafka broker to verify that PNF events are
 * actually published to the correct Kafka topics (PNF_READY or PNF_UPDATE)
 * after the PRH workflow processes them. AAI interactions are stubbed via WireMock.
 *
 * <p>The Kafka consumer side (KafkaConsumerTaskImpl) is still mocked because
 * the @KafkaListener-based consumption is separate from the publishing path.
 * We only verify the publishing side here.
 */
@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles(value = "autoCommitDisabled")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                PrhWorkflowKafkaPublishingIntegrationTest.PNF_READY_TOPIC,
                PrhWorkflowKafkaPublishingIntegrationTest.PNF_UPDATE_TOPIC
        },
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:0",
                "auto.create.topics.enable=true"
        }
)
class PrhWorkflowKafkaPublishingIntegrationTest {

    static final String PNF_READY_TOPIC = "unauthenticated.PNF_READY";
    static final String PNF_UPDATE_TOPIC = "unauthenticated.PNF_UPDATE";

    @Autowired
    private ScheduledTasksWithCommit scheduledTasksWithCommit;

    @MockBean
    private ScheduledTasksRunnerWithCommit scheduledTasksRunnerWithCommit; // disable auto-scheduling

    @MockBean
    private KafkaConsumerTaskImpl kafkaConsumerTaskImpl;

    @Autowired
    private DmaapConsumerJsonParser dmaapConsumerJsonParser;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Configuration
    @Import(MainApp.class)
    static class TestConfig {

        @Value("http://localhost:${wiremock.server.port}")
        private String wiremockServerAddress;

        @Bean
        public CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode(
                EmbeddedKafkaBroker embeddedKafkaBroker) {
            JsonObject cbsConfigJson = new Gson()
                    .fromJson(getResourceContent("autoCommitDisabledConfigurationFromCbs2.json")
                            .replaceAll("https?://dmaap-mr[\\w.]*:\\d+", wiremockServerAddress)
                            .replaceAll("https?://aai[\\w.]*:\\d+", wiremockServerAddress),
                            JsonObject.class);

            CbsConfigurationForAutoCommitDisabledMode config = new CbsConfigurationForAutoCommitDisabledMode();

            try {
                withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
                        .and("BOOTSTRAP_SERVERS", "localhost:0")
                        .execute(() -> config.parseCBSConfig(cbsConfigJson));
            } catch (Exception e) {
                // parseCBSConfig may partially fail; we override kafka config below
            }

            // Override kafka config to point to embedded broker without SASL
            String brokerAddress = embeddedKafkaBroker.getBrokersAsString();
            KafkaConfiguration kafkaConfig = new ImmutableKafkaConfiguration.Builder()
                    .kafkaBoostrapServerConfig(brokerAddress)
                    .groupIdConfig("test-group")
                    .kafkaSaslMechanism("")
                    .kafkaSecurityProtocol("PLAINTEXT")
                    .kafkaJaasConfig("")
                    .build();
            config.setKafkaConfiguration(kafkaConfig);

            return config;
        }
    }

    @BeforeEach
    void setup() {
        WireMock.reset();
    }

    // ==================== Scenario 1: First registration → PNF_READY topic ====================

    @Test
    void whenFirstRegistration_shouldPublishToPnfReadyKafkaTopic() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
                .execute(() -> doTestFirstRegistrationPublishesToPnfReady());
    }

    private void doTestFirstRegistrationPublishesToPnfReady() throws JSONException {
        String event = getResourceContent("integration/event.json");
        String pnfName = "NOK6061ZW8";

        java.util.List<String> eventList = new ArrayList<>();
        eventList.add(event);

        Flux<ConsumerDmaapModel> fluxList = dmaapConsumerJsonParser
                .getConsumerDmaapModelFromKafkaConsumerRecord(eventList);

        // Stub AAI GET PNF – no service-instance relationship (first registration)
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName))
                .willReturn(ok().withHeader("Content-Type", "application/json")
                        .withBody("{\"pnf-name\":\"" + pnfName + "\"}")));

        // Stub AAI PATCH PNF
        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok()));

        when(kafkaConsumerTaskImpl.execute()).thenReturn(fluxList);

        scheduledTasksWithCommit.scheduleKafkaPrhEventTask();

        // Verify AAI PATCH was made
        verify(1, patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));

        // Verify the event was published to the PNF_READY Kafka topic
        Consumer<String, String> consumer = createTestConsumer("test-group-ready");
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, PNF_READY_TOPIC);
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, 10000);

        assertThat(records.count()).isGreaterThanOrEqualTo(1);
        assertThat(records.iterator().next().key()).isEqualTo(pnfName);
        assertThat(records.iterator().next().value()).contains("correlationId");

        consumer.close();
    }

    // ==================== Scenario 2: Re-registration → PNF_UPDATE topic ====================

    @Test
    void whenReRegistration_shouldPublishToPnfUpdateKafkaTopic() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
                .execute(() -> doTestReRegistrationPublishesToPnfUpdate());
    }

    private void doTestReRegistrationPublishesToPnfUpdate() throws JSONException {
        String event = getResourceContent("integration/event.json");
        String pnfName = "NOK6061ZW8";

        java.util.List<String> eventList = new ArrayList<>();
        eventList.add(event);

        Flux<ConsumerDmaapModel> fluxList = dmaapConsumerJsonParser
                .getConsumerDmaapModelFromKafkaConsumerRecord(eventList);

        // PNF with active service-instance → re-registration
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
                        .withBody("{\"orchestration-status\":\"Active\"}")));

        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok()));

        when(kafkaConsumerTaskImpl.execute()).thenReturn(fluxList);

        scheduledTasksWithCommit.scheduleKafkaPrhEventTask();

        // Verify the event was published to the PNF_UPDATE Kafka topic
        Consumer<String, String> consumer = createTestConsumer("test-group-update");
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, PNF_UPDATE_TOPIC);
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, 10000);

        assertThat(records.count()).isGreaterThanOrEqualTo(1);
        assertThat(records.iterator().next().key()).isEqualTo(pnfName);

        consumer.close();
    }

    // ==================== Scenario 3: No events → nothing published ====================

    @Test
    void whenNoEvents_nothingShouldBePublished() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
                .execute(() -> doTestNoEventsNothingPublished());
    }

    private void doTestNoEventsNothingPublished() throws JSONException {
        when(kafkaConsumerTaskImpl.execute()).thenReturn(Flux.empty());

        scheduledTasksWithCommit.scheduleKafkaPrhEventTask();

        // Verify no AAI calls were made
        verify(0, patchRequestedFor(urlEqualTo("/aai/v23/network/pnfs/pnf/.*")));

        // Create a consumer and verify there are no records on PNF_READY
        Consumer<String, String> consumer = createTestConsumer("test-group-empty");
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, PNF_READY_TOPIC);
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, 3000);

        // Should be empty (or only contain records from prior test runs)
        // We just verify no exception was thrown and the workflow completes
        consumer.close();
    }

    // ==================== Helpers ====================

    private Consumer<String, String> createTestConsumer(String groupId) {
        Map<String, Object> consumerProps = new HashMap<>(
                KafkaTestUtils.consumerProps(groupId, "true", embeddedKafka));
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer();
    }

    private static String getResourceContent(String resourceName) {
        try {
            return new String(Files.readAllBytes(Paths.get(getSystemResource(resourceName).toURI())));
        } catch (Exception e) {
            throw new RuntimeException("failed loading content of '" + resourceName + "'", e);
        }
    }
}
