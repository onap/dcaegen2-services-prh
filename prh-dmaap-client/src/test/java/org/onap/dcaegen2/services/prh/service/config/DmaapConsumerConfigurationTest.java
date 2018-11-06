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
        String trustStorePath = "trustStorePath";
        String trustStorePasswordPath = "trustStorePasswordPath";
        String keyStorePath = "keyStorePath";
        String keyStorePasswordPath = "keyStorePasswordPath";
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
                .trustStorePath(trustStorePath)
                .trustStorePasswordPath(trustStorePasswordPath)
                .keyStorePath(keyStorePath)
                .keyStorePasswordPath(keyStorePasswordPath)
                .enableDmaapCertAuth(enableDmaapCertAuth)
                .build();

        // Then
        assertEquals("DmaapConsumerConfiguration{"
                + "consumerId=1, consumerGroup=other, timeoutMs=1000, messageLimit=1000, dmaapHostName=localhost, "
                + "dmaapPortNumber=2222, dmaapTopicName=temp, dmaapProtocol=http, dmaapUserName=admin, "
                + "dmaapUserPassword=admin, dmaapContentType=application/json, "
                + "trustStorePath=trustStorePath, trustStorePasswordPath=trustStorePasswordPath, "
                + "keyStorePath=keyStorePath, keyStorePasswordPath=keyStorePasswordPath, enableDmaapCertAuth=true}",
                configuration.toString());

    }
}
