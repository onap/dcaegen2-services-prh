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
package org.onap.dcaegen2.services.prh.tasks.commit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import javax.annotation.PreDestroy;
import org.onap.dcaegen2.services.prh.configuration.PrhProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author <a href="mailto:pravin.kokane@t-systems.com">Pravin Kokane</a> on 3/13/23
 */

@Profile("autoCommitDisabled")
@Configuration
@EnableScheduling
public class ScheduledTasksRunnerWithCommit {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasksRunnerWithCommit.class);
    private static final Marker ENTRY = MarkerFactory.getMarker("ENTRY");
    private static List<ScheduledFuture> scheduledPrhTaskFutureList = new ArrayList<>();

    private final TaskScheduler taskScheduler;
    private final PrhProperties prhProperties;

    private ScheduledTasksWithCommit scheduledTasksWithCommit;

    public ScheduledTasksRunnerWithCommit(TaskScheduler taskScheduler, ScheduledTasksWithCommit scheduledTasksWithCommit,
                                          PrhProperties prhProperties) {
        this.taskScheduler = taskScheduler;
        this.scheduledTasksWithCommit = scheduledTasksWithCommit;
        this.prhProperties = prhProperties;
    }

    @EventListener
    public void onApplicationStartedEvent(ApplicationStartedEvent applicationStartedEvent) {
        LOGGER.info(ENTRY,"### in onApplicationStartedEvent");
        LOGGER.info(ENTRY,"###tryToStartTaskWithCommit="+tryToStartTaskWithCommit());
    }

    /**
     * Function which have to stop tasks execution.
     */
    @PreDestroy
    public synchronized void cancelTasks() {
        LOGGER.info(ENTRY,"###In cancelTasks");
        scheduledPrhTaskFutureList.forEach(x -> x.cancel(false));
        scheduledPrhTaskFutureList.clear();
    }

    /**
     * Function for starting scheduling PRH workflow.
     *
     * @return status of operation execution: true - started, false - not started
     */

    public synchronized boolean tryToStartTaskWithCommit() {
        LOGGER.info(ENTRY, "Start scheduling PRH workflow with Commit  Tasks Runner");
        if (scheduledPrhTaskFutureList.isEmpty()) {
            Collections.synchronizedList(scheduledPrhTaskFutureList);
            scheduledPrhTaskFutureList.add(taskScheduler
                .scheduleWithFixedDelay(scheduledTasksWithCommit::scheduleKafkaPrhEventTask,
                    prhProperties.getWorkflowSchedulingInterval()));
            return true;
        } else {
            return false;
        }
    }

}
