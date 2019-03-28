/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

import org.apache.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.integration.junit5.mockito.MockitoExtension;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLException;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduledTasksTest {
    private final static ConsumerDmaapModel DMAAP_MODEL =
            ImmutableConsumerDmaapModel
                    .builder()
                    .correlationId("SomeId")
                    .ipv4("ipv4")
                    .ipv6("ipv6")
                    .build();

    @Mock
    private DmaapPublisherTask readyPublisher;

    @Mock
    private DmaapPublisherTask updatePublisher;

    @Mock
    private DmaapConsumerTask consumer;

    @Mock
    private BbsActionsTask bbsActions;

    @Mock
    private AaiQueryTask aaiQuery;

    @Mock
    private AaiProducerTask aaiProducer;

    private final Map<String, String> context = Collections.emptyMap();

    private ScheduledTasks sut;

    @BeforeEach
    void setUp() throws PrhTaskException, SSLException {
        final Mono<ConsumerDmaapModel> consumerModel = Mono.just(DMAAP_MODEL);

        given(aaiProducer.execute(any())).willReturn(consumerModel);
        given(bbsActions.execute(any())).willReturn(consumerModel);

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
    void whenEmptyResultFromDMaaPConsumer_NotActionShouldBePerformed() throws SSLException, PrhTaskException {
        //given
        given(consumer.execute(anyString())).willReturn(Flux.empty());

        //when
        sut.scheduleMainPrhEventTask();

        //then
        verifyThatPnfUpdateWasNotSentToAai();
        verifyThatPnfModelWasNotSentDmaapPnfReadyTopic();
        verifyThatPnfModelWasNotSentDmaapPnfUpdateTopic();
    }

    @Test
    void whenPnfNotFoundInAai_NotActionShouldBePerformed() throws SSLException, PrhTaskException {
        //given
        given(consumer.execute(anyString())).willReturn(Flux.just(DMAAP_MODEL));
        given(aaiQuery.execute(any())).willReturn(Mono.error(new PrhTaskException("404 Not Found")));

        //when
        sut.scheduleMainPrhEventTask();

        verifyThatPnfUpdateWasNotSentToAai();
        verifyThatPnfModelWasNotSentDmaapPnfReadyTopic();
        verifyThatPnfModelWasNotSentDmaapPnfUpdateTopic();
    }

    @Test
    void whenPnfWithoutService_PatchToAaiAndPostToPnfReadyShouldBePerformed() throws SSLException, PrhTaskException {
        //given
        given(consumer.execute(anyString())).willReturn(Flux.just(DMAAP_MODEL));
        given(aaiQuery.execute(any())).willReturn(Mono.just(false));

        //when
        sut.scheduleMainPrhEventTask();

        //then
        verifyThatPnfUpdateWasSentToAai();
        verifyThatPnfModelWasSentDmaapPnfReadyTopic();
        verifyThatPnfModelWasNotSentDmaapPnfUpdateTopic();
    }

    @Test
    void whenPnfHasActiveService_OnlyPostToPnfUpdateShouldBePerformed() throws SSLException, PrhTaskException {
        //given
        given(consumer.execute(anyString())).willReturn(Flux.just(DMAAP_MODEL));
        given(aaiQuery.execute(any())).willReturn(Mono.just(true));

        //when
        sut.scheduleMainPrhEventTask();

        //then
        verifyThatPnfUpdateWasNotSentToAai();
        verifyThatPnfModelWasNotSentDmaapPnfReadyTopic();
        verifyThatPnfModelWasSentDmaapPnfUpdateTopic();
    }

    private void verifyThatPnfModelWasNotSentDmaapPnfReadyTopic() {
        verify(readyPublisher, never()).executeWithApache(DMAAP_MODEL);
    }

    private Mono<HttpResponse> verifyThatPnfModelWasNotSentDmaapPnfUpdateTopic() {
        return verify(updatePublisher, never()).executeWithApache(DMAAP_MODEL);
    }

    private void verifyThatPnfModelWasSentDmaapPnfReadyTopic() {
        verify(readyPublisher, atLeastOnce()).executeWithApache(DMAAP_MODEL);
    }

    private Mono<HttpResponse> verifyThatPnfModelWasSentDmaapPnfUpdateTopic() {
        return verify(updatePublisher, atLeastOnce()).executeWithApache(DMAAP_MODEL);
    }

    private void verifyThatPnfUpdateWasNotSentToAai() throws PrhTaskException, SSLException {
        verify(aaiProducer, never()).execute(DMAAP_MODEL);
    }

    private void verifyThatPnfUpdateWasSentToAai() throws PrhTaskException, SSLException {
        verify(aaiProducer, atLeastOnce()).execute(DMAAP_MODEL);
    }
}