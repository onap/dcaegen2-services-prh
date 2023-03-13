/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property.All rights reserved.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KafkaConfigTest {

    @InjectMocks
    KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig.kafkaBoostrapServerConfig = "0.0.0.0";
        kafkaConfig.groupIdConfig = "consumer-test";
        kafkaConfig.kafkaSecurityProtocol = "test";
        kafkaConfig.kafkaSaslMechanism = "test";
        kafkaConfig.kafkaUsername = "test";
        kafkaConfig.kafkaPassword = "test";
        kafkaConfig.kafkaJaasConfig = null;
        kafkaConfig.kafkaLoginModuleClassConfig = "test";
        kafkaConfig.kafkaJaasConfig = "test";
    }

    @Test
    public void consumerFactoryTest(){
        kafkaConfig.consumerFactory();
    }

    @Test
    public void kafkaListenerContainerFactoryTest(){
        kafkaConfig.kafkaListenerContainerFactory();
    }
}
