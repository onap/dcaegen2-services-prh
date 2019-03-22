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

import com.google.gson.JsonObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.onap.dcaegen2.services.prh.TestAppConfiguration;
import org.onap.dcaegen2.services.prh.configuration.CBSConfiguration;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.patch.AaiHttpPatchClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/14/18
 */
class AaiProducerTaskImplTest {

    private ConsumerDmaapModel consumerDmaapModel;
    private AaiProducerTaskImpl aaiProducerTask;
    private AaiClientConfiguration aaiClientConfiguration;
    private AaiHttpPatchClient aaiReactiveHttpPatchClient;
    private CBSConfiguration cbsConfiguration;
    private HttpClientResponse clientResponse;

    @BeforeEach
    void setUp() {
        clientResponse = mock(HttpClientResponse.class);
        aaiClientConfiguration = TestAppConfiguration.createDefaultAaiClientConfiguration();
        consumerDmaapModel = ImmutableConsumerDmaapModel.builder()
                .ipv4("10.16.123.234")
                .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
                .correlationId("NOKQTFCOC540002E")
                .serialNumber("QTFCOC540002E")
                .equipVendor("nokia")
                .equipModel("3310")
                .equipType("type")
                .nfRole("role")
                .swVersion("v4.5.0.1")
                .additionalFields(new JsonObject())
                .build();
        cbsConfiguration = mock(CBSConfiguration.class);

    }

    @Test
    void whenPassedObjectDoesntFit_ThrowsPrhTaskException() {
        //given/when/
        when(cbsConfiguration.getAaiClientConfiguration()).thenReturn(aaiClientConfiguration);
        aaiProducerTask = new AaiProducerTaskImpl(cbsConfiguration);
        Executable executableCode = () -> aaiProducerTask.execute(null);

        //then
        Assertions
            .assertThrows(PrhTaskException.class, executableCode, "Passing wrong object type to execute function");
    }

    @Test
    void whenPassedObjectFits_ReturnsCorrectStatus() throws PrhTaskException, SSLException {
        //given/when
        getAaiProducerTask_whenMockingResponseObject(HttpResponseStatus.OK);
        Mono<ConsumerDmaapModel> response = aaiProducerTask.execute(consumerDmaapModel);

        //then
        verify(aaiReactiveHttpPatchClient, times(1)).getAaiResponse(any());
        verifyNoMoreInteractions(aaiReactiveHttpPatchClient);
        Assertions.assertEquals(consumerDmaapModel, response.block());

    }

    @Test
    void whenPassedObjectFits_butIncorrectResponseReturns() throws PrhTaskException, SSLException {
        //given/when
        getAaiProducerTask_whenMockingResponseObject(HttpResponseStatus.BAD_REQUEST);
        StepVerifier.create(aaiProducerTask.execute(consumerDmaapModel)).expectSubscription()
            .expectError(PrhTaskException.class).verify();
        //then
        verify(aaiReactiveHttpPatchClient, times(1)).getAaiResponse(any());
        verifyNoMoreInteractions(aaiReactiveHttpPatchClient);
    }

    private void getAaiProducerTask_whenMockingResponseObject(HttpResponseStatus httpResponseStatus) throws SSLException {
        //given
        doReturn(httpResponseStatus).when(clientResponse).status();
        Mono<HttpClientResponse> clientResponseMono = Mono.just(clientResponse);
        aaiReactiveHttpPatchClient = mock(AaiHttpPatchClient.class);
        when(aaiReactiveHttpPatchClient.getAaiResponse(any()))
            .thenReturn(clientResponseMono);
        when(cbsConfiguration.getAaiClientConfiguration()).thenReturn(aaiClientConfiguration);
        aaiProducerTask = spy(new AaiProducerTaskImpl(cbsConfiguration));
        when(aaiProducerTask.resolveConfiguration()).thenReturn(aaiClientConfiguration);
        doReturn(aaiReactiveHttpPatchClient).when(aaiProducerTask).resolveClient();
    }
}