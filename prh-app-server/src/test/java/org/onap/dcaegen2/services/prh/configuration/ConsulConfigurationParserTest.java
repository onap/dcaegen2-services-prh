/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2026 Deutsche Telekom Intellectual Property. All rights reserved.
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
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.adapter.aai.main.ImmutableAaiClientConfiguration;


class ConsulConfigurationParserTest {

    private final String correctJson =
        new String(Files.readAllBytes(Paths.get(getSystemResource("configurationFromCbs.json").toURI())));
    private final ImmutableAaiClientConfiguration correctAaiClientConfig =
        TestAppConfiguration.createDefaultAaiClientConfiguration();

    private final JsonObject correctConfig = new Gson().fromJson(correctJson, JsonObject.class);
    private final CbsContentParser consulConfigurationParser = new CbsContentParser(correctConfig);

    ConsulConfigurationParserTest() throws Exception {
    }

    @Test
    void shouldCreateAaiConfigurationCorrectly() {
        AaiClientConfiguration aaiClientConfig = consulConfigurationParser.getAaiClientConfig();

        assertThat(aaiClientConfig).isNotNull();
        assertThat(aaiClientConfig).isEqualToComparingFieldByField(correctAaiClientConfig);
    }

    @Test
    void shouldCreateSubscribeTopicUrlCorrectly() {
        String topicUrl = consulConfigurationParser.getSubscribeTopicUrl();

        assertThat(topicUrl).isEqualTo("http://dmaap-mr:2222/events/unauthenticated.VES_PNFREG_OUTPUT");
    }

    @Test
    void shouldCreateSubscribeConsumerGroupCorrectly() {
        String consumerGroup = consulConfigurationParser.getSubscribeConsumerGroup();

        assertThat(consumerGroup).isEqualTo("OpenDCAE-c12");
    }

    @Test
    void shouldCreatePublishTopicUrlCorrectly() {
        String topicUrl = consulConfigurationParser.getPublishTopicUrl();

        assertThat(topicUrl).isEqualTo("http://dmaap-mr:2222/events/unauthenticated.PNF_READY");
    }

    @Test
    void shouldCreateUpdatePublishTopicUrlCorrectly() {
        String topicUrl = consulConfigurationParser.getUpdatePublishTopicUrl();

        assertThat(topicUrl).isEqualTo("http://dmaap-mr:2222/events/unauthenticated.PNF_UPDATE");
    }
}
