/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property. All rights reserved.
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
import static java.lang.ClassLoader.getSystemResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.ConsumerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@ExtendWith(MockitoExtension.class)
public class KafkaConfigTest {

    KafkaConfig kafkaConfig = new KafkaConfig();

     CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode = new
     CbsConfigurationForAutoCommitDisabledMode();

   
//    @BeforeEach
//    void setUp() {
//        kafkaConfig.kafkaBoostrapServerConfig = "0.0.0.0";
//        kafkaConfig.groupIdConfig = "consumer-test";
//        kafkaConfig.kafkaSecurityProtocol = "test";
//        kafkaConfig.kafkaSaslMechanism = "test";
//        kafkaConfig.kafkaUsername = "test";
//        kafkaConfig.kafkaPassword = "test";
//        kafkaConfig.kafkaJaasConfig = null;
//        kafkaConfig.kafkaLoginModuleClassConfig = "test";
//        kafkaConfig.kafkaJaasConfig = "test";
//    }

    @Test
    void beforecbsConfigurationForAutoCommitDisabledMode() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config").execute(() -> {
            this.consumerFactoryTest();
        });
    }

    void consumerFactoryTest() throws Exception {
        JsonObject cbsConfigJsonForAutoCommitDisabled = new Gson().fromJson(
                new String(Files.readAllBytes(
                        Paths.get(getSystemResource("autoCommitDisabledConfigurationFromCbs2.json").toURI()))),
                JsonObject.class);
        cbsConfigurationForAutoCommitDisabledMode.parseCBSConfig(cbsConfigJsonForAutoCommitDisabled);
        ConsumerFactory<String, String> consumerFactory = kafkaConfig
                .consumerFactory(cbsConfigurationForAutoCommitDisabledMode);

        String expectedKafkaBoostrapServerConfig = "onap-strimzi-kafka-bootstrap:9092";
        String actualKafkaBoostrapServerConfig = consumerFactory.getConfigurationProperties().get("bootstrap.servers")
                .toString();

        String expectedGroupIdConfig = "OpenDCAE-c12";
        String actualGroupIdConfig = consumerFactory.getConfigurationProperties().get("group.id").toString();

        String expectedKafkaSecurityProtocol = "SASL_PLAINTEXT";
        String actualKafkaSecurityProtocol = consumerFactory.getConfigurationProperties().get("security.protocol")
                .toString();

        String expectedKafkaSaslMechanism = "SCRAM-SHA-512";
        String actualKafkaSaslMechanism = consumerFactory.getConfigurationProperties().get("sasl.mechanism").toString();

        String expectedKafkaJaasConfig = "jaas_config";
        String actualKafkaJaasConfig = consumerFactory.getConfigurationProperties().get("sasl.jaas.config").toString();

        String expectedKeyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
        String actualKeyDeserializer = consumerFactory.getConfigurationProperties().get("key.deserializer").toString();

        String expectedValueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
        String actualValueDeserializer = consumerFactory.getConfigurationProperties().get("value.deserializer")
                .toString();

        String expectedEnableAutoCommit = "false";
        String actualEnableAutoCommit = consumerFactory.getConfigurationProperties().get("enable.auto.commit")
                .toString();

        assertEquals(expectedKafkaBoostrapServerConfig, actualKafkaBoostrapServerConfig,
                "Expected value of KafKaBoostrapServerConfig is not matching with actual value");
        assertEquals(expectedGroupIdConfig, actualGroupIdConfig,
                "Expected value of GroupIdConfig is not matching with actual value");
        assertEquals(expectedKafkaSecurityProtocol, actualKafkaSecurityProtocol,
                "Expected value of KafkaSecurityProtocol is not matching with actual value");
        assertEquals(expectedKafkaSaslMechanism, actualKafkaSaslMechanism,
                "Expected value of KafkaSaslMechanism is not matching with actual value");
        assertEquals(expectedKafkaJaasConfig, actualKafkaJaasConfig,
                "Expected value of KafkaJaasConfig is not matching with actual value");
        assertEquals(expectedKeyDeserializer, actualKeyDeserializer,
                "Expected value of KeyDeserializer is not matching with actual value");
        assertEquals(expectedValueDeserializer, actualValueDeserializer,
                "Expected value of ValueDeserializer is not matching with actual value");
        assertEquals(expectedEnableAutoCommit, actualEnableAutoCommit,
                "Expected value of EnableAutoCommit is not matching with actual value");

    }

    @Test
    void beforeKafkaListenerContainerFactoryTest() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config").execute(() -> {
            this.kafkaListenerContainerFactoryTest();
        });
    }

    public void kafkaListenerContainerFactoryTest() throws Exception {
        JsonObject cbsConfigJsonForAutoCommitDisabled = new Gson().fromJson(
                new String(Files.readAllBytes(
                        Paths.get(getSystemResource("autoCommitDisabledConfigurationFromCbs2.json").toURI()))),
                JsonObject.class);
        cbsConfigurationForAutoCommitDisabledMode.parseCBSConfig(cbsConfigJsonForAutoCommitDisabled);
        kafkaConfig.kafkaListenerContainerFactory(cbsConfigurationForAutoCommitDisabledMode);
    }
}
