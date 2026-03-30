/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.kafka.listener.auto-startup=false"})
@DirtiesContext
@ActiveProfiles("prod")
class PrhServiceControllerTest {

    @MockBean
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private WebTestClient webTestClient;

    private MessageListenerContainer mockContainer;

    @BeforeEach
    void setUp() {
        mockContainer = Mockito.mock(MessageListenerContainer.class);
        when(kafkaListenerEndpointRegistry.getListenerContainer(PrhServiceController.LISTENER_ID))
                .thenReturn(mockContainer);
    }

    @Test
    void startEndpointShouldAllowStartingPrhTasks() {
        when(mockContainer.isRunning()).thenReturn(false);
        webTestClient
                .get().uri("/start")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class).isEqualTo("PRH Service has been started!");
        verify(mockContainer).start();
    }

    @Test
    void whenPrhTasksAreAlreadyStarted_shouldRespondThatRequestWasNotAccepted() {
        when(mockContainer.isRunning()).thenReturn(true);
        webTestClient
                .get().uri("/start")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody(String.class).isEqualTo("PRH Service is already running!");
    }

    @Test
    void stopEndpointShouldAllowStoppingPrhTasks() {
        when(mockContainer.isRunning()).thenReturn(true);
        webTestClient
                .get().uri("/stopPrh")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("PRH Service has been stopped!");

        verify(mockContainer).stop();
    }
}
