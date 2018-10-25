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

import static java.lang.ClassLoader.getSystemResource;
import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onap.dcaegen2.services.prh.integration.junit5.mockito.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/9/18
 */
@ExtendWith({MockitoExtension.class})
class PrhAppConfigTest {

    private final String jsonString =
            new String(readAllBytes(Paths.get(getSystemResource("correct_config.json").toURI())));
    private final String incorrectJsonString =
            new String(readAllBytes(Paths.get(getSystemResource("incorrect_config.json").toURI())));
    private PrhAppConfig prhAppConfig;
    private AppConfig appConfig;


    PrhAppConfigTest() throws Exception {
    }

    @BeforeEach
    void setUp() {
        prhAppConfig = spy(PrhAppConfig.class);
        appConfig = spy(new AppConfig());
    }

    @Test
    void whenTheConfigurationFits_GetAaiAndDmaapObjectRepresentationConfiguration() {
        //
        // Given
        //
        InputStream inputStream = new ByteArrayInputStream((jsonString.getBytes(
                StandardCharsets.UTF_8)));
        //
        // When
        //
        prhAppConfig.setResourceFile(new InputStreamResource(inputStream));
        prhAppConfig.initFileStreamReader();
        appConfig.dmaapConsumerConfiguration = prhAppConfig.getDmaapConsumerConfiguration();
        appConfig.dmaapPublisherConfiguration = prhAppConfig.getDmaapPublisherConfiguration();
        appConfig.aaiClientConfiguration = prhAppConfig.getAaiClientConfiguration();
        //
        // Then
        //
        verify(prhAppConfig).initFileStreamReader();
        assertNotNull(prhAppConfig.getDmaapConsumerConfiguration());
        assertNotNull(prhAppConfig.getDmaapPublisherConfiguration());
        assertNotNull(prhAppConfig.getAaiClientConfiguration());
        assertEquals(appConfig.getDmaapPublisherConfiguration(), prhAppConfig.getDmaapPublisherConfiguration());
        assertEquals(appConfig.getDmaapConsumerConfiguration(), prhAppConfig.getDmaapConsumerConfiguration());
        assertEquals(appConfig.getAaiClientConfiguration(), prhAppConfig.getAaiClientConfiguration());

    }

    @Test
    void whenFileIsNotExist_ThrowIoException() throws IOException {
        //
        // Given
        InputStream inputStream = new ByteArrayInputStream((jsonString.getBytes(
                StandardCharsets.UTF_8)));
        Resource resource = spy(new InputStreamResource(inputStream));
        //
        when(resource.getInputStream()).thenThrow(new IOException());
        prhAppConfig.setResourceFile(resource);
        //
        // When
        //
        prhAppConfig.initFileStreamReader();
        //
        // Then
        //
        verify(prhAppConfig).initFileStreamReader();
        assertNull(prhAppConfig.getAaiClientConfiguration());
        assertNull(prhAppConfig.getDmaapConsumerConfiguration());
        assertNull(prhAppConfig.getDmaapPublisherConfiguration());

    }

    @Test
    void whenFileIsExistsButJsonIsIncorrect() {
        //
        // Given
        //
        InputStream inputStream = new ByteArrayInputStream((incorrectJsonString.getBytes(
                StandardCharsets.UTF_8)));
        //
        // When
        //
        prhAppConfig.setResourceFile(new InputStreamResource(inputStream));
        prhAppConfig.initFileStreamReader();

        //
        // Then
        //
        verify(prhAppConfig).initFileStreamReader();
        assertNotNull(prhAppConfig.getAaiClientConfiguration());
        assertNotNull(prhAppConfig.getDmaapConsumerConfiguration());
        assertNull(prhAppConfig.getDmaapPublisherConfiguration());

    }


    @Test
    void whenTheConfigurationFits_ButRootElementIsNotAJsonObject() {
        // Given
        InputStream inputStream = new ByteArrayInputStream((jsonString.getBytes(
                StandardCharsets.UTF_8)));
        // When
        prhAppConfig.setResourceFile(new InputStreamResource(inputStream));
        JsonElement jsonElement = mock(JsonElement.class);
        when(jsonElement.isJsonObject()).thenReturn(false);
        doReturn(jsonElement).when(prhAppConfig).getJsonElement(any(JsonParser.class), any(InputStream.class));
        prhAppConfig.initFileStreamReader();
        appConfig.dmaapConsumerConfiguration = prhAppConfig.getDmaapConsumerConfiguration();
        appConfig.dmaapPublisherConfiguration = prhAppConfig.getDmaapPublisherConfiguration();
        appConfig.aaiClientConfiguration = prhAppConfig.getAaiClientConfiguration();

        // Then
        verify(prhAppConfig).initFileStreamReader();
        assertNull(prhAppConfig.getAaiClientConfiguration());
        assertNull(prhAppConfig.getDmaapConsumerConfiguration());
        assertNull(prhAppConfig.getDmaapPublisherConfiguration());
    }
}