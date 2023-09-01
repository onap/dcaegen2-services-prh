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


import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableCbsClientConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@ConfigurationProperties("cbs")
public class CbsProperties {

    private Boolean enabled;
    private Duration updatesInterval;
    @NestedConfigurationProperty
    private RetryProperties fetchRetries = new RetryProperties();
    private String appName;

    CbsClientConfiguration toCbsClientConfiguration() {
        return ImmutableCbsClientConfiguration.builder()
                .appName(appName)
                .build();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getUpdatesInterval() {
        return updatesInterval;
    }

    public void setUpdatesInterval(Duration updatesInterval) {
        this.updatesInterval = updatesInterval;
    }

    public RetryProperties getFetchRetries() {
        return fetchRetries;
    }

    public void setFetchRetries(RetryProperties fetchRetries) {
        this.fetchRetries = fetchRetries;
    }

}
