/*-
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
package org.onap.dcaegen2.services.prh.controllers;

import java.util.concurrent.ScheduledFuture;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/5/18
 */
@Controller
@Component
public class ScheduleController {

    private static final int SCHEDULING_DELAY = 20000;

    private final TaskScheduler taskScheduler;
    private final ScheduledTask scheduledTask;

    private ScheduledFuture<?> scheduledFuture;

    @Autowired
    public ScheduleController(TaskScheduler taskScheduler, ScheduledTask scheduledTask) {
        this.taskScheduler = taskScheduler;
        this.scheduledTask = scheduledTask;
    }


    @RequestMapping(value = "preferences", method = RequestMethod.PUT)
    public ResponseEntity<Void> startTask() {
        scheduledFuture = taskScheduler
            .scheduleWithFixedDelay(scheduledTask::scheduledTaskAskingDMaaPOfConsumeEvent, SCHEDULING_DELAY);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("stopPrh")
    public ResponseEntity<Void> stopTask() {
        scheduledFuture.cancel(false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
