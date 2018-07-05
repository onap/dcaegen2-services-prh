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
package org.onap.dcaegen2.services.prh.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/5/18
 */
public class DMaaPReactiveWebClientTest {


    @Test
    public void builder_shouldBuildDMaaPReactiveWebClient() {
        //given
        DmaapConsumerConfiguration dmaapConsumerConfiguration = mock(DmaapConsumerConfiguration.class);
        WebClient dMaaPReactiveWebClient;
        String dMaaPContentType = "*/*";
        String dMaaPUserName = "DMaaP";
        String dMaaPUserPassword = "DMaaP";

        //when
        when(dmaapConsumerConfiguration.dmaapContentType()).thenReturn(dMaaPContentType);
        when(dmaapConsumerConfiguration.dmaapUserName()).thenReturn(dMaaPUserName);
        when(dmaapConsumerConfiguration.dmaapUserPassword()).thenReturn(dMaaPUserPassword);
        dMaaPReactiveWebClient = new DMaaPReactiveWebClient()
            .fromConfiguration(dmaapConsumerConfiguration)
            .build();

        //then
        Assertions.assertNotNull(dMaaPReactiveWebClient);

    }
}