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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.AAIClientConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AAIHttpClientImplTest {

    private static AAIClientImpl testedObject;
    private static AAIClientConfiguration aaiHttpClientConfigurationMock;


    @BeforeAll
    public static void setup() {
        aaiHttpClientConfigurationMock = mock(AAIClientConfiguration.class);
        when(aaiHttpClientConfigurationMock.aaiHost()).thenReturn("54.45.33.2");
        when(aaiHttpClientConfigurationMock.aaiProtocol()).thenReturn("https");
        when(aaiHttpClientConfigurationMock.aaiHostPortNumber()).thenReturn(1234);
        when(aaiHttpClientConfigurationMock.aaiUserName()).thenReturn("PNF");
        when(aaiHttpClientConfigurationMock.aaiUserPassword()).thenReturn("PNF");
        when(aaiHttpClientConfigurationMock.aaiIgnoreSSLCertificateErrors()).thenReturn(true);

        testedObject  = new AAIClientImpl(aaiHttpClientConfigurationMock);
    }

    @Test
    public void getAAIHttpClientObject_shouldNotBeNull() {
        testedObject.getAAIHttpClient();
        assertNotNull(testedObject.getAAIHttpClient());
    }
}

