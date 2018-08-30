/*-
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

package org.onap.dcaegen2.services.prh.service.producer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModelForUnitTest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AaiProducerReactiveHttpClientTest {

    private static final Integer SUCCESS_RESPONSE = 200;
    private static AaiProducerReactiveHttpClient aaiProducerReactiveHttpClient;
    private static AaiClientConfiguration aaiConfigurationMock = mock(AaiClientConfiguration.class);
    private static WebClient webClient = mock(WebClient.class);


    private ConsumerDmaapModel dmaapModel = spy(new ConsumerDmaapModelForUnitTest());
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private ResponseSpec responseSpec;

    private Map<String, String> aaiHeaders;
    private ClientResponse clientResponse;
    private Mono<ClientResponse> clientResponseMono;

    @BeforeEach
    void setUp() {
        setupHeaders();
        clientResponse = mock(ClientResponse.class);
        clientResponseMono = Mono.just(clientResponse);
        when(dmaapModel.getSourceName()).thenReturn("NOKnhfsadhff");
        when(aaiConfigurationMock.aaiHost()).thenReturn("54.45.33.2");
        when(aaiConfigurationMock.aaiProtocol()).thenReturn("https");
        when(aaiConfigurationMock.aaiPort()).thenReturn(1234);
        when(aaiConfigurationMock.aaiUserName()).thenReturn("PRH");
        when(aaiConfigurationMock.aaiUserPassword()).thenReturn("PRH");
        when(aaiConfigurationMock.aaiBasePath()).thenReturn("/aai/v11");
        when(aaiConfigurationMock.aaiPnfPath()).thenReturn("/network/pnfs/pnf");
        when(aaiConfigurationMock.aaiHeaders()).thenReturn(aaiHeaders);

        aaiProducerReactiveHttpClient = new AaiProducerReactiveHttpClient(aaiConfigurationMock);

        webClient = spy(WebClient.builder()
            .defaultHeaders(httpHeaders -> httpHeaders.setAll(aaiHeaders))
            .filter(basicAuthentication(aaiConfigurationMock.aaiUserName(), aaiConfigurationMock.aaiUserPassword()))
            .build());

        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        responseSpec = mock(ResponseSpec.class);
    }

    @Test
    void getAaiProducerResponse_shouldReturn200() {
        //given
        Mono<Integer> expectedResult = Mono.just(SUCCESS_RESPONSE);

        //when
        mockWebClientDependantObject();
        doReturn(expectedResult).when(responseSpec).bodyToMono(Integer.class);
        aaiProducerReactiveHttpClient.createAaiWebClient(webClient);

        //then
        StepVerifier.create(aaiProducerReactiveHttpClient.getAaiProducerResponse(dmaapModel)).expectSubscription()
            .expectNextMatches(results -> {
                Assertions.assertEquals(results, clientResponse);
                return true;
            }).verifyComplete();
    }

    @Test
    void getHttpResponse_whenUriSyntaxExceptionHasBeenThrown() throws URISyntaxException {
        ///given
        aaiProducerReactiveHttpClient = spy(aaiProducerReactiveHttpClient);
        //when
        when(webClient.patch()).thenReturn(requestBodyUriSpec);
        aaiProducerReactiveHttpClient.createAaiWebClient(webClient);
        doThrow(URISyntaxException.class).when(aaiProducerReactiveHttpClient).getUri(any());
        //then
        StepVerifier.create(
            aaiProducerReactiveHttpClient.getAaiProducerResponse(
                dmaapModel
            )).expectSubscription().expectError(Exception.class).verify();
    }

    @Test
    void getAppropriateUri_whenPassingCorrectedPathForPnf() throws URISyntaxException {
        Assertions.assertEquals(aaiProducerReactiveHttpClient.getUri("NOKnhfsadhff"),
            URI.create("https://54.45.33.2:1234/aai/v11/network/pnfs/pnf/NOKnhfsadhff"));
    }


    private void setupHeaders() {
        aaiHeaders = new HashMap<>();
        aaiHeaders.put("X-FromAppId", "PRH");
        aaiHeaders.put("X-TransactionId", "vv-temp");
        aaiHeaders.put("Accept", "application/json");
        aaiHeaders.put("Real-Time", "true");
        aaiHeaders.put("Content-Type", "application/merge-patch+json");
    }

    private void mockWebClientDependantObject() {
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(webClient.patch()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri((URI) any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(), (Class<Object>) any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchange()).thenReturn(clientResponseMono);
    }
}

