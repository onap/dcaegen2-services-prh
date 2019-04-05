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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.ImmutableDmaapPublisherConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/9/18
 */

@Configuration
@EnableConfigurationProperties
public class AppConfig extends PrhAppConfig {

    private static Predicate<String> isEmpty = String::isEmpty;
    @Value("${dmaap.dmaapConsumerConfiguration.dmaapHostName:}")
    public String consumerDmaapHostName;

    @Value("${dmaap.dmaapConsumerConfiguration.dmaapPortNumber:}")
    public Integer consumerDmaapPortNumber;

    @Value("${dmaap.dmaapConsumerConfiguration.dmaapTopicName:}")
    public String consumerDmaapTopicName;

    @Value("${dmaap.dmaapConsumerConfiguration.dmaapProtocol:}")
    public String consumerDmaapProtocol;

    @Value("${dmaap.dmaapConsumerConfiguration.dmaapUserName:}")
    public String consumerDmaapUserName;

    @Value("${dmaap.dmaapConsumerConfiguration.dmaapUserPassword:}")
    public String consumerDmaapUserPassword;

    @Value("${dmaap.dmaapConsumerConfiguration.dmaapContentType:}")
    public String consumerDmaapContentType;

    @Value("${dmaap.dmaapConsumerConfiguration.consumerId:}")
    public String consumerId;

    @Value("${dmaap.dmaapConsumerConfiguration.consumerGroup:}")
    public String consumerGroup;

    @Value("${dmaap.dmaapConsumerConfiguration.timeoutMs:}")
    public Integer consumerTimeoutMs;

    @Value("${dmaap.dmaapConsumerConfiguration.message-limit:}")
    public Integer consumerMessageLimit;

    @Value("${dmaap.dmaapProducerConfiguration.dmaapHostName:}")
    public String producerDmaapHostName;

    @Value("${dmaap.dmaapProducerConfiguration.dmaapPortNumber:}")
    public Integer producerDmaapPortNumber;

    @Value("${dmaap.dmaapProducerConfiguration.dmaapTopicName:}")
    public String producerDmaapTopicName;

    @Value("${dmaap.dmaapProducerConfiguration.dmaapProtocol:}")
    public String producerDmaapProtocol;

    @Value("${dmaap.dmaapProducerConfiguration.dmaapUserName:}")
    public String producerDmaapUserName;

    @Value("${dmaap.dmaapProducerConfiguration.dmaapUserPassword:}")
    public String producerDmaapUserPassword;

    @Value("${dmaap.dmaapProducerConfiguration.dmaapContentType:}")
    public String producerDmaapContentType;

    @Value("${dmaap.dmaapUpdateProducerConfiguration.dmaapHostName:}")
    public String updateProducerDmaapHostName;

    @Value("${dmaap.dmaapUpdateProducerConfiguration.dmaapPortNumber:}")
    public Integer updateProducerDmaapPortNumber;

    @Value("${dmaap.dmaapUpdateProducerConfiguration.dmaapTopicName:}")
    public String updateProducerDmaapTopicName;

    @Value("${dmaap.dmaapUpdateProducerConfiguration.dmaapProtocol:}")
    public String updateProducerDmaapProtocol;

    @Value("${dmaap.dmaapUpdateProducerConfiguration.dmaapUserName:}")
    public String updateProducerDmaapUserName;

    @Value("${dmaap.dmaapUpdateProducerConfiguration.dmaapUserPassword:}")
    public String updateProducerDmaapUserPassword;

    @Value("${dmaap.dmaapUpdateProducerConfiguration.dmaapContentType:}")
    public String updateProducerDmaapContentType;

    @Value("${aai.aaiClientConfiguration.pnfUrl:}")
    public String pnfUrl;

    @Value("${aai.aaiClientConfiguration.aaiHost:}")
    public String aaiHost;

    @Value("${aai.aaiClientConfiguration.aaiHostPortNumber:}")
    public Integer aaiPort;

    @Value("${aai.aaiClientConfiguration.aaiProtocol:}")
    public String aaiProtocol;

    @Value("${aai.aaiClientConfiguration.aaiUserName:}")
    public String aaiUserName;

    @Value("${aai.aaiClientConfiguration.aaiUserPassword:}")
    public String aaiUserPassword;

    @Value("${aai.aaiClientConfiguration.aaiIgnoreSslCertificateErrors:}")
    public Boolean aaiIgnoreSslCertificateErrors;

    @Value("${aai.aaiClientConfiguration.aaiBasePath:}")
    public String aaiBasePath;

    @Value("${aai.aaiClientConfiguration.aaiPnfPath:}")
    public String aaiPnfPath;

    @Value("${aai.aaiClientConfiguration.aaiServiceInstancePath:}")
    public String aaiServiceInstancePath;

    @Value("${security.trustStorePath:}")
    public String trustStorePath;

    @Value("${security.trustStorePasswordPath:}")
    public String trustStorePasswordPath;

