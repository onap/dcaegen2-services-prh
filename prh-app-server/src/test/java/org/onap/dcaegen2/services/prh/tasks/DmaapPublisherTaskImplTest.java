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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableDmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.producer.ExtendedDmaapProducerHttpClientImpl;
import org.springframework.http.HttpStatus;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
class DmaapPublisherTaskImplTest {

    private static ConsumerDmaapModel consumerDmaapModel;
    private static DmaapPublisherTaskImpl dmaapPublisherTask;
    private static ExtendedDmaapProducerHttpClientImpl extendedDmaapProducerHttpClient;
    private static AppConfig appConfig;
    private static DmaapPublisherConfiguration dmaapPublisherConfiguration;

    @BeforeAll
    public static void setUp() {
        dmaapPublisherConfiguration = new ImmutableDmaapPublisherConfiguration.Builder()
            .dmaapContentType("application/json").dmaapHostName("54.45.33.2").dmaapPortNumber(1234)
            .dmaapProtocol("https").dmaapUserName("PRH").dmaapUserPassword("PRH")
            .dmaapTopicName("unauthenticated.SEC_OTHER_OUTPUT").build();
        consumerDmaapModel = ImmutableConsumerDmaapModel.builder().ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .pnfName("NOKQTFCOC540002E").build();
        appConfig = mock(AppConfig.class);
    }

    @Test
    public void whenPassedObjectDoesntFit_ThrowsPrhTaskException() throws IOException {
        //given
        Object response = null;

        //when
        when(appConfig.getDmaapPublisherConfiguration()).thenReturn(dmaapPublisherConfiguration);
        try {
            dmaapPublisherTask = new DmaapPublisherTaskImpl(appConfig);
            response = dmaapPublisherTask.execute(null);
        } catch (PrhTaskException e) {
            e.printStackTrace();
        }

        //then
        Assertions.assertNull(response);
    }

    @Test
    public void whenPassedObjectFits_ReturnsCorrectStatus() throws PrhTaskException {
        //given
        Object response;
        extendedDmaapProducerHttpClient = mock(ExtendedDmaapProducerHttpClientImpl.class);

        //when
        when(extendedDmaapProducerHttpClient.getHttpProducerResponse(consumerDmaapModel))
            .thenReturn(Optional.of(HttpStatus.OK.toString()));
        when(appConfig.getDmaapPublisherConfiguration()).thenReturn(dmaapPublisherConfiguration);
        dmaapPublisherTask = spy(new DmaapPublisherTaskImpl(appConfig));
        when(dmaapPublisherTask.resolveConfiguration()).thenReturn(dmaapPublisherConfiguration);
        doReturn(extendedDmaapProducerHttpClient).when(dmaapPublisherTask).resolveClient();
        response = dmaapPublisherTask.execute(consumerDmaapModel);

        //then
        verify(extendedDmaapProducerHttpClient, times(1))
            .getHttpProducerResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(extendedDmaapProducerHttpClient);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK.toString(), response);
    }

    @Test
    public void whenPassedObjectFits_butIncorrectResponseReturns() {
        //given
        Object response = null;
        extendedDmaapProducerHttpClient = mock(ExtendedDmaapProducerHttpClientImpl.class);
        //when
        when(extendedDmaapProducerHttpClient.getHttpProducerResponse(consumerDmaapModel))
            .thenReturn(Optional.of("400"));
        when(appConfig.getDmaapPublisherConfiguration()).thenReturn(dmaapPublisherConfiguration);
        dmaapPublisherTask = spy(new DmaapPublisherTaskImpl(appConfig));
        when(dmaapPublisherTask.resolveConfiguration()).thenReturn(dmaapPublisherConfiguration);
        doReturn(extendedDmaapProducerHttpClient).when(dmaapPublisherTask).resolveClient();
        try {
            response = dmaapPublisherTask.execute(consumerDmaapModel);
        } catch (PrhTaskException e) {
            e.printStackTrace();
        }

        //then
        verify(extendedDmaapProducerHttpClient, times(1)).getHttpProducerResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(extendedDmaapProducerHttpClient);
        Assertions.assertNull(response);
    }
}