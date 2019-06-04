/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.tasks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 6/13/18
 */
@Configuration
@EnableScheduling
public class ScheduledTasksRunner {
    private static final int SCHEDULING_DELAY_FOR_PRH_TASKS = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasksRunner.class);
    private static final Marker ENTRY = MarkerFactory.getMarker("ENTRY");
    private static volatile List<ScheduledFuture> scheduledPrhTaskFutureList = new ArrayList<>();

    private final TaskScheduler taskScheduler;
    private final ScheduledTasks scheduledTask;

    public ScheduledTasksRunner(TaskScheduler taskScheduler, ScheduledTasks scheduledTask) {
        this.taskScheduler = taskScheduler;
        this.scheduledTask = scheduledTask;
    }

    /**
     * Function which have to stop tasks execution.
     */
    public synchronized void cancelTasks() {
        scheduledPrhTaskFutureList.forEach(x -> x.cancel(false));
        scheduledPrhTaskFutureList.clear();
    }

    /**
     * Function for starting scheduling PRH workflow.
     *
     * @return status of operation execution: true - started, false - not started
     */
    @PostConstruct
    public synchronized boolean tryToStartTask() {
        LOGGER.info(ENTRY, "Start scheduling PRH workflow");
        if (scheduledPrhTaskFutureList.isEmpty()) {
            scheduledPrhTaskFutureList.add(taskScheduler
                .scheduleWithFixedDelay(scheduledTask::scheduleMainPrhEventTask,
                    Duration.ofSeconds(SCHEDULING_DELAY_FOR_PRH_TASKS)));
            return true;
        } else {
            return false;
        }
    }

}
