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

import org.onap.dcaegen2.services.config.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/9/18
 */

@Component
@Configuration
public class AppConfig extends PrhAppConfig {

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

    @Value("${dmaap.dmaapConsumerConfiguration.timeoutMS:}")
    public Integer consumerTimeoutMS;

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

    @Value("${aai.aaiHttpClientConfiguration.aaiHost:}")
    public String aaiHost;

    @Value("${aai.aaiHttpClientConfiguration.aaiHostPortNumber:}")
    public Integer aaiHostPortNumber;

    @Value("${aai.aaiHttpClientConfiguration.aaiProtocol:}")
    public String aaiProtocol;

    @Value("${aai.aaiHttpClientConfiguration.aaiUserName:}")
    public String aaiUserName;

    @Value("${aai.aaiHttpClientConfiguration.aaiUserPassword:}")
    public String aaiUserPassword;

    @Value("${aai.aaiHttpClientConfiguration.aaiIgnoreSSLCertificateErrors:}")
    public Boolean aaiIgnoreSSLCertificateErrors;

    @Value("${aai.aaiHttpClientConfiguration.aaiBasePath:}")
    public String aaiBasePath;

    @Value("${aai.aaiHttpClientConfiguration.aaiPnfPath:}")
    public String aaiPnfPath;

    @Override
    public DmaapConsumerConfiguration getDmaapConsumerConfiguration() {
        return new ImmutableDmaapConsumerConfiguration.Builder()
            .dmaapUserPassword(
                Optional.ofNullable(consumerDmaapUserPassword).filter(p -> !p.isEmpty())
                    .orElse(dmaapConsumerConfiguration.dmaapUserPassword()))
            .dmaapUserName(
                Optional.ofNullable(consumerDmaapUserName).filter(p -> !p.isEmpty())
                    .orElse(dmaapConsumerConfiguration.dmaapUserName()))
            .dmaapHostName(
                Optional.ofNullable(consumerDmaapHostName).filter(p -> !p.isEmpty())
                    .orElse(dmaapConsumerConfiguration.dmaapHostName()))
            .dmaapPortNumber(
                Optional.ofNullable(consumerDmaapPortNumber).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapConsumerConfiguration.dmaapPortNumber()))
            .dmaapProtocol(
                Optional.ofNullable(consumerDmaapProtocol).filter(p -> !p.isEmpty())
                    .orElse(dmaapConsumerConfiguration.dmaapProtocol()))
            .dmaapContentType(
                Optional.ofNullable(consumerDmaapContentType).filter(p -> !p.isEmpty())
                    .orElse(dmaapConsumerConfiguration.dmaapContentType()))
            .dmaapTopicName(
                Optional.ofNullable(consumerDmaapTopicName).filter(p -> !p.isEmpty())
                    .orElse(dmaapConsumerConfiguration.dmaapTopicName()))
            .messageLimit(
                Optional.ofNullable(consumerMessageLimit).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapConsumerConfiguration.messageLimit()))
            .timeoutMS(Optional.ofNullable(consumerTimeoutMS).filter(p -> !p.toString().isEmpty())
                .orElse(dmaapConsumerConfiguration.timeoutMS()))
            .consumerGroup(Optional.ofNullable(consumerGroup).filter(p -> !p.isEmpty())
                .orElse(dmaapConsumerConfiguration.consumerGroup()))
            .consumerId(Optional.ofNullable(consumerId).filter(p -> !p.isEmpty())
                .orElse(dmaapConsumerConfiguration.consumerId()))
            .build();
    }

    @Override
    public AAIClientConfiguration getAAIClientConfiguration() {
        return new ImmutableAAIClientConfiguration.Builder()
            .aaiHost(Optional.ofNullable(aaiHost).filter(p -> !p.isEmpty()).orElse(aaiClientConfiguration.aaiHost()))
            .aaiHostPortNumber(
                Optional.ofNullable(aaiHostPortNumber).filter(p -> !p.toString().isEmpty())
                    .orElse(aaiClientConfiguration.aaiHostPortNumber()))
            .aaiIgnoreSSLCertificateErrors(
                Optional.ofNullable(aaiIgnoreSSLCertificateErrors).filter(p -> !p.toString().isEmpty())
                    .orElse(aaiClientConfiguration.aaiIgnoreSSLCertificateErrors()))
            .aaiProtocol(
                Optional.ofNullable(aaiProtocol).filter(p -> !p.isEmpty()).orElse(aaiClientConfiguration.aaiProtocol()))
            .aaiUserName(
                Optional.ofNullable(aaiUserName).filter(p -> !p.isEmpty()).orElse(aaiClientConfiguration.aaiUserName()))
            .aaiUserPassword(Optional.ofNullable(aaiUserPassword).filter(p -> !p.isEmpty())
                .orElse(aaiClientConfiguration.aaiUserPassword()))
            .aaiBasePath(Optional.ofNullable(aaiBasePath).filter(p -> !p.isEmpty())
                .orElse(aaiClientConfiguration.aaiBasePath()))
            .aaiPnfPath(
                Optional.ofNullable(aaiPnfPath).filter(p -> !p.isEmpty()).orElse(aaiClientConfiguration.aaiPnfPath()))
            .aaiHeaders(aaiClientConfiguration.aaiHeaders())
            .build();
    }

    @Override
    public DmaapPublisherConfiguration getDmaapPublisherConfiguration() {
        return new ImmutableDmaapPublisherConfiguration.Builder()
            .dmaapContentType(
                Optional.ofNullable(producerDmaapContentType).filter(p -> !p.isEmpty())
                    .orElse(dmaapPublisherConfiguration.dmaapContentType()))
            .dmaapHostName(
                Optional.ofNullable(producerDmaapHostName).filter(p -> !p.isEmpty())
                    .orElse(dmaapPublisherConfiguration.dmaapHostName()))
            .dmaapPortNumber(
                Optional.ofNullable(producerDmaapPortNumber).filter(p -> !p.toString().isEmpty())
                    .orElse(dmaapPublisherConfiguration.dmaapPortNumber()))
            .dmaapProtocol(
                Optional.ofNullable(producerDmaapProtocol).filter(p -> !p.isEmpty())
                    .orElse(dmaapPublisherConfiguration.dmaapProtocol()))
            .dmaapTopicName(
                Optional.ofNullable(producerDmaapTopicName).filter(p -> !p.isEmpty())
                    .orElse(dmaapPublisherConfiguration.dmaapTopicName()))
            .dmaapUserName(
                Optional.ofNullable(producerDmaapUserName).filter(p -> !p.isEmpty())
                    .orElse(dmaapPublisherConfiguration.dmaapUserName()))
            .dmaapUserPassword(
                Optional.ofNullable(producerDmaapUserPassword).filter(p -> !p.isEmpty())
                    .orElse(dmaapPublisherConfiguration.dmaapUserPassword()))
            .build();
    }

}
