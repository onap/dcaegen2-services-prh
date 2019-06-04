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

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CbsClientConfigurationResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsClientConfigurationResolver.class);
    private final CbsProperties cbsProperties;

    CbsClientConfigurationResolver(CbsProperties cbsProperties) {
        this.cbsProperties = cbsProperties;
    }

    CbsClientConfiguration resolveCbsClientConfiguration() {
        try {
            return CbsClientConfiguration.fromEnvironment();
        } catch (Exception e) {
            LOGGER.warn("Failed resolving CBS client configuration from system environments: " + e);
        }
        LOGGER.info("Falling back to use of default CBS client configuration properties");
        return cbsProperties.toCbsClientConfiguration();
    }

}
