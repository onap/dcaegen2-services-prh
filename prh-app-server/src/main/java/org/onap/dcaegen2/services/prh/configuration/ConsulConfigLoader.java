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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableEnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

@Configuration
public class ConsulConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulConfigLoader.class);
    private static final int DEFAULT_CONSUL_PORT = 8500;
    private ImmutableEnvProperties jsonEnvProperties;

    @Value("classpath:consul_config.json")
    private Resource consulConfig;

    public Mono<EnvProperties> evaluate() {
        initFileStreamReader();
        EnvProperties envProperties = ImmutableEnvProperties.builder()
                .consulHost(jsonEnvProperties.consulHost())
                .consulPort(Optional.ofNullable(jsonEnvProperties.consulPort()).orElse(DEFAULT_CONSUL_PORT))
                .cbsName(jsonEnvProperties.cbsName())
                .appName(jsonEnvProperties.appName())
                .build();
        LOGGER.info("Evaluated variables: {}", envProperties);
        return Mono.just(envProperties);
    }

    private void initFileStreamReader() {
        LOGGER.debug("Loading configuration from configuration file");
        Gson gson = new Gson();
        try (InputStream inputStream = consulConfig.getInputStream()) {
            JsonElement rootElement = getJsonElement(inputStream);
            if (rootElement.isJsonObject()) {
                jsonEnvProperties = gson.fromJson(rootElement, ImmutableEnvProperties.class);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load/parse file", e);
        }
    }

    private JsonElement getJsonElement(InputStream inputStream) {
        return new JsonParser().parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
}
