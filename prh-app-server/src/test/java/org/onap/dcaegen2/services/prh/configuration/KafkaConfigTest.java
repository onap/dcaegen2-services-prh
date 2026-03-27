/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023-2026 Deutsche Telekom Intellectual Property. All rights reserved.
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
package org.onap.dcaegen2.services.prh.configuration;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

public class KafkaConfigTest {

    @Test
    void consumerFactoryShouldReadFromEnvVars() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
        .and("BOOTSTRAP_SERVERS", "localhost:9092")
        .execute(() -> {
            KafkaConfig kafkaConfig = new KafkaConfig();
            ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

            assertEquals("localhost:9092",
                    consumerFactory.getConfigurationProperties().get("bootstrap.servers").toString(),
                    "Bootstrap servers should come from BOOTSTRAP_SERVERS env var");
            assertEquals("org.apache.kafka.common.serialization.StringDeserializer",
                    consumerFactory.getConfigurationProperties().get("key.deserializer").toString());
            assertEquals("org.apache.kafka.common.serialization.StringDeserializer",
                    consumerFactory.getConfigurationProperties().get("value.deserializer").toString());
            assertEquals("false",
                    consumerFactory.getConfigurationProperties().get("enable.auto.commit").toString());
            assertEquals("earliest",
                    consumerFactory.getConfigurationProperties().get("auto.offset.reset").toString());
            assertEquals("SASL_PLAINTEXT",
                    consumerFactory.getConfigurationProperties().get("security.protocol").toString());
            assertEquals("SCRAM-SHA-512",
                    consumerFactory.getConfigurationProperties().get("sasl.mechanism").toString());
            assertEquals("jaas_config",
                    consumerFactory.getConfigurationProperties().get("sasl.jaas.config").toString());
        });
    }

    @Test
    void consumerFactoryWithoutJaasConfigShouldSkipSasl() throws Exception {
        withEnvironmentVariable("BOOTSTRAP_SERVERS", "localhost:9092")
        .and("JAAS_CONFIG", null)
        .execute(() -> {
            KafkaConfig kafkaConfig = new KafkaConfig();
            ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

            assertEquals("localhost:9092",
                    consumerFactory.getConfigurationProperties().get("bootstrap.servers").toString());
            // SASL properties should not be set
            assertEquals(null,
                    consumerFactory.getConfigurationProperties().get("security.protocol"));
        });
    }

    @Test
    void kafkaListenerContainerFactoryShouldBeConfiguredCorrectly() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
        .and("BOOTSTRAP_SERVERS", "localhost:9092")
        .execute(() -> {
            KafkaConfig kafkaConfig = new KafkaConfig();
            ConcurrentKafkaListenerContainerFactory<String, String> factory =
                    kafkaConfig.kafkaListenerContainerFactory();

            assertNotNull(factory);
            assertEquals(ContainerProperties.AckMode.MANUAL,
                    factory.getContainerProperties().getAckMode());
        });
    }
}
