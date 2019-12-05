/*
 * ============LICENSE_START=======================================================
 * PROJECT
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
@ExtendWith(MockitoExtension.class)
class DmaapPublisherTaskImplTest {

    private DmaapPublisherTaskImpl dmaapPublisherTask;

    private MessageRouterPublishRequest mrRequest = createMRRequest();

    @Mock
    private static MessageRouterPublisher messageRouterPublisher;

    @Captor
    private ArgumentCaptor<Flux<JsonElement>> fluxCaptor;

    @Test
    void execute_whenPassedObjectDoesntFit_ThrowsPrhTaskException() {
        //given
        dmaapPublisherTask = new DmaapPublisherTaskImpl(() -> mrRequest, () -> messageRouterPublisher);
        //when
        Executable executableFunction = () -> dmaapPublisherTask.execute(null);
        //then
        assertThrows(PrhTaskException.class, executableFunction, "The specified parameter is incorrect");
    }

    @Test
    void execute_whenPassedObjectFits_ReturnsCorrectStatus() throws DmaapNotFoundException {
        //given
        dmaapPublisherTask = new DmaapPublisherTaskImpl(() -> mrRequest, () -> messageRouterPublisher);
        //when
        dmaapPublisherTask.execute(createConsumerDmaapModel());
        //then
        verify(messageRouterPublisher).put(eq(mrRequest), fluxCaptor.capture());

        StepVerifier.create(fluxCaptor.getValue())
                .expectNext(new JsonParser().parse("{\"correlationId\":\"NOKQTFCOC540002E\"}"))
                .verifyComplete();
    }


    private ImmutableConsumerDmaapModel createConsumerDmaapModel() {
        return ImmutableConsumerDmaapModel.builder()
                .ipv4("10.16.123.234")
                .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
                .correlationId("NOKQTFCOC540002E")
                .serialNumber("QTFCOC540002E")
                .equipVendor("nokia")
                .equipModel("3310")
                .equipType("type")
                .nfRole("gNB")
                .swVersion("v4.5.0.1")
                .additionalFields(null)
                .build();
    }

    private MessageRouterPublishRequest createMRRequest() {
        final MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
                .name("the topic")
                .topicUrl("http://dmaap-mr:2222/events/unauthenticated.PNF_READY")
                .build();

        return ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .build();
    }
}