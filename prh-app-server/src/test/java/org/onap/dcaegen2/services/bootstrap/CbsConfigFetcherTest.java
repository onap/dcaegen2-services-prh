/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019-2021 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2023-2026 Deutsche Telekom Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.bootstrap;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.RequestPath;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;
import reactor.test.scheduler.VirtualTimeScheduler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CbsConfigFetcherTest {

    private static final RequestPath GET_ALL_REQUEST_PATH = CbsRequests.getAll(RequestDiagnosticContext.create())
            .requestPath();

    private final CbsProperties cbsProperties = new CbsProperties();
    @Mock
    private CbsClientConfigurationResolver cbsClientConfigurationResolver;
    @Mock
    private CbsClientFactoryFacade cbsClientFactoryFacade;
    @Mock
    private CbsConfiguration cbsConfiguration;
    @Mock
    private CbsClient cbsClient;
    @Mock
    private JsonObject cbsConfigJsonObject;
    private VirtualTimeScheduler virtualTimeScheduler;
    private CbsConfigFetcher cbsConfigFetcher;

    @BeforeEach
    void setup() {
        virtualTimeScheduler = VirtualTimeScheduler.getOrSet(true);
        cbsConfigFetcher = new CbsConfigFetcher(cbsProperties, cbsClientConfigurationResolver,
                cbsClientFactoryFacade, cbsConfiguration);
    }

    @AfterEach
    void cleanup() {
        virtualTimeScheduler.dispose();
    }

    @Test
    void shouldUpdateCbsConfigurationStateBasedOnDataFetchedUsingCbsClient() {
        when(cbsClientFactoryFacade.createCbsClient(any())).thenReturn(Mono.just(cbsClient));
        when(cbsClient.get(argThat(request -> request.requestPath().equals(GET_ALL_REQUEST_PATH))))
                .thenReturn(Mono.just(cbsConfigJsonObject));

        cbsConfigFetcher.fetchAndParse();

        verify(cbsConfiguration).parseCBSConfig(cbsConfigJsonObject);
    }

    @Test
    void shouldPropagateExceptionWhenCbsConfigurationParsingFails() {
        when(cbsClientFactoryFacade.createCbsClient(any())).thenReturn(Mono.just(cbsClient));
        when(cbsClient.get(any(CbsRequest.class))).thenReturn(Mono.just(cbsConfigJsonObject));

        RuntimeException someCbsConfigParsingException = new RuntimeException("boom!");
        doThrow(someCbsConfigParsingException).when(cbsConfiguration).parseCBSConfig(cbsConfigJsonObject);

        assertThatThrownBy(() -> cbsConfigFetcher.fetchAndParse())
                .isSameAs(someCbsConfigParsingException);
    }

    @Test
    void shouldRetryFetchingConfigFromCbsInCaseOfFailure() {
        when(cbsClientFactoryFacade.createCbsClient(any())).thenReturn(Mono.just(cbsClient));
        assumeThat(cbsProperties.getFetchRetries().getMaxAttempts()).isGreaterThan(1);
        when(cbsClient.get(any(CbsRequest.class))).thenReturn(Mono.defer(() -> {
            virtualTimeScheduler.advanceTimeBy(cbsProperties.getFetchRetries().getMaxBackoff());
            return Mono.error(new RuntimeException("some connection failure"));
        })).thenReturn(Mono.just(cbsConfigJsonObject));

        cbsConfigFetcher.fetchAndParse();

        verify(cbsConfiguration).parseCBSConfig(cbsConfigJsonObject);
    }

    @Test
    void shouldFailAfterExhaustingAllOfConfiguredRetryAttempts() {
        when(cbsClientFactoryFacade.createCbsClient(any())).thenReturn(Mono.just(cbsClient));
        assumeThat(cbsProperties.getFetchRetries().getMaxAttempts()).isGreaterThan(1);
        when(cbsClient.get(any(CbsRequest.class))).thenReturn(Mono.defer(() -> {
            virtualTimeScheduler.advanceTimeBy(cbsProperties.getFetchRetries().getMaxBackoff());
            return Mono.error(new RuntimeException("some connection failure"));
        }));

        assertThatThrownBy(() -> cbsConfigFetcher.fetchAndParse()).hasMessageContaining("Retries exhausted")
                .hasMessageContaining(cbsProperties.getFetchRetries().getMaxAttempts().toString());
    }

}
