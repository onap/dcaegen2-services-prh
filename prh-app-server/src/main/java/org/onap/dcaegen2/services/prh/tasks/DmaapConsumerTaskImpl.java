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
import org.onap.dcaegen2.services.prh.service.DMaaPReactiveWebClient;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.prh.service.consumer.DMaaPConsumerReactiveHttpClient;
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
    private final DMaaPReactiveWebClient dmaapReactiveWebClient;

    @Autowired
    public DmaapConsumerTaskImpl(Config config) {
        this(config, new DmaapConsumerJsonParser(), new DMaaPReactiveWebClient());
    }

    DmaapConsumerTaskImpl(Config prhAppConfig, DmaapConsumerJsonParser dmaapConsumerJsonParser,
                          DMaaPReactiveWebClient dmaapReactiveWebClient) {
        this.config = prhAppConfig;
        this.dmaapConsumerJsonParser = dmaapConsumerJsonParser;
        this.dmaapReactiveWebClient = dmaapReactiveWebClient;
    }

    @Override
    public void initConfigs() {
        config.initFileStreamReader();
    }

    @Override
    public Flux<ConsumerDmaapModel> execute(String object) {
        DMaaPConsumerReactiveHttpClient dmaaPConsumerReactiveHttpClient = resolveClient();
        LOGGER.debug("Method called with arg {}", object);
        return consume(dmaaPConsumerReactiveHttpClient.getDMaaPConsumerResponse());
    }

    @Override
    public Flux<ConsumerDmaapModel> consume(Mono<String> message) {
        return dmaapConsumerJsonParser.getJsonObject(message);
    }

    @Override
    public DMaaPConsumerReactiveHttpClient resolveClient() {
        return new DMaaPConsumerReactiveHttpClient(
                config.getDmaapConsumerConfiguration()).createDMaaPWebClient(dmaapReactiveWebClient.build());
    }
}
