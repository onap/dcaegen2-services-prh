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

package org.onap.dcaegen2.services.prh.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.core.env.Environment;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.Collections;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CbsConfigRefreshSchedulerTest {

    private static final Duration SOME_UPDATES_INTERVAL = Duration.ofMinutes(5);
    private static final String CBS_UPDATES_INTERVAL_PROPERTY = "cbs.updates-interval";

    @Mock
    private ContextRefresher contextRefresher;
    @Mock
    private Environment environment;

    private VirtualTimeScheduler virtualTimeScheduler;

    private CbsConfigRefreshScheduler cbsConfigRefreshScheduler;


    @BeforeEach
    void setUp() {
        virtualTimeScheduler = VirtualTimeScheduler.getOrSet();
        when(environment.getProperty(CBS_UPDATES_INTERVAL_PROPERTY, Duration.class, Duration.ZERO))
                .thenReturn(SOME_UPDATES_INTERVAL);

        cbsConfigRefreshScheduler = new CbsConfigRefreshScheduler(contextRefresher, environment);
    }

    @AfterEach
    void tearDown() {
        virtualTimeScheduler.dispose();
    }

    @Test
    void configRefreshUpdatesShouldBeFiredAccordingToConfiguredInterval() {
        cbsConfigRefreshScheduler.startPollingForCbsUpdates();

        verify(contextRefresher, times(0)).refresh();

        virtualTimeScheduler.advanceTimeBy(SOME_UPDATES_INTERVAL);
        verify(contextRefresher, times(1)).refresh();

        virtualTimeScheduler.advanceTimeBy(SOME_UPDATES_INTERVAL);
        verify(contextRefresher, times(2)).refresh();
    }

    @Test
    void whenConfigUpdateIntervalIsSetToZero_UpdatesShouldNotBeExecuted() {
        when(environment.getProperty(CBS_UPDATES_INTERVAL_PROPERTY, Duration.class, Duration.ZERO))
                .thenReturn(Duration.ZERO);

        cbsConfigRefreshScheduler.startPollingForCbsUpdates();

        virtualTimeScheduler.advanceTimeBy(Duration.ofHours(10));

        verifyZeroInteractions(contextRefresher);
    }

    @Test
    void whenUpdateFails_shouldContinueWithUpdateRequestsAccordingToConfiguredSchedule() {
        when(contextRefresher.refresh())
                .thenThrow(new RuntimeException("kaboom!"))
                .thenReturn(Collections.emptySet());

        cbsConfigRefreshScheduler.startPollingForCbsUpdates();

        virtualTimeScheduler.advanceTimeBy(SOME_UPDATES_INTERVAL.plus(SOME_UPDATES_INTERVAL));
        verify(contextRefresher, times(2)).refresh();
    }


    @Test
    void whenUpdatesIntervalIsChangedInEnvironment_UpdatesShouldBeRescheduled() {
        when(environment.getProperty(CBS_UPDATES_INTERVAL_PROPERTY, Duration.class, Duration.ZERO))
                .thenReturn(Duration.ofMinutes(30))
                .thenReturn(Duration.ofSeconds(10));

        cbsConfigRefreshScheduler.startPollingForCbsUpdates();

        cbsConfigRefreshScheduler.onEnvironmentChanged(
                new EnvironmentChangeEvent(Collections.singleton(CBS_UPDATES_INTERVAL_PROPERTY)));

        virtualTimeScheduler.advanceTimeBy(Duration.ofMinutes(1));

        verify(contextRefresher, times(6)).refresh();
    }


    @Test
    void whenEnvironmentChangeDoesNotAffectUpdatesInterval_UpdatesScheduleShouldNotBeImpacted() {
        cbsConfigRefreshScheduler.startPollingForCbsUpdates();

        Duration envChangeDelay = Duration.ofMinutes(1);
        virtualTimeScheduler.advanceTimeBy(envChangeDelay);

        cbsConfigRefreshScheduler.onEnvironmentChanged(new EnvironmentChangeEvent(Collections.emptySet()));

        virtualTimeScheduler.advanceTimeBy(SOME_UPDATES_INTERVAL.minus(envChangeDelay));

        verify(contextRefresher).refresh();
    }
}