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

import static org.onap.dcaegen2.services.prh.model.logging.MDCVariables.INSTANCE_UUID;
import static org.onap.dcaegen2.services.prh.model.logging.MDCVariables.RESPONSE_CODE;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.onap.dcaegen2.services.prh.exceptions.DmaapEmptyResponseException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.logging.MDCVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class ScheduledTasks {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DmaapConsumerTask dmaapConsumerTask;
    private final DmaapPublisherTask dmaapProducerTask;
    private final AaiProducerTask aaiProducerTask;
    private Map<String, String> contextMap = MDC.getCopyOfContextMap();

    /**
     * Constructor for tasks registration in PRHWorkflow.
     *
     * @param dmaapConsumerTask - fist task
     * @param dmaapPublisherTask - third task
     * @param aaiPublisherTask - second task
     */
    @Autowired
    public ScheduledTasks(DmaapConsumerTask dmaapConsumerTask, DmaapPublisherTask dmaapPublisherTask,
        AaiProducerTask aaiPublisherTask) {
        this.dmaapConsumerTask = dmaapConsumerTask;
        this.dmaapProducerTask = dmaapPublisherTask;
        this.aaiProducerTask = aaiPublisherTask;
    }

    /**
     * Main function for scheduling prhWorkflow.
     */
    public void scheduleMainPrhEventTask() {
        MDCVariables.setMdcContextMap(contextMap);
        logger.trace("Execution of tasks was registered");

        Mono<String> dmaapProducerResponse = Mono.fromCallable(consumeFromDMaaPMessage())
            .doOnError(DmaapEmptyResponseException.class, error -> logger.warn("Nothing to consume from DMaaP"))
            .map(this::publishToAaiConfiguration)
            .flatMap(this::publishToDmaapConfiguration)
            .subscribeOn(Schedulers.elastic());

        dmaapProducerResponse.subscribe(this::onSuccess, this::onError, this::onComplete);
    }

    private void onComplete() {
        logger.info("PRH tasks have been completed");
    }

    private void onSuccess(String responseCode) {
        MDC.put(RESPONSE_CODE, responseCode);
        logger.info("Prh consumed tasks. HTTP Response code {}", responseCode);
    }

    private void onError(Throwable throwable) {
        if (!(throwable instanceof DmaapEmptyResponseException)) {
            logger.warn("Chain of tasks have been aborted due to errors in PRH workflow", throwable);
        }
    }

    private Callable<Mono<ConsumerDmaapModel>> consumeFromDMaaPMessage() {
        return () -> {
            MDCVariables.setMdcContextMap(contextMap);
            MDC.put(INSTANCE_UUID, UUID.randomUUID().toString());
            dmaapConsumerTask.initConfigs();
            return dmaapConsumerTask.execute("");
        };
    }

    private Mono<ConsumerDmaapModel> publishToAaiConfiguration(Mono<ConsumerDmaapModel> monoDMaaPModel) {
        try {
            return aaiProducerTask.execute(monoDMaaPModel);
        } catch (PrhTaskException e) {
            return Mono.error(e);
        }
    }

    private Mono<String> publishToDmaapConfiguration(Mono<ConsumerDmaapModel> monoAaiModel) {
        try {
            return dmaapProducerTask.execute(monoAaiModel);
        } catch (PrhTaskException e) {
            return Mono.error(e);
        }
    }
}
