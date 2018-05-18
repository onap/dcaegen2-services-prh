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
package org.onap.dcaegen2.services.prh.tasks;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.consumer.ExtendedDmaapConsumerHttpClientImpl;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
class DmaapConsumerTaskImplTest {

    private static ConsumerDmaapModel consumerDmaapModel;
    private static DmaapConsumerTaskImpl dmaapConsumerTask;
    private static ExtendedDmaapConsumerHttpClientImpl extendedDmaapConsumerHttpClient;
    private static AppConfig appConfig;
    private static DmaapConsumerConfiguration dmaapConsumerConfiguration;


    @BeforeAll
    public static void setUp() {
        dmaapConsumerConfiguration = new ImmutableDmaapConsumerConfiguration.Builder().consumerGroup("OpenDCAE-c12")
            .consumerId("c12").dmaapContentType("application/json").dmaapHostName("54.45.33.2").dmaapPortNumber(1234)
            .dmaapProtocol("https").dmaapUserName("PRH").dmaapUserPassword("PRH")
            .dmaapTopicName("unauthenticated.SEC_OTHER_OUTPUT").timeoutMS(-1).messageLimit(-1).build();

        consumerDmaapModel = ImmutableConsumerDmaapModel.builder().ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .pnfName("NOKQTFCOC540002E").build();
        appConfig = mock(AppConfig.class);
    }

    @Test
    public void whenPassedObjectDoesntFit_DoNotThrowsPrhTaskException() {
        //given
        Object response = null;
        extendedDmaapConsumerHttpClient = mock(ExtendedDmaapConsumerHttpClientImpl.class);

        //when
        when(extendedDmaapConsumerHttpClient.getHttpConsumerResponse()).thenReturn(Optional.empty());
        when(appConfig.getDmaapConsumerConfiguration()).thenReturn(dmaapConsumerConfiguration);
        dmaapConsumerTask = spy(new DmaapConsumerTaskImpl(appConfig));
        when(dmaapConsumerTask.resolveConfiguration()).thenReturn(dmaapConsumerConfiguration);
        doReturn(extendedDmaapConsumerHttpClient).when(dmaapConsumerTask).resolveClient();
        try {
            response = dmaapConsumerTask.execute(null);
        } catch (PrhTaskException e) {
            e.printStackTrace();
        }

        //then
        verify(extendedDmaapConsumerHttpClient, times(1)).getHttpConsumerResponse();
        verifyNoMoreInteractions(extendedDmaapConsumerHttpClient);
        Assertions.assertNull(response);
    }

    @Test
    public void whenPassedObjectFits_ReturnsCorrectResponse() throws PrhTaskException {
        //given
        Object response;
        extendedDmaapConsumerHttpClient = mock(ExtendedDmaapConsumerHttpClientImpl.class);
        String message =
            "{\"event\":{\"commonEventHeader\":{\"domain\":\"other\",\"eventId\":\"<<SerialNumber>>-reg\",\"eventName\""
                + ":\"pnfRegistration_5GDU\",\"eventType\":\"pnfRegistration\",\"internalHeaderFields\":{},"
                + "\"lastEpochMicrosec\":1519837825682,\"nfNamingCode\":\"5GRAN\",\"nfcNamingCode\":\"5DU\","
                + "\"priority\":\"Normal\",\"reportingEntityName\":\"5GRAN_DU\",\"sequence\":0,\"sourceId\":"
                + "\"<<SerialNumber>>\",\"sourceName\":\"5GRAN_DU\",\"startEpochMicrosec\":1519837825682,\"version\":3},"
                + "\"otherFields\":{\"otherFieldsVersion\":1,\"pnfFamily\":\"BBU\",\"pnfLastServiceDate\":1517206400,"
                + "\"pnfManufactureDate\":1516406400,\"pnfModelNumber\":\"AJ02\",\"pnfOamIpv4Address\":\"10.16.123.234\","
                + "\"pnfOamIpv6Address\":\"0:0:0:0:0:FFFF:0A10:7BEA\",\"pnfSerialNumber\":\"QTFCOC540002E\","
                + "\"pnfSoftwareVersion\":\"v4.5.0.1\",\"pnfType\":\"AirScale\",\"pnfVendorName\":\"Nokia\"}}}";
        //when
        when(extendedDmaapConsumerHttpClient.getHttpConsumerResponse()).thenReturn(Optional.of(message));
        when(appConfig.getDmaapConsumerConfiguration()).thenReturn(dmaapConsumerConfiguration);
        dmaapConsumerTask = spy(new DmaapConsumerTaskImpl(appConfig));
        when(dmaapConsumerTask.resolveConfiguration()).thenReturn(dmaapConsumerConfiguration);
        doReturn(extendedDmaapConsumerHttpClient).when(dmaapConsumerTask).resolveClient();
        response = dmaapConsumerTask.execute(null);

        //then
        verify(extendedDmaapConsumerHttpClient, times(1)).getHttpConsumerResponse();
        verifyNoMoreInteractions(extendedDmaapConsumerHttpClient);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(consumerDmaapModel, response);

    }
}