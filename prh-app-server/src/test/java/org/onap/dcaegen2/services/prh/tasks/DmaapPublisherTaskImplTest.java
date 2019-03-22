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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.services.prh.TestAppConfiguration.createDefaultDmaapPublisherConfiguration;

import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Optional;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.onap.dcaegen2.services.prh.configuration.ConsulConfiguration;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DMaaPPublisherReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.PublisherReactiveHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.model.DmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;
import reactor.test.StepVerifier;

;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
class DmaapPublisherTaskImplTest {

    private static ConsumerDmaapModel consumerDmaapModel;
    private static DmaapPublisherTaskImpl dmaapPublisherTask;
    private static DMaaPPublisherReactiveHttpClient dMaaPPublisherReactiveHttpClient;
    private static ConsulConfiguration consulConfiguration;
    private static DmaapPublisherConfiguration dmaapPublisherConfiguration;
    private Optional<RequestDiagnosticContext> requestDiagnosticContextOptionalMock;
    private DmaapModel dmaapModel;
    private PublisherReactiveHttpClientFactory publisherReactiveHttpClientFactory;

    @BeforeEach
    public void beforeEach() throws SSLException {
        dmaapPublisherConfiguration = createDefaultDmaapPublisherConfiguration();
        consumerDmaapModel = mock(ConsumerDmaapModel.class);
        consulConfiguration = mock(ConsulConfiguration.class);
        requestDiagnosticContextOptionalMock = Optional.empty();
        dmaapModel = mock(DmaapModel.class);
        dMaaPPublisherReactiveHttpClient = mock(DMaaPPublisherReactiveHttpClient.class);
        publisherReactiveHttpClientFactory = mock(PublisherReactiveHttpClientFactory.class);
        when(consulConfiguration.getDmaapPublisherConfiguration()).thenReturn(dmaapPublisherConfiguration);
        when(publisherReactiveHttpClientFactory.create(dmaapPublisherConfiguration))
            .thenReturn(dMaaPPublisherReactiveHttpClient);
    }

    @Test
    void execute_whenPassedObjectDoesntFit_ThrowsPrhTaskException() {
        //given
        dmaapPublisherTask = new DmaapPublisherTaskImpl(consulConfiguration);
        //when
        Executable executableFunction = () -> dmaapPublisherTask.execute(null);
        //then
        assertThrows(PrhTaskException.class, executableFunction, "The specified parameter is incorrect");
    }


    @Test
    void execute_whenPassedObjectFits_ReturnsCorrectStatus() throws PrhTaskException, SSLException {
        //given
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;
        HttpClientResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);
        dmaapPublisherTask = new DmaapPublisherTaskImpl(consulConfiguration, publisherReactiveHttpClientFactory);

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
        HttpClientResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);
        dmaapPublisherTask = new DmaapPublisherTaskImpl(consulConfiguration, publisherReactiveHttpClientFactory);

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
        HttpClientResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);
        dmaapPublisherTask = new DmaapPublisherTaskImpl(consulConfiguration, publisherReactiveHttpClientFactory);
        assertThrows(DmaapNotFoundException.class, () -> {
            dmaapPublisherTask.execute(null);
        });
    }

    @Test
    public void resolveClient() throws SSLException {
        //given
        dmaapPublisherTask = new DmaapPublisherTaskImpl(consulConfiguration, publisherReactiveHttpClientFactory);
        //when
        DMaaPPublisherReactiveHttpClient dMaaPPublisherReactiveHttpClientResolved = dmaapPublisherTask.resolveClient();
        //then
        assertSame(dMaaPPublisherReactiveHttpClientResolved, dMaaPPublisherReactiveHttpClient);
    }

    private HttpClientResponse prepareMocksForTests(HttpResponseStatus httpResponseStatus) {
        HttpClientResponse httpClientResponse = mock(HttpClientResponse.class);
        when(httpClientResponse.status()).thenReturn(httpResponseStatus);
        when(
            dMaaPPublisherReactiveHttpClient.getDMaaPProducerResponse(dmaapModel, requestDiagnosticContextOptionalMock))
            .thenReturn(Mono.just(httpClientResponse));
        return httpClientResponse;
    }

}