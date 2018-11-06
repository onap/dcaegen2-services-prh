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

package org.onap.dcaegen2.services.prh.service.consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.ssl.SslFactory;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/5/18
 */
class DMaaPReactiveWebClientFactoryTest {

    private static final String KEY_STORE = "keyStore";
    private static final String KEY_STORE_PASS = "keyStorePass";
    private static final String TRUST_STORE = "trustStore";
    private static final String TRUST_STORE_PASS = "trustStorePass";
    private SslFactory sslFactory = mock(SslFactory.class);
    private DMaaPReactiveWebClientFactory webClientFactory = new DMaaPReactiveWebClientFactory(sslFactory);

    @Test
    void builder_shouldBuildDMaaPReactiveWebClientwithInsecureSslContext() throws Exception {
        //given
        DmaapConsumerConfiguration dmaapConsumerConfiguration = givenDmaapConfigurationWithSslDisabled();

        //when
        WebClient dmaapReactiveWebClient = webClientFactory.build(dmaapConsumerConfiguration);

        //then
        Assertions.assertNotNull(dmaapReactiveWebClient);
        verify(sslFactory).createInsecureContext();
    }

    @Test
    void builder_shouldBuildDMaaPReactiveWebClientwithSecureSslContext() throws Exception {
        //given
        DmaapConsumerConfiguration dmaapConsumerConfiguration = givenDmaapConfigurationWithSslEnabled();

        //when
        WebClient dmaapReactiveWebClient = webClientFactory.build(dmaapConsumerConfiguration);

        //then
        Assertions.assertNotNull(dmaapReactiveWebClient);
        verify(sslFactory).createSecureContext(KEY_STORE, KEY_STORE_PASS, TRUST_STORE, TRUST_STORE_PASS);
    }

    private DmaapConsumerConfiguration givenDmaapConfigurationWithSslDisabled() {
        DmaapConsumerConfiguration dmaapConsumerConfiguration = mock(DmaapConsumerConfiguration.class);
        when(dmaapConsumerConfiguration.enableDmaapCertAuth()).thenReturn(false);
        return dmaapConsumerConfiguration;
    }

    private DmaapConsumerConfiguration givenDmaapConfigurationWithSslEnabled() {
        DmaapConsumerConfiguration dmaapConsumerConfiguration = mock(DmaapConsumerConfiguration.class);
        when(dmaapConsumerConfiguration.enableDmaapCertAuth()).thenReturn(true);
        when(dmaapConsumerConfiguration.keyStorePath()).thenReturn(KEY_STORE);
        when(dmaapConsumerConfiguration.keyStorePasswordPath()).thenReturn(KEY_STORE_PASS);
        when(dmaapConsumerConfiguration.trustStorePath()).thenReturn(TRUST_STORE);
        when(dmaapConsumerConfiguration.trustStorePasswordPath()).thenReturn(TRUST_STORE_PASS);
        return dmaapConsumerConfiguration;
    }
}