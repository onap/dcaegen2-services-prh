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

import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/27/18
 */
@Configuration
public class DmaapConsumerTaskTestConfig {

    /**
     * Mocking bean for tests.
     */
    @Bean
    @Primary
    public DmaapConsumerTask registerSimpleDmaapConsumerTask() {
        CbsConfiguration cbsConfiguration = mock(CbsConfiguration.class);
        DmaapConsumerJsonParser dmaapConsumerJsonParser = mock(DmaapConsumerJsonParser.class);
        doReturn(mock(MessageRouterPublishRequest.class)).when(cbsConfiguration).getMessageRouterPublishRequest();
        return new DmaapConsumerTaskImpl(cbsConfiguration, dmaapConsumerJsonParser);
    }
}
