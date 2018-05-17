/*-
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

package org.onap.dcaegen2.services.prh.tasks;

import org.onap.dcaegen2.services.prh.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.AAINotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AAIConsumerTaskImpl extends AAIConsumerTask<AAIClientConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(AAIConsumerTaskImpl.class);

    private final Config prhAppConfig;
    private Optional<String> response;

    @Autowired
    public AAIConsumerTaskImpl(AppConfig prhAppConfig) {
        this.prhAppConfig = prhAppConfig;
    }

    @Override
    protected void consume() throws AAINotFoundException {
    }

    @Override
    public Object execute(Object object) throws AAINotFoundException {
        consume();
        return null;
    }

    @Override
    void initConfigs() {
        logger.trace("initConfigs for AAIConsumerTaskImpl not needed/supported");
    }

    @Override
    protected AAIClientConfiguration resolveConfiguration() {
        return prhAppConfig.getAAIClientConfiguration();
    }
}
