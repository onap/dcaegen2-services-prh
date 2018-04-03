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

package org.onap.dcaegen2.services.service.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.utils.HttpRequestDetails;
import org.onap.dcaegen2.services.utils.RequestVerbs;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestDetailsTest {

    private HttpRequestDetails testObject;

    private Map<String,String>  headers = new HashMap<>();
    private Map<String,String> queryParameters = new HashMap<>();
    private String aaiPath;

    @BeforeEach
    public void setup() {
        aaiPath = "aaiPathTest";
        headers.put("headerKey1","headerValue1");
        headers.get("queryKey1");
        queryParameters.put("queryKey1", "queryValue1");
        RequestVerbs httpVerb = RequestVerbs.GET;
        testObject = new HttpRequestDetails(aaiPath, queryParameters, headers, httpVerb);
    }

    @Test
    public void testGetters_success() {
        Assertions.assertEquals("aaiPathTest", testObject.getAaiAPIPath());
        Assertions.assertNotNull("headerValue1", testObject.getHeaders().get("headerKey1"));
        Assertions.assertNotNull("queryValue1", testObject.getQueryParameters().get("queryKey1"));
        Assertions.assertEquals(RequestVerbs.GET, testObject.getHttpVerb());
    }
}
