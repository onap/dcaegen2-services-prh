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

import org.onap.dcaegen2.services.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.configuration.PrhAppConfig;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;

import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.service.consumer.ExtendedDmaapConsumerHttpClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class DmaapConsumerTaskImpl extends DmaapConsumerTask<String, ConsumerDmaapModel> {


    private static final Logger logger = LoggerFactory.getLogger(DmaapConsumerTaskImpl.class);

    private ExtendedDmaapConsumerHttpClientImpl extendedDmaapConsumerHttpClient;
    private PrhAppConfig prhAppConfig;

    @Autowired
    public DmaapConsumerTaskImpl(ExtendedDmaapConsumerHttpClientImpl extendedDmaapConsumerHttpClient,
        PrhAppConfig appConfig) {
        this.extendedDmaapConsumerHttpClient = extendedDmaapConsumerHttpClient;
        this.prhAppConfig = appConfig;
    }

    @Override
    protected ConsumerDmaapModel consume(String message) throws DmaapNotFoundException {
        logger.trace("Method %M called with arg {}", message);
        return DmaapConsumerJsonParser.getJsonObject(message);
    }

    @Override
    public Object execute(Object object) throws PrhTaskException {
        logger.trace("Method %M called with arg {}", object);
        return consume((extendedDmaapConsumerHttpClient.getHttpConsumerResponse().orElseThrow(() ->
            new PrhTaskException("DmaapConsumerTask has returned null"))));
    }

    @Override
    protected void initConfigs() {
        prhAppConfig.initFileStreamReader();
    }
}