/*-
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
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

package org.onap.dcaegen2.services.prh.adapter.aai.api.patch;

import static org.onap.dcaegen2.services.prh.adapter.aai.impl.AaiRequests.createAaiPatchRequest;
import static org.onap.dcaegen2.services.prh.adapter.aai.main.AaiHttpClientFactory.createRequestDiagnosticContext;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiHttpClient;
import org.onap.dcaegen2.services.prh.adapter.aai.model.AaiModel;
import org.onap.dcaegen2.services.prh.adapter.aai.model.JsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI;
import reactor.core.publisher.Mono;

public final class AaiHttpPatchClient implements AaiHttpClient<AaiModel, HttpResponse> {

    private final static Map<String, String> CONTENT_TYPE = HashMap.of("Content-Type", "application/merge-patch+json");

    private RxHttpClient httpClient;
    private final AaiClientConfiguration configuration;
    private final JsonBodyBuilder jsonBodyBuilder;


    public AaiHttpPatchClient(final AaiClientConfiguration configuration, JsonBodyBuilder jsonBodyBuilder,
        RxHttpClient httpClient) {
        this.configuration = configuration;
        this.jsonBodyBuilder = jsonBodyBuilder;
        this.httpClient = httpClient;
    }

    public Mono<HttpResponse> getAaiResponse(AaiModel aaiModel) {
        final Map<String, String> headers = CONTENT_TYPE.merge(HashMap.ofAll(configuration.aaiHeaders()));

        final HttpRequest aaiPatchRequest = createAaiPatchRequest(
            getUri(aaiModel.getCorrelationId()),
            createRequestDiagnosticContext(),
            headers.toJavaMap(),
            jsonBodyBuilder,
            aaiModel);

        return httpClient.call(aaiPatchRequest);
    }

    private String getUri(String pnfName) {
        return new URI.URIBuilder()
            .path(configuration.pnfUrl() + "/" + pnfName).build().toString();
    }
}
