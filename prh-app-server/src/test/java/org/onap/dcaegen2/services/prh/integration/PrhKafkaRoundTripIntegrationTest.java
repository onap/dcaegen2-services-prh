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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.onap.dcaegen2.services.prh.service.KafkaConsumerJsonParser;
import org.onap.dcaegen2.services.prh.tasks.KafkaConsumerTaskImpl;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import reactor.core.publisher.Flux;

/**
 * Verifies a true Kafka produce/consume round-trip using an embedded broker.
 * Does NOT load the full Spring context — exercises KafkaConsumerJsonParser
 * in isolation with real Kafka records.
 */
@EmbeddedKafka(partitions = 1, topics = {"unauthenticated.VES_PNFREG_OUTPUT"})
class PrhKafkaRoundTripIntegrationTest {

    private static final String TOPIC = "unauthenticated.VES_PNFREG_OUTPUT";
    private static final String VES_EVENT = "{"
            + "\"event\":{"
            + "  \"commonEventHeader\":{"
            + "    \"sourceName\":\"TESTPNF001\","
            + "    \"nfNamingCode\":\"gNB\""
            + "  },"
            + "  \"pnfRegistrationFields\":{"
            + "    \"vendorName\":\"Nokia\","
            + "    \"serialNumber\":\"SN12345\","
            + "    \"pnfRegistrationFieldsVersion\":\"2.0\","
            + "    \"modelNumber\":\"AirScale\","
            + "    \"unitType\":\"BBU\","
            + "    \"unitFamily\":\"BBU\","
            + "    \"oamV4IpAddress\":\"10.0.0.1\","
            + "    \"oamV6IpAddress\":\"::1\","
            + "    \"softwareVersion\":\"v1.0\""
            + "  }"
            + "}"
            + "}";

    @Test
    void shouldConsumeVesEventFromEmbeddedKafka(EmbeddedKafkaBroker broker) throws Exception {
        String bootstrapServers = broker.getBrokersAsString();

        // --- Producer sends a VES event ---
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaTemplate<String, String> producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
        producer.send(TOPIC, VES_EVENT).get();

        // --- Consumer reads via a real KafkaMessageListenerContainer ---
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProps = new ContainerProperties(TOPIC);

        java.util.concurrent.CopyOnWriteArrayList<String> received = new java.util.concurrent.CopyOnWriteArrayList<>();
        containerProps.setMessageListener((MessageListener<String, String>) record -> received.add(record.value()));

        KafkaMessageListenerContainer<String, String> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
        container.start();
        ContainerTestUtils.waitForAssignment(container, broker.getPartitionsPerTopic());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(received).hasSize(1));

        container.stop();

        // --- Parse the received message through KafkaConsumerJsonParser ---
        KafkaConsumerJsonParser parser = new KafkaConsumerJsonParser();
        Flux<ConsumerPnfModel> result = parser.getConsumerModelFromKafkaRecords(received);

        ConsumerPnfModel model = result.blockFirst();
        assertThat(model).isNotNull();
        assertThat(model.getCorrelationId()).isEqualTo("TESTPNF001");
        assertThat(model.getIpv4()).isEqualTo("10.0.0.1");
        assertThat(model.getIpv6()).isEqualTo("::1");
        assertThat(model.getSerialNumber()).isEqualTo("SN12345");
        assertThat(model.getEquipVendor()).isEqualTo("Nokia");
        assertThat(model.getEquipModel()).isEqualTo("AirScale");
        assertThat(model.getEquipType()).isEqualTo("BBU");
        assertThat(model.getSwVersion()).isEqualTo("v1.0");
        assertThat(model.getNfRole()).isEqualTo("gNB");
    }
}
