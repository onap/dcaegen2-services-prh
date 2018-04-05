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
package org.onap.dcaegen2.services.prh.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.onap.dcaegen2.services.prh.exceptions.AAINotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class ScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DmaapTask dmaapConsumerTask;

    @Autowired
    public ScheduledTask(DmaapTask dmaapConsumerTask) {
        this.dmaapConsumerTask = dmaapConsumerTask;
    }

    public void scheduledTaskAskingDMaaPOfConsumeEvent() {
        logger.debug("Task scheduledTaskAskingDMaaPOfConsumeEvent() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        try {
            dmaapConsumerTask.execute();
        } catch (AAINotFoundException e) {
            logger
                .warn("Task scheduledTaskAskingDMaaPOfConsumeEvent()::AAINotFoundException :: Execution Time - {}:{}",
                    dateTimeFormatter.format(
                        LocalDateTime.now()), e);
        }
    }

}
