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
package org.onap.dcaegen2.services.prh.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.onap.dcaegen2.services.prh.configuration.PrhAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/19/18
 */
@RestController
public class HeartbeatController {

    private static final Logger logger = LoggerFactory.getLogger(PrhAppConfig.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @RequestMapping(value = "heartbeat", method = RequestMethod.GET)
    public Mono<ResponseEntity<String>> heartbeat() {
        logger.debug("Receiving request on on thread={} , time={} ", Thread.currentThread().getName(),
            dateTimeFormatter.format(
                LocalDateTime.now()));

        return Mono.defer(() -> {
            logger.debug("Sending response on thread={} , time={} ", Thread.currentThread().getName(),
                dateTimeFormatter.format(
                    LocalDateTime.now()));
            return Mono.just(new ResponseEntity<>("I'm living", HttpStatus.OK));
        });
    }
}
