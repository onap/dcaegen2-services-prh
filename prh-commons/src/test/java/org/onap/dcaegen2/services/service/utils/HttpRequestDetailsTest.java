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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.onap.dcaegen2.services.utils.HttpRequestDetails;
import org.onap.dcaegen2.services.utils.RequestVerbs;

import java.util.Optional;


public class HttpRequestDetailsTest {

    private static HttpRequestDetails testObject;

    private static final String AAI_PATH = "aaiPathTest";
    private static final RequestVerbs HTTP_VERB = RequestVerbs.PATCH;
    private static final String QUERY_KEY1 = "queryKey1";
    private static final String QUERY_VALUE1 = "queryValue1";
    private static final String HEADERS_KEY1 = "headersKey1";
    private static final String HEADERS_VALUE1 = "headersValue1";
    private static final String JSON_MESSAGE = "{\"dare_to\": \"dream_big\"}";


//    @BeforeAll
//    public static void init() {
//        testObject = ImmutableRequestDetails.builder()
//                .aaiAPIPath(AAI_PATH)
//                .requestVerb(HTTP_VERB)
//                .putQueryParameters(QUERY_KEY1,QUERY_VALUE1)
//                .putHeaders(HEADERS_KEY1,HEADERS_VALUE1)
//                .jsonBody(JSON_MESSAGE)
//                .build();
//    }
//
//    @Test
//    public void testGetters_success() {
//        Assertions.assertEquals(AAI_PATH, testObject.aaiAPIPath());
//        Assertions.assertEquals(HEADERS_VALUE1, testObject.headers().get(HEADERS_KEY1));
//        Assertions.assertEquals(QUERY_VALUE1, testObject.queryParameters().get(QUERY_KEY1));
//        Assertions.assertEquals(RequestVerbs.PATCH, testObject.requestVerb());
//        Assertions.assertEquals(Optional.of(JSON_MESSAGE), testObject.jsonBody());
//    }
}
