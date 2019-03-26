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

import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.INSTANCE_UUID;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.RESPONSE_CODE;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import javax.net.ssl.SSLException;

import org.onap.dcaegen2.services.prh.exceptions.DmaapEmptyResponseException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.logging.MdcVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;
import org.apache.http.HttpResponse;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final Marker INVOKE = MarkerFactory.getMarker("INVOKE");
    private final DmaapConsumerTask dmaapConsumerTask;
    private final DmaapPublisherTask dmaapProducerTask;
    private final AaiProducerTask aaiProducerTask;
    private final BbsActionsTask bbsActionsTask;
    private Map<String, String> mdcContextMap;

    /**
     * Constructor for tasks registration in PRHWorkflow.
     *
     * @param dmaapConsumerTask - fist task
     * @param dmaapPublisherTask - third task
     * @param aaiPublisherTask - second task
     */
    @Autowired
    public ScheduledTasks(
        DmaapConsumerTask dmaapConsumerTask,
        DmaapPublisherTask dmaapPublisherTask,
        AaiProducerTask aaiPublisherTask,
        BbsActionsTask bbsActionsTask,
        Map<String, String> mdcContextMap) {
        this.dmaapConsumerTask = dmaapConsumerTask;
        this.dmaapProducerTask = dmaapPublisherTask;
        this.aaiProducerTask = aaiPublisherTask;
        this.bbsActionsTask = bbsActionsTask;
        this.mdcContextMap = mdcContextMap;
    }

    /**
     * Main function for scheduling prhWorkflow.
     */
    public void scheduleMainPrhEventTask() {
        MdcVariables.setMdcContextMap(mdcContextMap);
        try {
            logger.trace("Execution of tasks was registered");
            CountDownLatch mainCountDownLatch = new CountDownLatch(1);
            consumeFromDMaaPMessage()
                .doOnError(DmaapEmptyResponseException.class, error ->
                    logger.warn("Nothing to consume from DMaaP")
                )
                .flatMap(this::publishToAaiConfiguration)
                .doOnError(exception ->
                    logger.warn("AAIProducerTask exception has been registered: ", exception))
                .onErrorResume(resumePrhPredicate(), exception -> Mono.empty())
                .flatMap(this::processAdditionalFields)
                .doOnError(exception ->
                    logger.warn("BBSActionsTask exception has been registered: ", exception))
                .flatMap(this::publishToDmaapConfigurationWithApache)
                .doOnError(exception ->
                    logger.warn("DMaaPProducerTask exception has been registered: ", exception))
                .onErrorResume(resumePrhPredicate(), exception -> Mono.empty())
                .doOnTerminate(mainCountDownLatch::countDown)
                .subscribe(this::onSuccess, this::onError, this::onComplete);

            mainCountDownLatch.await();
        } catch (InterruptedException e) {
            logger.warn("Interruption problem on countDownLatch ", e);
            Thread.currentThread().interrupt();
        }
    }

    private void onComplete() {
        logger.info("PRH tasks have been completed");
    }

    /**
     * Marked as deprecated due to problems with DMaaP MR, to be fixed in future
     * */
    @Deprecated
    private void onSuccess(HttpClientResponse response) {
        String statusCode = Integer.toString(response.status().code());
        MDC.put(RESPONSE_CODE, statusCode);
        logger.info("Prh consumed tasks successfully. HTTP Response code from DMaaPProducer {}",
            statusCode);
        MDC.remove(RESPONSE_CODE);
    }

    private void onSuccess(HttpResponse response) {
        String statusCode = Integer.toString(response.getStatusLine().getStatusCode());
        MDC.put(RESPONSE_CODE, statusCode);
        logger.info("Prh consumed tasks successfully. HTTP Response code from DMaaPProducer {}",
            statusCode);
        MDC.remove(RESPONSE_CODE);
    }



    private void onError(Throwable throwable) {
        if (!(throwable instanceof DmaapEmptyResponseException)) {
            logger.warn("Chain of tasks have been aborted due to errors in PRH workflow", throwable);
        }
    }

    private Flux<ConsumerDmaapModel> consumeFromDMaaPMessage() {
        return Flux.defer(() -> {
            MdcVariables.setMdcContextMap(mdcContextMap);
            MDC.put(INSTANCE_UUID, UUID.randomUUID().toString());
            logger.info(INVOKE, "Init configs");
            dmaapConsumerTask.initConfigs();
            return consumeFromDMaaP();
        });
    }

    private Flux<ConsumerDmaapModel> consumeFromDMaaP() {
        try {
            return dmaapConsumerTask.execute("");
        } catch (SSLException e) {
            return Flux.error(e);
        }
    }

    private Mono<ConsumerDmaapModel> publishToAaiConfiguration(ConsumerDmaapModel monoDMaaPModel) {
        try {
            return aaiProducerTask.execute(monoDMaaPModel);
        } catch (PrhTaskException | SSLException e) {
            return Mono.error(e);
        }
    }

    private Mono<ConsumerDmaapModel> processAdditionalFields(ConsumerDmaapModel consumerDmaapModel) {
        return bbsActionsTask.execute(consumerDmaapModel);
    }

    /**
     * Marked as deprecated due to problems with DMaaP MR, to be fixed in future
     * */
    @Deprecated
    private Mono<HttpClientResponse> publishToDmaapConfiguration(ConsumerDmaapModel monoAaiModel) {
        try {
            return dmaapProducerTask.execute(monoAaiModel);
        } catch (PrhTaskException | SSLException e) {
            return Mono.error(e);
        }
    }

    private Mono<HttpResponse> publishToDmaapConfigurationWithApache(ConsumerDmaapModel monoAaiModel) {
        try {
            return dmaapProducerTask.executeWithApache(monoAaiModel);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }



    private Predicate<Throwable> resumePrhPredicate() {
        return exception -> exception instanceof PrhTaskException;
    }
}
