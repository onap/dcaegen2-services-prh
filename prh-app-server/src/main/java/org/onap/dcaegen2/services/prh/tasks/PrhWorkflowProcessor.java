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
import java.util.concurrent.atomic.AtomicBoolean;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PrhWorkflowProcessor {
    private final KafkaPublisherTask kafkaReadyProducerTask;
    private final KafkaPublisherTask kafkaUpdateProducerTask;
    private final AaiQueryTask aaiQueryTask;
    private final AaiProducerTask aaiProducerTask;
    private final BbsActionsTask bbsActionsTask;
    private final Map<String, String> mdcContextMap;

    @Autowired
    public PrhWorkflowProcessor(
            @Qualifier("ReadyPublisherTask") final KafkaPublisherTask kafkaReadyPublisherTask,
            @Qualifier("UpdatePublisherTask") final KafkaPublisherTask kafkaUpdatePublisherTask,
            final AaiQueryTask aaiQueryTask,
            final AaiProducerTask aaiPublisherTask,
            final BbsActionsTask bbsActionsTask,
            final Map<String, String> mdcContextMap) {
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

    /**
     * Processes a flux of PNF registration models through the PRH workflow pipeline.
     *
     * @param models the PNF models to process
     * @return true if all PNFs were found in AAI and offsets should be committed, false otherwise
     */
    @WithSpan("processMessages")
    public boolean processMessages(Flux<ConsumerPnfModel> models) {
        MdcVariables.setMdcContextMap(mdcContextMap);
        MDC.put(INSTANCE_UUID, UUID.randomUUID().toString());
        log.trace("Execution of tasks was registered");

        AtomicBoolean allPnfsFound = new AtomicBoolean(true);

        models
                .flatMap(model -> queryAaiForPnf(model)
                        .doOnError(e -> {
                            log.warn("PNF not found in AAI for {}: {}", model.getCorrelationId(), e.getMessage());
                            allPnfsFound.set(false);
                        })
                        .onErrorResume(e -> Mono.empty()))
                .flatMap(this::queryAaiForConfiguration)
                .flatMap(this::publishToAaiConfiguration)
                .flatMap(this::processAdditionalFields)
                .flatMap(this::publishToKafka)
                .onErrorResume(exception -> {
                    if (!(exception instanceof PrhTaskException)) {
                        log.warn("Chain of tasks aborted due to errors in PRH workflow", exception);
                    }
                    return Mono.empty();
                })
                .doOnNext(this::onPublishSuccess)
                .blockLast();

        log.info("PRH tasks have been completed");
        if (!allPnfsFound.get()) {
            log.info("Offset not committed — PNF was not found in AAI");
        }
        return allPnfsFound.get();
    }

    private void onPublishSuccess(String topicName) {
        String statusCodeOk = HttpStatus.OK.name();
        MDC.put(RESPONSE_CODE, statusCodeOk);
        log.info("Prh consumed tasks successfully. Published to {}", topicName);
        MDC.remove(RESPONSE_CODE);
    }

    private Mono<ConsumerPnfModel> queryAaiForPnf(final ConsumerPnfModel model) {
        log.info("Find PNF in AAI --> {}", model.getCorrelationId());
        return aaiQueryTask.findPnfinAAI(model);
    }

    private Mono<State> queryAaiForConfiguration(final ConsumerPnfModel monoKafkaModel) {
        log.info("Find AAI Info --> {}", monoKafkaModel.getCorrelationId());
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
            log.warn("AAIProducerTask exception has been registered: ", e);
            return Mono.error(e);
        }
    }

    private Mono<State> processAdditionalFields(final State state) {
        if (state.activationStatus) {
            log.debug("Re-registration - Logical links won't be updated.");
            return Mono.just(state);
        }
        return bbsActionsTask.execute(state.pnfModel).map(x -> state);
    }

    private Mono<String> publishToKafka(final State state) {
        try {
            if (state.activationStatus) {
                log.debug("Re-registration - Using PNF_UPDATE Kafka topic.");
                return kafkaUpdateProducerTask.execute(state.pnfModel);
            }
            return kafkaReadyProducerTask.execute(state.pnfModel);
        } catch (PrhTaskException e) {
            log.warn("KafkaProducerTask exception has been registered: ", e);
            return Mono.error(e);
        }
    }
}
