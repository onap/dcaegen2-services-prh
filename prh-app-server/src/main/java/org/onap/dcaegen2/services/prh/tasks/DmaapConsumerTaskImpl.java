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

package org.onap.dcaegen2.services.prh.tasks;

import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class DmaapConsumerTaskImpl implements DmaapConsumerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapConsumerTaskImpl.class);
    private final Config config;
    private final DmaapConsumerJsonParser dmaapConsumerJsonParser;


    @Autowired
    public DmaapConsumerTaskImpl(Config config) {
        this(config, new DmaapConsumerJsonParser());
    }

    DmaapConsumerTaskImpl(Config prhAppConfig, DmaapConsumerJsonParser dmaapConsumerJsonParser) {
        this.config = prhAppConfig;
        this.dmaapConsumerJsonParser = dmaapConsumerJsonParser;
    }

    @Override
    public Flux<ConsumerDmaapModel> execute(String object) {
        MessageRouterSubscriber messageRouterSubscriberClient =
                new MessageRouterSubscriberResolver().resolveClient();
        LOGGER.debug("Method called with arg {}", object);
        Mono<MessageRouterSubscribeResponse> response = messageRouterSubscriberClient
                .get(config.getMessageRouterSubscribeRequest());
        return dmaapConsumerJsonParser.getJsonObject(response);
    }

}