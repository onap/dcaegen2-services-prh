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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 *  * @author <a href="mailto:PRANIT.KAPDULE@t-systems.com">Pranit Kapdule</a> on
 *   *        24/08/23
 *    */

public class CbsConfigurationForAutoCommitDisabledModeTest {

    /**
     * Testcase is used to check correctness of values provided by
     * autoCommitDisabledConfigurationFromCbs2.json
     */

    @Test
    void beforecbsConfigurationForAutoCommitDisabledMode() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
        .and("BOOTSTRAP_SERVERS", "localhost:9092")
        .execute(() -> {
            this.cbsConfigurationForAutoCommitDisabledMode();
        });
    }

    void cbsConfigurationForAutoCommitDisabledMode() throws Exception {

        JsonObject cbsConfigJsonForAutoCommitDisabled = new Gson().fromJson(
                new String(Files.readAllBytes(
                        Paths.get(getSystemResource("autoCommitDisabledConfigurationFromCbs2.json").toURI()))),
                JsonObject.class);
        CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabled = new CbsConfigurationForAutoCommitDisabledMode();

        cbsConfigurationForAutoCommitDisabled.parseCBSConfig(cbsConfigJsonForAutoCommitDisabled);

        String expectedKafKaBoostrapServerConfig = "onap-strimzi-kafka-bootstrap:9092";
        String actualKafkaBoostrapServerConfig = (cbsConfigurationForAutoCommitDisabled.getKafkaConfig()
                .kafkaBoostrapServerConfig());

        String expectedGroupIdConfig = "OpenDCAE-c12";
        String actualGroupIdConfig = (cbsConfigurationForAutoCommitDisabled.getKafkaConfig().groupIdConfig());

        String expectedKafkaSecurityProtocol = "SASL_PLAINTEXT";
        String actualKafkaSecurityProtocol = (cbsConfigurationForAutoCommitDisabled.getKafkaConfig()
                .kafkaSecurityProtocol());

        String expectedKafkaSaslMechanism = "SCRAM-SHA-512";
        String actualKafkaSaslMechanism = (cbsConfigurationForAutoCommitDisabled.getKafkaConfig().kafkaSaslMechanism());

        String expectedKafkaJaasConfig = "jaas_config";
        String actualKafkaJaasConfig = (cbsConfigurationForAutoCommitDisabled.getKafkaConfig().kafkaJaasConfig());

        String expectedAaiUserName = "AAI";
        String actualAaiUserName = (cbsConfigurationForAutoCommitDisabled.getAaiClientConfiguration().aaiUserName());

        String expectedConsumerGroup = "OpenDCAE-c12";
        String actualConsumerGroup = (cbsConfigurationForAutoCommitDisabled.getMessageRouterSubscribeRequest()
                .consumerGroup());

        assertEquals(expectedKafKaBoostrapServerConfig, actualKafkaBoostrapServerConfig,
                "Expected value of KafKaBoostrapServerConfig is not matching with actual value");
        assertEquals(expectedGroupIdConfig, actualGroupIdConfig,
                "Expected value of GroupIdConfig is not matching with actual value");
        assertEquals(expectedKafkaSecurityProtocol, actualKafkaSecurityProtocol,
                "Expected value of KafkaSecurityProtocol is not matching with actual value");
        assertEquals(expectedKafkaSaslMechanism, actualKafkaSaslMechanism,
                "Expected value of KafkaSaslMechanism is not matching with actual value");
        assertEquals(expectedKafkaJaasConfig, actualKafkaJaasConfig,
                "Expected value of KafkaJaasConfig is not matching with actual value");
        assertEquals(expectedAaiUserName, actualAaiUserName,
                "Expected value of AaiUserName is not matching with actual value");
        assertEquals(expectedConsumerGroup, actualConsumerGroup,
                "Expected value of ConsumerGroup is not matching with actual value");

        assertThat((cbsConfigurationForAutoCommitDisabled).getAaiClientConfiguration()).isNotNull();
        assertThat((cbsConfigurationForAutoCommitDisabled).getMessageRouterPublisher()).isNotNull();
        assertThat((cbsConfigurationForAutoCommitDisabled).getMessageRouterSubscriber()).isNotNull();
        assertThat((cbsConfigurationForAutoCommitDisabled).getMessageRouterPublishRequest()).isNotNull();
        assertThat((cbsConfigurationForAutoCommitDisabled).getMessageRouterSubscribeRequest()).isNotNull();
        assertThat((cbsConfigurationForAutoCommitDisabled).getMessageRouterUpdatePublishRequest()).isNotNull();

    }

}
