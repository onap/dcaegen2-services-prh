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

package org.onap.dcaegen2.services.prh.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasksRunner;
import org.onap.dcaegen2.services.prh.tasks.commit.ScheduledTasksRunnerWithCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/5/18
 */
@RestController
@Api(value = "ScheduleController", description = "Schedule Controller")
public class ScheduleController {

    String profile = System.getenv("SPRING_PROFILES_ACTIVE");

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleController.class);

    private ScheduledTasksRunner scheduledTasksRunner;
    private ScheduledTasksRunnerWithCommit scheduledTasksRunnerWithCommit;

    @Autowired(required = false)
    public ScheduleController(ScheduledTasksRunner scheduledTasksRunner) {
        this.scheduledTasksRunner = scheduledTasksRunner;
    }

    @Autowired(required = false)
    public ScheduleController(ScheduledTasksRunnerWithCommit scheduledTasksRunnerWithCommit) {
        this.scheduledTasksRunnerWithCommit = scheduledTasksRunnerWithCommit;
    }

    @RequestMapping(value = "start", method = RequestMethod.GET)
    @ApiOperation(value = "Start scheduling worker request")
    public Mono<ResponseEntity<String>> startTasks() {
        LOGGER.trace("Receiving start scheduling worker request with Comit SchedulerController");
        if(!(profile.equals("autoCommitDisabled")))
        {
            LOGGER.info("The active profile with auto-commit enabled condition: "+profile);
            return Mono.fromSupplier(scheduledTasksRunner::tryToStartTask).map(this::createStartTaskResponse);
        }
        else
        {
            LOGGER.info("The active profile with auto-commit disabled condition: "+profile);
            return Mono.fromSupplier(scheduledTasksRunnerWithCommit::tryToStartTaskWithCommit).map(this::createStartTaskResponse);
        }

    }


    @RequestMapping(value = "stopPrh", method = RequestMethod.GET)
    @ApiOperation(value = "Receiving stop scheduling worker request")
    public Mono<ResponseEntity<String>> stopTask() {
        LOGGER.trace("Receiving stop scheduling worker request");
        return Mono.defer(() -> {
                scheduledTasksRunner.cancelTasks();
                return Mono.just(new ResponseEntity<>("PRH Service has been stopped!", HttpStatus.OK));
            }
        );
    }

    private ResponseEntity<String> createStartTaskResponse(boolean wasScheduled) {
        if (wasScheduled) {
            return new ResponseEntity<>("PRH Service has been started!", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("PRH Service is already running!", HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