    @Value("${security.keyStorePath:}")
    public String keyStorePath;

    @Value("${security.keyStorePasswordPath:}")
    public String keyStorePasswordPath;

    @Value("${security.enableAaiCertAuth:}")
    public Boolean enableAaiCertAuth;

    @Value("${security.enableDmaapCertAuth:}")
    public Boolean enableDmaapCertAuth;

    @Override
    public DmaapConsumerConfiguration getDmaapConsumerConfiguration() {
        if (noFileConfiguration(dmaapConsumerConfiguration)) {
            return null;
        }
        return new ImmutableDmaapConsumerConfiguration.Builder()
            .dmaapUserPassword(
                Optional.ofNullable(consumerDmaapUserPassword).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.dmaapUserPassword()))
            .dmaapUserName(
                Optional.ofNullable(consumerDmaapUserName).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.dmaapUserName()))
            .dmaapHostName(
                Optional.ofNullable(consumerDmaapHostName).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.dmaapHostName()))
            .dmaapPortNumber(
                Optional.ofNullable(consumerDmaapPortNumber).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapConsumerConfiguration.dmaapPortNumber()))
            .dmaapProtocol(
                Optional.ofNullable(consumerDmaapProtocol).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.dmaapProtocol()))
            .dmaapContentType(
                Optional.ofNullable(consumerDmaapContentType).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.dmaapContentType()))
            .dmaapTopicName(
                Optional.ofNullable(consumerDmaapTopicName).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.dmaapTopicName()))
            .messageLimit(
                Optional.ofNullable(consumerMessageLimit).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapConsumerConfiguration.messageLimit()))
            .timeoutMs(Optional.ofNullable(consumerTimeoutMs).filter(p -> !p.toString().isEmpty())
                .orElse(dmaapConsumerConfiguration.timeoutMs()))
            .consumerGroup(Optional.ofNullable(consumerGroup).filter(isEmpty.negate())
                .orElse(dmaapConsumerConfiguration.consumerGroup()))
            .consumerId(Optional.ofNullable(consumerId).filter(isEmpty.negate())
                .orElse(dmaapConsumerConfiguration.consumerId()))
            .trustStorePath(
                Optional.ofNullable(trustStorePath).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.trustStorePath()))
            .trustStorePasswordPath(
                Optional.ofNullable(trustStorePasswordPath).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.trustStorePasswordPath()))
            .keyStorePath(
                Optional.ofNullable(keyStorePath).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.keyStorePath()))
            .keyStorePasswordPath(
                Optional.ofNullable(keyStorePasswordPath).filter(isEmpty.negate())
                    .orElse(dmaapConsumerConfiguration.keyStorePasswordPath()))
            .enableDmaapCertAuth(
                Optional.ofNullable(enableDmaapCertAuth).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapConsumerConfiguration.enableDmaapCertAuth()))
            .build();
    }

    @Override
    public AaiClientConfiguration getAaiClientConfiguration() {
        if (noFileConfiguration(aaiClientConfiguration)) {
            return null;
        }
        return new ImmutableAaiClientConfiguration.Builder()
            .pnfUrl(Optional.ofNullable(pnfUrl).filter(isEmpty.negate()).orElse(aaiClientConfiguration.pnfUrl()))
            .aaiHost(Optional.ofNullable(aaiHost).filter(isEmpty.negate()).orElse(aaiClientConfiguration.aaiHost()))
            .aaiPort(
                Optional.ofNullable(aaiPort).filter(p -> !p.toString().isEmpty())
                    .orElse(aaiClientConfiguration.aaiPort()))
            .aaiIgnoreSslCertificateErrors(
                Optional.ofNullable(aaiIgnoreSslCertificateErrors).filter(p -> !p.toString().isEmpty())
                    .orElse(aaiClientConfiguration.aaiIgnoreSslCertificateErrors()))
            .aaiProtocol(
                Optional.ofNullable(aaiProtocol).filter(isEmpty.negate()).orElse(aaiClientConfiguration.aaiProtocol()))
            .aaiUserName(
                Optional.ofNullable(aaiUserName).filter(isEmpty.negate()).orElse(aaiClientConfiguration.aaiUserName()))
            .aaiUserPassword(Optional.ofNullable(aaiUserPassword).filter(isEmpty.negate())
                .orElse(aaiClientConfiguration.aaiUserPassword()))
            .aaiBasePath(Optional.ofNullable(aaiBasePath).filter(isEmpty.negate())
                .orElse(aaiClientConfiguration.aaiBasePath()))
            .aaiPnfPath(
                Optional.ofNullable(aaiPnfPath).filter(isEmpty.negate()).orElse(aaiClientConfiguration.aaiPnfPath()))
            .aaiServiceInstancePath(
                Optional.ofNullable(aaiServiceInstancePath).filter(isEmpty.negate()).orElse(aaiClientConfiguration.aaiServiceInstancePath()))
            .aaiHeaders(aaiClientConfiguration.aaiHeaders())
            .trustStorePath(
                Optional.ofNullable(trustStorePath).filter(isEmpty.negate())
                    .orElse(aaiClientConfiguration.trustStorePath()))
            .trustStorePasswordPath(
                Optional.ofNullable(trustStorePasswordPath).filter(isEmpty.negate())
                    .orElse(aaiClientConfiguration.trustStorePasswordPath()))
            .keyStorePath(
                Optional.ofNullable(keyStorePath).filter(isEmpty.negate())
                    .orElse(aaiClientConfiguration.keyStorePath()))
            .keyStorePasswordPath(
                Optional.ofNullable(keyStorePasswordPath).filter(isEmpty.negate())
                    .orElse(aaiClientConfiguration.keyStorePasswordPath()))
            .enableAaiCertAuth(
                Optional.ofNullable(enableAaiCertAuth).filter(p -> !p.toString().isEmpty())
                    .orElse(aaiClientConfiguration.enableAaiCertAuth()))
            .build();
    }

    @Override
    public DmaapPublisherConfiguration getDmaapPublisherConfiguration() {
        if (noFileConfiguration(dmaapPublisherConfiguration)) {
            return null;
        }
        return new ImmutableDmaapPublisherConfiguration.Builder()
            .dmaapContentType(
                Optional.ofNullable(producerDmaapContentType).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.dmaapContentType()))
            .dmaapHostName(
                Optional.ofNullable(producerDmaapHostName).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.dmaapHostName()))
            .dmaapPortNumber(
                Optional.ofNullable(producerDmaapPortNumber).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapPublisherConfiguration.dmaapPortNumber()))
            .dmaapProtocol(
                Optional.ofNullable(producerDmaapProtocol).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.dmaapProtocol()))
            .dmaapTopicName(
                Optional.ofNullable(producerDmaapTopicName).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.dmaapTopicName()))
            .dmaapUserName(
                Optional.ofNullable(producerDmaapUserName).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.dmaapUserName()))
            .dmaapUserPassword(
                Optional.ofNullable(producerDmaapUserPassword).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.dmaapUserPassword()))
            .trustStorePath(
                Optional.ofNullable(trustStorePath).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.trustStorePath()))
            .trustStorePasswordPath(
                Optional.ofNullable(trustStorePasswordPath).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.trustStorePasswordPath()))
            .keyStorePath(
                Optional.ofNullable(keyStorePath).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.keyStorePath()))
            .keyStorePasswordPath(
                Optional.ofNullable(keyStorePasswordPath).filter(isEmpty.negate())
                    .orElse(dmaapPublisherConfiguration.keyStorePasswordPath()))
            .enableDmaapCertAuth(
                Optional.ofNullable(enableDmaapCertAuth).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapPublisherConfiguration.enableDmaapCertAuth()))
            .build();
    }

    @Override
    public DmaapPublisherConfiguration getDmaapUpdatePublisherConfiguration() {
        if (noFileConfiguration(dmaapUpdatePublisherConfiguration)) {
            return null;
        }
        return new ImmutableDmaapPublisherConfiguration.Builder()
            .dmaapContentType(
                Optional.ofNullable(updateProducerDmaapContentType).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.dmaapContentType()))
            .dmaapHostName(
                Optional.ofNullable(updateProducerDmaapHostName).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.dmaapHostName()))
            .dmaapPortNumber(
                Optional.ofNullable(updateProducerDmaapPortNumber).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapPublisherConfiguration.dmaapPortNumber()))
            .dmaapProtocol(
                Optional.ofNullable(updateProducerDmaapProtocol).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.dmaapProtocol()))
            .dmaapTopicName(
                Optional.ofNullable(updateProducerDmaapTopicName).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.dmaapTopicName()))
            .dmaapUserName(
                Optional.ofNullable(updateProducerDmaapUserName).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.dmaapUserName()))
            .dmaapUserPassword(
                Optional.ofNullable(updateProducerDmaapUserPassword).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.dmaapUserPassword()))
            .trustStorePath(
                Optional.ofNullable(trustStorePath).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.trustStorePath()))
            .trustStorePasswordPath(
                Optional.ofNullable(trustStorePasswordPath).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.trustStorePasswordPath()))
            .keyStorePath(
                Optional.ofNullable(keyStorePath).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.keyStorePath()))
            .keyStorePasswordPath(
                Optional.ofNullable(keyStorePasswordPath).filter(isEmpty.negate())
                    .orElse(dmaapUpdatePublisherConfiguration.keyStorePasswordPath()))
            .enableDmaapCertAuth(
                Optional.ofNullable(enableDmaapCertAuth).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapUpdatePublisherConfiguration.enableDmaapCertAuth()))
            .build();
    }

    private boolean noFileConfiguration(Object object) {
        return Objects.isNull(object);
    }
}
