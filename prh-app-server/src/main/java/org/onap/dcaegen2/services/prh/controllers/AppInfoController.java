/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

import io.swagger.annotations.*;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/19/18
 */
@RestController
@Api(value = "AppInfoController", description = "Provides basic information about application")
public class AppInfoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppInfoController.class);
    private final Resource gitInfo;

    @Autowired
    public AppInfoController(Config config) {
        gitInfo = config.getGitInfo();
    }

    @GetMapping(value = "heartbeat", produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation("Returns liveness of PRH service")
    @ApiResponses(@ApiResponse(code = 200, message = "Service is alive"))
    public Mono<ResponseEntity<String>> heartbeat() {
        LOGGER.trace("Heartbeat request received");
        return Mono.defer(() -> Mono.just(new ResponseEntity<>("alive", HttpStatus.OK))
        );
    }

    @GetMapping(value = "version", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Returns version information")
    public Mono<Resource> version() {
        return Mono.defer(() -> Mono.just(gitInfo));
    }
}
