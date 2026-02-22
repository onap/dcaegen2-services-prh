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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.configuration.CbsConfigurationForAutoCommitDisabledMode;
import org.onap.dcaegen2.services.prh.exceptions.DmaapEmptyResponseException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.tasks.AaiProducerTask;
import org.onap.dcaegen2.services.prh.tasks.AaiQueryTask;
import org.onap.dcaegen2.services.prh.tasks.BbsActionsTask;
import org.springframework.boot.configurationprocessor.json.JSONException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@ExtendWith(MockitoExtension.class)
class ScheduledTasksWithCommitTest {
    private final static ConsumerDmaapModel DMAAP_MODEL =
        ImmutableConsumerDmaapModel
            .builder()
            .correlationId("SomeId")
            .ipv4("ipv4")
            .ipv6("ipv6")
            .build();

    @Mock
    private KafkaPublisherTask kafkaPublisherTask;

    @Mock
    private CbsConfigurationForAutoCommitDisabledMode cbsConfig;

    @Mock
    private BbsActionsTask bbsActionsTask;

    @Mock
    private KafkaConsumerTask kafkaConsumerTask;

    @Mock
    private AaiQueryTask aaiQueryTask;

    @Mock
    private AaiProducerTask aaiProducerTask;

    private final Map<String, String> context = Collections.emptyMap();

    private ScheduledTasksWithCommit sut;

    @BeforeEach
    void setUp() {
        lenient().when(cbsConfig.getPnfReadyTopic()).thenReturn("unauthenticated.PNF_READY");
        lenient().when(cbsConfig.getPnfUpdateTopic()).thenReturn("unauthenticated.PNF_UPDATE");
        sut = new ScheduledTasksWithCommit(
            kafkaConsumerTask,
            kafkaPublisherTask,
            cbsConfig,
            aaiQueryTask,
            aaiProducerTask,
            bbsActionsTask,
            context);
    }

    @Test
    void testQueryAAiForPNFOnSuccess() throws JSONException, PrhTaskException {
        ScheduledTasksWithCommit.State state = new ScheduledTasksWithCommit.State(DMAAP_MODEL, false );
        when(kafkaConsumerTask.execute()).thenReturn(Flux.just(DMAAP_MODEL));
        when(aaiQueryTask.findPnfinAAI(DMAAP_MODEL)).thenReturn(Mono.just(DMAAP_MODEL));
        when(aaiQueryTask.execute(DMAAP_MODEL)).thenReturn(Mono.just(true));
        when(aaiProducerTask.execute(state.dmaapModel)).thenReturn(Mono.just(DMAAP_MODEL));

        sut.scheduleKafkaPrhEventTask();

        // Re-registration (activationStatus=true from aaiQuery) → PNF_UPDATE topic
        verify(kafkaPublisherTask).execute(eq("unauthenticated.PNF_UPDATE"), eq(DMAAP_MODEL));
        verifyIfLogicalLinkWasNotCreated();
    }

    @Test
    void testQueryAAiForPNF() throws JSONException, PrhTaskException {
        ScheduledTasksWithCommit.State state = new ScheduledTasksWithCommit.State(DMAAP_MODEL, true);
        when(kafkaConsumerTask.execute()).thenReturn(Flux.just(DMAAP_MODEL));
        when(aaiQueryTask.findPnfinAAI(DMAAP_MODEL)).thenReturn(Mono.just(DMAAP_MODEL));
        when(aaiQueryTask.execute(DMAAP_MODEL)).thenReturn(Mono.just(true));
        when(aaiProducerTask.execute(state.dmaapModel)).thenReturn(Mono.just(DMAAP_MODEL));

        sut.scheduleKafkaPrhEventTask();

        // Re-registration → PNF_UPDATE topic
        verify(kafkaPublisherTask).execute(eq("unauthenticated.PNF_UPDATE"), eq(DMAAP_MODEL));
        verifyIfLogicalLinkWasNotCreated();
    }

    @Test
    void testQueryAAiForPNFOnError() throws JSONException, PrhTaskException {
            when(kafkaConsumerTask.execute()).thenReturn(Flux.just(DMAAP_MODEL));

            sut.scheduleKafkaPrhEventTask();

            verifyThatPnfUpdateWasNotSentToAai();

            verifyIfLogicalLinkWasNotCreated();
            verify(kafkaPublisherTask, never()).execute(anyString(), any());
    }

