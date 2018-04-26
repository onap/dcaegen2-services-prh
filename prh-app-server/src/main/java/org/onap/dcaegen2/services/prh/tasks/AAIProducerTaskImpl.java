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

import org.onap.dcaegen2.services.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.AAINotFoundException;
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
public class AAIProducerTaskImpl extends AAIProducerTask<AAIClientConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Config prhAppConfig;
    private AAIProducerClient producerClient;
    private HttpRequestDetails requestDetails;
    private String jsonBody = "{\"ipaddress-v4-oam\":\"11.22.33.155\"}";
    private String pnfName = "example-pnf-name-val-40510"; // pnf name received from dmaap required for URI
    public Optional<String> response;

    @Autowired
    public AAIProducerTaskImpl(AppConfig prhAppConfig, HttpRequestDetails requestDetails) {
        this.prhAppConfig = prhAppConfig;
        this.requestDetails = requestDetails;
    }

    @Override
    protected void publish() throws AAINotFoundException {
        logger.debug("Start task AAIConsumerTask::publish() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));

        producerClient = new AAIProducerClient(prhAppConfig.getAAIClientConfiguration());

        response = producerClient.getHttpResponse(requestDetails);

        logger.debug("End task AAIConsumerTask::publish() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));

    }

    @Override
    public ResponseEntity execute(Object object) throws AAINotFoundException {
        logger.debug("Start task AAIProducerTaskImpl::execute() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        publish();
        logger.debug("End task AAIPublisherTaskImpl::execute() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        return null;
    }

    @Override
    void initConfigs() {
    }

    @Override
    protected AAIClientConfiguration resolveConfiguration() {
        return prhAppConfig.getAAIClientConfiguration();
    }

}
