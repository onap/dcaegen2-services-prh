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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.ADDITIONAL_FIELDS;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.COMMON_EVENT_HEADER;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.COMMON_FORMAT_FOR_JSON_OBJECT;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.COMMON_FORMAT_FOR_STRING;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.CORRELATION_ID;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.EQUIP_MODEL;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.EQUIP_TYPE;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.EQUIP_VENDOR;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.EVENT;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.NF_ROLE;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.OAM_IPV_4_ADDRESS;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.OAM_IPV_6_ADDRESS;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.PNF_REGISTRATION_FIELDS;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.SERIAL_NUMBER;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.SOURCE_NAME;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.SW_VERSION;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/8/18
 */
@Component
public class DmaapConsumerJsonParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapConsumerJsonParser.class);

    private String pnfSourceName;
    private String pnfOamIpv4Address;
    private String pnfOamIpv6Address;
    private String pnfSerialNumberOptionalField;
    private String pnfEquipVendorOptionalField;
    private String pnfEquipModelOptionalField;
    private String pnfEquipTypeOptionalField;
    private String pnfNfRoleOptionalField;
    private String pnfSwVersionOptionalField;
    private JsonObject pnfAdditionalFields;

    /**
     * Extract info from string and create @see {@link org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel}.
     *
     * @param monoMessage - results from DMaaP
     * @return reactive DMaaPModel
     */
    public Flux<ConsumerDmaapModel> getJsonObject(Mono<MessageRouterSubscribeResponse> monoMessage) {
        return monoMessage.flatMapMany(msgRouterResponse -> getConsumerDmaapModelFromJsonArray(msgRouterResponse.items()));
    }

    private Flux<ConsumerDmaapModel> getConsumerDmaapModelFromJsonArray(JsonArray items) {
        LOGGER.debug("DmaapConsumerJsonParser input for parsing: {}", items);

        if (items.size() == 0) {
            LOGGER.debug("Nothing to consume from DMaaP");
            return Flux.empty();
        }
        return create(
                Flux.defer(() -> Flux.fromStream(StreamSupport.stream(items.spliterator(), false)
                        .map(jsonElementFromArray -> getJsonObjectFromAnArray(jsonElementFromArray)
                                .orElseGet(JsonObject::new)))));
    }

    Optional<JsonObject> getJsonObjectFromAnArray(JsonElement element) {
        JsonParser jsonParser = new JsonParser();
        return element.isJsonPrimitive() ? Optional.of(jsonParser.parse(element.getAsString()).getAsJsonObject())
            : Optional.of(jsonParser.parse(element.toString()).getAsJsonObject());
    }

    private Flux<ConsumerDmaapModel> create(Flux<JsonObject> jsonObject) {
        return jsonObject.flatMap(monoJsonP ->
            !containsHeader(monoJsonP) ? logErrorAndReturnMonoEmpty("Incorrect JsonObject - missing header")
                : transform(monoJsonP))
            .onErrorResume(exception -> exception instanceof DmaapNotFoundException, e -> Mono.empty());
    }

    private Mono<ConsumerDmaapModel> transform(JsonObject responseFromDmaap) {
        JsonObject commonEventHeader = responseFromDmaap.getAsJsonObject(EVENT)
            .getAsJsonObject(COMMON_EVENT_HEADER);
        JsonObject pnfRegistrationFields = responseFromDmaap.getAsJsonObject(EVENT)
            .getAsJsonObject(PNF_REGISTRATION_FIELDS);

        this.pnfSourceName = getValueFromJson(commonEventHeader, SOURCE_NAME);
        this.pnfNfRoleOptionalField = getValueFromJson(commonEventHeader, NF_ROLE);
        this.pnfOamIpv4Address = getValueFromJson(pnfRegistrationFields, OAM_IPV_4_ADDRESS);
        this.pnfOamIpv6Address = getValueFromJson(pnfRegistrationFields, OAM_IPV_6_ADDRESS);
        this.pnfSerialNumberOptionalField = getValueFromJson(pnfRegistrationFields, SERIAL_NUMBER);
        this.pnfEquipVendorOptionalField = getValueFromJson(pnfRegistrationFields, EQUIP_VENDOR);
        this.pnfEquipModelOptionalField = getValueFromJson(pnfRegistrationFields, EQUIP_MODEL);
        this.pnfEquipTypeOptionalField = getValueFromJson(pnfRegistrationFields, EQUIP_TYPE);
        this.pnfSwVersionOptionalField = getValueFromJson(pnfRegistrationFields, SW_VERSION);
        this.pnfAdditionalFields = pnfRegistrationFields.getAsJsonObject(ADDITIONAL_FIELDS);

        return (StringUtils.isEmpty(pnfSourceName))
            ? logErrorAndReturnMonoEmpty("Incorrect json, consumerDmaapModel can not be created: "
            + printMessage()) :
            Mono.just(ImmutableConsumerDmaapModel.builder()
                .correlationId(pnfSourceName)
                .ipv4(pnfOamIpv4Address)
                .ipv6(pnfOamIpv6Address)
                .serialNumber(pnfSerialNumberOptionalField)
                .equipVendor(pnfEquipVendorOptionalField)
                .equipModel(pnfEquipModelOptionalField)
                .equipType(pnfEquipTypeOptionalField)
                .nfRole(pnfNfRoleOptionalField)
                .swVersion(pnfSwVersionOptionalField)
                .additionalFields(pnfAdditionalFields).build());
    }

    private String getValueFromJson(JsonObject jsonObject, String jsonKey) {
        return jsonObject.has(jsonKey) ? jsonObject.get(jsonKey).getAsString() : "";
    }

    private boolean containsHeader(JsonObject jsonObject) {
        return jsonObject.has(EVENT) && jsonObject.getAsJsonObject(EVENT).has(PNF_REGISTRATION_FIELDS);
    }

    private String printMessage() {
        return String.format("%n{"
                + "\"" + CORRELATION_ID + COMMON_FORMAT_FOR_STRING + ","
                + "\"" + OAM_IPV_4_ADDRESS + COMMON_FORMAT_FOR_STRING + ","
                + "\"" + OAM_IPV_6_ADDRESS + COMMON_FORMAT_FOR_STRING + ","
                + "\"" + SERIAL_NUMBER + COMMON_FORMAT_FOR_STRING + ","
                + "\"" + EQUIP_VENDOR + COMMON_FORMAT_FOR_STRING + ","
                + "\"" + EQUIP_MODEL + COMMON_FORMAT_FOR_STRING + ","
                + "\"" + EQUIP_TYPE + COMMON_FORMAT_FOR_STRING + ","
                + "\"" + NF_ROLE + COMMON_FORMAT_FOR_STRING + ","
                + "\"" + SW_VERSION + COMMON_FORMAT_FOR_STRING + ","
                + "\"" + ADDITIONAL_FIELDS + COMMON_FORMAT_FOR_JSON_OBJECT
                + "%n}", this.pnfSourceName, this.pnfOamIpv4Address, this.pnfOamIpv6Address,
            this.pnfSerialNumberOptionalField, this.pnfEquipVendorOptionalField,
            this.pnfEquipModelOptionalField, this.pnfEquipTypeOptionalField,
            this.pnfNfRoleOptionalField, this.pnfSwVersionOptionalField, this.pnfAdditionalFields
        );
    }

    private <T> Mono<T> logErrorAndReturnMonoEmpty(String messageForLogger) {
        LOGGER.warn(messageForLogger);
        return Mono.empty();
    }
}
