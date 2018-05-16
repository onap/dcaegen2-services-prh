/*
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
package org.onap.dcaegen2.services.prh.configuration;

import org.onap.dcaegen2.services.service.AAIProducerClient;
import org.onap.dcaegen2.services.service.consumer.ExtendedDmaapConsumerHttpClientImpl;
import org.onap.dcaegen2.services.service.producer.ExtendedDmaapProducerHttpClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/15/18
 */

@Configuration
public class PrhConfigBeans {

    private AppConfig appConfig;

    @Autowired
    public PrhConfigBeans(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Bean
    public AAIProducerClient createProducerClient() {
        return new AAIProducerClient(appConfig.getAAIClientConfiguration());
    }

    @Bean
    public ExtendedDmaapProducerHttpClientImpl createDmaapProducerClient() {
        return new ExtendedDmaapProducerHttpClientImpl(appConfig.getDmaapPublisherConfiguration());
    }

    @Bean
    public ExtendedDmaapConsumerHttpClientImpl createDmaapConsumerClient() {
        return new ExtendedDmaapConsumerHttpClientImpl(appConfig.getDmaapConsumerConfiguration());
    }
}
