/*-
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

package org.onap.dcaegen2.services.prh.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.net.ssl.SSLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.ssl.SslFactory;


class AaiReactiveWebClientFactoryTest {

    private static final String TRUST_STORE_PATH = "trust_store_path";
    private static final String TRUST_STORE_PASS_PATH = "trust_store_pass_path";
    private static final String KEY_STORE_PATH = "key_store_path";
    private static final String KEY_STORE_PASS_PATH = "key_store_pass_path";
    private SslFactory sslFactory = mock(SslFactory.class);
    private AaiClientConfiguration aaiClientConfiguration = mock(AaiClientConfiguration.class);
    private AaiReactiveWebClientFactory aaiReactiveWebClientFactory;

    @Test
    void shouldCreateWebClientWithSecureSslContext() throws SSLException {
        givenEnabledAaiCertAuthConfiguration();
        aaiReactiveWebClientFactory = new AaiReactiveWebClientFactory(sslFactory, aaiClientConfiguration);

        Assertions.assertNotNull(aaiReactiveWebClientFactory.build());
        verify(sslFactory).createSecureContext(KEY_STORE_PATH, KEY_STORE_PASS_PATH,
                TRUST_STORE_PATH, TRUST_STORE_PASS_PATH);
    }

    @Test
    void shouldCreateWebClientWithInsecureSslContext() throws SSLException {
        when(aaiClientConfiguration.enableAaiCertAuth()).thenReturn(false);
        aaiReactiveWebClientFactory = new AaiReactiveWebClientFactory(sslFactory, aaiClientConfiguration);

        Assertions.assertNotNull(aaiReactiveWebClientFactory.build());
        verify(sslFactory).createInsecureContext();
    }

    private void givenEnabledAaiCertAuthConfiguration() {
        when(aaiClientConfiguration.enableAaiCertAuth()).thenReturn(true);
        when(aaiClientConfiguration.trustStorePath()).thenReturn(TRUST_STORE_PATH);
        when(aaiClientConfiguration.trustStorePasswordPath()).thenReturn(TRUST_STORE_PASS_PATH);
        when(aaiClientConfiguration.keyStorePath()).thenReturn(KEY_STORE_PATH);
        when(aaiClientConfiguration.keyStorePasswordPath()).thenReturn(KEY_STORE_PASS_PATH);
    }
}