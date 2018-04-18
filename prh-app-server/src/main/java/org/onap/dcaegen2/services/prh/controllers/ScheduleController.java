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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import org.onap.dcaegen2.services.prh.configuration.PrhAppConfig;
import org.onap.dcaegen2.services.prh.tasks.ScheduledTasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/5/18
 */
@RestController
@Component
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(PrhAppConfig.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int SCHEDULING_DELAY = 20000;

    private final TaskScheduler taskScheduler;
    private final ScheduledTasks scheduledTask;

    private ScheduledFuture<?> scheduledFuture;

    @Autowired
    public ScheduleController(TaskScheduler taskScheduler, ScheduledTasks scheduledTask) {
        this.taskScheduler = taskScheduler;
        this.scheduledTask = scheduledTask;
    }

    @RequestMapping(value = "start", method = RequestMethod.GET)
    public Mono<ResponseEntity<String>> startTasks() {
        logger.debug("Starting scheduling worker request on on thread={} , time={} ", Thread.currentThread().getName(),
            dateTimeFormatter.format(
                LocalDateTime.now()));
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Future<Void> future = executor.submit(() -> {
            scheduledTask.scheduleMainPrhEventTask();
            return null;
        });
        scheduledFuture = taskScheduler.scheduleAtFixedRate((Runnable) future, SCHEDULING_DELAY);

        return Mono.just(new ResponseEntity<>(HttpStatus.CREATED)).map(resp -> {
            logger.debug("Sending success response on starting task execution thread={} , time={} ",
                Thread.currentThread().getName(),
                dateTimeFormatter.format(
                    LocalDateTime.now()));
            return ResponseEntity.status(resp.getStatusCode()).body("PRH Service has already been started!");
        });

//        return (Mono) Mono.just(scheduledFuture)
//            .doOnSuccess(q -> Mono.just(new ResponseEntity<>(HttpStatus.CREATED)).map(resp -> {
//                logger.debug("Sending success response on starting task execution thread={} , time={} ",
//                    Thread.currentThread().getName(),
//                    dateTimeFormatter.format(
//                        LocalDateTime.now()));
//                return ResponseEntity.status(resp.getStatusCode()).body("PRH Service has already been started!");
//            }).doOnError(p -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)).map(resp -> {
//                logger.debug("Sending error response on starting task execution thread={} , time={} ",
//                    Thread.currentThread().getName(),
//                    dateTimeFormatter.format(
//                        LocalDateTime.now()));
//                return ResponseEntity.status(resp.getStatusCode()).body("PRH Service has not been started!");
//            })));

    }

    @RequestMapping(value = "stopPrh", method = RequestMethod.GET)
    public Mono<ResponseEntity<String>> stopTask() {
        logger.debug("Stopping scheduling worker request on on thread={} , time={} ",
            Thread.currentThread().getName(),
            dateTimeFormatter.format(
                LocalDateTime.now()));
        scheduledFuture.cancel(false);

        return Mono.just(new ResponseEntity<>(HttpStatus.CREATED)).map(resp -> {
            logger.debug("Sending success response on stopping task execution thread={} , time={} ",
                Thread.currentThread().getName(),
                dateTimeFormatter.format(
                    LocalDateTime.now()));
            return ResponseEntity.status(resp.getStatusCode()).body("PRH Service has already been stopped!");
        });
//        return (Mono) Mono.just(scheduledFuture)
//            .doOnSuccess(q -> Mono.just(new ResponseEntity<>(HttpStatus.ACCEPTED)).map(resp -> {
//                logger.debug("Sending success response on stopping task execution thread={} , time={} ",
//                    Thread.currentThread().getName(),
//                    dateTimeFormatter.format(
//                        LocalDateTime.now()));
//                return ResponseEntity.status(resp.getStatusCode()).body("PRH Service has already been started!");
//            }).doOnError(p -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)).map(resp -> {
//                logger.debug("Sending error response on stopping task execution thread={} , time={} ",
//                    Thread.currentThread().getName(),
//                    dateTimeFormatter.format(
//                        LocalDateTime.now()));
//                return ResponseEntity.status(resp.getStatusCode()).body("PRH Service has not been started!");
//            })));
    }
}
