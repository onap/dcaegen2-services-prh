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
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/8/18
 */
public class DmaapConsumerJsonParser {

    private static final String EVENT = "event";
    private static final String COMMON_EVENT_HEADER = "commonEventHeader";
    private static final String PNF_REGISTRATION_FIELDS = "pnfRegistrationFields";
    private static final String OAM_IPV_4_ADDRESS = "oamV4IpAddress";
    private static final String OAM_IPV_6_ADDRESS = "oamV6IpAddress";
    private static final String SOURCE_NAME = "sourceName";
    private static final String CORRELATION_ID = "correlationId";

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
        return StringUtils.isEmpty(message) ? Mono.error(new DmaapEmptyResponseException())
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
            !containsHeader(monoJsonP) ? Flux.error(new DmaapNotFoundException("Incorrect JsonObject - missing header"))
                : transform(monoJsonP))
            .onErrorResume(exception -> exception instanceof DmaapNotFoundException, e -> Mono.empty());
    }

    private Mono<ConsumerDmaapModel> transform(JsonObject responseFromDmaap) {

        JsonObject commonEventHeader = responseFromDmaap.getAsJsonObject(EVENT)
            .getAsJsonObject(COMMON_EVENT_HEADER);
        JsonObject pnfRegistrationFields = responseFromDmaap.getAsJsonObject(EVENT)
            .getAsJsonObject(PNF_REGISTRATION_FIELDS);

        String pnfSourceName = getValueFromJson(commonEventHeader, SOURCE_NAME);
        String pnfOamIpv4Address = getValueFromJson(pnfRegistrationFields, OAM_IPV_4_ADDRESS);
        String pnfOamIpv6Address = getValueFromJson(pnfRegistrationFields, OAM_IPV_6_ADDRESS);

        return (StringUtils.isEmpty(pnfSourceName) || !ipPropertiesNotEmpty(pnfOamIpv4Address, pnfOamIpv6Address))
            ? Mono.error(new DmaapNotFoundException("Incorrect json, consumerDmaapModel can not be created: "
            + printMessage(pnfSourceName, pnfOamIpv4Address, pnfOamIpv6Address))) :
            Mono.just(ImmutableConsumerDmaapModel.builder()
                .correlationId(pnfSourceName)
                .ipv4(pnfOamIpv4Address)
                .ipv6(pnfOamIpv6Address).build());
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

    private String printMessage(String sourceName, String oamIpv4Address, String oamIpv6Address) {
        return String.format("%n{"
            + "\"" + CORRELATION_ID + "\": \"%s\","
            + "\"" + OAM_IPV_4_ADDRESS + "\": \"%s\","
            + "\"" + OAM_IPV_6_ADDRESS + "\": \"%s\""
            + "%n}", sourceName, oamIpv4Address, oamIpv6Address);
    }
}
