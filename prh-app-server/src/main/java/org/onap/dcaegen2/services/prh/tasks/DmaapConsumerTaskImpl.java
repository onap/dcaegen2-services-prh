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

import javax.net.ssl.SSLException;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.ConsumerReactiveHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.DMaaPConsumerReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.DMaaPReactiveWebClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.model.ConsumerDmaapModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class DmaapConsumerTaskImpl implements DmaapConsumerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapConsumerTaskImpl.class);
    private final Config config;
    private final DmaapConsumerJsonParser dmaapConsumerJsonParser;
    private final ConsumerReactiveHttpClientFactory httpClientFactory;

    @Autowired
    public DmaapConsumerTaskImpl(Config config) {
        this(config, new DmaapConsumerJsonParser(),
                new ConsumerReactiveHttpClientFactory(new DMaaPReactiveWebClientFactory()));
    }

    DmaapConsumerTaskImpl(Config prhAppConfig,
                          DmaapConsumerJsonParser dmaapConsumerJsonParser,
                          ConsumerReactiveHttpClientFactory httpClientFactory) {
        this.config = prhAppConfig;
        this.dmaapConsumerJsonParser = dmaapConsumerJsonParser;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void initConfigs() {
        config.initFileStreamReader();
    }

    @Override
    public Flux<ConsumerDmaapModel> execute(String object) throws SSLException {
        DMaaPConsumerReactiveHttpClient dmaaPConsumerReactiveHttpClient = resolveClient();
        LOGGER.debug("Method called with arg {}", object);
        return dmaapConsumerJsonParser.getJsonObject(dmaaPConsumerReactiveHttpClient.getDMaaPConsumerResponse());
    }

    @Override
    public DMaaPConsumerReactiveHttpClient resolveClient() throws SSLException {
        return httpClientFactory.create(config.getDmaapConsumerConfiguration());
    }
}
