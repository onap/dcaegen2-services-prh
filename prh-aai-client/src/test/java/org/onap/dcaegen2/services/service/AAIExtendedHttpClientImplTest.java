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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.*;
import org.onap.dcaegen2.services.config.AAIHttpClientConfiguration;
import org.onap.dcaegen2.services.utils.HttpRequestDetails;
import org.onap.dcaegen2.services.utils.RequestVerbs;

public class AAIExtendedHttpClientImplTest {

    private static AAIExtendedHttpClientImpl testedObject;
    private static AAIHttpClientConfiguration aaiHttpClientConfigurationMock = mock(AAIHttpClientConfiguration.class);
    private static CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
    private static HttpRequestDetails httpRequestDetailsMock = mock(HttpRequestDetails.class);
    private static Optional<String> expectedResult = Optional.empty();

    @BeforeAll
    public static void init() throws NoSuchFieldException, IllegalAccessException {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("ipaddress-v4-oam", "11.22.33.44");

        Map<String, String> AAI_HEADERS = new HashMap<>();
        AAI_HEADERS.put("X-FromAppId", "prh");
        AAI_HEADERS.put("X-TransactionId", "vv-temp");
        AAI_HEADERS.put("Accept", "application/json");
        AAI_HEADERS.put("Real-Time", "true");
        AAI_HEADERS.put("Content-Type", "application/json");

        when(aaiHttpClientConfigurationMock.aaiHost()).thenReturn("54.45.33.2");
        when(aaiHttpClientConfigurationMock.aaiProtocol()).thenReturn("https");
        when(aaiHttpClientConfigurationMock.aaiHostPortNumber()).thenReturn(1234);
        when(aaiHttpClientConfigurationMock.aaiUserName()).thenReturn("PRH");
        when(aaiHttpClientConfigurationMock.aaiUserPassword()).thenReturn("PRH");

        when(httpRequestDetailsMock.aaiAPIPath()).thenReturn("/aai/v11/network/pnfs/pnf/NOKQTFCOC540002E");
        when(httpRequestDetailsMock.headers()).thenReturn(AAI_HEADERS);
        when(httpRequestDetailsMock.queryParameters()).thenReturn(queryParams);

        testedObject = new AAIExtendedHttpClientImpl(aaiHttpClientConfigurationMock);
        setField();
    }

    @AfterAll
    public static void teardown() {
        testedObject = null;
        aaiHttpClientConfigurationMock = null;
        closeableHttpClientMock = null;
        httpRequestDetailsMock = null;
        expectedResult = null;
    }

    @Test
    public void getHttpResponseGet_success() throws IOException {
        when(httpRequestDetailsMock.requestVerb()).thenReturn(RequestVerbs.GET);

        expectedResult = Optional.of("getExtendedDetailsOK");

        when(closeableHttpClientMock.execute(any(HttpGet.class), any(ResponseHandler.class))).
                thenReturn(expectedResult);
        Optional<String> actualResult = testedObject.getHttpResponse(httpRequestDetailsMock);

        Assertions.assertEquals(expectedResult.get(), actualResult.get());
    }

    @Test
    public void getHttpResponsePut_success() throws IOException {
        when(httpRequestDetailsMock.requestVerb()).thenReturn(RequestVerbs.PUT);

        expectedResult = Optional.of("getExtendedDetailsOK");

        when(closeableHttpClientMock.execute(any(HttpPut.class), any(ResponseHandler.class))).
                thenReturn(expectedResult);
        Optional<String>  actualResult = testedObject.getHttpResponse(httpRequestDetailsMock);

        Assertions.assertEquals(expectedResult.get(), actualResult.get());
    }

    @Test
    public void getExtendedDetails_returnsNull() throws IOException {
        when(httpRequestDetailsMock.requestVerb()).thenReturn(RequestVerbs.GET);
        when(closeableHttpClientMock.execute(any(HttpGet.class), any(ResponseHandler.class))).
                thenReturn(Optional.empty());
        Optional<String>  actualResult = testedObject.getHttpResponse(httpRequestDetailsMock);
        Assertions.assertEquals(Optional.empty(),actualResult);
    }

    @Test
    public void getHttpResponsePut_failure() {
        when(httpRequestDetailsMock.requestVerb()).thenReturn(RequestVerbs.PUT);

    }

    private static void setField() throws NoSuchFieldException, IllegalAccessException {
        Field field = testedObject.getClass().getDeclaredField("closeableHttpClient");
        field.setAccessible(true);
        field.set(testedObject, closeableHttpClientMock);
    }
}
