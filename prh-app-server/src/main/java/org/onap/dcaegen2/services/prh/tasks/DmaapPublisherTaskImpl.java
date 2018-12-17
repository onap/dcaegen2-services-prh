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

import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;

import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.JsonBodyBuilderImpl;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DMaaPPublisherReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DmaaPRestTemplateFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.PublisherReactiveHttpClientFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Component
public class DmaapPublisherTaskImpl implements DmaapPublisherTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapPublisherTaskImpl.class);
    private final Config config;
    private final PublisherReactiveHttpClientFactory httpClientFactory;

    @Autowired
    public DmaapPublisherTaskImpl(Config config) {
        this(config, new PublisherReactiveHttpClientFactory(new DmaaPRestTemplateFactory(),new JsonBodyBuilderImpl()));
    }

    DmaapPublisherTaskImpl(Config config, PublisherReactiveHttpClientFactory httpClientFactory) {
        this.config = config;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public Mono<ResponseEntity<String>> execute(ConsumerDmaapModel consumerDmaapModel) throws DmaapNotFoundException {
        if (consumerDmaapModel == null) {
            throw new DmaapNotFoundException("Invoked null object to DMaaP task");
        }
        DMaaPPublisherReactiveHttpClient dmaapPublisherReactiveHttpClient = resolveClient();
        LOGGER.info("Method called with arg {}", consumerDmaapModel);
        return dmaapPublisherReactiveHttpClient.getDMaaPProducerResponse(consumerDmaapModel);
    }

    @Override
    public DMaaPPublisherReactiveHttpClient resolveClient() {
        return httpClientFactory.create(config.getDmaapPublisherConfiguration());
    }
}