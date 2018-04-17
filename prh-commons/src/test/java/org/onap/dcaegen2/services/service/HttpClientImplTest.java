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
package org.onap.dcaegen2.services.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.config.AAIClientConfiguration;


public class HttpClientImplTest {

    @Mock
    private static AAIClientConfiguration aaiClientConfigurationMock;


    @Test
    @Disabled
    public void getHttpClientObjectAai_shouldNotBeNull() {
        Mockito.when(aaiClientConfigurationMock.aaiHost()).thenReturn("54.45.33.2");
        Mockito.when(aaiClientConfigurationMock.aaiProtocol()).thenReturn("https");
        Mockito.when(aaiClientConfigurationMock.aaiHostPortNumber()).thenReturn(1234);
        Mockito.when(aaiClientConfigurationMock.aaiUserName()).thenReturn("PNF");
        Mockito.when(aaiClientConfigurationMock.aaiUserPassword()).thenReturn("PNF");
        Mockito.when(aaiClientConfigurationMock.aaiIgnoreSSLCertificateErrors()).thenReturn(true);
        Assertions.assertNotNull(HttpClientImpl.getHttpClient(aaiClientConfigurationMock));
    }
}

