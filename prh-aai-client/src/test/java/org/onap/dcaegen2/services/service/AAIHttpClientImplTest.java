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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.config.AAIHttpClientConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


public class AAIHttpClientImplTest {

    private AAIHttpClientImpl aaiHttpClientImpl;
    private AAIHttpClientConfiguration aaiHttpClientConfiguration;


    @BeforeEach
    public void setup() {
        aaiHttpClientConfiguration = mock(AAIHttpClientConfiguration.class);
        aaiHttpClientImpl  = new AAIHttpClientImpl(aaiHttpClientConfiguration);
    }

    @Test
    public void getAAIHttpClientObject_shouldNotBeNull() {
        aaiHttpClientImpl.getAAIHttpClient();
        assertNotNull(aaiHttpClientImpl.getAAIHttpClient());
    }
}

