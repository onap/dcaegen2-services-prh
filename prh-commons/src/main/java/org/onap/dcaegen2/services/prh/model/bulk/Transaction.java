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

package org.onap.dcaegen2.services.prh.model.bulk;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * @see Transactions
 */
@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true)
public interface Transaction {

    enum Action {
        @SerializedName("put") PUT,
        @SerializedName("patch") PATCH,
        @SerializedName("delete") DELETE
    }

    Action action();

    String uri();

    @Value.Default
    default JsonObject body() {
        return new JsonObject();
    }
}