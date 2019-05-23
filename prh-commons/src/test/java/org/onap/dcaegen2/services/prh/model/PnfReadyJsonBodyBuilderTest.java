/*
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

package org.onap.dcaegen2.services.prh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class PnfReadyJsonBodyBuilderTest {

    @Test
    void createJsonBody_shouldReturnJsonInString() {

        JsonObject jsonObject = parse("{\n"
            + "        \"attachmentPoint\": \"bla-bla-30-3\",\n"
            + "        \"cvlan\": \"678\",\n"
            + "        \"svlan\": \"1005\"\n"
            + "      }");

        ConsumerDmaapModel model = ImmutableConsumerDmaapModel.builder()
            .correlationId("NOKnhfsadhff")
            .additionalFields(jsonObject)
            .build();

        JsonObject expectedResult = parse("{"
            + "\"correlationId\":\"NOKnhfsadhff\","
            + "\"additionalFields\":{\"attachmentPoint\":\"bla-bla-30-3\",\"cvlan\":\"678\",\"svlan\":\"1005\"}"
            + "}");

        assertEquals(expectedResult, new PnfReadyJsonBodyBuilder().createJsonBody(model));
    }

    @Test
    void createJsonBodyWithNullableFieldsNotSet_shouldReturnJsonInString() {

        ConsumerDmaapModel model = ImmutableConsumerDmaapModel.builder()
            .correlationId("NOKnhfsadhff")
            .build();

        JsonObject expectedResult = parse("{\"correlationId\":\"NOKnhfsadhff\"}");

        assertEquals(expectedResult, new PnfReadyJsonBodyBuilder().createJsonBody(model));
    }

    @Test
    void createJsonBodyWithEmptyOptionalPnfRegistrationFields_shouldReturnJsonInString() {
        JsonObject jsonObject = new JsonObject();

        ConsumerDmaapModel model = ImmutableConsumerDmaapModel.builder()
            .correlationId("NOKnhfsadhff")
            .additionalFields(jsonObject)
            .build();

        JsonObject expectedResult = parse("{\"correlationId\":\"NOKnhfsadhff\"}");

        assertEquals(expectedResult, new PnfReadyJsonBodyBuilder().createJsonBody(model));
    }

    private static JsonObject parse(String jsonString) {
        return new JsonParser().parse(jsonString).getAsJsonObject();
    }
}