    @Test
    void testQueryAAiForPNFOnPRHException() throws JSONException, PrhTaskException {
        ScheduledTasksWithCommit.State state = new ScheduledTasksWithCommit.State(DMAAP_MODEL, false );
        when(kafkaConsumerTask.execute()).thenReturn(Flux.just(DMAAP_MODEL));
        when(aaiQueryTask.findPnfinAAI(DMAAP_MODEL)).thenReturn(Mono.just(DMAAP_MODEL));
        when(aaiQueryTask.execute(DMAAP_MODEL)).thenReturn(Mono.just(true));
        when(aaiProducerTask.execute(state.dmaapModel)).thenThrow(new PrhTaskException());

        sut.scheduleKafkaPrhEventTask();

        verifyIfLogicalLinkWasNotCreated();
        verify(kafkaPublisherTask, never()).execute(anyString(), any());
    }

    @Test
    void queryAAiForPNFOnPRHExceptionTest() throws JSONException, PrhTaskException {
        ScheduledTasksWithCommit.State state = new ScheduledTasksWithCommit.State(DMAAP_MODEL, true);
        when(kafkaConsumerTask.execute()).thenReturn(Flux.just(DMAAP_MODEL));
        when(aaiQueryTask.findPnfinAAI(DMAAP_MODEL)).thenReturn(Mono.just(DMAAP_MODEL));
        when(aaiQueryTask.execute(DMAAP_MODEL)).thenReturn(Mono.just(true));
        when(aaiProducerTask.execute(state.dmaapModel)).thenReturn(Mono.just(DMAAP_MODEL));

        sut.scheduleKafkaPrhEventTask();

        // Publishing should succeed (no exception thrown from kafkaPublisherTask since it's a mock)
        verify(kafkaPublisherTask).execute(eq("unauthenticated.PNF_UPDATE"), eq(DMAAP_MODEL));
        verifyIfLogicalLinkWasNotCreated();
    }

    @Test
    void queryAAiForPNFOnReRegistrationTest() throws JSONException, PrhTaskException {
        ScheduledTasksWithCommit.State state = new ScheduledTasksWithCommit.State(DMAAP_MODEL, true);
        when(kafkaConsumerTask.execute()).thenReturn(Flux.just(DMAAP_MODEL));
        when(aaiQueryTask.findPnfinAAI(DMAAP_MODEL)).thenReturn(Mono.just(DMAAP_MODEL));
        when(aaiQueryTask.execute(DMAAP_MODEL)).thenReturn(Mono.just(true));
        when(aaiProducerTask.execute(state.dmaapModel)).thenReturn(Mono.just(DMAAP_MODEL));

        sut.scheduleKafkaPrhEventTask();

        // Re-registration → PNF_UPDATE topic
        verify(kafkaPublisherTask).execute(eq("unauthenticated.PNF_UPDATE"), eq(DMAAP_MODEL));
        verifyIfLogicalLinkWasNotCreated();
    }

    @Test
    void queryAAiForPNFOnPRHExceptionOnFalseTest() throws JSONException, PrhTaskException {
        ScheduledTasksWithCommit.State state = new ScheduledTasksWithCommit.State(DMAAP_MODEL, false);
        when(kafkaConsumerTask.execute()).thenReturn(Flux.just(DMAAP_MODEL));
        when(aaiQueryTask.findPnfinAAI(DMAAP_MODEL)).thenReturn(Mono.just(DMAAP_MODEL));
        when(aaiQueryTask.execute(DMAAP_MODEL)).thenReturn(Mono.just(false));
        when(aaiProducerTask.execute(state.dmaapModel)).thenReturn(Mono.just(DMAAP_MODEL));
        when(bbsActionsTask.execute(DMAAP_MODEL)).thenReturn(Mono.just(DMAAP_MODEL));

        sut.scheduleKafkaPrhEventTask();
        // First registration (activationStatus=false) → PNF_READY topic
        verify(kafkaPublisherTask).execute(eq("unauthenticated.PNF_READY"), eq(DMAAP_MODEL));
    }

    @Test
    void queryAAiForPNFOnPRHExceptionOnJSONExceptionTest() throws PrhTaskException, JSONException {
        when(kafkaConsumerTask.execute()).thenThrow(new JSONException("json format exception"));

        sut.scheduleKafkaPrhEventTask();

        verifyIfLogicalLinkWasNotCreated();
        verify(kafkaPublisherTask, never()).execute(anyString(), any());
    }

    private void verifyThatPnfUpdateWasNotSentToAai() throws PrhTaskException {
        verify(aaiProducerTask, never()).execute(DMAAP_MODEL);
    }

    private void verifyIfLogicalLinkWasNotCreated(){
        verify(bbsActionsTask, never()).execute(DMAAP_MODEL);
    }
}

