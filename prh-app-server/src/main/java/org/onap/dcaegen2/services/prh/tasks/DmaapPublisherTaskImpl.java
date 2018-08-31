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

import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.producer.DMaaPProducerReactiveHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Component
public class DmaapPublisherTaskImpl extends DmaapPublisherTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapPublisherTaskImpl.class);
    private static final Marker INVOKE = MarkerFactory.getMarker("INVOKE");
    private final Config config;
    private DMaaPProducerReactiveHttpClient dmaapProducerReactiveHttpClient;

    @Autowired
    public DmaapPublisherTaskImpl(Config config) {
        this.config = config;
    }

    @Override
    Mono<ResponseEntity<String>> publish(ConsumerDmaapModel consumerDmaapModel) {
        return dmaapProducerReactiveHttpClient.getDMaaPProducerResponse(consumerDmaapModel);
    }

    @Override
    public Mono<ResponseEntity<String>> execute(ConsumerDmaapModel consumerDmaapModel) throws DmaapNotFoundException {
        if (consumerDmaapModel == null) {
            throw new DmaapNotFoundException("Invoked null object to DMaaP task");
        }
        dmaapProducerReactiveHttpClient = resolveClient();
        LOGGER.info(INVOKE, "Method called with arg {}", consumerDmaapModel);
        return publish(consumerDmaapModel);
    }

    @Override
    RestTemplate buildWebClient() {
        return new RestTemplate();
    }

    @Override
    protected DmaapPublisherConfiguration resolveConfiguration() {
        return config.getDmaapPublisherConfiguration();
    }

    @Override
    DMaaPProducerReactiveHttpClient resolveClient() {
        return new DMaaPProducerReactiveHttpClient(resolveConfiguration()).createDMaaPWebClient(buildWebClient());
    }
}