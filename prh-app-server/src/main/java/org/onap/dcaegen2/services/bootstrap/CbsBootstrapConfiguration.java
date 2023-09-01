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
import org.onap.dcaegen2.services.prh.configuration.CbsConfigurationForAutoCommitDisabledMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@EnableConfigurationProperties(CbsProperties.class)
public class CbsBootstrapConfiguration {
    
    private static CbsConfiguration CBS_CONFIGURATION =  new CbsConfiguration();
    private static CbsConfigurationForAutoCommitDisabledMode CBS_CONFIGURATION_FOR_AUTO_COMMIT_DISABLED_MODE =
            new CbsConfigurationForAutoCommitDisabledMode();

    @Bean
    public CbsProperties cbsProperties()
    {
        return new CbsProperties();
    }

    @Bean
    @ConditionalOnProperty(value = "cbs.enabled", matchIfMissing = true)
    @Profile("!autoCommitDisabled")
    public CbsPropertySourceLocator cbsPropertySourceLocator(
            CbsProperties cbsProperties,
            CbsConfiguration cbsConfiguration) {
        
        System.out.println("Trying to return CbsPropertySourceLocator bean");

        return new CbsPropertySourceLocator(
                cbsProperties,
                new CbsJsonToPropertyMapConverter(),
                new CbsClientConfigurationResolver(cbsProperties),
                new CbsClientFactoryFacade(),
                cbsConfiguration);
    }
    
    @Bean
    @ConditionalOnProperty(value = "cbs.enabled", matchIfMissing = true)
    @Profile("autoCommitDisabled")
    public CbsPropertySourceLocatorForAutoCommitDisabled cbsPropertySourceLocatorForAutoCommitDisabled(CbsProperties cbsProperties,
            CbsConfigurationForAutoCommitDisabledMode cbsConfigurationforAutoCommitdisabledMode) {
        
        System.out.println("Trying to return CbsPropertySourceLocatorForAutoCommitDisabled bean"); 

        CbsPropertySourceLocatorForAutoCommitDisabled cbsPropertySourceLocatorACDM = new CbsPropertySourceLocatorForAutoCommitDisabled(cbsProperties,
                new CbsJsonToPropertyMapConverter(), new CbsClientConfigurationResolver(cbsProperties),
                new CbsClientFactoryFacade(), cbsConfigurationforAutoCommitdisabledMode);

        return cbsPropertySourceLocatorACDM;

    }

    @Bean
    @Profile("!autoCommitDisabled")
    public CbsConfiguration cbsConfiguration() {
     return CBS_CONFIGURATION;
    }
    
    @Bean
    @Profile("autoCommitDisabled")
    public CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode() {
        return CBS_CONFIGURATION_FOR_AUTO_COMMIT_DISABLED_MODE;
    }
    
}
