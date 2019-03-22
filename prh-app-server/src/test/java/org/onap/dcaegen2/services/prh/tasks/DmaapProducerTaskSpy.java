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
import org.onap.dcaegen2.services.prh.configuration.CBSConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DMaaPPublisherReactiveHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Configuration
public class DmaapProducerTaskSpy {

    /**
     * Mocking bean for tests.
     *
     * @return DMaaP PublisherTask spy
     */
    @Bean
    @Primary
    public DmaapPublisherTask registerSimpleDmaapPublisherTask() throws SSLException {
        CBSConfiguration cbsConfiguration = spy(CBSConfiguration.class);
        doReturn(mock(DmaapPublisherConfiguration.class)).when(cbsConfiguration).getDmaapPublisherConfiguration();
        DmaapPublisherTaskImpl dmaapPublisherTask = spy(new DmaapPublisherTaskImpl(cbsConfiguration));
        DMaaPPublisherReactiveHttpClient extendedDmaapProducerHttpClient = mock(
            DMaaPPublisherReactiveHttpClient.class);
        doReturn(extendedDmaapProducerHttpClient).when(dmaapPublisherTask).resolveClient();
        return dmaapPublisherTask;
    }
}