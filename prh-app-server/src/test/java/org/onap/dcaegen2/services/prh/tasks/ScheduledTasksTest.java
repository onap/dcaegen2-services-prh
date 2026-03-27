/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2026 Deutsche Telekom Intellectual Property. All rights reserved.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerPnfModel;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksTest {
    private final static ConsumerPnfModel PNF_MODEL =
            ImmutableConsumerPnfModel
                    .builder()
                    .correlationId("SomeId")
                    .ipv4("ipv4")
                    .ipv6("ipv6")
                    .build();

    @Mock
    private KafkaPublisherTask readyPublisher;

    @Mock
    private KafkaPublisherTask updatePublisher;

    @Mock
    private KafkaConsumerTask consumer;

    @Mock
    private BbsActionsTask bbsActions;

    @Mock
    private AaiQueryTask aaiQuery;

    @Mock
    private AaiProducerTask aaiProducer;

    private final Map<String, String> context = Collections.emptyMap();

    private ScheduledTasks sut;

    @BeforeEach
    void setUp() {
        sut = new ScheduledTasks(
                consumer,
                readyPublisher,
                updatePublisher,
                aaiQuery,
                aaiProducer,
                bbsActions,
                context);
    }

    @Test
    void whenEmptyResultFromKafkaConsumer_NotActionShouldBePerformed() throws SSLException, PrhTaskException {
        given(consumer.execute()).willReturn(Flux.empty());

        sut.scheduleMainPrhEventTask();

        verifyThatPnfUpdateWasNotSentToAai();
        verifyIfLogicalLinkWasNotCreated();
        verifyPnfModelWasNotSentToReadyTopic();
        verifyPnfModelWasNotSentToUpdateTopic();
    }

    @Test
    void whenPnfNotFoundInAai_offsetShouldNotBeCommitted() throws SSLException, PrhTaskException {
        given(consumer.execute()).willReturn(Flux.just(PNF_MODEL));
        given(aaiQuery.findPnfinAAI(any())).willReturn(Mono.error(new PrhTaskException("404 Not Found")));

        sut.scheduleMainPrhEventTask();

        verifyThatPnfUpdateWasNotSentToAai();
        verifyIfLogicalLinkWasNotCreated();
        verifyPnfModelWasNotSentToReadyTopic();
        verifyPnfModelWasNotSentToUpdateTopic();
        verify(consumer, never()).commitOffset();
    }

    @Test
    void whenPnfWithoutService_PatchToAaiAndPostToPnfReadyShouldBePerformed() throws SSLException, PrhTaskException {
        Mono<ConsumerPnfModel> consumerModel = Mono.just(PNF_MODEL);

        given(aaiQuery.findPnfinAAI(any())).willReturn(Mono.just(PNF_MODEL));
        given(aaiProducer.execute(PNF_MODEL)).willReturn(consumerModel);
        given(bbsActions.execute(PNF_MODEL)).willReturn(consumerModel);

        given(consumer.execute()).willReturn(Flux.just(PNF_MODEL));
        given(aaiQuery.execute(any())).willReturn(Mono.just(false));
        given(readyPublisher.execute(PNF_MODEL)).willReturn(Mono.just("unauthenticated.PNF_READY"));

        sut.scheduleMainPrhEventTask();

        verifyThatPnfUpdateWasSentToAai();
        verifyIfLogicalLinkWasCreated();
        verifyPnfModelWasSentToReadyTopic();
        verifyPnfModelWasNotSentToUpdateTopic();
        verify(consumer, atLeastOnce()).commitOffset();
    }

    @Test
    void whenPnfHasActiveService_OnlyPostToPnfUpdateShouldBePerformed() throws SSLException, PrhTaskException {
        Mono<ConsumerPnfModel> consumerModel = Mono.just(PNF_MODEL);

        given(aaiQuery.findPnfinAAI(any())).willReturn(Mono.just(PNF_MODEL));
        given(consumer.execute()).willReturn(Flux.just(PNF_MODEL));
        given(aaiQuery.execute(any())).willReturn(Mono.just(true));
        given(aaiProducer.execute(PNF_MODEL)).willReturn(consumerModel);
        given(updatePublisher.execute(PNF_MODEL)).willReturn(Mono.just("unauthenticated.PNF_UPDATE"));

        sut.scheduleMainPrhEventTask();

        verifyThatPnfUpdateWasSentToAai();
        verifyIfLogicalLinkWasNotCreated();
        verifyPnfModelWasNotSentToReadyTopic();
        verifyPnfModelWasSentToUpdateTopic();
        verify(consumer, atLeastOnce()).commitOffset();
    }

    private void verifyPnfModelWasNotSentToReadyTopic() throws PrhTaskException {
        verify(readyPublisher, never()).execute(PNF_MODEL);
    }

    private void verifyPnfModelWasNotSentToUpdateTopic() throws PrhTaskException {
        verify(updatePublisher, never()).execute(PNF_MODEL);
    }

    private void verifyPnfModelWasSentToReadyTopic() throws PrhTaskException {
        verify(readyPublisher, atLeastOnce()).execute(PNF_MODEL);
    }

    private void verifyPnfModelWasSentToUpdateTopic() throws PrhTaskException {
        verify(updatePublisher, atLeastOnce()).execute(PNF_MODEL);
    }

    private void verifyThatPnfUpdateWasNotSentToAai() throws PrhTaskException, SSLException {
        verify(aaiProducer, never()).execute(PNF_MODEL);
    }

    private void verifyThatPnfUpdateWasSentToAai() throws PrhTaskException, SSLException {
        verify(aaiProducer, atLeastOnce()).execute(PNF_MODEL);
    }

    private void verifyIfLogicalLinkWasCreated() {
        verify(bbsActions, atLeastOnce()).execute(PNF_MODEL);
    }

    private void verifyIfLogicalLinkWasNotCreated() {
        verify(bbsActions, never()).execute(PNF_MODEL);
    }
}

