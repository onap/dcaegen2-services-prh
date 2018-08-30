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

package org.onap.dcaegen2.services.prh.configuration;

import io.swagger.annotations.ApiOperation;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import javax.annotation.PostConstruct;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 6/13/18
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    private static final int SCHEDULING_DELAY_FOR_PRH_TASKS = 5;
    private static final int SCHEDULING_REQUEST_FOR_CONFIGURATION_DELAY = 5;
    private static volatile List<ScheduledFuture> scheduledPrhTaskFutureList = new ArrayList<>();

    private final ConcurrentTaskScheduler taskScheduler;
    private final ScheduledTasks scheduledTask;
    private final TaskScheduler cloudTaskScheduler;
    private final CloudConfiguration cloudConfiguration;

    @Autowired
    public SchedulerConfig(@Qualifier("concurrentTaskScheduler") ConcurrentTaskScheduler concurrentTaskScheduler,
        ScheduledTasks scheduledTask, ThreadPoolTaskScheduler cloudTaskScheduler,
        CloudConfiguration cloudConfiguration) {
        this.taskScheduler = concurrentTaskScheduler;
        this.scheduledTask = scheduledTask;
        this.cloudTaskScheduler = cloudTaskScheduler;
        this.cloudConfiguration = cloudConfiguration;
    }

    /**
     * Function which have to stop tasks execution.
     *
     * @return response entity about status of cancellation operation
     */
    @ApiOperation(value = "Get response on stopping task execution")
    public synchronized Mono<ResponseEntity<String>> getResponseFromCancellationOfTasks() {
        scheduledPrhTaskFutureList.forEach(x -> x.cancel(false));
        scheduledPrhTaskFutureList.clear();
        return Mono.defer(() ->
            Mono.just(new ResponseEntity<>("PRH Service has already been stopped!", HttpStatus.CREATED))
        );
    }

    /**
     * Function for starting scheduling PRH workflow.
     *
     * @return status of operation execution: true - started, false - not started
     */

    @PostConstruct
    @ApiOperation(value = "Start task if possible")
    public synchronized boolean tryToStartTask() {
        //entry
        if (scheduledPrhTaskFutureList.isEmpty()) {
            scheduledPrhTaskFutureList.add(cloudTaskScheduler
                .scheduleAtFixedRate(cloudConfiguration::runTask, Instant.now(),
                    Duration.ofMinutes(SCHEDULING_REQUEST_FOR_CONFIGURATION_DELAY)));
            scheduledPrhTaskFutureList.add(taskScheduler
                .scheduleWithFixedDelay(scheduledTask::scheduleMainPrhEventTask,
                    Duration.ofSeconds(SCHEDULING_DELAY_FOR_PRH_TASKS)));
            return true;
        } else {
            return false;
        }
    }

}
