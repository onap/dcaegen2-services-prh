/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019-2022 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2026 Deutsche Telekom Intellectual Property. All rights reserved.
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
import org.onap.dcaegen2.services.bootstrap.CbsConfigFetcher;
import org.onap.dcaegen2.services.bootstrap.CbsProperties;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CbsConfigRefreshSchedulerTest {

    private static final Duration SOME_UPDATES_INTERVAL = Duration.ofMinutes(5);

    @Mock
    private CbsConfigFetcher cbsConfigFetcher;
    @Mock
    private CbsProperties cbsProperties;

    private VirtualTimeScheduler virtualTimeScheduler;

    private CbsConfigRefreshScheduler cbsConfigRefreshScheduler;


    @BeforeEach
    void setUp() {
        virtualTimeScheduler = VirtualTimeScheduler.getOrSet();
        cbsConfigRefreshScheduler = new CbsConfigRefreshScheduler(cbsConfigFetcher, cbsProperties);
    }

    @AfterEach
    void tearDown() {
        virtualTimeScheduler.dispose();
    }

    @Test
    void configRefreshUpdatesShouldBeFiredAccordingToConfiguredInterval() {
        cbsConfigRefreshScheduler.startPollingForCbsUpdates(SOME_UPDATES_INTERVAL);

        verify(cbsConfigFetcher, times(0)).fetchAndParse();

        virtualTimeScheduler.advanceTimeBy(SOME_UPDATES_INTERVAL);
        verify(cbsConfigFetcher, times(1)).fetchAndParse();

        virtualTimeScheduler.advanceTimeBy(SOME_UPDATES_INTERVAL);
        verify(cbsConfigFetcher, times(2)).fetchAndParse();
    }

    @Test
    void whenConfigUpdateIntervalIsSetToZero_UpdatesShouldNotBeExecuted() {
        cbsConfigRefreshScheduler.startPollingForCbsUpdates(Duration.ZERO);

        virtualTimeScheduler.advanceTimeBy(Duration.ofHours(10));

        verifyNoInteractions(cbsConfigFetcher);
    }

    @Test
    void whenUpdateFails_shouldContinueWithUpdateRequestsAccordingToConfiguredSchedule() {
        doThrow(new RuntimeException("kaboom!"))
                .doNothing()
                .when(cbsConfigFetcher).fetchAndParse();

        cbsConfigRefreshScheduler.startPollingForCbsUpdates(SOME_UPDATES_INTERVAL);

        virtualTimeScheduler.advanceTimeBy(SOME_UPDATES_INTERVAL.plus(SOME_UPDATES_INTERVAL));
        verify(cbsConfigFetcher, times(2)).fetchAndParse();
    }

    @Test
    void whenUpdatesIntervalIsZeroFromProperties_PostConstructShouldNotSchedule() {
        when(cbsProperties.getUpdatesInterval()).thenReturn(Duration.ZERO);

        cbsConfigRefreshScheduler.startPollingForCbsUpdates();

        virtualTimeScheduler.advanceTimeBy(Duration.ofHours(10));
        verifyNoInteractions(cbsConfigFetcher);
    }

    @Test
    void whenUpdatesIntervalIsNullFromProperties_PostConstructShouldNotSchedule() {
        when(cbsProperties.getUpdatesInterval()).thenReturn(null);

        cbsConfigRefreshScheduler.startPollingForCbsUpdates();

        virtualTimeScheduler.advanceTimeBy(Duration.ofHours(10));
        verifyNoInteractions(cbsConfigFetcher);
    }
}
