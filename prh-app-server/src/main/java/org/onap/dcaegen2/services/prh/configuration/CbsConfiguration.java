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
import java.util.Optional;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;


@Profile("!autoCommitDisabled")
public class CbsConfiguration implements Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(CbsConfiguration.class);
    protected static final String CBS_CONFIG_MISSING = "CBS config missing";
    protected AaiClientConfiguration aaiClientCBSConfiguration;
    protected MessageRouterPublishRequest messageRouterCBSPublishRequest;
    protected MessageRouterSubscribeRequest messageRouterCBSSubscribeRequest;
    protected MessageRouterPublishRequest messageRouterCBSUpdatePublishRequest;
    
    public void parseCBSConfig(JsonObject jsonObject) {
        
        LOGGER.info("Received application configuration: {}", jsonObject);
        CbsContentParser  consulConfigurationParser = new CbsContentParser(jsonObject);
        aaiClientCBSConfiguration = consulConfigurationParser.getAaiClientConfig();

        messageRouterCBSPublishRequest = consulConfigurationParser.getMessageRouterPublishRequest();
        messageRouterCBSUpdatePublishRequest = consulConfigurationParser.getMessageRouterUpdatePublishRequest();

        messageRouterCBSSubscribeRequest = consulConfigurationParser.getMessageRouterSubscribeRequest();
     }

    @Override
    public MessageRouterPublishRequest getMessageRouterPublishRequest() {
        return Optional.ofNullable(messageRouterCBSPublishRequest)
                .orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public MessageRouterPublishRequest getMessageRouterUpdatePublishRequest() {
        return Optional.ofNullable(messageRouterCBSUpdatePublishRequest)
                .orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public AaiClientConfiguration getAaiClientConfiguration() {
        return Optional.ofNullable(aaiClientCBSConfiguration)
                .orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    @Override
    public MessageRouterSubscribeRequest getMessageRouterSubscribeRequest() {
        return Optional.ofNullable(messageRouterCBSSubscribeRequest)
                .orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }
    
}
