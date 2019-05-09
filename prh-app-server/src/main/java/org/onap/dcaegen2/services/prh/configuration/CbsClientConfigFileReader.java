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

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableCbsClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CbsClientConfigFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsClientConfigFileReader.class);

    private final Resource cbsClientConfigFile;

    public CbsClientConfigFileReader(@Value("classpath:cbs_client_config.json") Resource cbsClientConfigFile) {
        this.cbsClientConfigFile = cbsClientConfigFile;
    }

    public Mono<CbsClientConfiguration> readConfig() {
        LOGGER.debug("Loading CBS client configuration from configuration file");
        try (InputStream inputStream = cbsClientConfigFile.getInputStream()) {
            CbsClientConfiguration config = new Gson().fromJson(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8), ImmutableCbsClientConfiguration.class);
            LOGGER.info("Evaluated variables: {}", config);
            return Mono.just(config);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to load/parse CBS client configuration file", e));
        }
    }

}
