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

package org.onap.dcaegen2.services.prh.service.producer;

import static org.mockito.Mockito.mock;

import javax.net.ssl.SSLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;



class PublisherReactiveHttpClientFactoryTest {

    private DmaaPRestTemplateFactory restTemplateFactory = mock(DmaaPRestTemplateFactory.class);
    private DmaapPublisherConfiguration dmaapPublisherConfiguration = mock(DmaapPublisherConfiguration.class);
    private PublisherReactiveHttpClientFactory httpClientFactory =
            new PublisherReactiveHttpClientFactory(restTemplateFactory);

    @Test
    void create_shouldReturnNotNullFactoryInstance() throws SSLException {
        Assertions.assertNotNull(httpClientFactory.create(dmaapPublisherConfiguration));
    }
}