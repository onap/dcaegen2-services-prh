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
import org.onap.dcaegen2.services.prh.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableAAIClientConfiguration;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.configuration.AppConfig;
import org.onap.dcaegen2.services.prh.exceptions.AAINotFoundException;
import org.onap.dcaegen2.services.prh.service.AAIProducerClient;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/14/18
 */
class AAIProducerTaskImplTest {


    private static final String AAI_HOST = "/aai/v11/network/pnfs/pnf/NOKQTFCOC540002E";
    private static final Integer PORT = 1234;
    private static final String PROTOCOL = "https";
    private static final String USER_NAME_PASSWORD = "PRH";
    private static final String BASE_PATH = "/aai/v11";
    private static final String PNF_PATH = "/network/pnfs/pnf";

    private static ConsumerDmaapModel consumerDmaapModel;
    private static AAIProducerTaskImpl aaiProducerTask;
    private static AAIClientConfiguration aaiClientConfiguration;
    private static AAIProducerClient aaiProducerClient;
    private static AppConfig appConfig;

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
        appConfig = mock(AppConfig.class);

    }

    @Test
    public void whenPassedObjectDoesntFit_ThrowsPrhTaskException() {
        //given/when/
        when(appConfig.getAAIClientConfiguration()).thenReturn(aaiClientConfiguration);
        aaiProducerTask = new AAIProducerTaskImpl(appConfig);
        Executable executableCode = () -> aaiProducerTask.execute(null);

        //then
        Assertions
            .assertThrows(PrhTaskException.class, executableCode, "Passing wrong object type to execute function");
    }

    @Test
    public void whenPassedObjectFits_ReturnsCorrectStatus() throws AAINotFoundException, URISyntaxException {
        //given/when
        getAAIProducerTask_whenMockingResponseObject(200, false);
        ConsumerDmaapModel response = aaiProducerTask.execute(consumerDmaapModel);

        //then
        verify(aaiProducerClient, times(1)).getHttpResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(aaiProducerClient);
        Assertions.assertEquals(consumerDmaapModel, response);

    }


    @Test
    public void whenPassedObjectFits_butIncorrectResponseReturns() throws IOException, URISyntaxException {
        //given/when
        getAAIProducerTask_whenMockingResponseObject(400, false);
        Executable executableCode = () -> aaiProducerTask.execute(consumerDmaapModel);
        Assertions
            .assertThrows(PrhTaskException.class, executableCode, "Incorrect status code in response message");
        //then
        verify(aaiProducerClient, times(1)).getHttpResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(aaiProducerClient);
    }

    @Test
    public void whenPassedObjectFits_butHTTPClientThrowsIOExceptionHandleIt() throws URISyntaxException {
        //given/when
        getAAIProducerTask_whenMockingResponseObject(0, true);

        Executable executableCode = () -> aaiProducerTask.execute(consumerDmaapModel);
        Assertions
            .assertThrows(PrhTaskException.class, executableCode, "");
        //then
        verify(aaiProducerClient, times(1)).getHttpResponse(any(ConsumerDmaapModel.class));
        verifyNoMoreInteractions(aaiProducerClient);
    }


    private static void getAAIProducerTask_whenMockingResponseObject(int statusCode, boolean throwsException)
        throws URISyntaxException {
        //given
        aaiProducerClient = mock(AAIProducerClient.class);
        if (throwsException) {
            when(aaiProducerClient.getHttpResponse(consumerDmaapModel)).thenThrow(URISyntaxException.class);
        } else {
            when(aaiProducerClient.getHttpResponse(consumerDmaapModel)).thenReturn(Optional.of(statusCode));
        }
        when(appConfig.getAAIClientConfiguration()).thenReturn(aaiClientConfiguration);
        aaiProducerTask = spy(new AAIProducerTaskImpl(appConfig));
        when(aaiProducerTask.resolveConfiguration()).thenReturn(aaiClientConfiguration);
        doReturn(aaiProducerClient).when(aaiProducerTask).resolveClient();
    }
}