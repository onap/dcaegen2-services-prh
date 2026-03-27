/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2023-2026 Deutsche Telekom Intellectual Property. All rights reserved.
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerPnfModel;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/8/18
 */
class KafkaConsumerJsonParserTest {

    @Test
    void whenPassingCorrectJson_validationNotThrowingAnException() {
        //given
        String message = "{\"event\": {"
            + "\"commonEventHeader\": { "
            + " \"sourceName\":\"NOKQTFCOC540002E\","
            + " \"nfNamingCode\":\"gNB\" "
            + "},"
            + "\"pnfRegistrationFields\": {"
            + " \"vendorName\": \"nokia\","
            + " \"serialNumber\": \"QTFCOC540002E\","
            + " \"pnfRegistrationFieldsVersion\": \"2.0\","
            + " \"modelNumber\": \"3310\","
            + " \"unitType\": \"type\",\n"
            + " \"unitFamily\": \"BBU\","
            + " \"oamV4IpAddress\": \"10.16.123.234\","
            + " \"softwareVersion\": \"v4.5.0.1\","
            + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\","
            + " \"additionalFields\": {\"attachmentPoint\":\"bla-bla-30-3\",\"cvlan\":\"678\",\"svlan\":\"1005\"}"
            + "}}}";

        JsonObject jsonObject = JsonParser.parseString("{\n"
            + "        \"attachmentPoint\": \"bla-bla-30-3\",\n"
            + "        \"cvlan\": \"678\",\n"
            + "        \"svlan\": \"1005\"\n"
            + "      }").getAsJsonObject();

        ConsumerPnfModel expectedObject = ImmutableConsumerPnfModel.builder()
            .ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .correlationId("NOKQTFCOC540002E")
            .serialNumber("QTFCOC540002E")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("gNB")
            .swVersion("v4.5.0.1")
            .additionalFields(jsonObject)
            .build();

        //when
        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();
        ConsumerPnfModel consumerPnfModel = kafkaConsumerJsonParser
            .getConsumerModelFromKafkaRecords(Collections.singletonList(message)).blockFirst();
        //then
        Assertions.assertNotNull(consumerPnfModel);
        Assertions.assertEquals(expectedObject, consumerPnfModel);
    }

    @Test
    void whenPassingJsonWithoutAdditionalFields_validationNotThrowingAnException() {
        //given
        String message = "{\"event\": {"
            + "\"commonEventHeader\": { "
            + " \"sourceName\":\"NOKQTFCOC540002E\","
            + " \"nfNamingCode\":\"gNB\" "
            + "},"
            + "\"pnfRegistrationFields\": {"
            + " \"vendorName\": \"nokia\","
            + " \"serialNumber\": \"QTFCOC540002E\","
            + " \"pnfRegistrationFieldsVersion\": \"2.0\","
            + " \"modelNumber\": \"3310\","
            + " \"unitType\": \"type\",\n"
            + " \"unitFamily\": \"BBU\","
            + " \"oamV4IpAddress\": \"10.16.123.234\","
            + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\","
            + " \"softwareVersion\": \"v4.5.0.1\""
            + "}}}";

        ConsumerPnfModel expectedObject = ImmutableConsumerPnfModel.builder()
            .correlationId("NOKQTFCOC540002E")
            .serialNumber("QTFCOC540002E")
            .ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("gNB")
            .swVersion("v4.5.0.1")
            .build();

        //when
        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();
        ConsumerPnfModel consumerPnfModel = kafkaConsumerJsonParser
            .getConsumerModelFromKafkaRecords(Collections.singletonList(message)).blockFirst();
        //then
        Assertions.assertNotNull(consumerPnfModel);
        Assertions.assertEquals(expectedObject, consumerPnfModel);
    }

