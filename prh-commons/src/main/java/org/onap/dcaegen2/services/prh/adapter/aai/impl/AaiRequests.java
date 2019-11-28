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

import io.vavr.collection.HashMap;
import java.util.Map;
import org.onap.dcaegen2.services.prh.adapter.aai.model.ClientModel;
import org.onap.dcaegen2.services.prh.adapter.aai.model.JsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;


public final class AaiRequests {

    private AaiRequests(){}

    public static HttpRequest createAaiPatchRequest(String url,
            RequestDiagnosticContext context,
            Map<String, String> customHeaders,
            JsonBodyBuilder jsonBodyBuilder,
            ClientModel clientModel) {

        return buildAaiRequestWithBody(url, context, customHeaders,
                jsonBodyBuilder, clientModel, HttpMethod.PATCH);
    }

    public static HttpRequest createAaiPutRequest(String url,
            RequestDiagnosticContext context,
            Map<String, String> customHeaders,
            JsonBodyBuilder jsonBodyBuilder,
            ClientModel clientModel) {

        return buildAaiRequestWithBody(url, context, customHeaders,
                jsonBodyBuilder, clientModel, HttpMethod.PUT);
    }

    private static HttpRequest buildAaiRequestWithBody(String url,
            RequestDiagnosticContext context,
            Map<String, String> customHeaders,
            JsonBodyBuilder jsonBodyBuilder,
            ClientModel clientModel,
            HttpMethod method) {

        String jsonBody = jsonBodyBuilder.createJsonBody(clientModel);

        return ImmutableHttpRequest.builder()
                .url(url)
                .customHeaders(HashMap.ofAll(customHeaders))
                .diagnosticContext(context)
                .body(RequestBody.fromString(jsonBody))
                .method(method)
                .build();
    }

    public static HttpRequest createAaiGetRequest(String url,
            RequestDiagnosticContext context,
            Map<String, String> customHeaders) {
        return ImmutableHttpRequest.builder()
                .method(HttpMethod.GET)
                .url(url)
                .customHeaders(HashMap.ofAll(customHeaders))
                .diagnosticContext(context)
                .build();
    }
}
