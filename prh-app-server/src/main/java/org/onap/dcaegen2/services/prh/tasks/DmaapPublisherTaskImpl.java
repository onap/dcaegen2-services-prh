/*
 * ============LICENSE_START=======================================================
 * PROJECT
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

import com.google.gson.JsonPrimitive;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.PnfReadyJsonBodyBuilderImpl;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
public class DmaapPublisherTaskImpl implements DmaapPublisherTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapPublisherTaskImpl.class);

    private final Supplier<MessageRouterPublishRequest> config;
    private final MessageRouterPublisherResolver messageRouterPublisherClientResolver;
    private final PnfReadyJsonBodyBuilderImpl pnfReadyJsonBodyBuilder = new PnfReadyJsonBodyBuilderImpl();


    public DmaapPublisherTaskImpl(Supplier<MessageRouterPublishRequest> config, MessageRouterPublisherResolver messageRouterPublisherClientResolver) {
        this.config = config;
        this.messageRouterPublisherClientResolver = messageRouterPublisherClientResolver;
    }

    @Override
    public Flux<MessageRouterPublishResponse> execute(ConsumerDmaapModel consumerDmaapModel) throws DmaapNotFoundException {
        if (consumerDmaapModel == null) {
            throw new DmaapNotFoundException("Invoked null object to DMaaP task");
        }
        MessageRouterPublisher messageRouterPublisher = messageRouterPublisherClientResolver.resolveClient();
        LOGGER.info("Method called with arg {}", consumerDmaapModel);
        String json = pnfReadyJsonBodyBuilder.createJsonBody(consumerDmaapModel);
        return messageRouterPublisher.put(
                config.get(),
                Flux.just(json).map(JsonPrimitive::new));
    }
}