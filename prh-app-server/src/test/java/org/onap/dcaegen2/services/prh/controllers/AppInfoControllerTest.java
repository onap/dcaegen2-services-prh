/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.controllers;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.configuration.PrhAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppInfoControllerTest {

    private static final String SAMPLE_GIT_INFO_CONTENT = "{ \"git.commit.id\" : \"37444e\" }";

    @MockBean
    private PrhAppConfig prhAppConfig;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldProvideHeartbeatResponse() {
        webTestClient
                .get().uri("/heartbeat")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("alive");
    }


    @Test
    void shouldProvideVersionInfo() {
        when(prhAppConfig.getGitInfo()).thenReturn(new ByteArrayResource(SAMPLE_GIT_INFO_CONTENT.getBytes()));

        webTestClient
                .get().uri("/version")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(SAMPLE_GIT_INFO_CONTENT);
    }
}