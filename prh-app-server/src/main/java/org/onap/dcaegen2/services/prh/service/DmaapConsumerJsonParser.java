/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * Modification Copyright (C) 2023 Deutsche Telekom Intellectual Property.All rights reserved.
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
import io.vavr.collection.List;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.onap.dcaegen2.services.prh.service.PnfRegistrationFields.*;



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

    private String sourceName;

    /**
     * Extract info from string and create @see {@link ConsumerDmaapModel}.
     *
     * @param monoMessage - results from DMaaP
     * @return reactive DMaaPModel
     */
    public Flux<ConsumerDmaapModel> getJsonObject(Mono<MessageRouterSubscribeResponse> monoMessage) {
        return monoMessage.flatMapMany(msgRouterResponse -> getConsumerDmaapModelFromJsonArray(msgRouterResponse.items()));
    }

    public JSONObject getJsonObjectKafka(String jsonStr) throws JSONException {
        return new JSONObject(jsonStr);
    }


    private Flux<ConsumerDmaapModel> getConsumerDmaapModelFromJsonArray(List<JsonElement> items) {
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

    /**
     * Extract info from string and create @see {@link ConsumerDmaapModel}.
     *
     * @param monoMessage - results from Kafka
     * @return reactive DMaaPModel
     */

    public Flux<ConsumerDmaapModel> getConsumerDmaapModelFromKafkaConsumerRecord(java.util.List<String> items)
    {
        LOGGER.info("DmaapConsumerJsonParser input for parsing: {} with commit", items);
        if (items.size() == 0) {
            LOGGER.info("Nothing to consume from Kafka");
            return Flux.empty();
        }
       return create(
                Flux.defer(() -> Flux.fromStream(StreamSupport.stream(items.spliterator(), false)
                        .map(jsonObjectFromString -> getJsonObjectFromString(jsonObjectFromString)
                                .orElseGet(JsonObject::new)))));
    }

    Optional<JsonObject> getJsonObjectFromString(String element) {
        return Optional.ofNullable(JsonParser.parseString(element).getAsJsonObject());
    }

    public String getSourceName() {
        return sourceName;
    }

    Optional<JsonObject> getJsonObjectFromAnArray(JsonElement element) {
        JsonParser jsonParser = new JsonParser();
        return element.isJsonPrimitive() ? Optional.of(jsonParser.parse(element.getAsString()).getAsJsonObject())
                : Optional.of(jsonParser.parse(element.toString()).getAsJsonObject());
    }

    Optional<JsonObject> getJsonObjectFromKafkaRecords(String element) {
        return Optional.ofNullable(new JsonObject().getAsJsonObject(element));
    }


    private Flux<ConsumerDmaapModel> create(Flux<JsonObject> jsonObject) {
        return jsonObject.flatMap(monoJsonP -> !containsHeader(monoJsonP) ? logErrorAndReturnMonoEmpty("Incorrect JsonObject - missing header")
                            : transform(monoJsonP));
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
        try {
            return jsonObject.has(EVENT) && jsonObject.getAsJsonObject(EVENT).has(PNF_REGISTRATION_FIELDS);
        }catch(Exception e){
            LOGGER.info("Fetching an error in containsHeader method {}",e.getMessage());
        }
        return false;
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
        LOGGER.info(messageForLogger);
        return Mono.empty();
    }

    public JSONArray getJsonArray(String value) throws JSONException {
        return new JSONArray(value);
    }
}