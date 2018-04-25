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

import java.util.Optional;
import org.onap.dcaegen2.services.config.AAIHttpClientConfiguration;
import org.onap.dcaegen2.services.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.config.ImmutableAAIHttpClientConfiguration;
import org.onap.dcaegen2.services.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.services.config.ImmutableDmaapPublisherConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/9/18
 */

@Component
@Configuration
public class AppConfig extends PrhAppConfig {

    @Value("${dmaap.dmaapConsumerConfiguration.dmmapHostName:}")
    public String consumerDmmapHostName;

    @Value("${dmaap.dmaapConsumerConfiguration.dmmapPortNumber:}")
    public Integer consumerDmmapPortNumber;

    @Value("${dmaap.dmaapConsumerConfiguration.dmmapTopicName:}")
    public String consumerDmmapTopicName;

    @Value("${dmaap.dmaapConsumerConfiguration.dmmapProtocol:}")
    public String consumerDmmapProtocol;

    @Value("${dmaap.dmaapConsumerConfiguration.dmmapUserName:}")
    public String consumerDmmapUserName;

    @Value("${dmaap.dmaapConsumerConfiguration.dmmapUserPassword:}")
    public String consumerDmmapUserPassword;

    @Value("${dmaap.dmaapConsumerConfiguration.dmmapContentType:}")
    public String consumerDmmapContentType;

    @Value("${dmaap.dmaapConsumerConfiguration.consumerId:}")
    public String consumerId;

    @Value("${dmaap.dmaapConsumerConfiguration.consumerGroup:}")
    public String consumerGroup;

    @Value("${dmaap.dmaapConsumerConfiguration.timeoutMS:}")
    public Integer consumerTimeoutMS;

    @Value("${dmaap.dmaapConsumerConfiguration.message-limit:}")
    public Integer consumerMessageLimit;

    @Value("${dmaap.dmaapProducerConfiguration.dmmapHostName:}")
    public String producerDmmapHostName;

    @Value("${dmaap.dmaapProducerConfiguration.dmmapPortNumber:}")
    public Integer producerDmmapPortNumber;

    @Value("${dmaap.dmaapProducerConfiguration.dmmapTopicName:}")
    public String producerDmmapTopicName;

    @Value("${dmaap.dmaapProducerConfiguration.dmmapProtocol:}")
    public String producerDmmapProtocol;

    @Value("${dmaap.dmaapProducerConfiguration.dmmapUserName:}")
    public String producerDmmapUserName;

    @Value("${dmaap.dmaapProducerConfiguration.dmmapUserPassword:}")
    public String producerDmmapUserPassword;

    @Value("${dmaap.dmaapProducerConfiguration.dmmapContentType:}")
    public String producerDmmapContentType;

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

    @Override
    public DmaapConsumerConfiguration getDmaapConsumerConfiguration() {
        return new ImmutableDmaapConsumerConfiguration.Builder()
            .dmaapUserPassword(
                Optional.ofNullable(consumerDmmapUserPassword).orElse(dmaapConsumerConfiguration.dmaapUserPassword()))
            .dmaapUserName(
                Optional.ofNullable(consumerDmmapUserName).orElse(dmaapConsumerConfiguration.dmaapUserName()))
            .dmaapHostName(
                Optional.ofNullable(consumerDmmapHostName).orElse(dmaapConsumerConfiguration.dmaapHostName()))
            .dmaapPortNumber(
                Optional.ofNullable(consumerDmmapPortNumber).orElse(dmaapConsumerConfiguration.dmaapPortNumber()))
            .dmaapProtocol(
                Optional.ofNullable(consumerDmmapProtocol).orElse(dmaapConsumerConfiguration.dmaapProtocol()))
            .dmaapContentType(
                Optional.ofNullable(consumerDmmapContentType).orElse(dmaapConsumerConfiguration.dmaapContentType()))
            .dmaapTopicName(
                Optional.ofNullable(consumerDmmapTopicName).orElse(dmaapConsumerConfiguration.dmaapTopicName()))
            .messageLimit(
                Optional.ofNullable(consumerMessageLimit).orElse(dmaapConsumerConfiguration.messageLimit()))
            .timeoutMS(Optional.ofNullable(consumerTimeoutMS).orElse(dmaapConsumerConfiguration.timeoutMS()))
            .consumerGroup(Optional.ofNullable(consumerGroup).orElse(dmaapConsumerConfiguration.consumerGroup()))
            .consumerId(Optional.ofNullable(consumerId).orElse(dmaapConsumerConfiguration.consumerId()))
            .build();
    }

    @Override
    public AAIHttpClientConfiguration getAAIHttpClientConfiguration() {
        return new ImmutableAAIHttpClientConfiguration.Builder()
            .aaiHost(Optional.ofNullable(aaiHost).orElse(aaiHttpClientConfiguration.aaiHost()))
            .aaiHostPortNumber(
                Optional.ofNullable(aaiHostPortNumber).orElse(aaiHttpClientConfiguration.aaiHostPortNumber()))
            .aaiIgnoreSSLCertificateErrors(
                Optional.ofNullable(aaiIgnoreSSLCertificateErrors)
                    .orElse(aaiHttpClientConfiguration.aaiIgnoreSSLCertificateErrors()))
            .aaiProtocol(Optional.ofNullable(aaiProtocol).orElse(aaiHttpClientConfiguration.aaiProtocol()))
            .aaiUserName(
                Optional.ofNullable(aaiUserName).orElse(aaiHttpClientConfiguration.aaiUserName()))
            .aaiUserPassword(Optional.ofNullable(
                aaiUserPassword).orElse(aaiHttpClientConfiguration.aaiUserPassword()))
            .build();
    }

    @Override
    public DmaapPublisherConfiguration getDmaapPublisherConfiguration() {
        return new ImmutableDmaapPublisherConfiguration.Builder()
            .dmaapContentType(
                Optional.ofNullable(producerDmmapContentType).orElse(dmaapPublisherConfiguration.dmaapContentType()))
            .dmaapHostName(
                Optional.ofNullable(producerDmmapHostName).orElse(dmaapPublisherConfiguration.dmaapHostName()))
            .dmaapPortNumber(
                Optional.ofNullable(producerDmmapPortNumber).orElse(dmaapPublisherConfiguration.dmaapPortNumber()))
            .dmaapProtocol(
                Optional.ofNullable(producerDmmapProtocol).orElse(dmaapPublisherConfiguration.dmaapProtocol()))
            .dmaapTopicName(
                Optional.ofNullable(producerDmmapTopicName).orElse(dmaapPublisherConfiguration.dmaapTopicName()))
            .dmaapUserName(
                Optional.ofNullable(producerDmmapUserName).orElse(dmaapPublisherConfiguration.dmaapUserName()))
            .dmaapUserPassword(
                Optional.ofNullable(producerDmmapUserPassword).orElse(dmaapPublisherConfiguration.dmaapUserPassword()))
            .build();
    }

}
