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

package org.onap.dcaegen2.services.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;

import org.mockito.Mockito;
import org.onap.dcaegen2.services.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.utils.HttpRequestDetails;
import org.onap.dcaegen2.services.utils.RequestVerbs;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ExtendedHttpClientImplTest {

    private static AAIClientConfiguration aaiClientConfigMock = spy(AAIClientConfiguration.class);

    private static CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
    private static HttpRequestDetails httpRequestDetailsMock = mock(HttpRequestDetails.class);
    private static Optional<String> expectedResult = Optional.empty();
    private static final String JSON_MESSAGE = "{ \"ipaddress-v4-oam\": \"11.22.33.44\" }";
    private static final String PNF_ID = "NOKQTFCOC540002E";

    @BeforeAll
    public static void init() {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pnf-id", PNF_ID);

        Map<String, String> aaiHeaders = new HashMap<>();
        aaiHeaders.put("X-FromAppId", "prh");
        aaiHeaders.put("X-TransactionId", "vv-temp");
        aaiHeaders.put("Accept", "application/json");
        aaiHeaders.put("Real-Time", "true");
        aaiHeaders.put("Content-Type", "application/json");

        Mockito.when(aaiClientConfigMock.aaiHost()).thenReturn("54.45.33.2");
        Mockito.when(aaiClientConfigMock.aaiProtocol()).thenReturn("https");
        Mockito.when(aaiClientConfigMock.aaiHostPortNumber()).thenReturn(1234);
        Mockito.when(aaiClientConfigMock.aaiUserName()).thenReturn("PRH");
        Mockito.when(aaiClientConfigMock.aaiUserPassword()).thenReturn("PRH");

        Mockito.when(httpRequestDetailsMock.aaiAPIPath()).thenReturn("/aai/v11/network/pnfs/pnf");
        Mockito.when(httpRequestDetailsMock.headers()).thenReturn(aaiHeaders);
        Mockito.when(httpRequestDetailsMock.queryParameters()).thenReturn(queryParams);
        Mockito.when(httpRequestDetailsMock.jsonBody()).thenReturn(Optional.of(JSON_MESSAGE));

    }

    @AfterAll
    public static void teardown() {
//        aaiClientConfigMock = null;
        closeableHttpClientMock = null;
        httpRequestDetailsMock = null;
        expectedResult = null;
    }

    @Test
    @Disabled
    public void getHttpResponsePatch_success() throws IOException {
        Mockito.when(httpRequestDetailsMock.requestVerb()).thenReturn(RequestVerbs.PATCH);

        expectedResult = Optional.of(JSON_MESSAGE);

        Mockito.when(closeableHttpClientMock.execute(ArgumentMatchers.any(HttpPatch.class), ArgumentMatchers.any(ResponseHandler.class)))
                .thenReturn(expectedResult);
        Optional<String> actualResult = ExtendedHttpClientImpl.getHttpResponse(aaiClientConfigMock, httpRequestDetailsMock);

        Assertions.assertEquals(expectedResult.get(), actualResult.get());
    }

    @Test
    @Disabled
    public void getHttpResponsePut_success() throws IOException {
        Mockito.when(httpRequestDetailsMock.requestVerb()).thenReturn(RequestVerbs.PUT);

        expectedResult = Optional.of("getExtendedDetailsOK");

        Mockito.when(closeableHttpClientMock.execute(ArgumentMatchers.any(HttpPut.class), ArgumentMatchers.any(ResponseHandler.class))).
                thenReturn(expectedResult);
        Optional<String>  actualResult = ExtendedHttpClientImpl.getHttpResponse(aaiClientConfigMock, httpRequestDetailsMock);

        Assertions.assertEquals(expectedResult.get(), actualResult.get());
    }

    @Test
    @Disabled
    public void getExtendedDetails_returnsNull() throws IOException {
        Mockito.when(httpRequestDetailsMock.requestVerb()).thenReturn(RequestVerbs.GET);
        Mockito.when(closeableHttpClientMock.execute(ArgumentMatchers.any(HttpGet.class), ArgumentMatchers.any(ResponseHandler.class))).
                thenReturn(Optional.empty());
        Optional<String>  actualResult = ExtendedHttpClientImpl.getHttpResponse(aaiClientConfigMock, httpRequestDetailsMock);
        Assertions.assertEquals(Optional.empty(),actualResult);
    }

    @Test
    @Disabled
    public void getHttpResponsePut_failure() {
        Mockito.when(httpRequestDetailsMock.requestVerb()).thenReturn(RequestVerbs.PUT);

    }
}
