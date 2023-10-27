/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property. All rights reserved.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.JsonPath;

import io.vavr.collection.List;
import reactor.core.publisher.Flux;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.dcaegen2.services.prh.MainApp;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.kafka.ImmutableKafkaConfiguration;
import org.onap.dcaegen2.services.prh.adapter.kafka.KafkaConfiguration;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.configuration.CbsConfigurationForAutoCommitDisabledMode;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.prh.tasks.commit.KafkaConsumerTaskImpl;
import org.onap.dcaegen2.services.prh.tasks.commit.ScheduledTasksRunnerWithCommit;
import org.onap.dcaegen2.services.prh.tasks.commit.ScheduledTasksWithCommit;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import static java.lang.ClassLoader.getSystemResource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *  * @author <a href="mailto:PRANIT.KAPDULE@t-systems.com">Pranit Kapdule</a> on
 *   *        24/08/23
 *    */

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles(value = "autoCommitDisabled")
class PrhWorkflowIntegrationForAutoCommitDisabledTest {

    @Autowired
    private ScheduledTasksWithCommit scheduledTasksWithCommit;

    @MockBean
    private ScheduledTasksRunnerWithCommit scheduledTasksRunnerWithCommit; // just to disable scheduling - some
                                                                           // configurability in ScheduledTaskRunner not
                                                                           // to start tasks at app startup would be
                                                                           // welcome

    @MockBean
    private KafkaConsumerTaskImpl kafkaConsumerTaskImpl;

    @Autowired
    private DmaapConsumerJsonParser dmaapConsumerJsonParser;
    
    @SpyBean
    CbsConfiguration cbsConfiguration;
    
    @Mock
    MessageRouterPublisher publisher;

    @Configuration
    @Import(MainApp.class)
    static class CbsConfigTestConfig {

        @Value("http://localhost:${wiremock.server.port}")
        private String wiremockServerAddress;

        protected KafkaConfiguration kafkaConfiguration;

        @Bean
        public CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode() {

            JsonObject cbsConfigJson = new Gson()
                    .fromJson(getResourceContent("autoCommitDisabledConfigurationFromCbs2.json")
                            .replaceAll("https?://dmaap-mr[\\w.]*:\\d+", wiremockServerAddress)
                            .replaceAll("https?://aai[\\w.]*:\\d+", wiremockServerAddress), JsonObject.class);

            CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode = new CbsConfigurationForAutoCommitDisabledMode();

            try {
                withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
                .and("BOOTSTRAP_SERVERS", "localhost:9092")
                .execute(() -> {
                    cbsConfigurationForAutoCommitDisabledMode.parseCBSConfig(cbsConfigJson);
                });
                
            } catch (Exception e) {
               //Exception is expected as environment variable for JAAS_CONFIG is not available
                if (e.getMessage() == "kafkaJaasConfig") {
                    kafkaConfiguration = new ImmutableKafkaConfiguration.Builder().kafkaBoostrapServerConfig("0.0.0.0")
                            .groupIdConfig("CG1").kafkaSaslMechanism("SASL_MECHANISM")
                            .kafkaSecurityProtocol("SEC-PROTOCOL").kafkaJaasConfig("JAAS_CONFIG").build();
                    cbsConfigurationForAutoCommitDisabledMode.setKafkaConfiguration(kafkaConfiguration);

                }

            }
            return cbsConfigurationForAutoCommitDisabledMode;
        };

    }

    @BeforeEach
    void resetWireMock() {
        WireMock.reset();
    }

    @Test
    void beforeCbsConfigurationForAutoCommitDisabledMode() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
        .execute(() -> {
            this.whenThereAreNoEventsInDmaap_WorkflowShouldFinish();
        });
    }

    void whenThereAreNoEventsInDmaap_WorkflowShouldFinish() throws JSONException {

        when(kafkaConsumerTaskImpl.execute()).thenReturn(Flux.empty());
       
        scheduledTasksWithCommit.scheduleKafkaPrhEventTask();

        verify(0, anyRequestedFor(urlPathMatching("/aai.*")));
        verify(0, postRequestedFor(urlPathMatching("/events.*")));
    }

    @Test
    void beforeWhenThereIsAnEventsInDmaap_ShouldSendPnfReadyNotification() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
        .execute(() -> {
            this.whenThereIsAnEventsInDmaap_ShouldSendPnfReadyNotification();
        });
    }

    void whenThereIsAnEventsInDmaap_ShouldSendPnfReadyNotification()
            throws JSONException, JsonMappingException, JsonProcessingException {

        String event = getResourceContent("integration/event.json");
        String pnfName = JsonPath.read(event, "$.event.commonEventHeader.sourceName");

        java.util.List<String> eventList = new ArrayList<>();
        eventList.add(event);

        Flux<ConsumerDmaapModel> fluxList = dmaapConsumerJsonParser
                .getConsumerDmaapModelFromKafkaConsumerRecord(eventList);

        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok().withBody("{}")));
        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));
        stubFor(post(urlEqualTo("/events/unauthenticated.PNF_READY")));

        when(kafkaConsumerTaskImpl.execute()).thenReturn(fluxList);
        
        List<String> expectedItems = List.of(event);
        Flux<MessageRouterPublishResponse> pubresp = Flux.just(ImmutableMessageRouterPublishResponse
                .builder()
                .items(expectedItems.map(JsonPrimitive::new))
                .build());
        when(cbsConfiguration.getMessageRouterPublisher()).thenReturn(publisher);
        when(publisher.put(any(MessageRouterPublishRequest.class),any())).thenReturn(pubresp);
        scheduledTasksWithCommit.scheduleKafkaPrhEventTask();
        verify(publisher,times(1)).put(any(MessageRouterPublishRequest.class),any());

    }

    private static String getResourceContent(String resourceName) {
        try {
            return new String(Files.readAllBytes(Paths.get(getSystemResource(resourceName).toURI())));
        } catch (Exception e) {
            throw new RuntimeException("failed loading content of '" + resourceName + "'", e);
        }
    }
}
