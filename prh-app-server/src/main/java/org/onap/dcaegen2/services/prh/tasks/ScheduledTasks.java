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

import org.onap.dcaegen2.services.prh.exceptions.DmaapEmptyResponseException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

import static org.onap.dcaegen2.services.prh.model.AaiModelConverter.dmaapConsumerModelToPnf;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.INSTANCE_UUID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.RESPONSE_CODE;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class ScheduledTasks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final Marker INVOKE = MarkerFactory.getMarker("INVOKE");

    private final DmaapConsumerTask dmaapConsumerTask;
    private final DmaapPublisherTask dmaapReadyProducerTask;
    private final DmaapPublisherTask dmaapUpdateProducerTask;
    private final AaiQueryTask aaiQueryTask;
    private final AaiProducerTask aaiProducerTask;
    private final BbsActionsTask bbsActionsTask;
    private Map<String, String> mdcContextMap;

    /**
     * Constructor for tasks registration in PRHWorkflow.
     *
     * @param dmaapConsumerTask        - fist task
     * @param dmaapReadyPublisherTask  - third task
     * @param dmaapUpdatePublisherTask - fourth task
     * @param aaiPublisherTask         - second task
     */
    @Autowired
    public ScheduledTasks(
            final DmaapConsumerTask dmaapConsumerTask,
            @Qualifier("ReadyPublisherTask") final DmaapPublisherTask dmaapReadyPublisherTask,
            @Qualifier("UpdatePublisherTask") final DmaapPublisherTask dmaapUpdatePublisherTask,
            final AaiQueryTask aaiQueryTask,
            final AaiProducerTask aaiPublisherTask,
            final BbsActionsTask bbsActionsTask,
            final Map<String, String> mdcContextMap) {
        this.dmaapConsumerTask = dmaapConsumerTask;
        this.dmaapReadyProducerTask = dmaapReadyPublisherTask;
        this.dmaapUpdateProducerTask = dmaapUpdatePublisherTask;
        this.aaiQueryTask = aaiQueryTask;
        this.aaiProducerTask = aaiPublisherTask;
        this.bbsActionsTask = bbsActionsTask;
        this.mdcContextMap = mdcContextMap;
    }

    static class State {
        public final ConsumerDmaapModel dmaapModel;
        public final Boolean activationStatus;

        public State(final ConsumerDmaapModel dmaapModel, final Boolean activationStatus) {
            this.dmaapModel = dmaapModel;
            this.activationStatus = activationStatus;
        }
    }

    /**
     * Main function for scheduling prhWorkflow.
     */
    public void scheduleMainPrhEventTask() {
        MdcVariables.setMdcContextMap(mdcContextMap);
        try {
            LOGGER.trace("Execution of tasks was registered");
            CountDownLatch mainCountDownLatch = new CountDownLatch(1);
            consumeFromDMaaPMessage()
                    .doOnError(DmaapEmptyResponseException.class, error ->
                            LOGGER.warn("Nothing to consume from DMaaP")
                    )
                    .flatMap(this::queryAaiForConfiguration)
                    .flatMap(this::publishToAaiConfiguration)
                    .flatMap(this::processAdditionalFields)
                    .flatMap(this::publishToDmaapConfiguration)
                    .onErrorResume(resumePrhPredicate(), exception -> Mono.empty())
                    .doOnTerminate(mainCountDownLatch::countDown)
                    .subscribe(this::onSuccess, this::onError, this::onComplete);

            mainCountDownLatch.await();
        } catch (InterruptedException e) {
            LOGGER.warn("Interruption problem on countDownLatch ", e);
            Thread.currentThread().interrupt();
        }
    }

    private void onComplete() {
        LOGGER.info("PRH tasks have been completed");
    }

    private void onSuccess(MessageRouterPublishResponse response) {
        if (response.successful()) {
            String statusCodeOk = HttpStatus.OK.name();
            MDC.put(RESPONSE_CODE, statusCodeOk);
            LOGGER.info("Prh consumed tasks successfully. HTTP Response code from DMaaPProducer {}", statusCodeOk);
            MDC.remove(RESPONSE_CODE);
        }
    }

    private void onError(Throwable throwable) {
        if (!(throwable instanceof DmaapEmptyResponseException)) {
            LOGGER.warn("Chain of tasks have been aborted due to errors in PRH workflow", throwable);
        }
    }

    private Flux<ConsumerDmaapModel> consumeFromDMaaPMessage() {
        return Flux.defer(() -> {
            MdcVariables.setMdcContextMap(mdcContextMap);
            MDC.put(INSTANCE_UUID, UUID.randomUUID().toString());
            LOGGER.info(INVOKE, "Init configs");
            return dmaapConsumerTask.execute();
        });
    }

    private Mono<State> queryAaiForConfiguration(final ConsumerDmaapModel monoDMaaPModel) {
        return aaiQueryTask
                .execute(dmaapConsumerModelToPnf(monoDMaaPModel))
                .map(x -> new State(monoDMaaPModel, x));
    }

    private Mono<State> publishToAaiConfiguration(final State state) {
        try {
            return state.activationStatus
                    ? Mono.just(state)
                    : aaiProducerTask
                    .execute(state.dmaapModel)
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
        return bbsActionsTask.execute(state.dmaapModel).map(x -> state);
    }

    private Flux<MessageRouterPublishResponse> publishToDmaapConfiguration(final State state) {
        try {
            if (state.activationStatus) {
                LOGGER.debug("Re-registration - Using PNF_UPDATE DMaaP topic.");
                return dmaapUpdateProducerTask.execute(state.dmaapModel);
            }
            return dmaapReadyProducerTask.execute(state.dmaapModel);
        } catch (PrhTaskException e) {
            LOGGER.warn("DMaaPProducerTask exception has been registered: ", e);
            return Flux.error(e);
        }
    }

    private Predicate<Throwable> resumePrhPredicate() {
        return exception -> exception instanceof PrhTaskException;
    }
}
