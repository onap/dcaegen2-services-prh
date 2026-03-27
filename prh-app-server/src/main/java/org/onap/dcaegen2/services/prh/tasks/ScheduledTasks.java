/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2023-2026 Deutsche Telekom Intellectual Property. All rights reserved.
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

import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.INSTANCE_UUID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.RESPONSE_CODE;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ScheduledTasks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
    private volatile boolean pnfFound = true;
    private final KafkaConsumerTask kafkaConsumerTask;
    private final KafkaPublisherTask kafkaReadyProducerTask;
    private final KafkaPublisherTask kafkaUpdateProducerTask;
    private final AaiQueryTask aaiQueryTask;
    private final AaiProducerTask aaiProducerTask;
    private final BbsActionsTask bbsActionsTask;
    private final Map<String, String> mdcContextMap;

    @Autowired
    public ScheduledTasks(
            final KafkaConsumerTask kafkaConsumerTask,
            @Qualifier("ReadyPublisherTask") final KafkaPublisherTask kafkaReadyPublisherTask,
            @Qualifier("UpdatePublisherTask") final KafkaPublisherTask kafkaUpdatePublisherTask,
            final AaiQueryTask aaiQueryTask,
            final AaiProducerTask aaiPublisherTask,
            final BbsActionsTask bbsActionsTask,
            final Map<String, String> mdcContextMap) {
        this.kafkaConsumerTask = kafkaConsumerTask;
        this.kafkaReadyProducerTask = kafkaReadyPublisherTask;
        this.kafkaUpdateProducerTask = kafkaUpdatePublisherTask;
        this.aaiQueryTask = aaiQueryTask;
        this.aaiProducerTask = aaiPublisherTask;
        this.bbsActionsTask = bbsActionsTask;
        this.mdcContextMap = mdcContextMap;
    }

    static class State {
        public final ConsumerPnfModel pnfModel;
        public final Boolean activationStatus;

        public State(ConsumerPnfModel pnfModel, final Boolean activationStatus) {
            this.pnfModel = pnfModel;
            this.activationStatus = activationStatus;
        }
    }

    @WithSpan("scheduleMainPrhEventTask")
    public void scheduleMainPrhEventTask() {
        MdcVariables.setMdcContextMap(mdcContextMap);
        try {
            LOGGER.trace("Execution of tasks was registered");
            CountDownLatch mainCountDownLatch = new CountDownLatch(1);
            consumeFromKafkaMessage()
                    .flatMap(model -> queryAaiForPnf(model)
                            .doOnError(e -> {
                                LOGGER.warn("PNF not found in AAI for {}: {}", model.getCorrelationId(), e.getMessage());
                                pnfFound = false;
                            })
                            .onErrorResume(e -> Mono.empty()))
                    .flatMap(this::queryAaiForConfiguration)
                    .flatMap(this::publishToAaiConfiguration)
                    .flatMap(this::processAdditionalFields)
                    .flatMap(this::publishToKafka)
                    .onErrorResume(exception -> {
                        if (!(exception instanceof PrhTaskException)) {
                            LOGGER.warn("Chain of tasks aborted due to errors in PRH workflow", exception);
                        }
                        return Mono.empty();
                    })
                    .doOnTerminate(mainCountDownLatch::countDown)
                    .subscribe(this::onPublishSuccess, this::onError, this::onComplete);

            mainCountDownLatch.await();
        } catch (InterruptedException e) {
            LOGGER.warn("Interruption problem on countDownLatch ", e);
            Thread.currentThread().interrupt();
        }
    }

    private void onComplete() {
        LOGGER.info("PRH tasks have been completed");
        if (pnfFound) {
            kafkaConsumerTask.commitOffset();
        } else {
            LOGGER.info("Offset not committed — PNF was not found in AAI");
            pnfFound = true;
        }
    }

    private void onPublishSuccess(String topicName) {
        String statusCodeOk = HttpStatus.OK.name();
        MDC.put(RESPONSE_CODE, statusCodeOk);
        LOGGER.info("Prh consumed tasks successfully. Published to {}", topicName);
        MDC.remove(RESPONSE_CODE);
    }

    private void onError(Throwable throwable) {
        LOGGER.warn("Chain of tasks have been aborted due to errors in PRH workflow", throwable);
    }

    private Flux<ConsumerPnfModel> consumeFromKafkaMessage() {
        return Flux.defer(() -> {
            MdcVariables.setMdcContextMap(mdcContextMap);
            MDC.put(INSTANCE_UUID, UUID.randomUUID().toString());
            return kafkaConsumerTask.execute();
        });
    }

    private Mono<ConsumerPnfModel> queryAaiForPnf(final ConsumerPnfModel model) {
        LOGGER.info("Find PNF in AAI --> {}", model.getCorrelationId());
        return aaiQueryTask.findPnfinAAI(model);
    }

    private Mono<State> queryAaiForConfiguration(final ConsumerPnfModel monoKafkaModel) {
        LOGGER.info("Find AAI Info --> {}", monoKafkaModel.getCorrelationId());
        return aaiQueryTask
                .execute(monoKafkaModel)
                .map(x -> new State(monoKafkaModel, x));
    }

    private Mono<State> publishToAaiConfiguration(final State state) {
        try {
            return aaiProducerTask
                    .execute(state.pnfModel)
                    .map(x -> state);
        } catch (PrhTaskException e) {
            LOGGER.warn("AAIProducerTask exception has been registered: ", e);
            return Mono.error(e);
        }
    }

    private Mono<State> processAdditionalFields(final State state) {
        if (state.activationStatus) {
            LOGGER.debug("Re-registration - Logical links won't be updated.");
            return Mono.just(state);
        }
        return bbsActionsTask.execute(state.pnfModel).map(x -> state);
    }

    private Mono<String> publishToKafka(final State state) {
        try {
            if (state.activationStatus) {
                LOGGER.debug("Re-registration - Using PNF_UPDATE Kafka topic.");
                return kafkaUpdateProducerTask.execute(state.pnfModel);
            }
            return kafkaReadyProducerTask.execute(state.pnfModel);
        } catch (PrhTaskException e) {
            LOGGER.warn("KafkaProducerTask exception has been registered: ", e);
            return Mono.error(e);
        }
    }
}

