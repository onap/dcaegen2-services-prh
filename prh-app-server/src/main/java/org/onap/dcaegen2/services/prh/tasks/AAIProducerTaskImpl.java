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

import org.onap.dcaegen2.services.prh.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.AAINotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.AAIProducerClient;
import org.onap.dcaegen2.services.prh.service.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.Optional;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Component
public class AAIProducerTaskImpl extends
    AAIProducerTask<ConsumerDmaapModel, ConsumerDmaapModel, AAIClientConfiguration> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Config prhAppConfig;
    private AAIProducerClient aaiProducerClient;

    @Autowired
    public AAIProducerTaskImpl(AppConfig prhAppConfig) {
        this.prhAppConfig = prhAppConfig;
    }

    @Override
    ConsumerDmaapModel publish(ConsumerDmaapModel consumerDmaapModel) throws AAINotFoundException {
        logger.info("Sending PNF model to AAI {}", consumerDmaapModel);
        try {
            return aaiProducerClient.getHttpResponse(consumerDmaapModel)
                .filter(HttpUtils::isSuccessfulResponseCode).map(response -> consumerDmaapModel).orElseThrow(() ->
                    new AAINotFoundException("Incorrect response code for continuation of tasks workflow"));
        } catch (URISyntaxException e) {
            logger.warn("Patch request not successful", e);
            throw new AAINotFoundException("Patch request not successful");
        }
    }

    @Override
    protected void receiveRequest(ConsumerDmaapModel body) throws PrhTaskException {
        ConsumerDmaapModel response = execute(body);
        if (taskProcess != null && response != null) {
            taskProcess.receiveRequest(response);
        }
    }

    @Override
    public ConsumerDmaapModel execute(ConsumerDmaapModel consumerDmaapModel) throws AAINotFoundException {
        consumerDmaapModel = Optional.ofNullable(consumerDmaapModel)
            .orElseThrow(() -> new AAINotFoundException("Invoked null object to AAI task"));
        logger.trace("Method called with arg {}", consumerDmaapModel);
        aaiProducerClient = resolveClient();
        return publish(consumerDmaapModel);
    }

    protected AAIClientConfiguration resolveConfiguration() {
        return prhAppConfig.getAAIClientConfiguration();
    }

    @Override
    AAIProducerClient resolveClient() {
        return Optional.ofNullable(aaiProducerClient).orElseGet(() -> new AAIProducerClient(resolveConfiguration()));
    }
}
