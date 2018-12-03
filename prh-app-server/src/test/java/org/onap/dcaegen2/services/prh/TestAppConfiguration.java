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

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.ImmutableDmaapPublisherConfiguration;


public class TestAppConfiguration {
    public static ImmutableDmaapConsumerConfiguration createDefaultDmaapConsumerConfiguration() {
        return new ImmutableDmaapConsumerConfiguration.Builder()
                .consumerGroup("OpenDCAE-c12")
                .consumerId("c12")
                .dmaapContentType("application/json")
                .dmaapHostName("message-router.onap.svc.cluster.local")
                .dmaapPortNumber(3904)
                .dmaapProtocol("http")
                .dmaapUserName("admin")
                .dmaapUserPassword("admin")
                .trustStorePath("/opt/app/prh/local/org.onap.prh.trust.jks")
                .trustStorePasswordPath("change_it")
                .keyStorePath("/opt/app/prh/local/org.onap.prh.p12")
                .keyStorePasswordPath("change_it")
                .enableDmaapCertAuth(false)
                .dmaapTopicName("/events/unauthenticated.SEC_OTHER_OUTPUT")
                .timeoutMs(-1)
                .messageLimit(-1)
                .build();
    }

    public static ImmutableDmaapPublisherConfiguration createDefaultDmaapPublisherConfiguration() {
        return new ImmutableDmaapPublisherConfiguration.Builder()
                .dmaapContentType("application/json")
                .dmaapHostName("message-router.onap.svc.cluster.local")
                .dmaapPortNumber(3904)
                .dmaapProtocol("http")
                .dmaapUserName("admin")
                .dmaapUserPassword("admin")
                .trustStorePath("/opt/app/prh/local/org.onap.prh.trust.jks")
                .trustStorePasswordPath("change_it")
                .keyStorePath("/opt/app/prh/local/org.onap.prh.p12")
                .keyStorePasswordPath("change_it")
                .enableDmaapCertAuth(false)
                .dmaapTopicName("/events/unauthenticated.PNF_READY")
                .build();
    }

    public static ImmutableAaiClientConfiguration createDefaultAaiClientConfiguration() {
        return new ImmutableAaiClientConfiguration.Builder()
                .aaiHost("aai.onap.svc.cluster.local")
                .aaiPort(8443)
                .aaiProtocol("https")
                .aaiUserName("AAI")
                .aaiUserPassword("AAI")
                .aaiIgnoreSslCertificateErrors(true)
                .aaiBasePath("/aai/v12")
                .aaiPnfPath("/network/pnfs/pnf")
                .trustStorePath("/opt/app/prh/local/org.onap.prh.trust.jks")
                .trustStorePasswordPath("change_it")
                .keyStorePath("/opt/app/prh/local/org.onap.prh.p12")
                .keyStorePasswordPath("change_it")
                .enableAaiCertAuth(false)
                .build();
    }
}