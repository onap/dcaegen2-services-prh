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

package org.onap.dcaegen2.services.bootstrap;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

class CbsJsonToPropertyMapConverter {

    private static final String CBS_CONFIG_ROOT_PROPERTY = "config";

    Map<String, Object> convertToMap(JsonObject jsonObject) {
        verifyExpectedCbsJsonFormat(jsonObject);
        JsonObject config = jsonObject.getAsJsonObject(CBS_CONFIG_ROOT_PROPERTY);
        return config.entrySet().stream()
                .filter(entry -> entry.getValue().isJsonPrimitive())
                .collect(toMap(Map.Entry::getKey, entry -> unpack(entry.getValue().getAsJsonPrimitive())));
    }

    private static void verifyExpectedCbsJsonFormat(JsonObject jsonObject) {
        if (!jsonObject.has(CBS_CONFIG_ROOT_PROPERTY)) {
            throw new IllegalArgumentException("Missing expected '" + CBS_CONFIG_ROOT_PROPERTY + "'" +
                    " property in json from CBS.");
        }
    }

    private Object unpack(JsonPrimitive value) {
        if (value.isString()) {
            return value.getAsString();
        }
        if (value.isBoolean()) {
            return value.getAsBoolean();
        }
        if (value.isNumber()) {
            return value.getAsLong();
        }
        throw new IllegalArgumentException("Unexpected JsonPrimitive type found in configuration form CBS: " + value);
    }

}
