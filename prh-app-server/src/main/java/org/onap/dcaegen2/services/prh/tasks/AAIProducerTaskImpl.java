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

import com.google.gson.Gson;
import org.onap.dcaegen2.services.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.AAINotFoundException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.service.AAIProducerClient;
import org.onap.dcaegen2.services.utils.HttpRequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Component
public class AAIProducerTaskImpl extends AAIProducerTask<AAIClientConfiguration, ConsumerDmaapModel, Object> {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private final Config prhAppConfig;
    private AAIProducerClient producerClient;
    private Optional<String> response;

    @Autowired
    public AAIProducerTaskImpl(AppConfig prhAppConfig) {
        this.prhAppConfig = prhAppConfig;
    }

    @Override
    protected Object publish(ConsumerDmaapModel consumerDmaapModel) throws AAINotFoundException {
        logger.trace("Method %M called with arg {}", consumerDmaapModel);
        producerClient = new AAIProducerClient(prhAppConfig.getAAIClientConfiguration());
        //TODO: @Piotr Wielebski
        response = producerClient.getHttpResponse(null);
        return response.get();
    }

    @Override
    public Object execute(Object object) throws AAINotFoundException {
        logger.trace("Method %M called with arg {}", object);
        //TODO: @Piotr Wielebski
        return publish((ConsumerDmaapModel) object);
    }

    @Override
    void initConfigs() {
        logger.trace("initConfigs for AAIProducerTaskImpl not needed/supported");
    }

    @Override
    protected AAIClientConfiguration resolveConfiguration() {
        return prhAppConfig.getAAIClientConfiguration();
    }

}
