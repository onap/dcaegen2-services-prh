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

import com.google.gson.JsonPrimitive;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DmaapClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.model.DmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.services.prh.TestAppConfiguration.createDefaultMessageRouterPublishRequest;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
class DmaapPublisherTaskImplTest {

    private static ConsumerDmaapModel consumerDmaapModel;
    private static DmaapPublisherTaskImpl dmaapPublisherTask;
    private static CbsConfiguration cbsConfiguration;
    private static MessageRouterPublishRequest messageRouterPublishRequest;
    private Optional<RequestDiagnosticContext> requestDiagnosticContextOptionalMock;
    private DmaapModel dmaapModel;
    private Supplier<MessageRouterPublishRequest> configSupplier;
    private static MessageRouterPublishResponse messageRouterPublishResponseMock;

    @BeforeEach
    void beforeEach() {
        messageRouterPublishRequest = createDefaultMessageRouterPublishRequest();
        messageRouterPublishResponseMock = mock(MessageRouterPublishResponse.class);
        consumerDmaapModel = mock(ConsumerDmaapModel.class);
        cbsConfiguration = mock(CbsConfiguration.class);
        requestDiagnosticContextOptionalMock = Optional.empty();
        dmaapModel = mock(DmaapModel.class);
        when(cbsConfiguration.getMessageRouterPublishRequest()).thenReturn(messageRouterPublishRequest);
        configSupplier = () -> cbsConfiguration.getMessageRouterPublishRequest();
    }

    @Test
    void execute_whenPassedObjectDoesntFit_ThrowsPrhTaskException() {
        //given
        dmaapPublisherTask = new DmaapPublisherTaskImpl(configSupplier);
        //when
        Executable executableFunction = () -> dmaapPublisherTask.execute(null);
        //then
        assertThrows(PrhTaskException.class, executableFunction, "The specified parameter is incorrect");
    }

//
//    @Test
//    void execute_whenPassedObjectFits_ReturnsCorrectStatus() throws PrhTaskException, SSLException {
//        //given
//        HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;
//        HttpResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);
//        dmaapPublisherTask = new DmaapPublisherTaskImpl(configSupplier);
//
//        //when
//        StepVerifier.create(dmaapPublisherTask.execute(consumerDmaapModel)).expectSubscription()
//            .expectNext(httpClientReponse);
//
//        //then
//        verify(dMaaPPublisherReactiveHttpClient, times(1))
//            .getDMaaPProducerResponse(consumerDmaapModel, requestDiagnosticContextOptionalMock);
//
//        verifyNoMoreInteractions(dMaaPPublisherReactiveHttpClient);
//    }
//
//    @Test
//    void execute_whenPassedObjectFits_butIncorrectResponseReturns() throws DmaapNotFoundException, SSLException {
//        //given
//        HttpResponseStatus httpResponseStatus = HttpResponseStatus.UNAUTHORIZED;
//        HttpResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);
//        dmaapPublisherTask = new DmaapPublisherTaskImpl(configSupplier);
//
//        //when
//        StepVerifier.create(dmaapPublisherTask.execute(consumerDmaapModel)).expectSubscription()
//            .expectNext(httpClientReponse);
//
//        //then
//        verify(dMaaPPublisherReactiveHttpClient, times(1))
//            .getDMaaPProducerResponse(consumerDmaapModel, requestDiagnosticContextOptionalMock);
//        verifyNoMoreInteractions(dMaaPPublisherReactiveHttpClient);
//    }
//
//    @Test()
//    void execute_whenConsumerDmaapModelIsNull() {
//        //given
//        HttpResponseStatus httpResponseStatus = HttpResponseStatus.UNAUTHORIZED;
//        HttpResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);
//        dmaapPublisherTask = new DmaapPublisherTaskImpl(configSupplier);
//        assertThrows(DmaapNotFoundException.class, () -> {
//            dmaapPublisherTask.execute(null);
//        });
//    }
//
//    @Test
//    public void resolveClient() {
//        //given
//        dmaapPublisherTask = new DmaapPublisherTaskImpl(configSupplier);
//        //when
//        DMaaPPublisherReactiveHttpClient dMaaPPublisherReactiveHttpClientResolved = dmaapPublisherTask.resolveClient();
//        //then
//        assertSame(dMaaPPublisherReactiveHttpClientResolved);
//    }

    private HttpResponse prepareMocksForTests(HttpResponseStatus httpResponseStatus) {
        HttpResponse httpClientResponse = mock(HttpResponse.class);
        when(httpClientResponse.statusCode()).thenReturn(httpResponseStatus.code());
        when(DmaapClientFactory.createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault())
                        .put(messageRouterPublishRequest, Flux.just(consumerDmaapModel.toString()).map(JsonPrimitive::new)))
        .thenReturn(Flux.just(messageRouterPublishResponseMock));
        return httpClientResponse;
    }

}