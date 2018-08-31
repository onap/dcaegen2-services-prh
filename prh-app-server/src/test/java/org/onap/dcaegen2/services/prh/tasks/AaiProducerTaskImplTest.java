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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.net.ssl.SSLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.producer.AaiProducerReactiveHttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/14/18
 */
class AaiProducerTaskImplTest {


    private static final String AAI_HOST = "/aai/v11/network/pnfs/pnf/NOKQTFCOC540002E";
    private static final Integer PORT = 1234;
    private static final String PROTOCOL = "https";
    private static final String USER_NAME_PASSWORD = "PRH";
    private static final String BASE_PATH = "/aai/v11";
    private static final String PNF_PATH = "/network/pnfs/pnf";

    private ConsumerDmaapModel consumerDmaapModel;
    private AaiProducerTaskImpl aaiProducerTask;
    private AaiClientConfiguration aaiClientConfiguration;
    private AaiProducerReactiveHttpClient aaiProducerReactiveHttpClient;
    private AppConfig appConfig;
    private ClientResponse clientResponse;

    @BeforeEach
    void setUp() {
        clientResponse = mock(ClientResponse.class);
        aaiClientConfiguration = new ImmutableAaiClientConfiguration.Builder()
            .aaiHost(AAI_HOST)
            .aaiPort(PORT)
            .aaiProtocol(PROTOCOL)
            .aaiUserName(USER_NAME_PASSWORD)
            .aaiUserPassword(USER_NAME_PASSWORD)
            .aaiIgnoreSslCertificateErrors(true)
            .aaiBasePath(BASE_PATH)
            .aaiPnfPath(PNF_PATH)
            .build();
        consumerDmaapModel = ImmutableConsumerDmaapModel.builder().ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .sourceName("NOKQTFCOC540002E").build();
        appConfig = mock(AppConfig.class);

    }

    @Test
    void whenPassedObjectDoesntFit_ThrowsPrhTaskException() {
        //given/when/
        when(appConfig.getAaiClientConfiguration()).thenReturn(aaiClientConfiguration);
        aaiProducerTask = new AaiProducerTaskImpl(appConfig);
        Executable executableCode = () -> aaiProducerTask.execute(null);

        //then
        Assertions
            .assertThrows(PrhTaskException.class, executableCode, "Passing wrong object type to execute function");
    }

    @Test
    void whenPassedObjectFits_ReturnsCorrectStatus() throws PrhTaskException, SSLException {
        //given/when
        getAaiProducerTask_whenMockingResponseObject(200);
        Mono<ConsumerDmaapModel> response = aaiProducerTask.execute(consumerDmaapModel);

        //then
        verify(aaiProducerReactiveHttpClient, times(1)).getAaiProducerResponse(any());
        verifyNoMoreInteractions(aaiProducerReactiveHttpClient);
        Assertions.assertEquals(consumerDmaapModel, response.block());

    }

    @Test
    void whenPassedObjectFits_butIncorrectResponseReturns() throws PrhTaskException, SSLException {
        //given/when
        getAaiProducerTask_whenMockingResponseObject(400);
        StepVerifier.create(aaiProducerTask.execute(consumerDmaapModel)).expectSubscription()
            .expectError(PrhTaskException.class).verify();
        //then
        verify(aaiProducerReactiveHttpClient, times(1)).getAaiProducerResponse(any());
        verifyNoMoreInteractions(aaiProducerReactiveHttpClient);
    }

    private void getAaiProducerTask_whenMockingResponseObject(int statusCode) throws SSLException {
        //given
        doReturn(HttpStatus.valueOf(statusCode)).when(clientResponse).statusCode();
        Mono<ClientResponse> clientResponseMono = Mono.just(clientResponse);
        aaiProducerReactiveHttpClient = mock(AaiProducerReactiveHttpClient.class);
        when(aaiProducerReactiveHttpClient.getAaiProducerResponse(any()))
            .thenReturn(clientResponseMono);
        when(appConfig.getAaiClientConfiguration()).thenReturn(aaiClientConfiguration);
        aaiProducerTask = spy(new AaiProducerTaskImpl(appConfig));
        when(aaiProducerTask.resolveConfiguration()).thenReturn(aaiClientConfiguration);
        doReturn(aaiProducerReactiveHttpClient).when(aaiProducerTask).resolveClient();
    }
}