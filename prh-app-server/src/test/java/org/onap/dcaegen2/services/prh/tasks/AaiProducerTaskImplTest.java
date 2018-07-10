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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableAaiClientConfiguration;

import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.exceptions.AaiNotFoundException;

import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;

import org.onap.dcaegen2.services.prh.service.AaiProducerClient;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/14/18
 */
class AaiProducerTaskImplTest {


    private static final String AAI_HOST = "/aai/v11/network/pnfs/pnf/NOKQTFCOC540002E";
    private static final Integer PORT = 1234;
    private static final String PROTOCOL = "https";
    private static final String USER_NAME_PASSWORD = "PRH";
    private static final String BASE_PATH = "/aai/v11";
    private static final String PNF_PATH = "/network/pnfs/pnf";

    private static ConsumerDmaapModel consumerDmaapModel;
    private static AaiProducerTaskImpl aaiProducerTask;
    private static AaiClientConfiguration aaiClientConfiguration;
    private static AaiProducerClient aaiProducerClient;
    private static AppConfig appConfig;

    @BeforeAll
    static void setUp() {
        aaiClientConfiguration = new ImmutableAaiClientConfiguration.Builder()
            .aaiHost(AAI_HOST)
            .aaiHostPortNumber(PORT)
            .aaiProtocol(PROTOCOL)
            .aaiUserName(USER_NAME_PASSWORD)
            .aaiUserPassword(USER_NAME_PASSWORD)
            .aaiIgnoreSslCertificateErrors(true)
            .aaiBasePath(BASE_PATH)
            .aaiPnfPath(PNF_PATH)
            .build();
        consumerDmaapModel = ImmutableConsumerDmaapModel.builder().ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .pnfName("NOKQTFCOC540002E").build();
        appConfig = mock(AppConfig.class);

    }

    @Test
    void whenPassedObjectDoesntFit_ThrowsPrhTaskException() {
        //given/when/
        when(appConfig.getAaiClientConfiguration()).thenReturn(aaiClientConfiguration);
        aaiProducerTask = new AaiProducerTaskImpl(appConfig);
        Executable executableCode = () -> aaiProducerTask.execute(null);

        //then
        Assertions
            .assertThrows(PrhTaskException.class, executableCode, "Passing wrong object type to execute function");
    }

    @Test
    void whenPassedObjectFits_ReturnsCorrectStatus() throws AaiNotFoundException, URISyntaxException {
        //given/when
        getAaiProducerTask_whenMockingResponseObject(200, false);
        ConsumerDmaapModel response = aaiProducerTask.execute(consumerDmaapModel);

        //then
        verify(aaiProducerClient, times(1)).getHttpResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(aaiProducerClient);
        Assertions.assertEquals(consumerDmaapModel, response);

    }


    @Test
    void whenPassedObjectFits_butIncorrectResponseReturns() throws URISyntaxException {
        //given/when
        getAaiProducerTask_whenMockingResponseObject(400, false);
        Executable executableCode = () -> aaiProducerTask.execute(consumerDmaapModel);
        Assertions
            .assertThrows(PrhTaskException.class, executableCode, "Incorrect status code in response message");
        //then
        verify(aaiProducerClient, times(1)).getHttpResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(aaiProducerClient);
    }

    @Test
    void whenPassedObjectFits_butHttpClientThrowsIoExceptionHandleIt() throws URISyntaxException {
        //given/when
        getAaiProducerTask_whenMockingResponseObject(0, true);

        Executable executableCode = () -> aaiProducerTask.execute(consumerDmaapModel);
        Assertions
            .assertThrows(PrhTaskException.class, executableCode, "");
        //then
        verify(aaiProducerClient, times(1)).getHttpResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(aaiProducerClient);
    }


    private static void getAaiProducerTask_whenMockingResponseObject(int statusCode, boolean throwsException)
        throws URISyntaxException {
        //given
        aaiProducerClient = mock(AaiProducerClient.class);
        if (throwsException) {
            when(aaiProducerClient.getHttpResponse(consumerDmaapModel)).thenThrow(URISyntaxException.class);
        } else {
            when(aaiProducerClient.getHttpResponse(consumerDmaapModel)).thenReturn(Optional.of(statusCode));
        }
        when(appConfig.getAaiClientConfiguration()).thenReturn(aaiClientConfiguration);
        aaiProducerTask = spy(new AaiProducerTaskImpl(appConfig));
        when(aaiProducerTask.resolveConfiguration()).thenReturn(aaiClientConfiguration);
        doReturn(aaiProducerClient).when(aaiProducerTask).resolveClient();
    }
}