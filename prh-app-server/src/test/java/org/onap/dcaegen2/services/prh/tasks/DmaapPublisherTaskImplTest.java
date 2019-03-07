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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.services.prh.TestAppConfiguration.createDefaultDmaapPublisherConfiguration;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DMaaPPublisherReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.PublisherReactiveHttpClientFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.netty.http.client.HttpClientResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
class DmaapPublisherTaskImplTest {

    private static ConsumerDmaapModel consumerDmaapModel;
    private static DmaapPublisherTaskImpl dmaapPublisherTask;
    private static DMaaPPublisherReactiveHttpClient dMaaPPublisherReactiveHttpClient;
    private static AppConfig appConfig;
    private static DmaapPublisherConfiguration dmaapPublisherConfiguration;

    @BeforeAll
    static void setUp() {
        dmaapPublisherConfiguration = createDefaultDmaapPublisherConfiguration();
        consumerDmaapModel = ImmutableConsumerDmaapModel.builder()
                .correlationId("NOKQTFCOC540002E")
                .serialNumber("QTFCOC540002E")
                .equipVendor("nokia")
                .equipModel("3310")
                .equipType("type")
                .nfRole("role")
                .swVersion("v4.5.0.1")
                .additionalFields(new JsonObject())
                .build();
        appConfig = mock(AppConfig.class);
    }

    @Test
    void whenPassedObjectDoesntFit_ThrowsPrhTaskException() {
        //given
        when(appConfig.getDmaapPublisherConfiguration()).thenReturn(dmaapPublisherConfiguration);
        dmaapPublisherTask = new DmaapPublisherTaskImpl(appConfig);

        //when
        Executable executableFunction = () -> dmaapPublisherTask.execute(null);

        //then
        assertThrows(PrhTaskException.class, executableFunction, "The specified parameter is incorrect");
    }

    @Test
    void whenPassedObjectFits_ReturnsCorrectStatus() throws PrhTaskException, SSLException {
        //given
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;
        HttpClientResponse httpClientReponse = prepareMocksForTests(httpResponseStatus);

        //when
        when(httpClientReponse.status()).thenReturn(httpResponseStatus);
        StepVerifier.create(dmaapPublisherTask.execute(consumerDmaapModel)).expectSubscription()
            .expectNext(httpClientReponse).verifyComplete();

        //then
        verify(dMaaPPublisherReactiveHttpClient, times(1))
            .getDMaaPProducerResponse(consumerDmaapModel,Optional.empty());
        verifyNoMoreInteractions(dMaaPPublisherReactiveHttpClient);
    }


    @Test
    void whenPassedObjectFits_butIncorrectResponseReturns() throws DmaapNotFoundException, SSLException {
        //given
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.UNAUTHORIZED;
        HttpClientResponse httpClientResponse  = prepareMocksForTests(httpResponseStatus);

        //when
        when(httpClientResponse.status()).thenReturn(httpResponseStatus);
        StepVerifier.create(dmaapPublisherTask.execute(consumerDmaapModel)).expectSubscription()
            .expectNext(httpClientResponse).verifyComplete();

        //then
        verify(dMaaPPublisherReactiveHttpClient, times(1))
            .getDMaaPProducerResponse(consumerDmaapModel,Optional.empty());
        verifyNoMoreInteractions(dMaaPPublisherReactiveHttpClient);
    }


    private HttpClientResponse prepareMocksForTests(HttpResponseStatus httpResponseCode) throws SSLException {
        HttpClientResponse responseEntity = mock(HttpClientResponse.class);
        when(responseEntity.status()).thenReturn(httpResponseCode);
        dMaaPPublisherReactiveHttpClient = mock(DMaaPPublisherReactiveHttpClient.class);
        when(dMaaPPublisherReactiveHttpClient.getDMaaPProducerResponse(any(), Optional.empty()))
            .thenReturn(Mono.just(responseEntity));
        PublisherReactiveHttpClientFactory httpClientFactory = mock(PublisherReactiveHttpClientFactory.class);
        doReturn(dMaaPPublisherReactiveHttpClient).when(httpClientFactory).create(dmaapPublisherConfiguration);
        dmaapPublisherTask = new DmaapPublisherTaskImpl(appConfig, httpClientFactory);
        return responseEntity;
    }
}