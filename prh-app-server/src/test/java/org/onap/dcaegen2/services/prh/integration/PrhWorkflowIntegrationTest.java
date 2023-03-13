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
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.MainApp;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasks;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasksRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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


@SpringBootTest
@AutoConfigureWireMock(port = 0)
class PrhWorkflowIntegrationTest {

    @Autowired
    private ScheduledTasks scheduledTasks;

    @MockBean
    private ScheduledTasksRunner scheduledTasksRunner;  // just to disable scheduling - some configurability in ScheduledTaskRunner not to start tasks at app startup would be welcome


    @Configuration
    @Import(MainApp.class)
    static class CbsConfigTestConfig {

        @Value("http://localhost:${wiremock.server.port}")
        private String wiremockServerAddress;

        @Bean
        public CbsConfiguration cbsConfiguration() {
            JsonObject cbsConfigJson = new Gson().fromJson(getResourceContent("configurationFromCbs.json")
                            .replaceAll("https?://dmaap-mr[\\w.]*:\\d+", wiremockServerAddress)
                            .replaceAll("https?://aai[\\w.]*:\\d+", wiremockServerAddress),
                    JsonObject.class);

            CbsConfiguration cbsConfiguration = new CbsConfiguration();
            cbsConfiguration.parseCBSConfig(cbsConfigJson);
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

        stubFor(get(urlEqualTo("/events/unauthenticated.VES_PNFREG_OUTPUT/OpenDCAE-c12/c12"))
                .willReturn(ok().withBody(new Gson().toJson(singletonList(event)))));
        stubFor(get(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)).willReturn(ok().withBody("{}")));
        stubFor(patch(urlEqualTo("/aai/v23/network/pnfs/pnf/" + pnfName)));
        stubFor(post(urlEqualTo("/events/unauthenticated.PNF_READY")));

        scheduledTasks.scheduleMainPrhEventTask();

        verify(1, postRequestedFor(urlEqualTo("/events/unauthenticated.PNF_READY"))
                .withRequestBody(matchingJsonPath("$[0].correlationId", equalTo(pnfName))));
    }


    private static String getResourceContent(String resourceName) {
        try {
            return new String(Files.readAllBytes(Paths.get(getSystemResource(resourceName).toURI())));
        } catch (Exception e) {
            throw new RuntimeException("failed loading content of '" + resourceName + "'", e);
        }
    }
}
