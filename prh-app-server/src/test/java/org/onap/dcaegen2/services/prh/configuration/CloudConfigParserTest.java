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
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.TestAppConfiguration;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableDmaapPublisherConfiguration;


class CloudConfigParserTest {

    private final String correctJson =
            new String(Files.readAllBytes(Paths.get(getSystemResource("flattened_configuration.json").toURI())));
    private final ImmutableAaiClientConfiguration correctAaiClientConfig =
            TestAppConfiguration.createDefaultAaiClientConfiguration();
    private final ImmutableDmaapConsumerConfiguration correctDmaapConsumerConfig =
            TestAppConfiguration.createDefaultDmaapConsumerConfiguration();
    private final ImmutableDmaapPublisherConfiguration correctDmaapPublisherConfig =
            TestAppConfiguration.createDefaultDmaapPublisherConfiguration();
    private final CloudConfigParser cloudConfigParser = new CloudConfigParser(
            new Gson().fromJson(correctJson, JsonObject.class));

    CloudConfigParserTest() throws Exception {
    }

    @Test
    void shouldCreateAaiConfigurationCorrectly() {
        // when
        AaiClientConfiguration aaiClientConfig = cloudConfigParser.getAaiClientConfig();

        // then
        assertThat(aaiClientConfig).isNotNull();
        assertThat(aaiClientConfig).isEqualToComparingFieldByField(correctAaiClientConfig);
    }


    @Test
    void shouldCreateDmaapConsumerConfigurationCorrectly() {
        // when
        DmaapConsumerConfiguration dmaapConsumerConfig = cloudConfigParser.getDmaapConsumerConfig();

        // then
        assertThat(dmaapConsumerConfig).isNotNull();
        assertThat(dmaapConsumerConfig).isEqualToComparingFieldByField(correctDmaapConsumerConfig);
    }


    @Test
    void shouldCreateDmaapPublisherConfigurationCorrectly() {
        // when
        DmaapPublisherConfiguration dmaapPublisherConfig = cloudConfigParser.getDmaapPublisherConfig();

        // then
        assertThat(dmaapPublisherConfig).isNotNull();
        assertThat(dmaapPublisherConfig).isEqualToComparingFieldByField(correctDmaapPublisherConfig);
    }
}