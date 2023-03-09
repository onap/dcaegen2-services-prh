/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.tasks.commit;

import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.RESPONSE_CODE;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.onap.dcaegen2.services.prh.exceptions.DmaapEmptyResponseException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.tasks.AaiProducerTask;
import org.onap.dcaegen2.services.prh.tasks.AaiQueryTask;
import org.onap.dcaegen2.services.prh.tasks.BbsActionsTask;
import org.onap.dcaegen2.services.prh.tasks.DmaapPublisherTask;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:sangeeta.bellara@t-systems.com">Sangeeta Bellara</a> on 3/13/23
 */
@Profile("autoCommitDisabled")
@Component
public class ScheduledTasksWithCommit {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasksWithCommit.class);
    private static Boolean pnfFound = true;
    private KafkaConsumerTask kafkaConsumerTask;
    private DmaapPublisherTask dmaapReadyProducerTask;
    private DmaapPublisherTask dmaapUpdateProducerTask;
    private AaiQueryTask aaiQueryTask;
    private AaiProducerTask aaiProducerTask;
    private BbsActionsTask bbsActionsTask;
    private Map<String, String> mdcContextMap;

    /**
     * Constructor for tasks registration in PRHWorkflow.
     *
     * @param kafkaConsumerTask        - fist task
     * @param dmaapReadyPublisherTask  - third task
     * @param dmaapUpdatePublisherTask - fourth task
     * @param aaiPublisherTask         - second task
     */
    @Autowired
    public ScheduledTasksWithCommit(
        final KafkaConsumerTask kafkaConsumerTask,
        @Qualifier("ReadyPublisherTask") final DmaapPublisherTask dmaapReadyPublisherTask,
        @Qualifier("UpdatePublisherTask") final DmaapPublisherTask dmaapUpdatePublisherTask,
        final AaiQueryTask aaiQueryTask,
        final AaiProducerTask aaiPublisherTask,
        final BbsActionsTask bbsActionsTask,
        final Map<String, String> mdcContextMap) {
        this.dmaapReadyProducerTask = dmaapReadyPublisherTask;
        this.dmaapUpdateProducerTask = dmaapUpdatePublisherTask;
        this.kafkaConsumerTask=kafkaConsumerTask;
        this.aaiQueryTask = aaiQueryTask;
        this.aaiProducerTask = aaiPublisherTask;
        this.bbsActionsTask = bbsActionsTask;
        this.mdcContextMap = mdcContextMap;
    }

    static class State {
        public ConsumerDmaapModel dmaapModel;
        public  Boolean activationStatus;

        public State(ConsumerDmaapModel dmaapModel, final Boolean activationStatus) {
            this.dmaapModel = dmaapModel;
            this.activationStatus = activationStatus;
        }
    }

    public void scheduleKafkaPrhEventTask() {
        MdcVariables.setMdcContextMap(mdcContextMap);
        try {
            LOGGER.info("Execution of tasks was registered with commit");
            CountDownLatch mainCountDownLatch = new CountDownLatch(1);
            consumeFromKafkaMessage()
                    .flatMap(model->queryAaiForPnf(model)
                .doOnError(e -> { LOGGER.info("PNF Not Found in AAI --> {}" + e);
                   disableCommit();
                })
                .onErrorResume(e -> Mono.empty())
                
                )
                    .flatMap(this::queryAaiForConfiguration)
                .flatMap(this::publishToAaiConfiguration)
                .flatMap(this::processAdditionalFields)
                .flatMap(this::publishToDmaapConfiguration)
                    .onErrorResume(e -> Mono.empty())
                
                .doOnTerminate(mainCountDownLatch::countDown)
                .subscribe(this::onSuccess, this::onError, this::onCompleteKafka);
            mainCountDownLatch.await();
        } catch (InterruptedException | JSONException e ) {
            LOGGER.warn("Interruption problem on countDownLatch {}", e);
            Thread.currentThread().interrupt();
        }
    }

    private static void disableCommit()
    {
        pnfFound=false;
    }

    private void onCompleteKafka() {
        LOGGER.info("PRH tasks have been completed");
        if(pnfFound){
            kafkaConsumerTask.commitOffset();
            LOGGER.info("Committed the Offset");
        }
        else
        {
            LOGGER.info("Offset not Committed");
            pnfFound=true;
        }
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
            LOGGER.warn("Chain of tasks have been aborted due to errors in PRH workflow {}", throwable);
        }
    }

    private Flux<ConsumerDmaapModel> consumeFromKafkaMessage() throws JSONException {
        return kafkaConsumerTask.execute();
    }

    private Mono<State> queryAaiForConfiguration(final ConsumerDmaapModel monoDMaaPModel) {
        return   aaiQueryTask
            .execute(monoDMaaPModel)
            .map(x -> new State(monoDMaaPModel, x));
    }

    private Mono<ConsumerDmaapModel> queryAaiForPnf(final ConsumerDmaapModel monoDMaaPModel) {

        LOGGER.info("Find PNF --> "+monoDMaaPModel.getCorrelationId());
        return aaiQueryTask.findPnfinAAI(monoDMaaPModel);
    }


    private Mono<State> publishToAaiConfiguration(final State state) {
        try {
            return aaiProducerTask
                .execute(state.dmaapModel)
                .map(x -> state);
        } catch (PrhTaskException e) {
            LOGGER.warn("AAIProducerTask exception has been registered: {}", e);
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
}
