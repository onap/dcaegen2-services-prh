/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Wires the CBS configuration into the application context.
 *
 * <p>PRH used to obtain its configuration through a Spring Cloud bootstrap
 * {@code PropertySourceLocator} running in a dedicated bootstrap context. Since PRH
 * consumes CBS configuration exclusively through the {@link CbsConfiguration} accessor
 * (not through the Spring {@code Environment}), that machinery has been replaced with a
 * plain auto-configuration: the initial fetch happens synchronously while the
 * {@link CbsConfiguration} bean is created — before any consumer's {@code @PostConstruct}
 * runs, since they inject {@code Config} — and {@link CbsConfigRefreshScheduler} re-fetches
 * on a schedule.
 *
 * <p>The {@code cbsConfiguration} bean is {@link ConditionalOnMissingBean} so tests can
 * supply a pre-parsed {@link CbsConfiguration} instead of contacting a real CBS.
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CbsProperties.class)
public class CbsConfigAutoConfiguration {

    private static final CbsConfiguration CBS_CONFIGURATION = new CbsConfiguration();

    @Bean
    public CbsConfigFetcher cbsConfigFetcher(CbsProperties cbsProperties, CbsConfiguration cbsConfiguration) {
        return new CbsConfigFetcher(
                cbsProperties,
                new CbsClientConfigurationResolver(cbsProperties),
                new CbsClientFactoryFacade(),
                cbsConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public CbsConfiguration cbsConfiguration(CbsProperties cbsProperties) {
        if (Boolean.FALSE.equals(cbsProperties.getEnabled())) {
            log.info("CBS fetch disabled (cbs.enabled=false); using empty configuration");
            return CBS_CONFIGURATION;
        }
        new CbsConfigFetcher(
                cbsProperties,
                new CbsClientConfigurationResolver(cbsProperties),
                new CbsClientFactoryFacade(),
                CBS_CONFIGURATION).fetchAndParse();
        return CBS_CONFIGURATION;
    }
}
