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


import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CbsProperties.class)
public class CbsBootstrapConfiguration {
    private static final CbsConfiguration CBS_CONFIGURATION = new CbsConfiguration();

    @Bean
    public CbsProperties cbsProperties() {
        return new CbsProperties();
    }

    @Bean
    @ConditionalOnProperty(value = "cbs.enabled", matchIfMissing = true)
    public CbsPropertySourceLocator cbsPropertySourceLocator(
            CbsProperties cbsProperties,
            CbsConfiguration cbsConfiguration) {

        return new CbsPropertySourceLocator(
                cbsProperties,
                new CbsJsonToPropertyMapConverter(),
                new CbsClientConfigurationResolver(cbsProperties),
                new CbsClientFactoryFacade(),
                cbsConfiguration);
    }

    @Bean
    public CbsConfiguration cbsConfiguration() {
        return CBS_CONFIGURATION;
    }
}
