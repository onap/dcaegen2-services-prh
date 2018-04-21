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
package org.onap.dcaegen2.services.prh.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onap.dcaegen2.services.prh.IT.junit5.mockito.MockitoExtension;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/9/18
 */
@ExtendWith({MockitoExtension.class})
class PrhAppConfigTest {

    private static final String PRH_ENDPOINTS = "prh_endpoints.json";
    private static final String jsonString = "{\"configs\":{\"aai\":{\"aaiHttpClientConfiguration\":{\"aaiHost\":\"\",\"aaiHostPortNumber\":8080,\"aaiIgnoreSSLCertificateErrors\":true,\"aaiProtocol\":\"https\",\"aaiUserName\":\"admin\",\"aaiUserPassword\":\"admin\"}},\"dmaap\":{\"dmaapConsumerConfiguration\":{\"consumerGroup\":\"other\",\"consumerId\":\"1\",\"dmaapContentType\":\"application/json\",\"dmaapHostName\":\"localhost\",\"dmaapPortNumber\":2222,\"dmaapProtocol\":\"http\",\"dmaapTopicName\":\"temp\",\"dmaapUserName\":\"admin\",\"dmaapUserPassword\":\"admin\",\"messageLimit\":1000,\"timeoutMS\":1000},\"dmaapProducerConfiguration\":{\"dmaapContentType\":\"application/json\",\"dmaapHostName\":\"localhost\",\"dmaapPortNumber\":2223,\"dmaapProtocol\":\"http\",\"dmaapTopicName\":\"temp\",\"dmaapUserName\":\"admin\",\"dmaapUserPassword\":\"admin\"}}}}";
    private static final String incorrectJsonString = "{\"configs\":{\"aai\":{\"aaiHttpClientConfiguration\":{\"aaiHost\":\"\",\"aaiHostPortNumber\":8080,\"aaiIgnoreSSLCertificateErrors\":true,\"aaiProtocol\":\"https\",\"aaiUserName\":\"admin\",\"aaiUserPassword\":\"admin\"}},\"dmaap\":{\"dmaapConsumerConfiguration\":{\"consumerGroup\":\"other\",\"consumerId\":\"1\",\"dmaapContentType\":\"application/json\",\"dmaapHostName\":\"localhost\",\"dmaapPortNumber\":2222,\"dmaapProtocol\":\"http\",\"dmaapTopicName\":\"temp\",\"dmaapUserName\":\"admin\",\"dmaapUserPassword\":\"admin\",\"messageLimit\":1000,\"timeoutMS\":1000},\"dmaapProducerConfiguration\":{\"dmaapContentType\":\"application/json\",\"dmaapHostName\":\"localhost\",\"dmaapPortNumber\":2223,\"dmaapProtocol\":\"http\",\"dmaaptopicName\":\"temp\",\"dmaapuserName\":\"admin\",\"dmaapuserPassword\":\"admin\"}}}}";
    private static PrhAppConfig prhAppConfig;

    private static String filePath = Objects
        .requireNonNull(PrhAppConfigTest.class.getClassLoader().getResource(PRH_ENDPOINTS)).getFile();

    @BeforeEach
    public void setUp() {
        prhAppConfig = spy(new PrhAppConfig());
    }

    @Test
    public void whenApplicationWasStarted_FilePathIsSet() {
        //
        // When
        //
        prhAppConfig.setFilepath(filePath);
        //
        // Then
        //
        verify(prhAppConfig, times(1)).setFilepath(anyString());
        verify(prhAppConfig, times(0)).initFileStreamReader();
        Assertions.assertEquals(filePath, prhAppConfig.getFilepath());
    }

    @Test
    public void whenTheConfigurationFits_GetAaiAndDmaapObjectRepresentationConfiguration()
        throws FileNotFoundException {
        //
        // Given
        //
        InputStream inputStream = new ByteArrayInputStream((jsonString.getBytes(
            StandardCharsets.UTF_8)));
        //
        // When
        //
        prhAppConfig.setFilepath(filePath);
        doReturn(inputStream).when(prhAppConfig).getInputStream(any());
        prhAppConfig.initFileStreamReader();
        //
        // Then
        //
        verify(prhAppConfig, times(1)).setFilepath(anyString());
        verify(prhAppConfig, times(1)).initFileStreamReader();
        Assertions.assertNotNull(prhAppConfig.getAAIHttpClientConfiguration());
        Assertions.assertNotNull(prhAppConfig.getDmaapConsumerConfiguration());
        Assertions.assertNotNull(prhAppConfig.getDmaapPublisherConfiguration());
    }

    @Test
    public void whenFileIsNotExist_ThrowFileNotFoundExcepton() {
        //
        // Given
        //
        filePath = "/temp.json";
        prhAppConfig.setFilepath(filePath);
        //
        // When
        //
        prhAppConfig.initFileStreamReader();
        //
        // Then
        //
        verify(prhAppConfig, times(1)).setFilepath(anyString());
        verify(prhAppConfig, times(1)).initFileStreamReader();
        Assertions.assertNull(prhAppConfig.getAAIHttpClientConfiguration());
        Assertions.assertNull(prhAppConfig.getDmaapConsumerConfiguration());
        Assertions.assertNull(prhAppConfig.getDmaapPublisherConfiguration());

    }

    @Test
    public void whenFileIsExistsButJsonIsIncorrect() throws FileNotFoundException {
        //
        // Given
        //
        InputStream inputStream = new ByteArrayInputStream((incorrectJsonString.getBytes(
            StandardCharsets.UTF_8)));
        //
        // When
        //
        prhAppConfig.setFilepath(filePath);
        doReturn(inputStream).when(prhAppConfig).getInputStream(any());
        prhAppConfig.initFileStreamReader();
        //
        // Then
        //
        verify(prhAppConfig, times(1)).setFilepath(anyString());
        verify(prhAppConfig, times(1)).initFileStreamReader();
        Assertions.assertNotNull(prhAppConfig.getAAIHttpClientConfiguration());
        Assertions.assertNotNull(prhAppConfig.getDmaapConsumerConfiguration());
        Assertions.assertNull(prhAppConfig.getDmaapPublisherConfiguration());

    }
}