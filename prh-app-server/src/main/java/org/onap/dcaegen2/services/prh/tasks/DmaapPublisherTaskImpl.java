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

import org.onap.dcaegen2.services.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.service.producer.ExtendedDmaapProducerHttpClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Component
public class DmaapPublisherTaskImpl extends DmaapPublisherTask<DmaapPublisherConfiguration, String> {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Config prhAppConfig;

    @Autowired
    public DmaapPublisherTaskImpl(AppConfig prhAppConfig) {
        this.prhAppConfig = prhAppConfig;
    }

    @Override
    protected String publish(String message) throws DmaapNotFoundException {
        logger.debug("Start task DmaapPublisherTask::publish() :: {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        ExtendedDmaapProducerHttpClientImpl dmaapProducerHttpClient = new ExtendedDmaapProducerHttpClientImpl(
            resolveConfiguration());
        logger.debug("End task DmaapPublisherTask::publish() :: {}",
            dateTimeFormatter.format(LocalDateTime.now()));
        return null;
    }

    @Override
    public Object execute(Object object) throws PrhTaskException {
        logger.debug("Start task DmaapPublisherTask::consume() :: {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        logger.debug("End task DmaapPublisherTask::consume() :: {}",
            dateTimeFormatter.format(LocalDateTime.now()));
        return publish((String) object);
    }

    @Override
    void initConfigs() {

    }

    @Override
    protected DmaapPublisherConfiguration resolveConfiguration() {
        return prhAppConfig.getDmaapPublisherConfiguration();
    }
}
