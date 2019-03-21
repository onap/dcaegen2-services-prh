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

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel.Builder;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;

import java.util.ServiceLoader;
import org.springframework.util.StringUtils;


public class AaiJsonBodyBuilderImpl implements JsonBodyBuilder<ConsumerDmaapModel> {

    /**
     * Method for serialization object by GSON.
     *
     * @param consumerDmaapModel - object which will be serialized
     * @return string from serialization
     */
    public String createJsonBody(ConsumerDmaapModel consumerDmaapModel) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);

        Builder builder = ImmutableConsumerDmaapModel.builder()
            .correlationId(consumerDmaapModel.getCorrelationId())
            .serialNumber(consumerDmaapModel.getSerialNumber())
            .equipVendor(consumerDmaapModel.getEquipVendor())
            .equipModel(consumerDmaapModel.getEquipModel())
            .equipType(consumerDmaapModel.getEquipType())
            .nfRole(consumerDmaapModel.getNfRole())
            .swVersion(consumerDmaapModel.getSwVersion())
            .additionalFields(consumerDmaapModel.getAdditionalFields());

        String ipv4 = consumerDmaapModel.getIpv4();
        if (ipv4 != null && !StringUtils.isEmpty(ipv4)) {
            builder.ipv4(ipv4);
        }
        String ipv6 = consumerDmaapModel.getIpv6();
        if (ipv6 != null && !StringUtils.isEmpty(ipv6)) {
            builder.ipv6(ipv6);
        }

        return gsonBuilder.create().toJson(builder.build());
    }
}