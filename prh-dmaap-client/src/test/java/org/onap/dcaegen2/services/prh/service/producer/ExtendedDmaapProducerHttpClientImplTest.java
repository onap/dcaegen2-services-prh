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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModelForUnitTest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ExtendedDmaapProducerHttpClientImplTest {

    private static ExtendedDmaapProducerHttpClientImpl objectUnderTest;
    private static DmaapPublisherConfiguration configurationMock = mock(DmaapPublisherConfiguration.class);
    private static CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
    private static ConsumerDmaapModel consumerDmaapModel = new ConsumerDmaapModelForUnitTest();
    private static Integer expectedResult;
    private static final Integer RESPONSE_SUCCESS = 200;
    private static final Integer RESPONSE_FAILURE = 404;
    private final static HttpResponse httpResponseMock = mock(HttpResponse.class);
    private final static HttpEntity httpEntityMock = mock(HttpEntity.class);
    private final static StatusLine statusLineMock = mock(StatusLine.class);


    @BeforeAll
    static void init() throws NoSuchFieldException, IllegalAccessException {
        when(configurationMock.dmaapHostName()).thenReturn("54.45.33.2");
        when(configurationMock.dmaapProtocol()).thenReturn("https");
        when(configurationMock.dmaapPortNumber()).thenReturn(1234);
        when(configurationMock.dmaapUserName()).thenReturn("PRH");
        when(configurationMock.dmaapUserPassword()).thenReturn("PRH");
        when(configurationMock.dmaapContentType()).thenReturn("application/json");
        when(configurationMock.dmaapTopicName()).thenReturn("pnfReady");
        objectUnderTest = new ExtendedDmaapProducerHttpClientImpl(configurationMock);
        setField();
    }


    @Test
    void getHttpResponsePost_success() throws IOException {
        expectedResult = RESPONSE_SUCCESS;
        when(closeableHttpClientMock.execute(any(HttpPost.class), any(ResponseHandler.class)))
            .thenReturn(Optional.of(expectedResult));
        Optional<Integer> actualResult = objectUnderTest.getHttpProducerResponse(consumerDmaapModel);
        Assertions.assertEquals(expectedResult, actualResult.get());
    }

    @Test
    void getExtendedDetails_returnsFailure() throws IOException {
        expectedResult = RESPONSE_FAILURE;
        when(closeableHttpClientMock.execute(any(HttpPost.class), any(ResponseHandler.class)))
            .thenReturn(Optional.of(expectedResult));
        Optional<Integer> actualResult = objectUnderTest.getHttpProducerResponse(consumerDmaapModel);
        Assertions.assertEquals(expectedResult, actualResult.get());
    }

    @Test
    void handleResponse_shouldReturn200() throws IOException {
        // When
        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        when(httpResponseMock.getStatusLine().getStatusCode()).thenReturn(HttpStatus.SC_OK);
        // Then
        assertEquals(Optional.of(HttpStatus.SC_OK), objectUnderTest.handleResponse(httpResponseMock));
    }

    @Test
    void handleResponse_shouldReturn300() throws IOException {
        // When
        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        when(httpResponseMock.getStatusLine().getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        // Then
        assertEquals(Optional.of(HttpStatus.SC_BAD_REQUEST), objectUnderTest.handleResponse(httpResponseMock));
    }

    private static void setField() throws NoSuchFieldException, IllegalAccessException {
        Field field = objectUnderTest.getClass().getDeclaredField("closeableHttpClient");
        field.setAccessible(true);
        field.set(objectUnderTest, closeableHttpClientMock);
    }
}
