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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private static final String CORRECT_CONFIG_FILE = "correct_config.json";
    private static final String INCORRECT_CONFIG_FILE = "incorrect_config.json";
    private static final String NOT_JSON_OBJECT_FILE = "not_json_object.json";
    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        appConfig = new AppConfig();
    }

    @Test
    void whenTheConfigurationFits() throws Exception {
        InputStream inputStream = createInputStream(CORRECT_CONFIG_FILE);
        appConfig.setResourceFile(new InputStreamResource(inputStream));
        appConfig.initFileStreamReader();

        assertNotNull(appConfig.getDmaapConsumerConfiguration());
        assertNotNull(appConfig.getDmaapPublisherConfiguration());
        assertNotNull(appConfig.getAaiClientConfiguration());
    }

    @Test
    void whenFileDoesNotExist() throws Exception {
        InputStream inputStream = createInputStream(CORRECT_CONFIG_FILE);
        Resource resource = spy(new InputStreamResource(inputStream));
        when(resource.getInputStream()).thenThrow(new IOException());
        appConfig.setResourceFile(resource);
        appConfig.initFileStreamReader();

        assertNull(appConfig.getAaiClientConfiguration());
        assertNull(appConfig.getDmaapConsumerConfiguration());
        assertNull(appConfig.getDmaapPublisherConfiguration());
    }

    @Test
    void whenFileExistsButDmaapPublisherJsonConfigurationIsIncorrect() throws Exception {
        InputStream inputStream = createInputStream(INCORRECT_CONFIG_FILE);
        appConfig.setResourceFile(new InputStreamResource(inputStream));
        appConfig.initFileStreamReader();

        assertNotNull(appConfig.getAaiClientConfiguration());
        assertNotNull(appConfig.getDmaapConsumerConfiguration());
        assertNull(appConfig.getDmaapPublisherConfiguration());
    }

    @Test
    void whenRootElementIsNotAJsonObject() throws Exception {
        InputStream inputStream = createInputStream(NOT_JSON_OBJECT_FILE);
        appConfig.setResourceFile(new InputStreamResource(inputStream));
        appConfig.initFileStreamReader();


        assertNull(appConfig.getAaiClientConfiguration());
        assertNull(appConfig.getDmaapConsumerConfiguration());
        assertNull(appConfig.getDmaapPublisherConfiguration());
    }

    private InputStream createInputStream(String jsonFile) throws Exception {
        return new ByteArrayInputStream(readAllBytes(Paths.get(getSystemResource(jsonFile).toURI())));
    }
}