/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019-2021 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.bootstrap;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.util.retry.Retry;

/**
 * Fetches the application configuration from the DCAE Config Binding Service (CBS)
 * and parses it into the shared {@link CbsConfiguration} singleton.
 *
 * <p>This replaces the former Spring Cloud bootstrap {@code PropertySourceLocator}: PRH
 * consumes CBS configuration exclusively through the {@link CbsConfiguration} accessor
 * (not through the Spring {@code Environment}), so the fetch is performed directly here —
 * once at startup (blocking, with retries) and again on each scheduled refresh.
 */
@Slf4j
@RequiredArgsConstructor
public class CbsConfigFetcher {

    private final CbsProperties cbsProperties;
    private final CbsClientConfigurationResolver cbsClientConfigurationResolver;
    private final CbsClientFactoryFacade cbsClientFactoryFacade;
    private final CbsConfiguration cbsConfiguration;

    /**
     * Fetches the configuration from CBS and updates the {@link CbsConfiguration}
     * singleton. Blocks until CBS responds, retrying per {@code cbs.fetch-retries}.
     */
    public void fetchAndParse() {
        CbsClientConfiguration cbsClientConfiguration =
                cbsClientConfigurationResolver.resolveCbsClientConfiguration();
        cbsClientFactoryFacade.createCbsClient(cbsClientConfiguration)
                .flatMap(cbsClient -> cbsClient.get(CbsRequests.getAll(RequestDiagnosticContext.create())))
                .doOnError(e -> log.warn("Failed loading configuration - retrying...", e))
                .retryWhen(Retry
                        .backoff(cbsProperties.getFetchRetries().getMaxAttempts(),
                                cbsProperties.getFetchRetries().getFirstBackoff())
                        .maxBackoff(cbsProperties.getFetchRetries().getMaxBackoff()))
                .doOnNext(this::updateCbsConfig)
                .block();
    }

    private void updateCbsConfig(JsonObject jsonObject) {
        try {
            log.info("Updating CBS configuration");
            cbsConfiguration.parseCBSConfig(jsonObject);
        } catch (Exception e) {
            log.error("Failed parsing configuration", e);
            throw e;
        }
    }

}
