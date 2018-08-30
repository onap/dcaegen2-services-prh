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
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.Mockito.spy;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/8/18
 */
class DmaapConsumerJsonParserTest {

    @Test
    void whenPassingCorrectJson_validationNotThrowingAnException() {
        //given
        String message = "[{\"event\": {"
                + "\"commonEventHeader\": { \"sourceName\":\"NOKQTFCOC540002E\"},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"oamV4IpAddress\": \"10.16.123.234\","
                + " \"softwareVersion\": \"v4.5.0.1\","
                + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\""
                + "}}}]";

        String parsed = "{\"event\": {"
                + "\"commonEventHeader\": { \"sourceName\":\"NOKQTFCOC540002E\"},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"oamV4IpAddress\": \"10.16.123.234\","
                + " \"softwareVersion\": \"v4.5.0.1\","
                + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\""
                + "}}}";

        ConsumerDmaapModel expectedObject = ImmutableConsumerDmaapModel.builder().ipv4("10.16.123.234")
                .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
                .sourceName("NOKQTFCOC540002E").build();
        //when
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsed);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(dmaapConsumerJsonParser).getJsonObjectFromAnArray(jsonElement);
        ConsumerDmaapModel consumerDmaapModel = dmaapConsumerJsonParser
                .getJsonObject(Mono.just((message))).block();
        //then
        Assertions.assertNotNull(consumerDmaapModel);
        Assertions.assertEquals(expectedObject, consumerDmaapModel);
    }

    @Test
    void whenPassingCorrectJsonWithoutIpv4_validationNotThrowingAnException() {
        //given
        String message = "[{\"event\": {"
                + "\"commonEventHeader\": { \"sourceName\":\"NOKQTFCOC540002E\"},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"softwareVersion\": \"v4.5.0.1\","
                + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\""
                + "}}}]";

        String parsed = "{\"event\": {"
                + "\"commonEventHeader\": { \"sourceName\":\"NOKQTFCOC540002E\"},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"softwareVersion\": \"v4.5.0.1\","
                + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\""
                + "}}}";

        //when
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsed);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(dmaapConsumerJsonParser).getJsonObjectFromAnArray(jsonElement);
        dmaapConsumerJsonParser.getJsonObject(Mono.just((message)));
        ConsumerDmaapModel consumerDmaapModel = dmaapConsumerJsonParser.getJsonObject(Mono.just((message)))
                .block();
        //then
        ConsumerDmaapModel expectedObject = ImmutableConsumerDmaapModel.builder().ipv4("")
                .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
                .sourceName("NOKQTFCOC540002E").build();
        Assertions.assertNotNull(consumerDmaapModel);
        Assertions.assertEquals(expectedObject, consumerDmaapModel);
    }

    @Test
    void whenPassingCorrectJsonWithoutIpv6_validationNotThrowingAnException() {
        //given
        String message = "[{\"event\": {"
                + "\"commonEventHeader\": { \"sourceName\":\"NOKQTFCOC540002E\"},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"oamV4IpAddress\": \"10.16.123.234\","
                + " \"softwareVersion\": \"v4.5.0.1\""
                + "}}}]";

        String parsed = "{\"event\": {"
                + "\"commonEventHeader\": { \"sourceName\":\"NOKQTFCOC540002E\"},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"oamV4IpAddress\": \"10.16.123.234\","
                + " \"softwareVersion\": \"v4.5.0.1\""
                + "}}}";

        ConsumerDmaapModel expectedObject = ImmutableConsumerDmaapModel.builder().ipv4("10.16.123.234").ipv6("")
                .sourceName("NOKQTFCOC540002E").build();
        //when
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsed);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(dmaapConsumerJsonParser).getJsonObjectFromAnArray(jsonElement);
        ConsumerDmaapModel consumerDmaapModel = dmaapConsumerJsonParser.getJsonObject(Mono.just((message)))
                .block();
        //then
        Assertions.assertNotNull(consumerDmaapModel);
        Assertions.assertEquals(expectedObject, consumerDmaapModel);
    }

    @Test
    void whenPassingCorrectJsonWithoutIpv4andIpv6_validationThrowingAnException() {
        //given
        String message = "[{\"event\": {"
                + "\"commonEventHeader\": { \"sourceName\":\"NOKQTFCOC540002E\"},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"softwareVersion\": \"v4.5.0.1\""
                + "}}}]";

        String parsed = "{\"event\": {"
                + "\"commonEventHeader\": { \"sourceName\":\"NOKQTFCOC540002E\"},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"softwareVersion\": \"v4.5.0.1\""
                + "}}}";

        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsed);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(dmaapConsumerJsonParser).getJsonObjectFromAnArray(jsonElement);
        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(message)))
                .expectSubscription().expectError(DmaapNotFoundException.class).verify();

    }

    @Test
    void whenPassingJsonWithoutMandatoryHeaderInformation_validationThrowingAnException() {
        String parsed = "{\"event\": {"
                + "\"commonEventHeader\": {},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"softwareVersion\": \"v4.5.0.1\""
                + "}}}";

        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsed);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(dmaapConsumerJsonParser).getJsonObjectFromAnArray(jsonElement);
        String incorrectMessage = "[{\"event\": {"
                + "\"commonEventHeader\": {},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"softwareVersion\": \"v4.5.0.1\""
                + "}}}]";
        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(incorrectMessage)))
                .expectSubscription().expectError(DmaapNotFoundException.class).verify();
    }

    @Test
    void whenPassingJsonWithoutSourceName_validationThrowingAnException() {
        String parsed = "{\"event\": {"
                + "\"commonEventHeader\": {},"
                + "\"pnfRegistrationFields\": {"
                + " \"unitType\": \"AirScale\","
                + " \"serialNumber\": \"QTFCOC540002E\","
                + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                + " \"manufactureDate\": \"1535014037024\","
                + " \"modelNumber\": \"7BEA\",\n"
                + " \"lastServiceDate\": \"1535014037024\","
                + " \"unitFamily\": \"BBU\","
                + " \"vendorName\": \"Nokia\","
                + " \"softwareVersion\": \"v4.5.0.1\","
                + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\""
                + "}}}";

        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsed);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(dmaapConsumerJsonParser).getJsonObjectFromAnArray(jsonElement);
        String jsonWithoutSourceName =
                "[{\"event\": {"
                        + "\"commonEventHeader\": {},"
                        + "\"pnfRegistrationFields\": {"
                        + " \"unitType\": \"AirScale\","
                        + " \"serialNumber\": \"QTFCOC540002E\","
                        + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                        + " \"manufactureDate\": \"1535014037024\","
                        + " \"modelNumber\": \"7BEA\",\n"
                        + " \"lastServiceDate\": \"1535014037024\","
                        + " \"unitFamily\": \"BBU\","
                        + " \"vendorName\": \"Nokia\","
                        + " \"softwareVersion\": \"v4.5.0.1\","
                        + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\""
                        + "}}}]";
        StepVerifier
                .create(dmaapConsumerJsonParser.getJsonObject(Mono.just(jsonWithoutSourceName)))
                .expectSubscription().expectError(DmaapNotFoundException.class).verify();
    }

    @Test
    void whenPassingJsonWithoutIpInformation_validationThrowingAnException() {
        String parsed =
                "{\"event\": {"
                        + "\"commonEventHeader\": {\"sourceName\": \"NOKQTFCOC540002E\"},"
                        + "\"pnfRegistrationFields\": {"
                        + " \"unitType\": \"AirScale\","
                        + " \"serialNumber\": \"QTFCOC540002E\","
                        + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                        + " \"manufactureDate\": \"1535014037024\","
                        + " \"modelNumber\": \"7BEA\",\n"
                        + " \"lastServiceDate\": \"1535014037024\","
                        + " \"unitFamily\": \"BBU\","
                        + " \"vendorName\": \"Nokia\","
                        + " \"softwareVersion\": \"v4.5.0.1\","
                        + " \"oamV4IpAddress\": \"\","
                        + " \"oamV6IpAddress\": \"\""
                        + "}}}";

        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsed);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(dmaapConsumerJsonParser).getJsonObjectFromAnArray(jsonElement);
        String jsonWithoutIpInformation =
                "[{\"event\": {"
                        + "\"commonEventHeader\": {\"sourceName\": \"NOKQTFCOC540002E\"},"
                        + "\"pnfRegistrationFields\": {"
                        + " \"unitType\": \"AirScale\","
                        + " \"serialNumber\": \"QTFCOC540002E\","
                        + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                        + " \"manufactureDate\": \"1535014037024\","
                        + " \"modelNumber\": \"7BEA\",\n"
                        + " \"lastServiceDate\": \"1535014037024\","
                        + " \"unitFamily\": \"BBU\","
                        + " \"vendorName\": \"Nokia\","
                        + " \"softwareVersion\": \"v4.5.0.1\","
                        + " \"oamV4IpAddress\": \"\","
                        + " \"oamV6IpAddress\": \"\""
                        + "}}}]";
        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(jsonWithoutIpInformation)))
                .expectSubscription().expectError(DmaapNotFoundException.class).verify();
    }

    @Test
    void whenPassingJsonWithoutSourceNameValue_validationThrowingAnException() {
        String parsed =
                "{\"event\": {"
                        + "\"commonEventHeader\": {\"sourceName\": \"\"},"
                        + "\"pnfRegistrationFields\": {"
                        + " \"unitType\": \"AirScale\","
                        + " \"serialNumber\": \"QTFCOC540002E\","
                        + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                        + " \"manufactureDate\": \"1535014037024\","
                        + " \"modelNumber\": \"7BEA\",\n"
                        + " \"lastServiceDate\": \"1535014037024\","
                        + " \"unitFamily\": \"BBU\","
                        + " \"vendorName\": \"Nokia\","
                        + " \"softwareVersion\": \"v4.5.0.1\","
                        + " \"oamV4IpAddress\": \"10.16.123.234\","
                        + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\""
                        + "}}}";

        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsed);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject()))
                .when(dmaapConsumerJsonParser).getJsonObjectFromAnArray(jsonElement);
        String jsonWithoutIpInformation =
                "[{\"event\": {"
                        + "\"commonEventHeader\": {\"sourceName\": \"\"},"
                        + "\"pnfRegistrationFields\": {"
                        + " \"unitType\": \"AirScale\","
                        + " \"serialNumber\": \"QTFCOC540002E\","
                        + " \"pnfRegistrationFieldsVersion\": \"2.0\","
                        + " \"manufactureDate\": \"1535014037024\","
                        + " \"modelNumber\": \"7BEA\",\n"
                        + " \"lastServiceDate\": \"1535014037024\","
                        + " \"unitFamily\": \"BBU\","
                        + " \"vendorName\": \"Nokia\","
                        + " \"softwareVersion\": \"v4.5.0.1\","
                        + " \"oamV4IpAddress\": \"10.16.123.234\","
                        + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\""
                        + "}}}]";
        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(jsonWithoutIpInformation)))
                .expectSubscription().expectError(DmaapNotFoundException.class).verify();
    }
}
