/*
 * ============LICENSE_START=======================================================
 * PROJECT
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.service.producer.ExtendedDmaapProducerHttpClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Configuration
public class DmaapProducerTaskSpy {

    @Bean
    @Primary
    public Task registerSimpleDmaapPublisherTask() {
        AppConfig appConfig = spy(AppConfig.class);
        doReturn(mock(DmaapPublisherConfiguration.class)).when(appConfig).getDmaapPublisherConfiguration();
        DmaapPublisherTaskImpl dmaapPublisherTask = spy(new DmaapPublisherTaskImpl(appConfig));
        ExtendedDmaapProducerHttpClientImpl extendedDmaapProducerHttpClient = mock(
            ExtendedDmaapProducerHttpClientImpl.class);
        doReturn(mock(DmaapPublisherConfiguration.class)).when(dmaapPublisherTask).resolveConfiguration();
        doReturn(extendedDmaapProducerHttpClient).when(dmaapPublisherTask).resolveClient();
        return dmaapPublisherTask;
    }
}
