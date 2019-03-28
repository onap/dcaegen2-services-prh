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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.integration.junit5.mockito.MockitoExtension;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import reactor.core.publisher.Flux;

import javax.net.ssl.SSLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

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

    @InjectMocks
    private ScheduledTasks sut;

    @Test
    void whenEmptyResultFromDMaaPConsumer_NotActionShouldBePerformed() throws SSLException, PrhTaskException {
        //given
        given(consumer.execute(anyString())).willReturn(Flux.empty());

        //when
        sut.scheduleMainPrhEventTask();

        //then
        verify(aaiQuery, never()).execute(any());
        verify(aaiProducer, never()).execute(any());
        verify(bbsActions, never()).execute(any());
        verify(readyPublisher, never()).executeWithApache(any());
        verify(updatePublisher, never()).executeWithApache(any());
    }

    @Test
    void whenPnfNotFoundInAai_NotActionShouldBePerformed() throws SSLException, PrhTaskException {
        //given
        given(consumer.execute(anyString())).willReturn(Flux.just(DMAAP_MODEL));
        given(aaiProducer)

        //when
        sut.scheduleMainPrhEventTask();

        //then
        verify(aaiQuery, never()).execute(any());
        verify(aaiProducer, never()).execute(any());
        verify(bbsActions, never()).execute(any());
        verify(readyPublisher, never()).executeWithApache(any());
        verify(updatePublisher, never()).executeWithApache(any());
    }

}
