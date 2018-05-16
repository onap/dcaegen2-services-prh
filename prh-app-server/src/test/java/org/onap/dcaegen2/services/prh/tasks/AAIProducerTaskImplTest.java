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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.config.ImmutableAAIClientConfiguration;
import org.onap.dcaegen2.services.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.exceptions.AAINotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.service.AAIProducerClient;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/14/18
 */
class AAIProducerTaskImplTest {

    private static ConsumerDmaapModel consumerDmaapModel;
    private static AAIProducerTaskImpl aaiProducerTask;

    private static final String AAI_HOST = "/aai/v11/network/pnfs/pnf/NOKQTFCOC540002E";
    private static final Integer PORT = 1234;
    private static final String PROTOCOL = "https";
    private static final String USER_NAME_PASSWORD = "PRH";
    private static final String BASE_PATH = "/aai/v11";
    private static final String PNF_PATH = "/network/pnfs/pnf";

    private static AAIClientConfiguration aaiClientConfiguration;
    private static AAIProducerClient aaiProducerClient;

    @BeforeAll
    public static void setUp() {
        aaiClientConfiguration = new ImmutableAAIClientConfiguration.Builder()
            .aaiHost(AAI_HOST)
            .aaiHostPortNumber(PORT)
            .aaiProtocol(PROTOCOL)
            .aaiUserName(USER_NAME_PASSWORD)
            .aaiUserPassword(USER_NAME_PASSWORD)
            .aaiIgnoreSSLCertificateErrors(true)
            .aaiBasePath(BASE_PATH)
            .aaiPnfPath(PNF_PATH)
            .build();
        consumerDmaapModel = ImmutableConsumerDmaapModel.builder().ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .pnfName("NOKQTFCOC540002E").build();

    }

    @Test
    public void whenPassedObjectDoesntFit_ThrowsPrhTaskException() throws IOException {
        //given
        Object response = null;
        aaiProducerClient = mock(AAIProducerClient.class);

        //when

        //when
        try {
            when(aaiProducerClient.getHttpResponse(consumerDmaapModel)).thenReturn(Optional.of(200));
            aaiProducerTask = new AAIProducerTaskImpl(aaiProducerClient);
            response = aaiProducerTask.execute("Some string");
        } catch (PrhTaskException e) {
            e.printStackTrace();
        }

        //then
        Assertions.assertNull(response);
    }

    @Test
    public void whenPassedObjectFits_ReturnsCorrectStatus() throws AAINotFoundException, IOException {
        //given
        Object response;
        aaiProducerClient = mock(AAIProducerClient.class);

        //when
        when(aaiProducerClient.getHttpResponse(consumerDmaapModel)).thenReturn(Optional.of(200));
        aaiProducerTask = new AAIProducerTaskImpl(aaiProducerClient);
        aaiProducerTask.initConfigs();
        response = aaiProducerTask.execute(consumerDmaapModel);

        //then
        verify(aaiProducerClient, times(1)).getHttpResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(aaiProducerClient);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(consumerDmaapModel, response);

    }

    @Test
    public void whenPassedObjectFits_butIncorrectResponseReturns() throws IOException {
        //given
        Object response = null;
        aaiProducerClient = mock(AAIProducerClient.class);
        //when
        when(aaiProducerClient.getHttpResponse(consumerDmaapModel)).thenReturn(Optional.of(400));
        aaiProducerTask = new AAIProducerTaskImpl(aaiProducerClient);
        aaiProducerTask.initConfigs();
        try {
            response = aaiProducerTask.execute(consumerDmaapModel);
        } catch (AAINotFoundException e) {
            e.printStackTrace();
        }

        //then
        verify(aaiProducerClient, times(1)).getHttpResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(aaiProducerClient);
        Assertions.assertNull(response);
    }

    @Test
    public void whenPassedObjectFits_ThrowsIOException() throws IOException {
        //given
        Object response = null;
        aaiProducerClient = mock(AAIProducerClient.class);
        //when
        when(aaiProducerClient.getHttpResponse(consumerDmaapModel)).thenThrow(IOException.class);
        aaiProducerTask = new AAIProducerTaskImpl(aaiProducerClient);
        aaiProducerTask.initConfigs();
        try {
            response = aaiProducerTask.execute(consumerDmaapModel);
        } catch (AAINotFoundException e) {
            e.printStackTrace();
        }

        //then
        verify(aaiProducerClient, times(1)).getHttpResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(aaiProducerClient);
        Assertions.assertNull(response);
    }
}