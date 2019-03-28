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

import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DMaaPPublisherReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.PublisherReactiveHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.model.DmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.net.ssl.SSLException;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.onap.dcaegen2.services.prh.TestAppConfiguration.createDefaultDmaapPublisherConfiguration;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
class DmaapPublisherTaskImplTest {

    private static ConsumerDmaapModel consumerDmaapModel;
    private static DmaapPublisherTaskImpl dmaapPublisherTask;
    private static DMaaPPublisherReactiveHttpClient dMaaPPublisherReactiveHttpClient;
    private static AppConfig appConfig;
    private static DmaapPublisherConfiguration dmaapPublisherConfiguration;
    private Optional<RequestDiagnosticContext> requestDiagnosticContextOptionalMock;
    private DmaapModel dmaapModel;
    private PublisherReactiveHttpClientFactory publisherReactiveHttpClientFactory;
    private Supplier<DmaapPublisherConfiguration> configSupplier;

    @BeforeEach
    public void beforeEach() throws SSLException {
        dmaapPublisherConfiguration = createDefaultDmaapPublisherConfiguration();
        consumerDmaapModel = mock(ConsumerDmaapModel.class);
        appConfig = mock(AppConfig.class);
        requestDiagnosticContextOptionalMock = Optional.empty();
        dmaapModel = mock(DmaapModel.class);
        dMaaPPublisherReactiveHttpClient = mock(DMaaPPublisherReactiveHttpClient.class);
        publisherReactiveHttpClientFactory = mock(PublisherReactiveHttpClientFactory.class);
        when(appConfig.getDmaapPublisherConfiguration()).thenReturn(dmaapPublisherConfiguration);
        when(publisherReactiveHttpClientFactory.create(dmaapPublisherConfiguration))
            .thenReturn(dMaaPPublisherReactiveHttpClient);
        configSupplier = () -> appConfig.getDmaapPublisherConfiguration();
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


    @Test
    void execute_whenPassedObjectFits_ReturnsCorrectStatus() throws PrhTaskException, SSLException {
        //given
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;
        HttpResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);
        dmaapPublisherTask = new DmaapPublisherTaskImpl(configSupplier, publisherReactiveHttpClientFactory);

        //when
        StepVerifier.create(dmaapPublisherTask.execute(consumerDmaapModel)).expectSubscription()
            .expectNext(httpClientReponse);

        //then
        verify(dMaaPPublisherReactiveHttpClient, times(1))
            .getDMaaPProducerResponse(consumerDmaapModel, requestDiagnosticContextOptionalMock);

        verifyNoMoreInteractions(dMaaPPublisherReactiveHttpClient);
    }

    @Test
    void execute_whenPassedObjectFits_butIncorrectResponseReturns() throws DmaapNotFoundException, SSLException {
        //given
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.UNAUTHORIZED;
        HttpResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);
        dmaapPublisherTask = new DmaapPublisherTaskImpl(configSupplier, publisherReactiveHttpClientFactory);

        //when
        StepVerifier.create(dmaapPublisherTask.execute(consumerDmaapModel)).expectSubscription()
            .expectNext(httpClientReponse);

        //then
        verify(dMaaPPublisherReactiveHttpClient, times(1))
            .getDMaaPProducerResponse(consumerDmaapModel, requestDiagnosticContextOptionalMock);
        verifyNoMoreInteractions(dMaaPPublisherReactiveHttpClient);
    }

    @Test()
    void execute_whenConsumerDmaapModelIsNull() {
        //given
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.UNAUTHORIZED;
        HttpResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);
        dmaapPublisherTask = new DmaapPublisherTaskImpl(configSupplier, publisherReactiveHttpClientFactory);
        assertThrows(DmaapNotFoundException.class, () -> {
            dmaapPublisherTask.execute(null);
        });
    }

    @Test
    public void resolveClient() throws SSLException {
        //given
        dmaapPublisherTask = new DmaapPublisherTaskImpl(configSupplier, publisherReactiveHttpClientFactory);
        //when
        DMaaPPublisherReactiveHttpClient dMaaPPublisherReactiveHttpClientResolved = dmaapPublisherTask.resolveClient();
        //then
        assertSame(dMaaPPublisherReactiveHttpClientResolved, dMaaPPublisherReactiveHttpClient);
    }

    private HttpResponse prepareMocksForTests(HttpResponseStatus httpResponseStatus) {
        HttpResponse httpClientResponse = mock(HttpResponse.class);
        when(httpClientResponse.statusCode()).thenReturn(httpResponseStatus.code());
        when(
            dMaaPPublisherReactiveHttpClient.getDMaaPProducerResponse(dmaapModel, requestDiagnosticContextOptionalMock))
            .thenReturn(Mono.just(httpClientResponse));
        return httpClientResponse;
    }

}