    @Test
    void whenPassingJsonWithEmptyAdditionalFields_validationNotThrowingAnException() {
        //given
        String message = "{\"event\": {"
            + "\"commonEventHeader\": { "
            + " \"sourceName\":\"NOKQTFCOC540002E\","
            + " \"nfNamingCode\":\"gNB\" "
            + "},"
            + "\"pnfRegistrationFields\": {"
            + " \"vendorName\": \"nokia\","
            + " \"serialNumber\": \"QTFCOC540002E\","
            + " \"pnfRegistrationFieldsVersion\": \"2.0\","
            + " \"modelNumber\": \"3310\","
            + " \"unitType\": \"type\",\n"
            + " \"unitFamily\": \"BBU\","
            + " \"softwareVersion\": \"v4.5.0.1\","
            + " \"oamV4IpAddress\": \"10.16.123.234\","
            + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\","
            + " \"additionalFields\": {}"
            + "}}}";

        ConsumerPnfModel expectedObject = ImmutableConsumerPnfModel.builder()
            .correlationId("NOKQTFCOC540002E")
            .serialNumber("QTFCOC540002E")
            .ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("gNB")
            .swVersion("v4.5.0.1")
            .additionalFields(new JsonObject())
            .build();

        //when
        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();
        ConsumerPnfModel consumerPnfModel = kafkaConsumerJsonParser
            .getConsumerModelFromKafkaRecords(Collections.singletonList(message)).blockFirst();
        //then
        Assertions.assertNotNull(consumerPnfModel);
        Assertions.assertEquals(expectedObject, consumerPnfModel);
    }

    @Test
    void whenPassingCorrectJsonWithoutIpv4andIpv6_validationNotThrowingAnException() {
        //given
        String message = "{\"event\": {"
            + "\"commonEventHeader\": { "
            + " \"sourceName\":\"NOKQTFCOC540002E\","
            + " \"nfNamingCode\":\"gNB\" "
            + "},"
            + "\"pnfRegistrationFields\": {"
            + " \"vendorName\": \"nokia\","
            + " \"serialNumber\": \"QTFCOC540002E\","
            + " \"pnfRegistrationFieldsVersion\": \"2.0\","
            + " \"modelNumber\": \"3310\","
            + " \"unitType\": \"type\",\n"
            + " \"unitFamily\": \"BBU\","
            + " \"softwareVersion\": \"v4.5.0.1\","
            + " \"additionalFields\": {}"
            + "}}}";

        ConsumerPnfModel expectedObject = ImmutableConsumerPnfModel.builder()
            .correlationId("NOKQTFCOC540002E")
            .serialNumber("QTFCOC540002E")
            .ipv4("")
            .ipv6("")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("gNB")
            .swVersion("v4.5.0.1")
            .additionalFields(new JsonObject())
            .build();

        //when
        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();
        ConsumerPnfModel consumerPnfModel = kafkaConsumerJsonParser
            .getConsumerModelFromKafkaRecords(Collections.singletonList(message)).blockFirst();
        //then
        Assertions.assertNotNull(consumerPnfModel);
        Assertions.assertEquals(expectedObject, consumerPnfModel);
    }


    @Test
    void whenPassingJsonWithoutMandatoryHeaderInformation_validationAddingAnException() {
        String incorrectMessage = "{\"event\": {"
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
            + " \"oamV4IpAddress\": \"10.16.123.234\","
            + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\","
            + " \"additionalFields\": {}"
            + "}}}";

        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();

        StepVerifier.create(kafkaConsumerJsonParser.getConsumerModelFromKafkaRecords(Collections.singletonList(incorrectMessage)))
            .expectSubscription().thenRequest(1).verifyComplete();
    }

    @Test
    void whenPassingJsonWithoutSourceName_validationAddingAnException() {
        String jsonWithoutSourceName =
            "{\"event\": {"
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
                + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\","
                + " \"additionalFields\": {}"
                + "}}}";

        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();

        StepVerifier
            .create(kafkaConsumerJsonParser.getConsumerModelFromKafkaRecords(Collections.singletonList(jsonWithoutSourceName)))
            .expectSubscription().thenRequest(1)
            .verifyComplete();
    }

    @Test
    void whenPassingJsonWithoutSourceNameValue_validationAddingAnException() {
        String jsonWithoutIpInformation =
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
                + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\","
                + " \"additionalFields\": {}"
                + "}}}";

        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();

        StepVerifier.create(kafkaConsumerJsonParser.getConsumerModelFromKafkaRecords(Collections.singletonList(jsonWithoutIpInformation)))
            .expectSubscription().thenRequest(1).verifyComplete();
    }

