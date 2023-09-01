/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property. All rights reserved.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

class CbsClientConfigurationResolverTest {

    private CbsProperties cbsProperties;

    @BeforeEach
    void setUp() {
        cbsProperties = new CbsProperties();
        cbsProperties.setAppName("client-app-name");
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CONFIG_BINDING_SERVICE", matches = ".+")
    void whenCbsEnvPropertiesAreNotePresentInEnvironment_ShouldFallbackToLoadingDefaultsFromCbsProperties() {
        CbsClientConfiguration config = new CbsClientConfigurationResolver(cbsProperties)
                .resolveCbsClientConfiguration();

        assertThat(config.appName()).isEqualTo(cbsProperties.getAppName());
    }
}
