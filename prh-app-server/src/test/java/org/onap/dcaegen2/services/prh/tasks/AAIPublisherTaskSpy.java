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

import org.onap.dcaegen2.services.prh.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.service.AAIProducerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Configuration
public class AAIPublisherTaskSpy {

    @Bean
    @Primary
    public AAIProducerTask registerSimpleAAIPublisherTask() {
        AppConfig appConfig = spy(AppConfig.class);
        doReturn(mock(AAIClientConfiguration.class)).when(appConfig).getAAIClientConfiguration();
        AAIProducerTaskImpl aaiProducerTask = spy(new AAIProducerTaskImpl(appConfig));
        AAIProducerClient aaiProducerClient = mock(AAIProducerClient.class);
        doReturn(mock(AAIClientConfiguration.class)).when(aaiProducerTask).resolveConfiguration();
        doReturn(aaiProducerClient).when(aaiProducerTask).resolveClient();
        return aaiProducerTask;
    }
}
