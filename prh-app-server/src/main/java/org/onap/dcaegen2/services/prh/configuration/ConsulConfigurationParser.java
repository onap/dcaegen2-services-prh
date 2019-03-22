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

package org.onap.dcaegen2.services.prh.configuration;

import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.ImmutableDmaapPublisherConfiguration;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 8/21/18
 */
class ConsulConfigurationParser {
    private static final String SECURITY_TRUST_STORE_PATH = "security.trustStorePath";
    private static final String SECURITY_TRUST_STORE_PASS_PATH = "security.trustStorePasswordPath";
    private static final String SECURITY_KEY_STORE_PATH = "security.keyStorePath";
    private static final String SECURITY_KEY_STORE_PASS_PATH = "security.keyStorePasswordPath";
    private final JsonObject jsonObject;

    ConsulConfigurationParser(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    DmaapPublisherConfiguration getDmaapPublisherConfig() {
        return new ImmutableDmaapPublisherConfiguration.Builder()
            .dmaapTopicName(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapTopicName").getAsString())
            .dmaapUserPassword(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapUserPassword").getAsString())
            .dmaapPortNumber(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapPortNumber").getAsInt())
            .dmaapProtocol(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapProtocol").getAsString())
            .dmaapContentType(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapContentType").getAsString())
            .dmaapHostName(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapHostName").getAsString())
            .dmaapUserName(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapUserName").getAsString())
            .dmaapUserPassword(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapUserPassword").getAsString())
            .trustStorePath(jsonObject.get(SECURITY_TRUST_STORE_PATH).getAsString())
            .trustStorePasswordPath(jsonObject.get(SECURITY_TRUST_STORE_PASS_PATH).getAsString())
            .keyStorePath(jsonObject.get(SECURITY_KEY_STORE_PATH).getAsString())
            .keyStorePasswordPath(jsonObject.get(SECURITY_KEY_STORE_PASS_PATH).getAsString())
            .enableDmaapCertAuth(jsonObject.get("security.enableDmaapCertAuth").getAsBoolean())
            .build();
    }

    DmaapPublisherConfiguration getDmaapUpdatePublisherConfig() {
        return new ImmutableDmaapPublisherConfiguration.Builder()
             .dmaapTopicName(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapTopicName").getAsString())
            .dmaapUserPassword(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapUserPassword").getAsString())
            .dmaapPortNumber(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapPortNumber").getAsInt())
            .dmaapProtocol(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapProtocol").getAsString())
            .dmaapContentType(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapContentType").getAsString())
            .dmaapHostName(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapHostName").getAsString())
            .dmaapUserName(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapUserName").getAsString())
            .dmaapUserPassword(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapUserPassword").getAsString())
            .trustStorePath(jsonObject.get(SECURITY_TRUST_STORE_PATH).getAsString())
            .trustStorePasswordPath(jsonObject.get(SECURITY_TRUST_STORE_PASS_PATH).getAsString())
            .keyStorePath(jsonObject.get(SECURITY_KEY_STORE_PATH).getAsString())
            .keyStorePasswordPath(jsonObject.get(SECURITY_KEY_STORE_PASS_PATH).getAsString())
            .enableDmaapCertAuth(jsonObject.get("security.enableDmaapCertAuth").getAsBoolean())
            .build();
    }

    AaiClientConfiguration getAaiClientConfig() {
        return new ImmutableAaiClientConfiguration.Builder()
            .aaiHost(jsonObject.get("aai.aaiClientConfiguration.aaiHost").getAsString())
            .aaiPort(jsonObject.get("aai.aaiClientConfiguration.aaiHostPortNumber").getAsInt())
            .aaiUserName(jsonObject.get("aai.aaiClientConfiguration.aaiUserName").getAsString())
            .aaiPnfPath(jsonObject.get("aai.aaiClientConfiguration.aaiPnfPath").getAsString())
            .aaiServiceInstancePath(jsonObject.get("aai.aaiClientConfiguration.aaiServiceInstancePath").getAsString())
            .aaiIgnoreSslCertificateErrors(
                jsonObject.get("aai.aaiClientConfiguration.aaiIgnoreSslCertificateErrors").getAsBoolean())
            .aaiUserPassword(jsonObject.get("aai.aaiClientConfiguration.aaiUserPassword").getAsString())
            .aaiProtocol(jsonObject.get("aai.aaiClientConfiguration.aaiProtocol").getAsString())
            .aaiBasePath(jsonObject.get("aai.aaiClientConfiguration.aaiBasePath").getAsString())
            .trustStorePath(jsonObject.get(SECURITY_TRUST_STORE_PATH).getAsString())
            .trustStorePasswordPath(jsonObject.get(SECURITY_TRUST_STORE_PASS_PATH).getAsString())
            .keyStorePath(jsonObject.get(SECURITY_KEY_STORE_PATH).getAsString())
            .keyStorePasswordPath(jsonObject.get(SECURITY_KEY_STORE_PASS_PATH).getAsString())
            .enableAaiCertAuth(jsonObject.get("security.enableAaiCertAuth").getAsBoolean())
            .build();
    }

    DmaapConsumerConfiguration getDmaapConsumerConfig() {
        return new ImmutableDmaapConsumerConfiguration.Builder()
            .timeoutMs(jsonObject.get("dmaap.dmaapConsumerConfiguration.timeoutMs").getAsInt())
            .dmaapHostName(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapHostName").getAsString())
            .dmaapUserName(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapUserName").getAsString())
            .dmaapUserPassword(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapUserPassword").getAsString())
            .dmaapTopicName(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapTopicName").getAsString())
            .dmaapPortNumber(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapPortNumber").getAsInt())
            .dmaapContentType(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapContentType").getAsString())
            .messageLimit(jsonObject.get("dmaap.dmaapConsumerConfiguration.messageLimit").getAsInt())
            .dmaapProtocol(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapProtocol").getAsString())
            .consumerId(jsonObject.get("dmaap.dmaapConsumerConfiguration.consumerId").getAsString())
            .consumerGroup(jsonObject.get("dmaap.dmaapConsumerConfiguration.consumerGroup").getAsString())
            .trustStorePath(jsonObject.get(SECURITY_TRUST_STORE_PATH).getAsString())
            .trustStorePasswordPath(jsonObject.get(SECURITY_TRUST_STORE_PASS_PATH).getAsString())
            .keyStorePath(jsonObject.get(SECURITY_KEY_STORE_PATH).getAsString())
            .keyStorePasswordPath(jsonObject.get(SECURITY_KEY_STORE_PASS_PATH).getAsString())
            .enableDmaapCertAuth(jsonObject.get("security.enableDmaapCertAuth").getAsBoolean())
            .build();
    }
}