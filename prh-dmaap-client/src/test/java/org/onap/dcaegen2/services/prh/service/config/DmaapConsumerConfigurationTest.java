/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.service.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableDmaapConsumerConfiguration;

class DmaapConsumerConfigurationTest {

    @Test
    void builder_shouldBuildConfigurationObject() {

        // Given
        DmaapConsumerConfiguration configuration;
        String consumerId = "1";
        String dmaapHostName = "localhost";
        Integer dmaapPortNumber = 2222;
        String dmaapTopicName = "temp";
        String dmaapProtocol = "http";
        String dmaapUserName = "admin";
        String dmaapUserPassword = "admin";
        String dmaapContentType = "application/json";
        String consumerGroup = "other";
        Integer timeoutMs = 1000;
        Integer messageLimit = 1000;
        String keyFile = "keyFile";
        String trustStore = "trustStore";
        String trustStorePass = "trustPass";
        String keyStore = "keyStore";
        String keyStorePass = "keyPass";
        Boolean enableDmaapCertAuth = true;

        // When
        configuration = new ImmutableDmaapConsumerConfiguration.Builder()
                .consumerId(consumerId)
                .dmaapHostName(dmaapHostName)
                .dmaapPortNumber(dmaapPortNumber)
                .dmaapTopicName(dmaapTopicName)
                .dmaapProtocol(dmaapProtocol)
                .dmaapUserName(dmaapUserName)
                .dmaapUserPassword(dmaapUserPassword)
                .dmaapContentType(dmaapContentType)
                .consumerGroup(consumerGroup)
                .timeoutMs(timeoutMs)
                .messageLimit(messageLimit)
                .keyFile(keyFile)
                .trustStore(trustStore)
                .trustStorePassword(trustStorePass)
                .keyStore(keyStore)
                .keyStorePassword(keyStorePass)
                .enableDmaapCertAuth(enableDmaapCertAuth)
                .build();

        // Then
        assertNotNull(configuration);
        assertEquals(consumerId, configuration.consumerId());
        assertEquals(dmaapHostName, configuration.dmaapHostName());
        assertEquals(dmaapPortNumber, configuration.dmaapPortNumber());
        assertEquals(dmaapTopicName, configuration.dmaapTopicName());
        assertEquals(dmaapProtocol, configuration.dmaapProtocol());
        assertEquals(dmaapUserName, configuration.dmaapUserName());
        assertEquals(dmaapUserPassword, configuration.dmaapUserPassword());
        assertEquals(consumerGroup, configuration.consumerGroup());
        assertEquals(timeoutMs, configuration.timeoutMs());
        assertEquals(messageLimit, configuration.messageLimit());
        assertEquals(keyFile, configuration.keyFile());
        assertEquals(trustStore, configuration.trustStore());
        assertEquals(trustStorePass, configuration.trustStorePassword());
        assertEquals(keyStore, configuration.keyStore());
        assertEquals(keyStorePass, configuration.keyStorePassword());
        assertEquals(enableDmaapCertAuth, configuration.enableDmaapCertAuth());
    }
}
