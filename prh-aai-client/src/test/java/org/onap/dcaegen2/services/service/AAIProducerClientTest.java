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

package org.onap.dcaegen2.services.service;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.utils.HttpRequestDetails;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AAIProducerClientTest {

    private static AAIProducerClient testedObject;
    private static AAIClientConfiguration aaiHttpClientConfigurationMock = mock(AAIClientConfiguration.class);
    private static CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
    private static HttpRequestDetails httpRequestDetailsMock = mock(HttpRequestDetails.class);
    private static final String JsonBody = "{\"ipaddress-v4-oam\":\"11.22.33.155\"}";
    private static final String SUCCESS = "200";
    private static final String PNF_NAME = "nokia-pnf-nhfsadhff";

    @BeforeAll
    public static void init() throws NoSuchFieldException, IllegalAccessException {

        Map<String, String> aaiHeaders = new HashMap<>();
        aaiHeaders.put("X-FromAppId", "prh");
        aaiHeaders.put("X-TransactionId", "vv-temp");
        aaiHeaders.put("Accept", "application/json");
        aaiHeaders.put("Real-Time", "true");
        aaiHeaders.put("Content-Type", "application/merge-patch+json");

        when(aaiHttpClientConfigurationMock.aaiHost()).thenReturn("eucalyptus.es-si-eu-dhn-20.eecloud.nsn-net.net");
        when(aaiHttpClientConfigurationMock.aaiProtocol()).thenReturn("https");
        when(aaiHttpClientConfigurationMock.aaiHostPortNumber()).thenReturn(1234);
        when(aaiHttpClientConfigurationMock.aaiUserName()).thenReturn("PRH");
        when(aaiHttpClientConfigurationMock.aaiUserPassword()).thenReturn("PRH");

        when(httpRequestDetailsMock.aaiAPIPath()).thenReturn("/aai/v11/network/pnfs/pnf");

        when(httpRequestDetailsMock.headers()).thenReturn(aaiHeaders);
        when(httpRequestDetailsMock.pnfName()).thenReturn(PNF_NAME);
        when(httpRequestDetailsMock.jsonBody()).thenReturn(Optional.of(JsonBody));

        testedObject = new AAIProducerClient(aaiHttpClientConfigurationMock);
        setField();
    }

    @Test
    public void getHttpResponsePatch_success() throws IOException {

        when(closeableHttpClientMock.execute(any(HttpPatch.class), any(ResponseHandler.class)))
                .thenReturn(Optional.of(SUCCESS));
        Optional<String> actualResult = testedObject.getHttpResponse(httpRequestDetailsMock);

        Assertions.assertEquals(SUCCESS, actualResult.get());
    }

    private static void setField() throws NoSuchFieldException, IllegalAccessException {
        Field field = testedObject.getClass().getDeclaredField("closeableHttpClient");
        field.setAccessible(true);
        field.set(testedObject, closeableHttpClientMock);
    }
}
