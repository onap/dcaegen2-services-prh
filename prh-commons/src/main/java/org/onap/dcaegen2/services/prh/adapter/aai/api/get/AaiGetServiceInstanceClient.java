/*
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

package org.onap.dcaegen2.services.prh.adapter.aai.api.get;

import static org.onap.dcaegen2.services.prh.adapter.aai.impl.AaiRequests.createAaiGetRequest;
import static org.onap.dcaegen2.services.prh.adapter.aai.main.AaiHttpClientFactory.createRequestDiagnosticContext;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.apache.commons.text.StringSubstitutor;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiHttpClient;
import org.onap.dcaegen2.services.prh.adapter.aai.model.AaiServiceInstanceQueryModel;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI;
import reactor.core.publisher.Mono;

public class AaiGetServiceInstanceClient implements
    AaiHttpClient<AaiServiceInstanceQueryModel, HttpResponse> {

    private static final String CUSTOMER = "customer";
    private static final String SERVICE_TYPE = "serviceType";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";

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

        final StringSubstitutor substitutor = new StringSubstitutor(mapping.toJavaMap());
        final String replaced = substitutor.replace(configuration.aaiServiceInstancePath());

        final HttpRequest getRequest = createAaiGetRequest(getUri(replaced),
            createRequestDiagnosticContext(), configuration.aaiHeaders());

        return httpClient.call(getRequest);
    }

    private String getUri(final String endpoint) {
        return new URI.URIBuilder().path(configuration.baseUrl() + endpoint).build().toString();
    }
}
