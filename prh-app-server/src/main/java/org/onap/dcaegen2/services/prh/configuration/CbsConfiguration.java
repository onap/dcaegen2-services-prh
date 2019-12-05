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

import com.google.gson.JsonObject;
import java.util.Optional;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DmaapClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CbsConfiguration implements Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(CbsConfiguration.class);
    private static final String CBS_CONFIG_MISSING = "CBS config missing";
    private AaiClientConfiguration aaiClientCBSConfiguration;
    private MessageRouterPublisher messageRouterPublisher;
    private MessageRouterSubscriber messageRouterSubscriber;
    private MessageRouterPublishRequest messageRouterCBSPublishRequest;
    private MessageRouterSubscribeRequest messageRouterCBSSubscribeRequest;
    private MessageRouterPublishRequest messageRouterCBSUpdatePublishRequest;


    public void parseCBSConfig(JsonObject jsonObject) {
        LOGGER.info("Received application configuration: {}", jsonObject);
        CbsContentParser consulConfigurationParser = new CbsContentParser(jsonObject);

        aaiClientCBSConfiguration = consulConfigurationParser.getAaiClientConfig();

        messageRouterPublisher = DmaapClientFactory.createMessageRouterPublisher(
                consulConfigurationParser.getMessageRouterPublisherConfig());
        messageRouterCBSPublishRequest = consulConfigurationParser.getMessageRouterPublishRequest();
        messageRouterCBSUpdatePublishRequest = consulConfigurationParser.getMessageRouterUpdatePublishRequest();

        messageRouterSubscriber = DmaapClientFactory.createMessageRouterSubscriber(
                consulConfigurationParser.getMessageRouterSubscriberConfig());
        messageRouterCBSSubscribeRequest = consulConfigurationParser.getMessageRouterSubscribeRequest();
    }


    @Override
    public MessageRouterPublisher getMessageRouterPublisher() {
        return Optional.ofNullable(messageRouterPublisher).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public MessageRouterSubscriber getMessageRouterSubscriber() {
        return Optional.ofNullable(messageRouterSubscriber).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public MessageRouterPublishRequest getMessageRouterPublishRequest() {
        return Optional.ofNullable(messageRouterCBSPublishRequest).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public MessageRouterPublishRequest getMessageRouterUpdatePublishRequest() {
        return Optional.ofNullable(messageRouterCBSUpdatePublishRequest).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public AaiClientConfiguration getAaiClientConfiguration() {
        return Optional.ofNullable(aaiClientCBSConfiguration).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public MessageRouterSubscribeRequest getMessageRouterSubscribeRequest() {
        return Optional.ofNullable(messageRouterCBSSubscribeRequest).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }
}
