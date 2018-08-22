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

import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.prh.service.consumer.DMaaPConsumerReactiveHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class DmaapConsumerTaskImpl extends DmaapConsumerTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Config config;
    private DmaapConsumerJsonParser dmaapConsumerJsonParser;
    private DMaaPConsumerReactiveHttpClient dmaaPConsumerReactiveHttpClient;

    @Autowired
    public DmaapConsumerTaskImpl(@Qualifier("cloudConfiguration") Config config) {
        this.config = config;
        this.dmaapConsumerJsonParser = new DmaapConsumerJsonParser();
    }

    DmaapConsumerTaskImpl(AppConfig prhAppConfig, DmaapConsumerJsonParser dmaapConsumerJsonParser) {
        this.config = prhAppConfig;
        this.dmaapConsumerJsonParser = dmaapConsumerJsonParser;
    }

    @Override
    Mono<ConsumerDmaapModel> consume(Mono<String> message) {
        return dmaapConsumerJsonParser.getJsonObject(message);
    }

    @Override
    public Mono<ConsumerDmaapModel> execute(String object) {
        dmaaPConsumerReactiveHttpClient = resolveClient();
        logger.trace("Method called with arg {}", object);
        return consume((dmaaPConsumerReactiveHttpClient.getDMaaPConsumerResponse()));
    }

    @Override
    void initConfigs() {
        config.initFileStreamReader();
    }

    @Override
    protected DmaapConsumerConfiguration resolveConfiguration() {
        return config.getDmaapConsumerConfiguration();
    }

    @Override
    DMaaPConsumerReactiveHttpClient resolveClient() {
        return dmaaPConsumerReactiveHttpClient == null
            ? new DMaaPConsumerReactiveHttpClient(resolveConfiguration()).createDMaaPWebClient(buildWebClient())
            : dmaaPConsumerReactiveHttpClient;
    }
}
