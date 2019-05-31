package org.onap.dcaegen2.services.prh.controllers;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasksRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScheduleControllerTest {

    @MockBean
    private ScheduledTasksRunner scheduledTasksRunner;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void startEndpointShouldAllowStartingPrhTasks() {
        when(scheduledTasksRunner.tryToStartTask()).thenReturn(true);
        webTestClient
                .get().uri("/start")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class).isEqualTo("PRH Service has been started!");
    }

    @Test
    void whenPrhTasksAreAlreadyStarted_shouldRespondThatRequestWasNotAccepted() {
        when(scheduledTasksRunner.tryToStartTask()).thenReturn(false);
        webTestClient
                .get().uri("/start")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody(String.class).isEqualTo("PRH Service is already running!");
    }

    @Test
    void stopEndpointShouldAllowStoppingPrhTasks() {
        webTestClient
                .get().uri("/stopPrh")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("PRH Service has been stopped!");

        verify(scheduledTasksRunner).cancelTasks();
    }
}