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

import org.onap.dcaegen2.services.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonValidator;
import org.onap.dcaegen2.services.service.consumer.ExtendedDmaapConsumerHttpClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class DmaapConsumerTaskImpl extends DmaapConsumerTask<DmaapConsumerConfiguration, String> {


    private static final Logger logger = LoggerFactory.getLogger(DmaapConsumerTaskImpl.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Config prhAppConfig;

    @Autowired
    public DmaapConsumerTaskImpl(AppConfig prhAppConfig) {
        this.prhAppConfig = prhAppConfig;
    }

    @Override
    protected String consume(String message) throws DmaapNotFoundException {
        logger.info("Start task DmaapConsumerTask::consume() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        String jsonInformation = DmaapConsumerJsonValidator.validate(message);
        logger.info("End task DmaapConsumerTask::consume() :: Execution Time - {}",
            dateTimeFormatter.format(LocalDateTime.now()));
        return jsonInformation;
    }

    @Override
    public Object execute(Object object) throws PrhTaskException {
        logger.info("Start task DmaapConsumerTask::execute() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        ExtendedDmaapConsumerHttpClientImpl dmaapConsumerHttpClient = new ExtendedDmaapConsumerHttpClientImpl(
            resolveConfiguration());
        logger.info("End task DmaapConsumerTask::execute() :: Execution Time - {}",
            dateTimeFormatter.format(LocalDateTime.now()));

        return consume((dmaapConsumerHttpClient.getHttpConsumerResponse().orElseThrow(() -> {
            logger.error("DmaapConsumerTask::execute() :: consume() :: DmaapConsumerResponse - null");
            return new PrhTaskException("DmaapConsumerTask has returned null");
        })));
    }

    @Override
    protected DmaapConsumerConfiguration resolveConfiguration() {
        return prhAppConfig.getDmaapConsumerConfiguration();
    }

    @Override
    protected void initConfigs() {
        prhAppConfig.initFileStreamReader();
    }
}