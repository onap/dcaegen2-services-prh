/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023 DTAG Intellectual Property. All rights reserved.
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
package org.onap.dcaegen2.services.prh.tasks.commit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.configuration.PrhProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.scheduling.TaskScheduler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class ScheduledTasksRunnerWithCommitTest  {

    @Mock
    private ScheduledTasksWithCommit scheduledTasksWithCommit;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private PrhProperties prhProperties;

    @Mock
    private ApplicationStartedEvent applicationStartedEvent;

    private ScheduledTasksRunnerWithCommit scheduledTasksRunnerWithCommit;

    @BeforeEach
    void setUp() {
        scheduledTasksRunnerWithCommit = new ScheduledTasksRunnerWithCommit(taskScheduler, scheduledTasksWithCommit, prhProperties);
    }

    @Test
    void onApplicationStartedEvent() {
        scheduledTasksRunnerWithCommit.onApplicationStartedEvent(applicationStartedEvent);
        assertFalse(scheduledTasksRunnerWithCommit.tryToStartTaskWithCommit());
    }

    @Test
    void cancelTasks() {
        scheduledTasksRunnerWithCommit.cancelTasks();
    }

    @Test
    void tryToStartTaskWithCommit() {
        scheduledTasksRunnerWithCommit.tryToStartTaskWithCommit();
        assertFalse(scheduledTasksRunnerWithCommit.tryToStartTaskWithCommit());
    }
}
