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

package org.onap.dcaegen2.services.prh.adapter.aai.api;

import static org.onap.dcaegen2.services.prh.adapter.aai.main.AaiHttpClientFactory.createRequestDiagnosticContext;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.model.AaiJsonBodyBuilderImpl;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import reactor.core.publisher.Mono;

public final class AaiHttpPatchClient implements AaiHttpClient<ConsumerDmaapModel, HttpResponse> {

    private final static Map<String, String> CONTENT_TYPE = HashMap.of("Content-Type", "application/merge-patch+json");

    private RxHttpClient httpClient;
    private final AaiClientConfiguration configuration;
    private final AaiJsonBodyBuilderImpl jsonBodyBuilder;


    public AaiHttpPatchClient(final AaiClientConfiguration configuration, AaiJsonBodyBuilderImpl jsonBodyBuilder,
        RxHttpClient httpClient) {
        this.configuration = configuration;
        this.jsonBodyBuilder = jsonBodyBuilder;
        this.httpClient = httpClient;
    }

    public Mono<HttpResponse> getAaiResponse(ConsumerDmaapModel aaiModel) {
        final Map<String, String> headers = CONTENT_TYPE.merge(HashMap.ofAll(configuration.aaiHeaders()));
        String jsonBody = jsonBodyBuilder.createJsonBody(aaiModel);

        return httpClient.call(ImmutableHttpRequest.builder()
            .url(configuration.pnfUrl() + "/" + aaiModel.getCorrelationId())
            .customHeaders(headers)
            .diagnosticContext(createRequestDiagnosticContext())
            .body(RequestBody.fromString(jsonBody))
            .method(HttpMethod.PATCH)
            .build());
    }
}
