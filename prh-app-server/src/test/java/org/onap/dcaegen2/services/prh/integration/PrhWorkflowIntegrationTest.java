/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019-2021 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.prh.MainApp;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.service.KafkaConsumerJsonParser;
import org.onap.dcaegen2.services.prh.tasks.PrhWorkflowProcessor;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.SettableListenableFuture;
import reactor.core.publisher.Flux;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.lang.ClassLoader.getSystemResource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@SpringBootTest(properties = {"spring.kafka.listener.auto-startup=false"})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles(value = "prod")
class PrhWorkflowIntegrationTest {

    @Autowired
    private PrhWorkflowProcessor scheduledTasks;

    @SpyBean
    CbsConfiguration cbsConfiguration;

    @Autowired
    private KafkaConsumerJsonParser kafkaConsumerJsonParser;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Configuration
    @Import(MainApp.class)
    static class CbsConfigTestConfig {

        @Value("http://localhost:${wiremock.server.port}")
        private String wiremockServerAddress;

        @Bean
        public CbsConfiguration cbsConfiguration() throws Exception {
            JsonObject cbsConfigJson = new Gson().fromJson(getResourceContent("configurationFromCbs.json")
                            .replaceAll("https?://dmaap-mr[\\w.]*:\\d+", wiremockServerAddress)
                            .replaceAll("https?://aai[\\w.]*:\\d+", wiremockServerAddress),
                    JsonObject.class);

            CbsConfiguration cbsConfiguration = new CbsConfiguration();
            withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
            .and("BOOTSTRAP_SERVERS", "localhost:9092")
            .execute(() -> {
                cbsConfiguration.parseCBSConfig(cbsConfigJson);
            });

            return cbsConfiguration;
        }

    }

    @BeforeEach
    void resetWireMock() {
        WireMock.reset();

        SettableListenableFuture mockFuture = new SettableListenableFuture();
        mockFuture.set(null);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(mockFuture);
    }


    @Test
    void whenThereAreNoEvents_WorkflowShouldFinish() {
        scheduledTasks.processMessages(Flux.empty());

        verify(0, anyRequestedFor(urlPathMatching("/aai.*")));
        Mockito.verify(kafkaTemplate, Mockito.never()).send(anyString(), anyString());
    }


    @Test
    void whenThereIsAnEvent_ShouldSendPnfReadyNotification() {
        String event = getResourceContent("integration/event.json");
        String pnfName = JsonPath.read(event, "$.event.commonEventHeader.sourceName");
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok().withBody("{}")));
        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));

        processEvents(event);
        Mockito.verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }

    private void processEvents(String... events) {
        Flux<ConsumerPnfModel> models = kafkaConsumerJsonParser.getConsumerModelFromKafkaRecords(
                Arrays.asList(events));
        scheduledTasks.processMessages(models);
    }


    private static String getResourceContent(String resourceName) {
        try {
            return new String(Files.readAllBytes(Paths.get(getSystemResource(resourceName).toURI())));
        } catch (Exception e) {
            throw new RuntimeException("failed loading content of '" + resourceName + "'", e);
        }
    }
}
