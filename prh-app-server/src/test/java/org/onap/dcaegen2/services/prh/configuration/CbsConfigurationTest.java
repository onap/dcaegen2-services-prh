/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.ClassLoader.getSystemResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CbsConfigurationTest {

    private static final String EXPECTED_ERROR_MESSAGE_WHEN_CBS_CONFIG_IS_NOT_INITIALIZED = "CBS config missing";

    @Test
    void whenConfigurationIsNotInitializedBasedOnDataReceivedFromCbs_shouldThrowExceptionWithDescriptiveMessage() {
        assertThatThrownBy(() -> new CbsConfiguration().getAaiClientConfiguration())
                .hasMessage(EXPECTED_ERROR_MESSAGE_WHEN_CBS_CONFIG_IS_NOT_INITIALIZED);
        assertThatThrownBy(() -> new CbsConfiguration().getMessageRouterPublisher())
                .hasMessage(EXPECTED_ERROR_MESSAGE_WHEN_CBS_CONFIG_IS_NOT_INITIALIZED);
        assertThatThrownBy(() -> new CbsConfiguration().getMessageRouterSubscriber())
                .hasMessage(EXPECTED_ERROR_MESSAGE_WHEN_CBS_CONFIG_IS_NOT_INITIALIZED);
        assertThatThrownBy(() -> new CbsConfiguration().getMessageRouterSubscribeRequest())
                .hasMessage(EXPECTED_ERROR_MESSAGE_WHEN_CBS_CONFIG_IS_NOT_INITIALIZED);
        assertThatThrownBy(() -> new CbsConfiguration().getMessageRouterPublishRequest())
                .hasMessage(EXPECTED_ERROR_MESSAGE_WHEN_CBS_CONFIG_IS_NOT_INITIALIZED);
        assertThatThrownBy(() -> new CbsConfiguration().getMessageRouterUpdatePublishRequest())
                .hasMessage(EXPECTED_ERROR_MESSAGE_WHEN_CBS_CONFIG_IS_NOT_INITIALIZED);
    }


    @Test
    void cbsConfigurationShouldExposeDataReceivedAsJsonFromCbs() throws Exception {
        JsonObject cbsConfigJson = new Gson().fromJson(new String(Files.readAllBytes(Paths.get(
                getSystemResource("configurationFromCbs.json").toURI()))), JsonObject.class);
        CbsConfiguration cbsConfiguration = new CbsConfiguration();

        cbsConfiguration.parseCBSConfig(cbsConfigJson);

        assertThat(cbsConfiguration.getAaiClientConfiguration()).isNotNull();
        assertThat(cbsConfiguration.getMessageRouterPublisher()).isNotNull();
        assertThat(cbsConfiguration.getMessageRouterSubscriber()).isNotNull();
        assertThat(cbsConfiguration.getMessageRouterPublishRequest()).isNotNull();
        assertThat(cbsConfiguration.getMessageRouterSubscribeRequest()).isNotNull();
        assertThat(cbsConfiguration.getMessageRouterUpdatePublishRequest()).isNotNull();
    }
}