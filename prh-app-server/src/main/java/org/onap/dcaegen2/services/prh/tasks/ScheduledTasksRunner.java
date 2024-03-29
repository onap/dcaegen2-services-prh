/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import javax.annotation.PreDestroy;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.configuration.PrhProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 6/13/18
 */

@Profile("!autoCommitDisabled")
@Configuration
@EnableScheduling
public class ScheduledTasksRunner {

    private static volatile List<ScheduledFuture> scheduledPrhTaskFutureList = new ArrayList<>();
    private final TaskScheduler taskScheduler;
    private final ScheduledTasks scheduledTask;
    private final PrhProperties prhProperties;
    private final CbsConfiguration cbsConfiguration;
    public ScheduledTasksRunner(TaskScheduler taskScheduler, ScheduledTasks scheduledTask,
        PrhProperties prhProperties, CbsConfiguration cbsConfiguration) {
        this.taskScheduler = taskScheduler;
        this.scheduledTask = scheduledTask;
        this.prhProperties = prhProperties;
        this.cbsConfiguration = cbsConfiguration;
    }
    
    @EventListener
    public void onApplicationStartedEvent(ApplicationStartedEvent applicationStartedEvent) {
            tryToStartTask();
    }

    /**
     * Function which have to stop tasks execution.
     */
    @PreDestroy
    public synchronized void cancelTasks() {
        scheduledPrhTaskFutureList.forEach(x -> x.cancel(false));
        scheduledPrhTaskFutureList.clear();
    }

    /**
     * Function for starting scheduling PRH workflow.
     *
     * @return status of operation execution: true - started, false - not started
     */
    public synchronized boolean tryToStartTask() {
        if (scheduledPrhTaskFutureList.isEmpty()) {
            scheduledPrhTaskFutureList.add(taskScheduler
                .scheduleWithFixedDelay(scheduledTask::scheduleMainPrhEventTask,
                    prhProperties.getWorkflowSchedulingInterval()));
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Function for cleaning resources for kafka subscriber and publisher.
     */
    public synchronized void closeKafkaPublisherSubscriber() {
        cbsConfiguration.getMessageRouterSubscriber().close();
        cbsConfiguration.getMessageRouterPublisher().close();
    }
}
