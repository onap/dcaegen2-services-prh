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

import java.util.Optional;
import java.util.Properties;
import org.onap.dcaegen2.services.prh.exceptions.EnvironmentLoaderException;
import org.onap.dcaegen2.services.prh.model.EnvProperties;
import org.onap.dcaegen2.services.prh.model.ImmutableEnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 8/10/18
 */
class EnvironmentProcessor {

    private static final int DEFAULT_CONSUL_PORT = 8500;
    private static Logger logger = LoggerFactory.getLogger(EnvironmentProcessor.class);

    private EnvironmentProcessor() {
    }

    static Mono<EnvProperties> evaluate(Properties systemEnvironment) {
        logger.info("Loading configuration from system environment variables {}", systemEnvironment);
        EnvProperties envProperties;
        try {
            envProperties = ImmutableEnvProperties.builder().consulHost(getConsulHost(systemEnvironment))
                .consulPort(getConsultPort(systemEnvironment)).cbsName(getConfigBindingService(systemEnvironment))
                .appName(getService(systemEnvironment)).build();
        } catch (EnvironmentLoaderException e) {
            return Mono.error(e);
        }
        logger.info("Evaluated environment system variables {}", envProperties);
        return Mono.just(envProperties);
    }

    private static String getConsulHost(Properties systemEnvironments) throws EnvironmentLoaderException {
        return Optional.ofNullable(systemEnvironments.getProperty("CONSUL_HOST"))
            .orElseThrow(() -> new EnvironmentLoaderException("$CONSUL_HOST environment has not been defined"));
    }

    private static Integer getConsultPort(Properties systemEnvironments) {
        return Optional.ofNullable(systemEnvironments.getProperty("CONSUL_PORT")).map(Integer::valueOf)
            .orElseGet(EnvironmentProcessor::getDefaultPortOfConsul);
    }

    private static String getConfigBindingService(Properties systemEnvironments) throws EnvironmentLoaderException {
        return Optional.ofNullable(systemEnvironments.getProperty("CONFIG_BINDING_SERVICE"))
            .orElseThrow(
                () -> new EnvironmentLoaderException("$CONFIG_BINDING_SERVICE environment has not been defined"));
    }

    private static String getService(Properties systemEnvironments) throws EnvironmentLoaderException {
        return Optional.ofNullable(Optional.ofNullable(systemEnvironments.getProperty("HOSTNAME"))
            .orElse(systemEnvironments.getProperty("SERVICE_NAME")))
            .orElseThrow(() -> new EnvironmentLoaderException(
                "Neither $HOSTNAME/$SERVICE_NAME have not been defined as system environment"));
    }

    private static Integer getDefaultPortOfConsul() {
        logger.warn("$CONSUL_PORT environment has not been defined");
        logger.warn("$CONSUL_PORT variable will be set to default port {}", DEFAULT_CONSUL_PORT);
        return DEFAULT_CONSUL_PORT;
    }
}

