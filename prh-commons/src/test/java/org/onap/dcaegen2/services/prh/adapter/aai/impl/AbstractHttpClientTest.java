/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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
package org.onap.dcaegen2.services.prh.adapter.aai.impl;

import static org.mockito.Mockito.mock;

import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;

public class AbstractHttpClientTest {

    protected final ConsumerDmaapModel aaiModel = mock(ConsumerDmaapModel.class);
    protected final RxHttpClient httpClient = mock(RxHttpClient.class);
    protected final AaiJsonBodyBuilderImpl bodyBuilder = mock(AaiJsonBodyBuilderImpl.class);
    protected final HttpResponse response = mock(HttpResponse.class);


    protected String constructAaiUri(AaiClientConfiguration configuration, String pnfName) {
        return configuration.pnfUrl() + "/" + pnfName;
    }
}
