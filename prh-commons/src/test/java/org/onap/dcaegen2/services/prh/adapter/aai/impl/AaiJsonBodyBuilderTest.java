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

package org.onap.dcaegen2.services.prh.adapter.aai.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerDmaapModel;

class AaiJsonBodyBuilderTest {

    @Test
    void createJsonBody_shouldReturnJsonInString() {

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
                .build();

        String expectedResult = "{"
                + "\"correlationId\":\"NOKnhfsadhff\","
                + "\"ipaddress-v4-oam\":\"256.22.33.155\","
                + "\"ipaddress-v6-oam\":\"200J:0db8:85a3:0000:0000:8a2e:0370:7334\","
                + "\"serial-number\":\"1234\","
                + "\"equip-vendor\":\"NOKIA\","
                + "\"equip-model\":\"3310\","
                + "\"equip-type\":\"cell\","
                + "\"nf-role\":\"role\","
                + "\"sw-version\":\"1.2.3\""
                + "}";

        assertEquals(expectedResult, new AaiJsonBodyBuilderImpl().createJsonBody(model));
    }

    @Test
    void createJsonBodyWithoutIPs_shouldReturnJsonInString() {

        ConsumerDmaapModel model = ImmutableConsumerDmaapModel.builder()
            .correlationId("NOKnhfsadhff")
            .serialNumber("1234")
            .equipVendor("NOKIA")
            .equipModel("3310")
            .equipType("cell")
            .nfRole("role")
            .swVersion("1.2.3")
            .build();

        String expectedResult = "{"
            + "\"correlationId\":\"NOKnhfsadhff\","
            + "\"serial-number\":\"1234\","
            + "\"equip-vendor\":\"NOKIA\","
            + "\"equip-model\":\"3310\","
            + "\"equip-type\":\"cell\","
            + "\"nf-role\":\"role\","
            + "\"sw-version\":\"1.2.3\""
            + "}";

        assertEquals(expectedResult, new AaiJsonBodyBuilderImpl().createJsonBody(model));
    }

    @Test
    void createJsonBodyWithEmptyIPs_shouldReturnJsonInString() {

        ConsumerDmaapModel model = ImmutableConsumerDmaapModel.builder()
            .correlationId("NOKnhfsadhff")
            .ipv4("")
            .ipv6("")
            .serialNumber("1234")
            .equipVendor("NOKIA")
            .equipModel("3310")
            .equipType("cell")
            .nfRole("role")
            .swVersion("1.2.3")
            .build();

        String expectedResult = "{"
            + "\"correlationId\":\"NOKnhfsadhff\","
            + "\"serial-number\":\"1234\","
            + "\"equip-vendor\":\"NOKIA\","
            + "\"equip-model\":\"3310\","
            + "\"equip-type\":\"cell\","
            + "\"nf-role\":\"role\","
            + "\"sw-version\":\"1.2.3\""
            + "}";

        assertEquals(expectedResult, new AaiJsonBodyBuilderImpl().createJsonBody(model));
    }
}
