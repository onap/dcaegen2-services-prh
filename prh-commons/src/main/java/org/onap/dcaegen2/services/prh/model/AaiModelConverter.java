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

package org.onap.dcaegen2.services.prh.model;

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.ImmutablePnf;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.ImmutablePnf.BuildFinal;

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.Pnf;
import org.springframework.util.StringUtils;

public final class AaiModelConverter {
    private AaiModelConverter() {}

    public static Pnf dmaapConsumerModelToPnf(ConsumerDmaapModel consumerDmaapModel) {
        final BuildFinal builder = ImmutablePnf
                .builder()
                .pnfName(consumerDmaapModel.getCorrelationId())
                .nfRole(consumerDmaapModel.getNfRole())
                .swVersion(consumerDmaapModel.getSwVersion())
                .serialNumber(consumerDmaapModel.getSerialNumber())
                .equipType(consumerDmaapModel.getEquipType())
                .equipModel(consumerDmaapModel.getEquipModel())
                .equipVendor(consumerDmaapModel.getEquipVendor());

        final String ipv4 = consumerDmaapModel.getIpv4();
        if (!StringUtils.isEmpty(ipv4)) {
            builder.ipaddressV4Oam(ipv4);
        }

        final String ipv6 = consumerDmaapModel.getIpv6();
        if (!StringUtils.isEmpty(ipv6)) {
            builder.ipaddressV6Oam(ipv6);
        }

        return builder.build();
    }
}
