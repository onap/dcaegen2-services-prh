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

package org.onap.dcaegen2.services.prh;

import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;

import java.time.Duration;


public class TestAppConfiguration {
    public static ImmutableMessageRouterSubscribeRequest createDefaultMessageRouterSubscribeRequest() {
        return ImmutableMessageRouterSubscribeRequest.builder()
                .consumerGroup("OpenDCAE-c12")
                .sourceDefinition(ImmutableMessageRouterSource.builder()
                        .name("the topic")
                        .topicUrl(String.format("http://%s:%d/events/TOPIC", "www", 1234))
                        .build())
                .consumerId("c12")
                .timeout(Duration.ofMillis(1))
                .build();
    }

    public static ImmutableMessageRouterPublishRequest createDefaultMessageRouterPublishRequest() {
        return ImmutableMessageRouterPublishRequest.builder()
                .contentType("application/json")
                .sinkDefinition(ImmutableMessageRouterSink.builder()
                        .name("the topic")
                        .topicUrl(String.format("http://%s:%d/events/TOPIC", "www", 1234))
                        .build())
                .build();

  }

    public static ImmutableAaiClientConfiguration createDefaultAaiClientConfiguration() {
        return new ImmutableAaiClientConfiguration.Builder()
                .pnfUrl("https://aai.onap.svc.cluster.local:8443/aai/v12/network/logical-links/logical-link")
                .aaiUserName("AAI")
                .aaiUserPassword("AAI")
                .aaiIgnoreSslCertificateErrors(true)
                .aaiServiceInstancePath("/business/customers/customer/${customer}/service-subscriptions/service-subscription/${serviceType}/service-instances/service-instance/${serviceInstanceId}")
                .trustStorePath("/opt/app/prh/local/org.onap.prh.trust.jks")
                .trustStorePasswordPath("change_it")
                .keyStorePath("/opt/app/prh/local/org.onap.prh.p12")
                .keyStorePasswordPath("change_it")
                .enableAaiCertAuth(false)
                .putAaiHeaders("X-FromAppId","prh")
                .putAaiHeaders("X-TransactionId","9999")
                .putAaiHeaders("Accept","application/json")
                .putAaiHeaders("Real-Time","true")
                .putAaiHeaders("Authorization","Basic QUFJOkFBSQ==")
                .build();
    }
}