/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019-2021 NOKIA Intellectual Property. All rights reserved.
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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.JsonPath;

import io.vavr.collection.List;
import reactor.core.publisher.Flux;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.prh.MainApp;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.tasks.DmaapConsumerTaskImpl;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasks;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasksRunner;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.ActiveProfiles;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import static java.lang.ClassLoader.getSystemResource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@SpringBootTest(properties = {"spring.kafka.listener.auto-startup=false"})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles(value = "prod")
class PrhWorkflowIntegrationTest {

    @Autowired
    private ScheduledTasks scheduledTasks;
    
    @SpyBean
    CbsConfiguration cbsConfiguration;
    
    @MockBean
    private ScheduledTasksRunner scheduledTasksRunner;  // just to disable scheduling - some configurability in ScheduledTaskRunner not to start tasks at app startup would be welcome
    
    @Autowired
    private DmaapConsumerTaskImpl dmaapConsumerTaskImpl;

    private MessageRouterPublisher publisher;
    
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
        dmaapConsumerTaskImpl.execute();  // drain any leftover events
        publisher = Mockito.mock(MessageRouterPublisher.class);
    }


    @Test
    void whenThereAreNoEventsInDmaap_WorkflowShouldFinish() {    
        scheduledTasks.scheduleMainPrhEventTask();

        verify(0, anyRequestedFor(urlPathMatching("/aai.*")));
        verify(0, postRequestedFor(urlPathMatching("/events.*")));
    }


    @Test
    void whenThereIsAnEventsInDmaap_ShouldSendPnfReadyNotification() {
        String event = getResourceContent("integration/event.json");
        String pnfName = JsonPath.read(event, "$.event.commonEventHeader.sourceName");
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok().withBody("{}")));
        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));
        
        // Inject event into consumer task buffer
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, null, event);
        dmaapConsumerTaskImpl.onMessage(Collections.singletonList(record),
                Mockito.mock(Acknowledgment.class));

        // Mock publisher (still SDK-based, Phase 2 will replace)
        List<String> expectedItems = List.of(event);
        Flux<MessageRouterPublishResponse> pubresp = Flux.just(ImmutableMessageRouterPublishResponse
                .builder()
                .items(expectedItems.map(JsonPrimitive::new))
                .build());
        
        when(cbsConfiguration.getMessageRouterPublisher()).thenReturn(publisher);
        when(publisher.put(any(MessageRouterPublishRequest.class),any())).thenReturn(pubresp);
        
        scheduledTasks.scheduleMainPrhEventTask();
        Mockito.verify(publisher,times(1)).put(any(MessageRouterPublishRequest.class),any());
    }


    private static String getResourceContent(String resourceName) {
        try {
            return new String(Files.readAllBytes(Paths.get(getSystemResource(resourceName).toURI())));
        } catch (Exception e) {
            throw new RuntimeException("failed loading content of '" + resourceName + "'", e);
        }
    }
}
