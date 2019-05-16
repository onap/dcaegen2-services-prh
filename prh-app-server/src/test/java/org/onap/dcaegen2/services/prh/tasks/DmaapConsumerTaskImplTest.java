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

package org.onap.dcaegen2.services.prh.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DmaapClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.onap.dcaegen2.services.prh.TestAppConfiguration.createDefaultMessageRouterSubscribeRequest;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
class DmaapConsumerTaskImplTest {

    private static ConsumerDmaapModel consumerDmaapModel;
    private static DmaapConsumerTaskImpl dmaapConsumerTask;
    private static MessageRouterSubscribeRequest messageRouterSubscribeRequest;
    private static String message;
    private static String messageContentEmpty;
    private static JsonArray jsonArray;
    private static JsonArray jsonArrayWrongContent;
    private static MessageRouterSubscribeResponse messageRouterSubscribeResponse;
    private static CbsConfiguration cbsConfiguration;

    @BeforeAll
    static void setUp() {
        messageRouterSubscribeRequest = createDefaultMessageRouterSubscribeRequest();

        JsonObject jsonObject = new JsonParser().parse("{\n"
            + "        \"attachmentPoint\": \"bla-bla-30-3\",\n"
            + "        \"cvlan\": \"678\",\n"
            + "        \"svlan\": \"1005\"\n"
            + "      }").getAsJsonObject();

        consumerDmaapModel = ImmutableConsumerDmaapModel.builder()
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
        cbsConfiguration = mock(CbsConfiguration.class);

        message = "[{\"event\": {"
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
            + " \"additionalFields\": {\"attachmentPoint\": \"bla-bla-30-3\",\"cvlan\": \"678\",\"svlan\": \"1005\"}"
            + "}}}]";

        messageContentEmpty = "[]";
        JsonParser jsonParser = new JsonParser();
        jsonArray = (JsonArray) jsonParser.parse(message);
        jsonArrayWrongContent = (JsonArray) jsonParser.parse(messageContentEmpty);

    }

    @Test
    void whenPassedObjectDoesNotFit_DoesNotThrowPrhTaskException() throws Exception {
        //given
        prepareMocksForDmaapConsumer(Optional.of(jsonArrayWrongContent));

        //when
        Flux<ConsumerDmaapModel> response = dmaapConsumerTask.execute("Sample input");

        //then
        verify(DmaapClientFactory.createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault())).get(messageRouterSubscribeRequest);
        assertNull(response.blockFirst());
    }

    @Test
    void whenPassedObjectFits_ReturnsCorrectResponse() throws Exception {
        //given
        prepareMocksForDmaapConsumer(Optional.of(jsonArray));

        //when
        Flux<ConsumerDmaapModel> response = dmaapConsumerTask.execute("Sample input");

        //then
        verify(DmaapClientFactory.createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault())).get(messageRouterSubscribeRequest);
        assertEquals(consumerDmaapModel, response.blockFirst());
    }


    private void prepareMocksForDmaapConsumer(Optional<JsonArray> message) {
        when(verify(DmaapClientFactory.createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault())).get(messageRouterSubscribeRequest))
                .thenReturn(Mono.just(ImmutableMessageRouterSubscribeResponse.builder().items(message.get()).build()));
        when(cbsConfiguration.getMessageRouterSubscribeRequest()).thenReturn(messageRouterSubscribeRequest);
        dmaapConsumerTask = new DmaapConsumerTaskImpl(cbsConfiguration, new DmaapConsumerJsonParser());
    }
}