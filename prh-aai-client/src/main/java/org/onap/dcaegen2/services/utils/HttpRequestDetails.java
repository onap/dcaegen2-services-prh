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
package org.onap.dcaegen2.services.utils;

import java.util.Map;


public class HttpRequestDetails {

    private final String aaiAPIPath;
    private final Map<String,String> queryParameters;
    private final Map<String,String> headers;
    private final RequestVerbs requestVerb;

    public HttpRequestDetails(final String aaiAPIPath, final Map<String, String> queryParams,
                              final Map<String, String> headers, RequestVerbs requestVerb) {

        this.aaiAPIPath = aaiAPIPath;
        this.queryParameters = queryParams;
        this.headers = headers;
        this.requestVerb = requestVerb;
    }

    public String getAaiAPIPath() {
        return aaiAPIPath;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public RequestVerbs getHttpVerb() {
        return requestVerb;
    }
}
