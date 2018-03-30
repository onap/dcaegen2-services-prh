/*-
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

package services.service;

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
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import services.config.AAIHttpClientConfiguration;

public class AAIExtendedHttpClientImplTest {

    private AAIExtendedHttpClientImpl testedObject;
    private AAIHttpClientConfiguration aaiHttpClientConfigurationMock = mock(AAIHttpClientConfiguration.class);
    private CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        when(aaiHttpClientConfigurationMock.aaiHost()).thenReturn("hostTest");
        when(aaiHttpClientConfigurationMock.aaiProtocol()).thenReturn("https");
        when(aaiHttpClientConfigurationMock.aaiHostPortNumber()).thenReturn(1234);
        testedObject = new AAIExtendedHttpClientImpl(aaiHttpClientConfigurationMock);
        setField();
    }

    @Test
    public void getExtendedDetails_success() throws IOException {
        String expectedResult = "getExtendedDetailsOK";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "value1");
        Map<String, String> headers = new HashMap<>();
        headers.put("headerKey", "headerValue");

        when(closeableHttpClientMock.execute(any(HttpGet.class), any(ResponseHandler.class))).
                thenReturn(Optional.of(expectedResult));
        String actualResult = testedObject.getExtendedDetails("testPath", queryParams, headers);
        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void getExtendedDetails_returnsNull() throws IOException {
        when(closeableHttpClientMock.execute(any(HttpGet.class), any(ResponseHandler.class))).
                thenReturn(Optional.empty());
        String actualResult = testedObject.getExtendedDetails("testPath", new HashMap<>(), new HashMap<>());
        Assert.assertNull(actualResult);
    }

    private void setField() throws NoSuchFieldException, IllegalAccessException {
        Field field = testedObject.getClass().getDeclaredField("closeableHttpClient");
        field.setAccessible(true);
        field.set(testedObject, closeableHttpClientMock);
    }


}
