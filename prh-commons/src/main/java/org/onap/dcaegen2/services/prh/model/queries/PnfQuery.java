/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
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

package org.onap.dcaegen2.services.prh.model.queries;

import static java.util.Arrays.asList;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.PUT;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.Request;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;

/**
 * @see NamedNodes
 * @see <a href="https://docs.onap.org/en/dublin/submodules/aai/aai-common.git/docs/AAI%20REST%20API%20Documentation/customQueries.html">AAI
 * Custom queries</a></a>
 */
public class PnfQuery implements Request {

    @SerializedName("start")
    private final List<String> startNodes;

    public PnfQuery(String pnfName) {
        this.startNodes = asList("/nodes/pnfs/pnf/" + pnfName);
    }

    @Override
    public HttpMethod method() {
        return PUT;
    }

    @Override
    public String uri() {
        return "/query?format=resource&subgraph=star&nodesOnly=true";
    }
}