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

import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * Model for AAI Custom query
 * <br/>
 * <br/>
 * Example response:
 * <br/>
 * <pre>
 *     {
 *     "results": [
 *         {
 *             "pnf": {
 *                 "pnf-name": "foo",
 *                 "in-maint": false,
 *                 "resource-version": "1559732370039"
 *             }
 *         },
 *         {
 *             "logical-link": {
 *                 "link-name": "bar",
 *                 "in-maint": false,
 *                 "link-type": "attachment-point",
 *                 "resource-version": "1559732357143"
 *             }
 *         },
 *         {
 *             "service-instance": {
 *                 "service-instance-id": "baz",
 *                 "service-instance-name": "pnf-service-name",
 *                 "resource-version": "1559732385933",
 *                 "orchestration-status": "ACTIVE"
 *             }
 *         }
 *     ]
 * }
 * </pre>
 * @see NamedNode
 * @see <a href="https://docs.onap.org/en/dublin/submodules/aai/aai-common.git/docs/AAI%20REST%20API%20Documentation/customQueries.html">AAI
 * Custom queries</a></a>
 */
@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true)
public interface NodesQuery {

    @SerializedName(value = "results")
    List<NamedNode> results();
}