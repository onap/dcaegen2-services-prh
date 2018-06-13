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
package org.onap.dcaegen2.services.prh.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.onap.dcaegen2.services.prh.exceptions.DmaapEmptyResponseException;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/8/18
 */
public class DmaapConsumerJsonParser {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String EVENT = "event";
    private static final String OTHER_FIELDS = "otherFields";
    private static final String PNF_OAM_IPV_4_ADDRESS = "pnfOamIpv4Address";
    private static final String PNF_OAM_IPV_6_ADDRESS = "pnfOamIpv6Address";
    private static final String PNF_VENDOR_NAME = "pnfVendorName";
    private static final String PNF_SERIAL_NUMBER = "pnfSerialNumber";


    public Optional<ConsumerDmaapModel> getJsonObject(String message)
        throws PrhTaskException {
        JsonElement jsonElement = new JsonParser().parse(message);
        Optional<ConsumerDmaapModel> consumerDmaapModel;
        if (jsonElement.isJsonObject()) {
            consumerDmaapModel = Optional.of(create(jsonElement.getAsJsonObject()));
        } else {
            consumerDmaapModel = Optional
                .of(create(StreamSupport.stream(jsonElement.getAsJsonArray().spliterator(), false).findFirst()
                    .flatMap(this::getJsonObjectFromAnArray)
                    .orElseThrow(DmaapEmptyResponseException::new)));
        }
        logger.info("Parsed model from DmaaP after getting it: {}", consumerDmaapModel);
        return consumerDmaapModel;
    }

    public Optional<JsonObject> getJsonObjectFromAnArray(JsonElement element) {
        return Optional.of(new JsonParser().parse(element.getAsString()).getAsJsonObject());
    }

    private ConsumerDmaapModel create(JsonObject jsonObject) throws DmaapNotFoundException {
        if (containsHeader(jsonObject)) {
            jsonObject = jsonObject.getAsJsonObject(EVENT).getAsJsonObject(OTHER_FIELDS);
            String pnfVendorName = getValueFromJson(jsonObject, PNF_VENDOR_NAME);
            String pnfSerialNumber = getValueFromJson(jsonObject, PNF_SERIAL_NUMBER);
            String pnfOamIpv4Address = getValueFromJson(jsonObject, PNF_OAM_IPV_4_ADDRESS);
            String pnfOamIpv6Address = getValueFromJson(jsonObject, PNF_OAM_IPV_6_ADDRESS);
            if (isVendorAndSerialNotEmpty(pnfSerialNumber, pnfVendorName) && isIpPropertiesNotEmpty(pnfOamIpv4Address,
                pnfOamIpv6Address)) {
                String correlationID = pnfVendorName.substring(0, Math.min(pnfVendorName.length(), 3)).toUpperCase()
                    .concat(pnfSerialNumber);
                return ImmutableConsumerDmaapModel.builder().pnfName(correlationID).ipv4(pnfOamIpv4Address)
                    .ipv6(pnfOamIpv6Address).build();
            }
            throw new DmaapNotFoundException("IPV4 and IPV6 are empty");
        }
        throw new DmaapNotFoundException("Incorrect JsonObject - missing header");
    }

    private String getValueFromJson(JsonObject jsonObject, String jsonKey) {
        return jsonObject.has(jsonKey) ? jsonObject.get(jsonKey).getAsString() : "";
    }

    private boolean isVendorAndSerialNotEmpty(String pnfSerialNumber, String pnfVendorName) {
        return ((pnfSerialNumber != null && !pnfSerialNumber.isEmpty()) && (pnfVendorName != null && !pnfVendorName
            .isEmpty()));
    }

    private boolean isIpPropertiesNotEmpty(String ipv4, String ipv6) {
        return (ipv4 != null && !ipv4.isEmpty()) || (ipv6 != null
            && !ipv6.isEmpty());
    }

    private boolean containsHeader(JsonObject jsonObject) {
        return jsonObject.has(EVENT) && jsonObject.getAsJsonObject(EVENT).has(OTHER_FIELDS);
    }

}
