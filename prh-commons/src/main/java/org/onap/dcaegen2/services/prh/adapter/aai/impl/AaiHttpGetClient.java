/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

import static org.onap.dcaegen2.services.prh.adapter.aai.main.AaiHttpClientFactory.createRequestDiagnosticContext;

import io.vavr.collection.HashMap;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiHttpClient;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import reactor.core.publisher.Mono;

public final class AaiHttpGetClient implements AaiHttpClient<ConsumerDmaapModel, HttpResponse> {

    private final RxHttpClient httpClient;
    private final AaiClientConfiguration configuration;


    public AaiHttpGetClient(AaiClientConfiguration configuration, RxHttpClient httpClient) {
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    @Override
    public Mono<HttpResponse> getAaiResponse(ConsumerDmaapModel aaiModel) {
        return httpClient.call(ImmutableHttpRequest.builder()
            .method(HttpMethod.GET)
            .url(configuration.pnfUrl() + "/" + aaiModel.getCorrelationId())
            .customHeaders(HashMap.ofAll(configuration.aaiHeaders()))
            .diagnosticContext(createRequestDiagnosticContext())
            .build());
    }
}