    @Test
    void whenPassingCorrectJsonWithoutIpv4_validationNotThrowingAnException() {
        //given
        String message = "{\"event\": {"
            + "\"commonEventHeader\": { "
            + " \"sourceName\":\"NOKQTFCOC540002E\","
            + " \"nfNamingCode\":\"gNB\" "
            + "},"
            + "\"pnfRegistrationFields\": {"
            + " \"vendorName\": \"nokia\","
            + " \"serialNumber\": \"QTFCOC540002E\","
            + " \"pnfRegistrationFieldsVersion\": \"2.0\","
            + " \"modelNumber\": \"3310\","
            + " \"unitType\": \"type\",\n"
            + " \"unitFamily\": \"BBU\","
            + " \"softwareVersion\": \"v4.5.0.1\","
            + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\""
            + "}}}";

        //when
        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();
        ConsumerPnfModel consumerPnfModel = kafkaConsumerJsonParser
            .getConsumerModelFromKafkaRecords(Collections.singletonList(message)).blockFirst();
        //then
        ConsumerPnfModel expectedObject = ImmutableConsumerPnfModel.builder()
            .ipv4("")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .correlationId("NOKQTFCOC540002E")
            .serialNumber("QTFCOC540002E")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("gNB")
            .swVersion("v4.5.0.1")
            .build();
        Assertions.assertNotNull(consumerPnfModel);
        Assertions.assertEquals(expectedObject, consumerPnfModel);
    }

    @Test
    void whenPassingCorrectJsonWithoutIpv6_validationNotThrowingAnException() {
        //given
        String message = "{\"event\": {"
            + "\"commonEventHeader\": { "
            + " \"sourceName\":\"NOKQTFCOC540002E\","
            + " \"nfNamingCode\":\"gNB\" "
            + "},"
            + "\"pnfRegistrationFields\": {"
            + " \"vendorName\": \"nokia\","
            + " \"serialNumber\": \"QTFCOC540002E\","
            + " \"pnfRegistrationFieldsVersion\": \"2.0\","
            + " \"modelNumber\": \"3310\","
            + " \"unitType\": \"type\",\n"
            + " \"unitFamily\": \"BBU\","
            + " \"oamV4IpAddress\": \"10.16.123.234\","
            + " \"softwareVersion\": \"v4.5.0.1\""
            + "}}}";

        ConsumerPnfModel expectedObject = ImmutableConsumerPnfModel.builder()
            .ipv4("10.16.123.234")
            .ipv6("")
            .correlationId("NOKQTFCOC540002E")
            .serialNumber("QTFCOC540002E")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("gNB")
            .swVersion("v4.5.0.1")
            .build();

        //when
        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();
        ConsumerPnfModel consumerPnfModel = kafkaConsumerJsonParser
            .getConsumerModelFromKafkaRecords(Collections.singletonList(message)).blockFirst();

        //then
        Assertions.assertNotNull(consumerPnfModel);
        Assertions.assertEquals(expectedObject, consumerPnfModel);
    }

    @Test
    void whenPassingCorrectJsonArrayWithoutIpv4_validationNotThrowingAnException() {
        //given
        String message = "{\"event\": {"
            + "\"commonEventHeader\": { "
            + "  \"sourceName\":\"NOKQTFCOC540002E\","
            + "  \"nfNamingCode\":\"gNB\" "
            + "  },"
            + "\"pnfRegistrationFields\": {"
            + " \"vendorName\": \"nokia\","
            + " \"serialNumber\": \"QTFCOC540002E\","
            + " \"pnfRegistrationFieldsVersion\": \"2.0\","
            + " \"modelNumber\": \"3310\","
            + " \"unitType\": \"type\",\n"
            + " \"unitFamily\": \"BBU\","
            + " \"softwareVersion\": \"v4.5.0.1\","
            + " \"oamV6IpAddress\": \"0:0:0:0:0:FFFF:0A10:7BEA\","
            + " \"additionalFields\": {}"
            + "}}}";

        ConsumerPnfModel expectedObject = ImmutableConsumerPnfModel.builder()
            .ipv4("")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .correlationId("NOKQTFCOC540002E")
            .serialNumber("QTFCOC540002E")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("gNB")
            .swVersion("v4.5.0.1")
            .additionalFields(new JsonObject())
            .build();

        //when
        KafkaConsumerJsonParser kafkaConsumerJsonParser = new KafkaConsumerJsonParser();

        //then
        StepVerifier.create(kafkaConsumerJsonParser.getConsumerModelFromKafkaRecords(Arrays.asList(message, message)))
            .expectSubscription().expectNext(expectedObject).expectNext(expectedObject).verifyComplete();
    }
}
