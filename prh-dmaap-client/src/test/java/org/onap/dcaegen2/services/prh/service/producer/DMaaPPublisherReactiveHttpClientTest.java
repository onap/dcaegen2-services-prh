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

package org.onap.dcaegen2.services.prh.service.producer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModelForUnitTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/4/18
 */

class DMaaPPublisherReactiveHttpClientTest {

    private DMaaPPublisherReactiveHttpClient dmaapPublisherReactiveHttpClient;
    private DmaapPublisherConfiguration dmaapPublisherConfigurationMock = mock(
            DmaapPublisherConfiguration.class);
    private ConsumerDmaapModel consumerDmaapModel = new ConsumerDmaapModelForUnitTest();
    private RestTemplate restTemplate = mock(RestTemplate.class);


    @BeforeEach
    void setUp() {
        when(dmaapPublisherConfigurationMock.dmaapHostName()).thenReturn("54.45.33.2");
        when(dmaapPublisherConfigurationMock.dmaapProtocol()).thenReturn("https");
        when(dmaapPublisherConfigurationMock.dmaapPortNumber()).thenReturn(1234);
        when(dmaapPublisherConfigurationMock.dmaapUserName()).thenReturn("PRH");
        when(dmaapPublisherConfigurationMock.dmaapUserPassword()).thenReturn("PRH");
        when(dmaapPublisherConfigurationMock.dmaapContentType()).thenReturn("application/json");
        when(dmaapPublisherConfigurationMock.dmaapTopicName()).thenReturn("unauthenticated.PNF_READY");
        dmaapPublisherReactiveHttpClient =
                new DMaaPPublisherReactiveHttpClient(dmaapPublisherConfigurationMock, Mono.just(restTemplate));

    }

    @Test
    void getHttpResponse_Success() {
        //given
        int responseSuccess = 200;
        ResponseEntity<String> mockedResponseEntity = mock(ResponseEntity.class);
        //when
        when(mockedResponseEntity.getStatusCode()).thenReturn(HttpStatus.valueOf(responseSuccess));
        doReturn(mockedResponseEntity).when(restTemplate)
                .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), (Class<Object>) any());

        //then
        StepVerifier.create(dmaapPublisherReactiveHttpClient.getDMaaPProducerResponse(consumerDmaapModel))
                .expectSubscription().expectNext(mockedResponseEntity).verifyComplete();
    }

    @Test
    void getAppropriateUri_whenPassingCorrectedPathForPnf() {
        Assertions.assertEquals(dmaapPublisherReactiveHttpClient.getUri(),
                URI.create("https://54.45.33.2:1234/unauthenticated.PNF_READY"));
    }
}