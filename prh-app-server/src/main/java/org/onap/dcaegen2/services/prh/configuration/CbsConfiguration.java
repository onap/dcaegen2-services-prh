/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2023-2026 Deutsche Telekom Intellectual Property. All rights reserved.
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

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;


@Slf4j
public class CbsConfiguration implements Config {
    protected static final String CBS_CONFIG_MISSING = "CBS config missing";
    protected AaiClientConfiguration aaiClientCBSConfiguration;
    protected String publishTopicUrl;
    protected String updatePublishTopicUrl;
    protected String subscribeTopicUrl;
    protected String subscribeConsumerGroup;

    public void parseCBSConfig(JsonObject jsonObject) {
        log.info("Received application configuration: {}", jsonObject);
        CbsContentParser consulConfigurationParser = new CbsContentParser(jsonObject);
        aaiClientCBSConfiguration = consulConfigurationParser.getAaiClientConfig();

        publishTopicUrl = consulConfigurationParser.getPublishTopicUrl();
        updatePublishTopicUrl = consulConfigurationParser.getUpdatePublishTopicUrl();
        subscribeTopicUrl = consulConfigurationParser.getSubscribeTopicUrl();
        subscribeConsumerGroup = consulConfigurationParser.getSubscribeConsumerGroup();
    }

    @Override
    public String getPublishTopicUrl() {
        return Optional.ofNullable(publishTopicUrl)
                .orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public String getUpdatePublishTopicUrl() {
        return Optional.ofNullable(updatePublishTopicUrl)
                .orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public AaiClientConfiguration getAaiClientConfiguration() {
        return Optional.ofNullable(aaiClientCBSConfiguration)
                .orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public String getSubscribeTopicUrl() {
        return Optional.ofNullable(subscribeTopicUrl)
                .orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public String getSubscribeConsumerGroup() {
        return Optional.ofNullable(subscribeConsumerGroup)
                .orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }
}
