/*-
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.onap.dcaegen2.services.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class DmaapConsumerTaskImpl extends DmaapConsumerTask<DmaapConsumerConfiguration> {


    private static final Logger logger = LoggerFactory.getLogger(DmaapConsumerTaskImpl.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Config prhAppConfig;

    @Autowired
    public DmaapConsumerTaskImpl(AppConfig prhAppConfig) {
        this.prhAppConfig = prhAppConfig;
    }

    @Override
    protected void consume() {
        logger.debug("Start task DmaapConsumerTask::consume() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));

        logger.debug("End task DmaapConsumerTask::consume() :: Execution Time - {}",
            dateTimeFormatter.format(LocalDateTime.now()));

    }

    @Override
    public ResponseEntity execute(Object object) {
        logger.debug("Start task DmaapConsumerTask::execute() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        consume();
        logger.debug("End task DmaapConsumerTask::execute() :: Execution Time - {}",
            dateTimeFormatter.format(LocalDateTime.now()));
        return null;
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