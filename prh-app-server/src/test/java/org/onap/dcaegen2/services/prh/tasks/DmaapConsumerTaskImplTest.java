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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.services.prh.TestAppConfiguration.createDefaultDmaapConsumerConfiguration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.ConsumerReactiveHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.DMaaPConsumerReactiveHttpClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
class DmaapConsumerTaskImplTest {

    private static ConsumerDmaapModel consumerDmaapModel;
    private static DmaapConsumerTaskImpl dmaapConsumerTask;
    private static DMaaPConsumerReactiveHttpClient dMaaPConsumerReactiveHttpClient;
    private static AppConfig appConfig;
    private static DmaapConsumerConfiguration dmaapConsumerConfiguration;
    private static String message;

    @BeforeAll
    static void setUp() {
        dmaapConsumerConfiguration = createDefaultDmaapConsumerConfiguration();

        JsonObject jsonObject = new JsonParser().parse("{\n"
            + "        \"attachmentPoint\": \"bla-bla-30-3\",\n"
            + "        \"cvlan\": \"678\",\n"
            + "        \"svlan\": \"1005\"\n"
            + "      }").getAsJsonObject();

        consumerDmaapModel = ImmutableConsumerDmaapModel.builder()
            .correlationId("NOKQTFCOC540002E")
            .serialNumber("QTFCOC540002E")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("gNB")
            .swVersion("v4.5.0.1")
            .additionalFields(jsonObject)
            .build();
        appConfig = mock(AppConfig.class);

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
            + " \"softwareVersion\": \"v4.5.0.1\","
            + " \"additionalFields\": {\"attachmentPoint\": \"bla-bla-30-3\",\"cvlan\": \"678\",\"svlan\": \"1005\"}"
            + "}}}]";
    }

    @Test
    void whenPassedObjectDoesNotFit_DoesNotThrowPrhTaskException() throws Exception {
        //given
        prepareMocksForDmaapConsumer(Optional.empty());

        //when
        Flux<ConsumerDmaapModel> response = dmaapConsumerTask.execute("Sample input");

        //then
        verify(dMaaPConsumerReactiveHttpClient).getDMaaPConsumerResponse();
        assertNull(response.blockFirst());
    }

    @Test
    void whenPassedObjectFits_ReturnsCorrectResponse() throws Exception {
        //given
        prepareMocksForDmaapConsumer(Optional.of(message));

        //when
        Flux<ConsumerDmaapModel> response = dmaapConsumerTask.execute("Sample input");

        //then
        verify(dMaaPConsumerReactiveHttpClient).getDMaaPConsumerResponse();
        assertEquals(consumerDmaapModel, response.blockFirst());
    }

    @Test
    void whenInitConfigs_initStreamReader() {
        //when
        dmaapConsumerTask.initConfigs();

        //then
        verify(appConfig).initFileStreamReader();
    }

    private void prepareMocksForDmaapConsumer(Optional<String> message) throws Exception {
        dMaaPConsumerReactiveHttpClient = mock(DMaaPConsumerReactiveHttpClient.class);
        when(dMaaPConsumerReactiveHttpClient.getDMaaPConsumerResponse()).thenReturn(Mono.just(message.orElse("")));
        when(appConfig.getDmaapConsumerConfiguration()).thenReturn(dmaapConsumerConfiguration);
        ConsumerReactiveHttpClientFactory httpClientFactory = mock(ConsumerReactiveHttpClientFactory.class);
        doReturn(dMaaPConsumerReactiveHttpClient).when(httpClientFactory).create(dmaapConsumerConfiguration);
        dmaapConsumerTask = new DmaapConsumerTaskImpl(appConfig, new DmaapConsumerJsonParser(), httpClientFactory);
    }
}