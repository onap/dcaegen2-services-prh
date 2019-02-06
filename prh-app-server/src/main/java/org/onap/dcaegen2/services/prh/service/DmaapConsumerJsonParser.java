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
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.COMMON_EVENT_HEADER;
import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.COMMON_FORMAT;
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
public class DmaapConsumerJsonParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapConsumerJsonParser.class);

    private String pnfSourceName;
    private String pnfOamIpv4Address;
    private String pnfOamIpv6Address;
    private String pnfSerialNumberAdditionalField;
    private String pnfEquipVendorAdditionalField;
    private String pnfEquipModelAdditionalField;
    private String pnfEquipTypeAdditionalField;
    private String pnfNfRoleAdditionalField;
    private String pnfSwVersionAdditionalField;

    /**
     * Extract info from string and create @see {@link org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel}.
     *
     * @param monoMessage - results from DMaaP
     * @return reactive DMaaPModel
     */
    public Flux<ConsumerDmaapModel> getJsonObject(Mono<String> monoMessage) {
        return monoMessage
                .flatMapMany(this::getJsonParserMessage)
                .flatMap(this::createJsonConsumerModel);
    }

    private Mono<JsonElement> getJsonParserMessage(String message) {
        return StringUtils.isEmpty(message) ? logErrorAndReturnMonoEmpty("DmaaP response is empty")
                : Mono.fromCallable(() -> new JsonParser().parse(message));
    }

    private Flux<ConsumerDmaapModel> createJsonConsumerModel(JsonElement jsonElement) {
        return jsonElement.isJsonObject()
                ? create(Flux.defer(() -> Flux.just(jsonElement.getAsJsonObject())))
                : getConsumerDmaapModelFromJsonArray(jsonElement);
    }

    private Flux<ConsumerDmaapModel> getConsumerDmaapModelFromJsonArray(JsonElement jsonElement) {
        return create(
                Flux.defer(() -> Flux.fromStream(StreamSupport.stream(jsonElement.getAsJsonArray().spliterator(), false)
                        .map(jsonElementFromArray -> getJsonObjectFromAnArray(jsonElementFromArray)
                                .orElseGet(JsonObject::new)))));
    }

    public Optional<JsonObject> getJsonObjectFromAnArray(JsonElement element) {
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
        this.pnfOamIpv4Address = getValueFromJson(pnfRegistrationFields, OAM_IPV_4_ADDRESS);
        this.pnfOamIpv6Address = getValueFromJson(pnfRegistrationFields, OAM_IPV_6_ADDRESS);
        this.pnfSerialNumberAdditionalField = getValueFromJson(pnfRegistrationFields, SERIAL_NUMBER);
        this.pnfEquipVendorAdditionalField = getValueFromJson(pnfRegistrationFields, EQUIP_VENDOR);
        this.pnfEquipModelAdditionalField = getValueFromJson(pnfRegistrationFields, EQUIP_MODEL);
        this.pnfEquipTypeAdditionalField = getValueFromJson(pnfRegistrationFields, EQUIP_TYPE);
        this.pnfNfRoleAdditionalField = getValueFromJson(pnfRegistrationFields, NF_ROLE);
        this.pnfSwVersionAdditionalField = getValueFromJson(pnfRegistrationFields, SW_VERSION);

        return (StringUtils.isEmpty(pnfSourceName) || !ipPropertiesNotEmpty(pnfOamIpv4Address, pnfOamIpv6Address))
                ? logErrorAndReturnMonoEmpty("Incorrect json, consumerDmaapModel can not be created: "
                + printMessage()) :
                Mono.just(ImmutableConsumerDmaapModel.builder()
                        .correlationId(pnfSourceName)
                        .ipv4(pnfOamIpv4Address)
                        .ipv6(pnfOamIpv6Address)
                        .serialNumber(pnfSerialNumberAdditionalField)
                        .equipVendor(pnfEquipVendorAdditionalField)
                        .equipModel(pnfEquipModelAdditionalField)
                        .equipType(pnfEquipTypeAdditionalField)
                        .nfRole(pnfNfRoleAdditionalField)
                        .swVersion(pnfSwVersionAdditionalField).build());
    }

    private String getValueFromJson(JsonObject jsonObject, String jsonKey) {
        return jsonObject.has(jsonKey) ? jsonObject.get(jsonKey).getAsString() : "";
    }

    private boolean ipPropertiesNotEmpty(String ipv4, String ipv6) {
        return (!StringUtils.isEmpty(ipv4)) || !(StringUtils.isEmpty(ipv6));
    }

    private boolean containsHeader(JsonObject jsonObject) {
        return jsonObject.has(EVENT) && jsonObject.getAsJsonObject(EVENT).has(PNF_REGISTRATION_FIELDS);
    }

    private String printMessage() {
        return String.format("%n{"
                        + "\"" + CORRELATION_ID + COMMON_FORMAT + ","
                        + "\"" + OAM_IPV_4_ADDRESS + COMMON_FORMAT + ","
                        + "\"" + OAM_IPV_6_ADDRESS + COMMON_FORMAT + ","
                        + "\"" + SERIAL_NUMBER + COMMON_FORMAT + ","
                        + "\"" + EQUIP_VENDOR + COMMON_FORMAT + ","
                        + "\"" + EQUIP_MODEL + COMMON_FORMAT + ","
                        + "\"" + EQUIP_TYPE + COMMON_FORMAT + ","
                        + "\"" + NF_ROLE + COMMON_FORMAT + ","
                        + "\"" + SW_VERSION + COMMON_FORMAT
                        + "%n}", this.pnfSourceName, this.pnfOamIpv4Address, this.pnfOamIpv6Address,
                this.pnfSerialNumberAdditionalField, this.pnfEquipVendorAdditionalField,
                this.pnfEquipModelAdditionalField, this.pnfEquipTypeAdditionalField,
                this.pnfNfRoleAdditionalField, this.pnfSwVersionAdditionalField
        );
    }

    private <T> Mono<T> logErrorAndReturnMonoEmpty(String messageForLogger) {
        LOGGER.warn(messageForLogger);
        return Mono.empty();
    }
}
