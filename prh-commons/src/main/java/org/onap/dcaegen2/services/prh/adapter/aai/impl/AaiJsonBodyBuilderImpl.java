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

package org.onap.dcaegen2.services.prh.adapter.aai.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.util.ServiceLoader;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerPnfModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerPnfModel.Builder;
import org.springframework.util.StringUtils;


public class AaiJsonBodyBuilderImpl {

    public String createJsonBody(ConsumerPnfModel consumerPnfModel) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);

        Builder builder = ImmutableConsumerPnfModel.builder()
            .correlationId(consumerPnfModel.getCorrelationId())
            .serialNumber(consumerPnfModel.getSerialNumber())
            .equipVendor(consumerPnfModel.getEquipVendor())
            .equipModel(consumerPnfModel.getEquipModel())
            .equipType(consumerPnfModel.getEquipType())
            .nfRole(consumerPnfModel.getNfRole())
            .swVersion(consumerPnfModel.getSwVersion())
            .additionalFields(consumerPnfModel.getAdditionalFields());

        String ipv4 = consumerPnfModel.getIpv4();
        if (!StringUtils.isEmpty(ipv4)) {
            builder.ipv4(ipv4);
        }
        String ipv6 = consumerPnfModel.getIpv6();
        if (!StringUtils.isEmpty(ipv6)) {
            builder.ipv6(ipv6);
        }

        return gsonBuilder.create().toJson(builder.build());
    }
}