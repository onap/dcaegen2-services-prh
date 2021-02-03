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

package org.onap.dcaegen2.services.bootstrap;

import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import reactor.util.retry.Retry;

import java.util.Map;

public class CbsPropertySourceLocator implements PropertySourceLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CbsPropertySourceLocator.class);

    private final CbsProperties cbsProperties;
    private final CbsJsonToPropertyMapConverter cbsJsonToPropertyMapConverter;
    private final CbsClientConfigurationResolver cbsClientConfigurationResolver;
    private final CbsClientFactoryFacade cbsClientFactoryFacade;
    private final CbsConfiguration cbsConfiguration;

    public CbsPropertySourceLocator(CbsProperties cbsProperties,
                                    CbsJsonToPropertyMapConverter cbsJsonToPropertyMapConverter,
                                    CbsClientConfigurationResolver cbsClientConfigurationResolver,
                                    CbsClientFactoryFacade cbsClientFactoryFacade,
                                    CbsConfiguration cbsConfiguration) {
        this.cbsProperties = cbsProperties;
        this.cbsJsonToPropertyMapConverter = cbsJsonToPropertyMapConverter;
        this.cbsClientConfigurationResolver = cbsClientConfigurationResolver;
        this.cbsClientFactoryFacade = cbsClientFactoryFacade;
        this.cbsConfiguration = cbsConfiguration;
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        CbsClientConfiguration cbsClientConfiguration = cbsClientConfigurationResolver.resolveCbsClientConfiguration();
        LOGGER.info("Fetching configuration from Config Binding Service @ {}:{} for {}",
                cbsClientConfiguration.hostname(), cbsClientConfiguration.port(), cbsClientConfiguration.appName());
        Map<String, Object> properties = cbsClientFactoryFacade.createCbsClient(cbsClientConfiguration)
                .flatMap(cbsClient -> cbsClient.get(CbsRequests.getAll(RequestDiagnosticContext.create())))
                .doOnError(e -> LOGGER.warn("Failed fetching config properties from CBS - retrying...", e))
                .retryWhen(Retry.
                        backoff(cbsProperties.getFetchRetries().getMaxAttempts(), cbsProperties.getFetchRetries().getFirstBackoff()).
                        maxBackoff(cbsProperties.getFetchRetries().getMaxBackoff()))
                .doOnNext(this::updateCbsConfig)
                .map(cbsJsonToPropertyMapConverter::convertToMap)
                .block();
        return new MapPropertySource("cbs", properties);
    }

    private void updateCbsConfig(JsonObject jsonObject) {
        try {
            cbsConfiguration.parseCBSConfig(jsonObject);
        } catch (Exception e) {
            LOGGER.error("Failed parsing configuration from CBS", e);
            throw e;
        }
    }

}
