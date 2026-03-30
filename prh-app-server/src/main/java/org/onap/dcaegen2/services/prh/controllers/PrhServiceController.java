/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2023-2026 Deutsche Telekom Intellectual Property. All rights reserved.
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@Api(value = "PrhServiceController")
public class PrhServiceController {

    static final String LISTENER_ID = "prhKafkaListener";
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @RequestMapping(value = "start", method = RequestMethod.GET)
    @ApiOperation(value = "Start scheduling worker request")
    public Mono<ResponseEntity<String>> startTasks() {
        log.trace("Receiving start request");
        return Mono.fromSupplier(this::tryToStart).map(this::createStartTaskResponse);
    }

    @RequestMapping(value = "stopPrh", method = RequestMethod.GET)
    @ApiOperation(value = "Receiving stop scheduling worker request")
    public Mono<ResponseEntity<String>> stopTask() {
        log.trace("Receiving stop request");
        return Mono.defer(() -> {
            MessageListenerContainer container = kafkaListenerEndpointRegistry.getListenerContainer(LISTENER_ID);
            if (container != null && container.isRunning()) {
                container.stop();
            }
            return Mono.just(new ResponseEntity<>("PRH Service has been stopped!", HttpStatus.OK));
        });
    }

    private boolean tryToStart() {
        MessageListenerContainer container = kafkaListenerEndpointRegistry.getListenerContainer(LISTENER_ID);
        if (container != null && !container.isRunning()) {
            container.start();
            return true;
        }
        return false;
    }

    private ResponseEntity<String> createStartTaskResponse(boolean wasStarted) {
        if (wasStarted) {
            return new ResponseEntity<>("PRH Service has been started!", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("PRH Service is already running!", HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
