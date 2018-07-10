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

package org.onap.dcaegen2.services.prh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModelForUnitTest;

class AaiProducerClientTest {

    private static final Integer SUCCESS = 200;
    private static final HttpResponse httpResponseMock = mock(HttpResponse.class);
    private static final HttpEntity httpEntityMock = mock(HttpEntity.class);
    private static final StatusLine statusLineMock = mock(StatusLine.class);

    private static AaiProducerClient testedObject;
    private static AaiClientConfiguration aaiHttpClientConfigurationMock = mock(AaiClientConfiguration.class);
    private static CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
    private static ConsumerDmaapModel consumerDmaapModel = new ConsumerDmaapModelForUnitTest();

    @BeforeAll
    static void setup() throws NoSuchFieldException, IllegalAccessException {
        when(aaiHttpClientConfigurationMock.aaiHost()).thenReturn("eucalyptus.es-si-eu-dhn-20.eecloud.nsn-net.net");
        when(aaiHttpClientConfigurationMock.aaiProtocol()).thenReturn("https");
        when(aaiHttpClientConfigurationMock.aaiHostPortNumber()).thenReturn(1234);
        when(aaiHttpClientConfigurationMock.aaiUserName()).thenReturn("PRH");
        when(aaiHttpClientConfigurationMock.aaiUserPassword()).thenReturn("PRH");
        when(aaiHttpClientConfigurationMock.aaiBasePath()).thenReturn("/aai/v11");
        when(aaiHttpClientConfigurationMock.aaiPnfPath()).thenReturn("/network/pnfs/pnf");
        when(aaiHttpClientConfigurationMock.aaiHeaders()).thenReturn(setupHeaders());

        testedObject = new AaiProducerClient(aaiHttpClientConfigurationMock);
        setField();
    }

    @Test
    void getHttpResponse_shouldReturnSuccessStatusCode() throws IOException, URISyntaxException {
        // when
        when(closeableHttpClientMock.execute(any(HttpPatch.class), any(ResponseHandler.class)))
            .thenReturn(Optional.of(SUCCESS));
        Optional<Integer> actualResult = testedObject.getHttpResponse(consumerDmaapModel);
        // then
        assertEquals(SUCCESS, actualResult.get());
    }

    @Test
    void getHttpResponse_shouldHandleIoException() throws IOException, URISyntaxException {
        // when
        when(closeableHttpClientMock.execute(any(HttpPatch.class), any(ResponseHandler.class)))
            .thenThrow(new IOException("Error occur"));

        // then
        assertNotNull(testedObject.getHttpResponse(consumerDmaapModel));
    }

    @Test
    void createHttpRequest_shouldCatchUnsupportedEncodingException() throws URISyntaxException, IOException {
        // when
        when(closeableHttpClientMock.execute(any(HttpPatch.class), any(ResponseHandler.class)))
            .thenThrow(new UnsupportedEncodingException("A new Error"));
        // then
        assertNotNull(testedObject.getHttpResponse(consumerDmaapModel));
    }

    @Test
    void encode_shouldCreateEncodedString_whenUserAndPasswordAreSet() throws UnsupportedEncodingException {
        // given
        String expected = "UFJIOlBSSA==";
        // when
        String result = testedObject.encode();
        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void createHttpPatch_shouldContainAuthorizationBasicValue() throws UnsupportedEncodingException {
        // given
        String expected = "Authorization: Basic UFJIOlBSSA==";
        // when
        HttpPatch patch = testedObject.createHttpPatch(URI.create("localhost"), "{}");
        // then
        assertNotNull(patch);
        assertEquals(expected, patch.getLastHeader("Authorization").toString());
    }

    @Test
    void handleResponse_shouldReturn200() throws IOException {
        // When
        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        when(httpResponseMock.getStatusLine().getStatusCode()).thenReturn(HttpStatus.SC_OK);
        // Then
        assertEquals(Optional.of(HttpStatus.SC_OK), testedObject.handleResponse(httpResponseMock));
    }

    @Test
    void handleResponse_shouldReturn300() throws IOException {
        // When
        when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        when(httpResponseMock.getStatusLine().getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        // Then
        assertEquals(Optional.of(HttpStatus.SC_BAD_REQUEST), testedObject.handleResponse(httpResponseMock));
    }


    private static void setField() throws NoSuchFieldException, IllegalAccessException {
        Field field = testedObject.getClass().getDeclaredField("closeableHttpClient");
        field.setAccessible(true);
        field.set(testedObject, closeableHttpClientMock);
    }

    private static Map<String, String> setupHeaders() {
        Map<String, String> aaiHeaders = new HashMap<>();
        aaiHeaders.put("X-FromAppId", "prh");
        aaiHeaders.put("X-TransactionId", "vv-temp");
        aaiHeaders.put("Accept", "application/json");
        aaiHeaders.put("Real-Time", "true");
        aaiHeaders.put("Content-Type", "application/merge-patch+json");
        return aaiHeaders;

    }
}
