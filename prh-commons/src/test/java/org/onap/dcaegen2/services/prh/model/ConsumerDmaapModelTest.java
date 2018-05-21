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

package org.onap.dcaegen2.services.prh.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConsumerDmaapModelTest {

    // Given
    private ConsumerDmaapModel consumerDmaapModel;
    private String pnfName = "NOKnhfsadhff";
    private String ipv4 = "11.22.33.155";
    private String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";

    @Test
    public void consumerDmaapModelBuilder_shouldBuildAnObject() {

        // When
        consumerDmaapModel = ImmutableConsumerDmaapModel.builder()
                .pnfName(pnfName)
                .ipv4(ipv4)
                .ipv6(ipv6)
                .build();

        // Then
        Assertions.assertNotNull(consumerDmaapModel);
        Assertions.assertEquals(pnfName,consumerDmaapModel.getPnfName());
        Assertions.assertEquals(ipv4,consumerDmaapModel.getIpv4());
        Assertions.assertEquals(ipv6,consumerDmaapModel.getIpv6());
    }
}
