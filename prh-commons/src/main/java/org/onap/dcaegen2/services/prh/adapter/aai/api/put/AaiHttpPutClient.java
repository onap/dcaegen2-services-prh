/*-
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


package org.onap.dcaegen2.services.prh.adapter.aai.api.put;

import static org.onap.dcaegen2.services.prh.adapter.aai.main.AaiHttpClientFactory.createRequestDiagnosticContext;

import io.vavr.collection.HashMap;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiHttpClient;
import org.onap.dcaegen2.services.prh.adapter.aai.model.AaiModel;
import org.onap.dcaegen2.services.prh.adapter.aai.model.JsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import reactor.core.publisher.Mono;

public class AaiHttpPutClient implements AaiHttpClient<AaiModel, HttpResponse> {

    private RxHttpClient httpClient;
    private final AaiClientConfiguration configuration;
    private final JsonBodyBuilder jsonBodyBuilder;
    private final String uri;

    public AaiHttpPutClient(final AaiClientConfiguration configuration, JsonBodyBuilder jsonBodyBuilder, String uri,
        RxHttpClient httpClient) {
        this.configuration = configuration;
        this.jsonBodyBuilder = jsonBodyBuilder;
        this.uri = uri;
        this.httpClient = httpClient;
    }

    @Override
    public Mono<HttpResponse> getAaiResponse(AaiModel aaiModel) {
        String jsonBody = jsonBodyBuilder.createJsonBody(aaiModel);

        return httpClient.call(ImmutableHttpRequest.builder()
            .url(uri)
            .customHeaders(HashMap.ofAll(configuration.aaiHeaders()))
            .diagnosticContext(createRequestDiagnosticContext())
            .body(RequestBody.fromString(jsonBody))
            .method(HttpMethod.PUT)
            .build());
    }
}
