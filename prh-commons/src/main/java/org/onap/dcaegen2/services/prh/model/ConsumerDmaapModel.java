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

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.DmaapModel;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/8/18
 */

@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true)
public interface ConsumerDmaapModel extends AaiModel, DmaapModel {

    @SerializedName(value = "correlationId", alternate = "correlationId")
    String getCorrelationId();

    @SerializedName(value = "serial-number", alternate = "serial-number")
    String getSerialNumber();

    @SerializedName(value = "equip-vendor", alternate = "equip-vendor")
    String getEquipVendor();

    @SerializedName(value = "equip-model", alternate = "equip-model")
    String getEquipModel();

    @SerializedName(value = "equip-type", alternate = "equip-type")
    String getEquipType();

    @SerializedName(value = "nf-role", alternate = "nf-role")
    String getNfRole();

    @SerializedName(value = "sw-version", alternate = "sw-version")
    String getSwVersion();

    @SerializedName(value = "additionalFields", alternate = "additionalFields")
    JsonObject getAdditionalFields();
}
