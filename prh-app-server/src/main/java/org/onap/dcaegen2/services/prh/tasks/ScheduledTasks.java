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
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/23/18
 */
@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Task dmaapConsumerTask;
    private final Task dmaapProducerTask;
    private final Task aaiPublisherTask;

    @Autowired
    public ScheduledTasks(DmaapConsumerTask dmaapConsumerTask, DmaapPublisherTask dmaapPublisherTask,
        AAIPublisherTask aaiPublisherTask) {
        this.dmaapConsumerTask = dmaapConsumerTask;
        this.dmaapProducerTask = dmaapPublisherTask;
        this.aaiPublisherTask = aaiPublisherTask;
    }

    public void scheduleMainPrhEventTask() {
        logger.debug("Task scheduledTaskAskingDMaaPOfConsumeEvent() :: Execution Time - {}", dateTimeFormatter.format(
            LocalDateTime.now()));
        setTaskExecutionFlow();
        try {
            dmaapConsumerTask.receiveRequest(null);
        } catch (PrhTaskException e) {
            logger
                .error("Task scheduledTaskAskingDMaaPOfConsumeEvent()::PrhTaskException :: Execution Time - {}:{}",
                    dateTimeFormatter.format(
                        LocalDateTime.now()), e);
        }
    }

    private void setTaskExecutionFlow() {
        dmaapConsumerTask.setNext(aaiPublisherTask);
        aaiPublisherTask.setNext(dmaapProducerTask);
    }

}
