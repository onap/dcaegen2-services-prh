/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019-2021 NOKIA Intellectual Property. All rights reserved.
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
import io.vavr.collection.Map;
import org.apache.commons.text.StringSubstitutor;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiHttpClient;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiServiceInstanceQueryModel;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import reactor.core.publisher.Mono;

public class AaiGetServiceInstanceClient implements
    AaiHttpClient<AaiServiceInstanceQueryModel, HttpResponse> {

    private static final String CUSTOMER = "customer";
    private static final String SERVICE_TYPE = "serviceType";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";

    private static final String VARIABLE_PREFIX = "{{";
    private static final String VARIABLE_SUFFIX = "}}";

    private final RxHttpClient httpClient;
    private final AaiClientConfiguration configuration;

    public AaiGetServiceInstanceClient(final AaiClientConfiguration configuration,
        final RxHttpClient httpClient) {
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    @Override
    public Mono<HttpResponse> getAaiResponse(AaiServiceInstanceQueryModel aaiModel) {
        final Map<String, String> mapping = HashMap.of(
            CUSTOMER, aaiModel.customerId(),
            SERVICE_TYPE, aaiModel.serviceType(),
            SERVICE_INSTANCE_ID, aaiModel.serviceInstanceId());

        final StringSubstitutor substitutor =
            new StringSubstitutor(mapping.toJavaMap(), VARIABLE_PREFIX, VARIABLE_SUFFIX);
        final String endpoint = substitutor.replace(configuration.aaiServiceInstancePath());

        return httpClient.call(ImmutableHttpRequest.builder()
            .method(HttpMethod.GET)
            .url(configuration.baseUrl() + endpoint)
            .customHeaders(HashMap.ofAll(configuration.aaiHeaders()))
            .diagnosticContext(createRequestDiagnosticContext())
            .build());
    }
}
