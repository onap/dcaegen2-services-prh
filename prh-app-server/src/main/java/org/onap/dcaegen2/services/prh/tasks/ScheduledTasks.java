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

import java.util.Optional;
import java.util.concurrent.Callable;
import org.onap.dcaegen2.services.prh.exceptions.DmaapEmptyResponseException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final AAIProducerTask aaiProducerTask;

    @Autowired
    public ScheduledTasks(DmaapConsumerTask dmaapConsumerTask, DmaapPublisherTask dmaapPublisherTask,
        AAIProducerTask aaiPublisherTask) {
        this.dmaapConsumerTask = dmaapConsumerTask;
        this.dmaapProducerTask = dmaapPublisherTask;
        this.aaiProducerTask = aaiPublisherTask;
    }

    public void scheduleMainPrhEventTask() {
        logger.trace("Execution of tasks was registered");

        Mono<Integer> dmaapProducerResponse = Mono.fromCallable(consumeFromDMaaPMessage())
            .doOnError(DmaapEmptyResponseException.class, error -> logger.warn("Nothing to consume from DMaaP"))
            .map(this::publishToAAIConfiguration)
            .flatMap(this::publishToDMaaPConfiguration)
            .subscribeOn(Schedulers.elastic());

        dmaapProducerResponse.subscribe(this::onSuccess, this::onError, this::onComplete);
    }

    private void onComplete() {
        logger.info("PRH tasks have been completed");
    }

    private void onSuccess(Integer responseCode) {
        logger.info("Prh consumed tasks. HTTP Response code {}", responseCode);
    }

    private void onError(Throwable throwable) {
        if (!(throwable instanceof DmaapEmptyResponseException)) {
            logger.warn("Chain of tasks have been aborted due to errors in PRH workflow", throwable);
        }
    }

    private Callable<Mono<ConsumerDmaapModel>> consumeFromDMaaPMessage() {
        return () ->
        {
            dmaapConsumerTask.initConfigs();
            return dmaapConsumerTask.execute("");
        };
    }

    private Mono<ConsumerDmaapModel> publishToAAIConfiguration(Mono<ConsumerDmaapModel> monoDMaaPModel) {
        return monoDMaaPModel.flatMap(dmaapModel -> {
            try {
                return Mono.just(aaiProducerTask.execute(dmaapModel));
            } catch (PrhTaskException e) {
                logger.warn("Exception in A&AIProducer task ", e);
                return Mono.error(e);
            }
        });
    }

    private Mono<Integer> publishToDMaaPConfiguration(Mono<ConsumerDmaapModel> monoAAIModel) {
        return monoAAIModel.flatMap(aaiModel -> {
            try {
                return Mono.just(dmaapProducerTask.execute(aaiModel));
            } catch (PrhTaskException e) {
                logger.warn("Exception in DMaaPProducer task ", e);
                return Mono.error(e);
            }
        });
    }
}
