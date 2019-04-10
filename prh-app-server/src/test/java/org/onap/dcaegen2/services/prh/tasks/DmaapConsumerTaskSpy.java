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

import javax.net.ssl.SSLException;

import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer.DMaaPConsumerReactiveHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/27/18
 */
@Configuration
public class DmaapConsumerTaskSpy {

    /**
     * Mocking bean for tests.
     *
     * @return DMaaP ConsumerTask spy
     */
    @Bean
    @Primary
    public DmaapConsumerTask registerSimpleDmaapConsumerTask() throws SSLException {
        CbsConfiguration cbsConfiguration = spy(CbsConfiguration.class);
        doReturn(mock(DmaapConsumerConfiguration.class)).when(cbsConfiguration).getDmaapConsumerConfiguration();
        DmaapConsumerTaskImpl dmaapConsumerTask = spy(new DmaapConsumerTaskImpl(cbsConfiguration));
        DMaaPConsumerReactiveHttpClient dmaapConsumerReactiveHttpClient = mock(
            DMaaPConsumerReactiveHttpClient.class);
        doReturn(dmaapConsumerReactiveHttpClient).when(dmaapConsumerTask).resolveClient();
        return dmaapConsumerTask;
    }
}
