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

package org.onap.dcaegen2.services.prh.service.producer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.net.ssl.SSLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;


class DmaaPRestTemplateFactoryTest {

    private static final String KEY_STORE = "org.onap.dcae.jks";
    private static final String KEYSTORE_PASSWORD = "keystore.password";
    private static final String TRUSTSTORE_PASSWORD = "truststore.password";
    private static final String TRUST_STORE = "org.onap.dcae.trust.jks";
    private DmaapPublisherConfiguration publisherConfiguration = mock(DmaapPublisherConfiguration.class);
    private DmaaPRestTemplateFactory factory = new DmaaPRestTemplateFactory();

    @Test
    void build_shouldCreateRestTemplateWithoutSslConfiguration() throws SSLException {
        when(publisherConfiguration.enableDmaapCertAuth()).thenReturn(false);

        Assertions.assertNotNull(factory.build(publisherConfiguration));
    }

    @Test
    void build_shouldCreateRestTemplateWithSslConfiguration() throws SSLException {
        when(publisherConfiguration.enableDmaapCertAuth()).thenReturn(true);
        when(publisherConfiguration.keyStorePath()).thenReturn(getPath(KEY_STORE));
        when(publisherConfiguration.keyStorePasswordPath()).thenReturn(getPath(KEYSTORE_PASSWORD));
        when(publisherConfiguration.trustStorePath()).thenReturn(getPath(TRUST_STORE));
        when(publisherConfiguration.trustStorePasswordPath()).thenReturn(getPath(TRUSTSTORE_PASSWORD));

        Assertions.assertNotNull(factory.build(publisherConfiguration));
    }

    @Test
    void build_shouldThrowSslExceptionWhenSslSetupFailed() {
        when(publisherConfiguration.enableDmaapCertAuth()).thenReturn(true);
        when(publisherConfiguration.keyStorePath()).thenReturn(getPath(KEY_STORE));
        when(publisherConfiguration.keyStorePasswordPath()).thenReturn(getPath(TRUSTSTORE_PASSWORD));
        when(publisherConfiguration.trustStorePath()).thenReturn(getPath(TRUST_STORE));
        when(publisherConfiguration.trustStorePasswordPath()).thenReturn(getPath(TRUSTSTORE_PASSWORD));

        Assertions.assertThrows(SSLException.class, () -> factory.build(publisherConfiguration));
    }

    private String getPath(String fileName) {
        return this.getClass().getClassLoader().getResource(fileName).getPath();
    }
}