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
package org.onap.dcaegen2.services.prh.adapter.aai;

import static org.mockito.Mockito.mock;

import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.adapter.aai.model.AaiModel;
import org.onap.dcaegen2.services.prh.adapter.aai.model.JsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI;

public class AbstractHttpClientTest {

    protected final AaiModel aaiModel = () -> "test-id";
    protected final RxHttpClient httpClient = mock(RxHttpClient.class);
    protected final JsonBodyBuilder bodyBuilder = mock(JsonBodyBuilder.class);
    protected final HttpResponse response = mock(HttpResponse.class);


    protected String constructAaiUri(AaiClientConfiguration configuration, String pnfName) {
        return new URI.URIBuilder().path(configuration.pnfUrl() + "/" + pnfName).build().toString();
    }
}
