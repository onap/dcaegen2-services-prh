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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.TestAppConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.ClassLoader.getSystemResource;
import static org.assertj.core.api.Assertions.assertThat;


class ConsulConfigurationParserTest {

    private final String correctJson =
            new String(Files.readAllBytes(Paths.get(getSystemResource("flattened_configuration.json").toURI())));
    private final ImmutableAaiClientConfiguration correctAaiClientConfig =
            TestAppConfiguration.createDefaultAaiClientConfiguration();
    private final ImmutableMessageRouterSubscribeRequest correctDmaapConsumerConfig =
            TestAppConfiguration.createDefaultMessageRouterSubscribeRequest();
    private final ImmutableMessageRouterPublishRequest correctDmaapPublisherConfig =
            TestAppConfiguration.createDefaultMessageRouterPublishRequest();
    private final CbsContentParser consulConfigurationParser = new CbsContentParser(
            new Gson().fromJson(correctJson, JsonObject.class));

    ConsulConfigurationParserTest() throws Exception {
    }

    @Test
    void shouldCreateAaiConfigurationCorrectly() {
        // when
        AaiClientConfiguration aaiClientConfig = consulConfigurationParser.getAaiClientConfig();

        // then
        assertThat(aaiClientConfig).isNotNull();
        assertThat(aaiClientConfig).isEqualToComparingFieldByField(correctAaiClientConfig);
    }


    @Test
    void shouldCreateDmaapConsumerConfigurationCorrectly() {
        // when
        MessageRouterSubscribeRequest messageRouterSubscribeRequest = consulConfigurationParser.getMessageRouterSubscribeRequest();

        // then
        assertThat(messageRouterSubscribeRequest).isNotNull();
        assertThat(messageRouterSubscribeRequest).isEqualToComparingFieldByField(correctDmaapConsumerConfig);
    }


    @Test
    void shouldCreateDmaapPublisherConfigurationCorrectly() {
        // when
        MessageRouterPublishRequest messageRouterPublishRequest = consulConfigurationParser.getMessageRouterPublishRequest();

        // then
        assertThat(messageRouterPublishRequest).isNotNull();
        assertThat(messageRouterPublishRequest).isEqualToComparingFieldByField(correctDmaapPublisherConfig);
    }
}