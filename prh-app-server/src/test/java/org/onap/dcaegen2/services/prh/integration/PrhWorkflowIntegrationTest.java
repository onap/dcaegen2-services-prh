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
import reactor.core.publisher.Mono;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.dcaegen2.services.prh.MainApp;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasks;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasksRunner;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import static java.lang.ClassLoader.getSystemResource;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles(value = "prod")
class PrhWorkflowIntegrationTest {

    @Autowired
    private ScheduledTasks scheduledTasks;
    
    @SpyBean
    CbsConfiguration cbsConfiguration;
    
    @MockBean
    private ScheduledTasksRunner scheduledTasksRunner;  // just to disable scheduling - some configurability in ScheduledTaskRunner not to start tasks at app startup would be welcome
    
    @Mock
    MessageRouterSubscriber subscriber;
    
    @Mock
    MessageRouterPublisher publisher;
    
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
    }


    @Test
    void whenThereAreNoEventsInDmaap_WorkflowShouldFinish() {    
        stubFor(get(urlEqualTo("/events/unauthenticated.VES_PNFREG_OUTPUT/OpenDCAE-c12/c12"))
                .willReturn(aResponse().withBody("[]")));

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
        
        List<String> expectedItems = List.of(event);
        Mono<MessageRouterSubscribeResponse> resp = Mono.just(ImmutableMessageRouterSubscribeResponse
                .builder()
                .items(expectedItems.map(JsonPrimitive::new))
                .build());
        Flux<MessageRouterPublishResponse> pubresp = Flux.just(ImmutableMessageRouterPublishResponse
                .builder()
                .items(expectedItems.map(JsonPrimitive::new))
                .build());
        
        when(cbsConfiguration.getMessageRouterSubscriber()).thenReturn(subscriber);
        when(cbsConfiguration.getMessageRouterPublisher()).thenReturn(publisher);
        when(subscriber.get(any(MessageRouterSubscribeRequest.class))).thenReturn(resp);
        when(publisher.put(any(MessageRouterPublishRequest.class),any())).thenReturn(pubresp);
        
        scheduledTasks.scheduleMainPrhEventTask();
        verify(subscriber,times(1)).get(any(MessageRouterSubscribeRequest.class));
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
