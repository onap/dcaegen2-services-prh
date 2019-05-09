/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CbsClientConfigurationResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsClientConfigurationResolver.class);
    private final CbsClientConfigFileReader cbsClientConfigFileReader;

    public CbsClientConfigurationResolver(CbsClientConfigFileReader cbsClientConfigFileReader) {
        this.cbsClientConfigFileReader = cbsClientConfigFileReader;
    }

    Mono<CbsClientConfiguration> resolveCbsClientConfiguration() {
        return Mono.fromSupplier(CbsClientConfiguration::fromEnvironment)
                .doOnError(err -> LOGGER.warn("Failed resolving CBS client configuration from system environments", err))
                .onErrorResume(err -> cbsClientConfigFileReader.readConfig());
    }

}
