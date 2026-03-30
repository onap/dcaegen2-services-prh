/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2026 Deutsche Telekom Intellectual Property. All rights reserved.
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;

@Slf4j
@RequiredArgsConstructor
class CbsClientConfigurationResolver {

    private final CbsProperties cbsProperties;

    CbsClientConfiguration resolveCbsClientConfiguration() {
        try {
            return CbsClientConfiguration.fromEnvironment();
        } catch (Exception e) {
            log.warn("Failed resolving CBS client configuration from system environments: " + e);
        }
        log.info("Falling back to use default CBS client configuration properties");
        return cbsProperties.toCbsClientConfiguration();
    }

}
