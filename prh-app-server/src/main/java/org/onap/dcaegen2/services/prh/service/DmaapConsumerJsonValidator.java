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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapterFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ServiceLoader;
import org.onap.dcaegen2.services.prh.configuration.PrhAppConfig;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/8/18
 */
public class DmaapConsumerJsonValidator {

    private static final String EVENT = "event";
    private static final String OTHER_FIELDS = "otherFields";
    private static final String PNF_OAM_IPV_4_ADDRESS = "pnfOamIpv4Address";
    private static final String PNF_OAM_IPV_6_ADDRESS = "pnfOamIpv6Address";
    private static final String PNF_VENDOR_NAME = "pnfVendorName";
    private static final String PNF_SERIAL_NUMBER = "pnfSerialNumber";
    private static final String INCORRECT_JSON_OBJECT = "Incorrect JsonObject";
    private static final String PNF_OAM_IPV4_ADDRESS_PNF_OAM_IPV6_ADDRESS_FORMAT = "pnfOamIpv4Address: %s, pnfOamIpv6Address: %s";
    private static final String INCORRECT_DMAAP_CONSUMER_JSON_FORMAT_TEXT = "Incorrect DmaapConsumer json format";

    private static final Logger logger = LoggerFactory.getLogger(PrhAppConfig.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String DMAAP_CONSUMER_LOG_ERROR_MESSAGE = "DmaapConsumerJsonValidator validate()::DmaapNotFoundException :: {}:{}";

    private DmaapConsumerJsonValidator() {
    }

    public static String validate(String message) throws DmaapNotFoundException {
        logger.debug("Start DmaapConsumerJsonValidator::validate() :: {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        JsonElement jsonElement = new JsonParser().parse(message);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return Optional.of(create(jsonObject, gsonBuilder))
            .orElseThrow(() -> {
                logger
                    .error(
                        DMAAP_CONSUMER_LOG_ERROR_MESSAGE,
                        dateTimeFormatter.format(
                            LocalDateTime.now()),
                        new DmaapNotFoundException(INCORRECT_DMAAP_CONSUMER_JSON_FORMAT_TEXT));
                return new DmaapNotFoundException(INCORRECT_DMAAP_CONSUMER_JSON_FORMAT_TEXT);
            });
    }


    private static String create(JsonObject jsonObject, GsonBuilder gsonBuilder) throws DmaapNotFoundException {
        logger.debug("Start DmaapConsumerJsonValidator::create() :: {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        if (checkHeaderPresenceInJson(jsonObject)) {
            jsonObject = jsonObject.getAsJsonObject(EVENT).getAsJsonObject(OTHER_FIELDS);
            if ((jsonObject.has(PNF_VENDOR_NAME) || jsonObject.has(PNF_SERIAL_NUMBER))) {

                String pnfVendorName = jsonObject.get(PNF_VENDOR_NAME).getAsString();
                String pnfSerialNumber = jsonObject.get(PNF_SERIAL_NUMBER).getAsString();
                String pnfOamIpv4Address = "";
                String pnfOamIpv6Address = "";

                if (jsonObject.has(PNF_OAM_IPV_6_ADDRESS) || jsonObject.has(PNF_OAM_IPV_4_ADDRESS)) {
                    pnfOamIpv4Address = jsonObject.get(PNF_OAM_IPV_4_ADDRESS).getAsString();
                    pnfOamIpv6Address = jsonObject.get(PNF_OAM_IPV_6_ADDRESS).getAsString();
                }
                String correlationID = pnfVendorName.substring(0, Math.min(pnfVendorName.length(), 3)).toUpperCase()
                    .concat(pnfSerialNumber);
                if ((pnfOamIpv4Address != null && !pnfOamIpv4Address.isEmpty()) || (pnfOamIpv6Address != null
                    && !pnfOamIpv6Address.isEmpty())) {
                    logger.debug("End DmaapConsumerJsonValidator::create() :: {}",
                        dateTimeFormatter.format(
                            LocalDateTime.now()));
                    return gsonBuilder.create()
                        .toJson(ImmutableConsumerDmaapModel.builder().pnfName(correlationID)
                            .ipv4(pnfOamIpv4Address).ipv6(pnfOamIpv6Address).build());
                }
                logger
                    .error(
                        DMAAP_CONSUMER_LOG_ERROR_MESSAGE,
                        dateTimeFormatter.format(
                            LocalDateTime.now()),
                        new DmaapNotFoundException(
                            String
                                .format(PNF_OAM_IPV4_ADDRESS_PNF_OAM_IPV6_ADDRESS_FORMAT, pnfOamIpv4Address,
                                    pnfOamIpv6Address)));
                throw new DmaapNotFoundException(
                    String
                        .format(PNF_OAM_IPV4_ADDRESS_PNF_OAM_IPV6_ADDRESS_FORMAT, pnfOamIpv4Address,
                            pnfOamIpv6Address));
            } else {
                logger
                    .error(
                        DMAAP_CONSUMER_LOG_ERROR_MESSAGE,
                        dateTimeFormatter.format(
                            LocalDateTime.now()),
                        new DmaapNotFoundException(INCORRECT_JSON_OBJECT));
                throw new DmaapNotFoundException(INCORRECT_JSON_OBJECT);
            }
        }
        logger
            .error(
                DMAAP_CONSUMER_LOG_ERROR_MESSAGE,
                dateTimeFormatter.format(
                    LocalDateTime.now()),
                new DmaapNotFoundException(INCORRECT_JSON_OBJECT));
        throw new DmaapNotFoundException(INCORRECT_JSON_OBJECT);
    }

    private static boolean checkHeaderPresenceInJson(JsonObject jsonObject) {
        return jsonObject.has(EVENT) && jsonObject.getAsJsonObject(EVENT).has(OTHER_FIELDS);
    }

}
