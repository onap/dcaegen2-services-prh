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

import java.util.Optional;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.prh.service.consumer.ExtendedDmaapConsumerHttpClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class DmaapConsumerTaskImpl extends DmaapConsumerTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Config prhAppConfig;
    private ExtendedDmaapConsumerHttpClientImpl extendedDmaapConsumerHttpClient;
    private DmaapConsumerJsonParser dmaapConsumerJsonParser;

    @Autowired
    public DmaapConsumerTaskImpl(AppConfig prhAppConfig) {
        this.prhAppConfig = prhAppConfig;
        this.dmaapConsumerJsonParser = new DmaapConsumerJsonParser();
    }

    DmaapConsumerTaskImpl(AppConfig prhAppConfig, DmaapConsumerJsonParser dmaapConsumerJsonParser) {
        this.prhAppConfig = prhAppConfig;
        this.dmaapConsumerJsonParser = dmaapConsumerJsonParser;
    }


    @Override
    ConsumerDmaapModel consume(String message) throws PrhTaskException {
        logger.info("Consumed model from DMaaP: {}", message);
        return dmaapConsumerJsonParser.getJsonObject(message)
            .orElseThrow(() -> new DmaapNotFoundException("Null response from JSON Object in single request"));
    }

    @Override
    public ConsumerDmaapModel execute(String object) throws PrhTaskException {
        extendedDmaapConsumerHttpClient = resolveClient();
        logger.trace("Method called with arg {}", object);
        return consume((extendedDmaapConsumerHttpClient.getHttpConsumerResponse().orElseThrow(() ->
            new PrhTaskException("DMaaPConsumerTask has returned null"))));
    }

    @Override
    void initConfigs() {
        prhAppConfig.initFileStreamReader();
    }

    protected DmaapConsumerConfiguration resolveConfiguration() {
        return prhAppConfig.getDmaapConsumerConfiguration();
    }

    @Override
    ExtendedDmaapConsumerHttpClientImpl resolveClient() {
        return Optional.ofNullable(extendedDmaapConsumerHttpClient)
            .orElseGet(() -> new ExtendedDmaapConsumerHttpClientImpl(resolveConfiguration()));
    }
}