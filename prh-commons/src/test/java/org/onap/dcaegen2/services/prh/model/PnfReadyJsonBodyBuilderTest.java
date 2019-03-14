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

        JsonObject jsonObject = new JsonParser().parse("{\n"
            + "        \"attachmentPoint\": \"bla-bla-30-3\",\n"
            + "        \"cvlan\": \"678\",\n"
            + "        \"svlan\": \"1005\"\n"
            + "      }").getAsJsonObject();

        ConsumerDmaapModel model = ImmutableConsumerDmaapModel.builder()
                .correlationId("NOKnhfsadhff")
                .ipv4("256.22.33.155")
                .ipv6("200J:0db8:85a3:0000:0000:8a2e:0370:7334")
                .serialNumber("1234")
                .equipVendor("NOKIA")
                .equipModel("3310")
                .equipType("cell")
                .nfRole("role")
                .swVersion("1.2.3")
                .additionalFields(jsonObject)
                .build();

        String expectedResult = "{"
                + "\"correlationId\":\"NOKnhfsadhff\","
                + "\"serial-number\":\"1234\","
                + "\"equip-vendor\":\"NOKIA\","
                + "\"equip-model\":\"3310\","
                + "\"equip-type\":\"cell\","
                + "\"nf-role\":\"role\","
                + "\"sw-version\":\"1.2.3\","
                + "\"additionalFields\":{\"attachmentPoint\":\"bla-bla-30-3\",\"cvlan\":\"678\",\"svlan\":\"1005\"}"
                + "}";

        assertEquals(expectedResult, new PnfReadyJsonBodyBuilderImpl().createJsonBody(model));
    }

    @Test
    void createJsonBodyWithEmptyOptionalPnfRegistrationFields_shouldReturnJsonInString() {
        ConsumerDmaapModel model = ImmutableConsumerDmaapModel.builder()
                .correlationId("NOKnhfsadhff")
                .ipv4("256.22.33.155")
                .ipv6("200J:0db8:85a3:0000:0000:8a2e:0370:7334")
                .serialNumber("")
                .equipVendor("")
                .equipModel("")
                .equipType("")
                .nfRole("")
                .swVersion("")
                .additionalFields(new JsonObject())
                .build();

        String expectedResult = "{"
                + "\"correlationId\":\"NOKnhfsadhff\","
                + "\"serial-number\":\"\","
                + "\"equip-vendor\":\"\","
                + "\"equip-model\":\"\","
                + "\"equip-type\":\"\","
                + "\"nf-role\":\"\","
                + "\"sw-version\":\"\""
                + "}";

        assertEquals(expectedResult, new PnfReadyJsonBodyBuilderImpl().createJsonBody(model));
    }
}
