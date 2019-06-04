/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

import com.google.common.collect.ImmutableMap;
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
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.RequestPath;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import reactor.core.publisher.Mono;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CbsPropertySourceLocatorTest {

    private static final RequestPath GET_ALL_REQUEST_PATH = CbsRequests.getAll(RequestDiagnosticContext.create()).requestPath();

    private CbsProperties cbsProperties = new CbsProperties();
    @Mock
    private CbsJsonToPropertyMapConverter cbsJsonToPropertyMapConverter;
    @Mock
    private CbsClientConfiguration cbsClientConfiguration;
    @Mock
    private CbsClientConfigurationResolver cbsClientConfigurationResolver;
    @Mock
    private CbsClientFactoryFacade cbsClientFactoryFacade;
    @Mock
    private CbsConfiguration cbsConfiguration;
    @Mock
    private Environment environment;
    @Mock
    private CbsClient cbsClient;
    @Mock
    private JsonObject cbsConfigJsonObject;
    private Map<String, Object> cbsConfigMap = ImmutableMap.of("foo", "bar");

    private VirtualTimeScheduler virtualTimeScheduler;

    private CbsPropertySourceLocator cbsPropertySourceLocator;


    @BeforeEach
    void setup() {
        virtualTimeScheduler = VirtualTimeScheduler.getOrSet();

        when(cbsClientConfigurationResolver.resolveCbsClientConfiguration()).thenReturn(cbsClientConfiguration);
        when(cbsClientFactoryFacade.createCbsClient(cbsClientConfiguration)).thenReturn(Mono.just(cbsClient));

        cbsPropertySourceLocator = new CbsPropertySourceLocator(
                cbsProperties, cbsJsonToPropertyMapConverter, cbsClientConfigurationResolver,
                cbsClientFactoryFacade, cbsConfiguration);
    }

    @AfterEach
    void cleanup() {
        virtualTimeScheduler.dispose();
    }


    @Test
    void shouldBuildCbsPropertySourceBasedOnDataFetchedUsingCbsClient() {
        when(cbsClient.get(argThat(request -> request.requestPath().equals(GET_ALL_REQUEST_PATH))))
                .thenReturn(Mono.just(cbsConfigJsonObject));
        when(cbsJsonToPropertyMapConverter.convertToMap(cbsConfigJsonObject)).thenReturn(cbsConfigMap);

        PropertySource<?> propertySource = cbsPropertySourceLocator.locate(environment);

        assertThat(propertySource).extracting(PropertySource::getName).isEqualTo("cbs");
        assertThat(propertySource).extracting(s -> s.getProperty("foo")).isEqualTo("bar");
    }


    @Test
    void shouldUpdateCbsConfigurationStateBasedOnDataFetchedUsingCbsClient() {
        when(cbsClient.get(argThat(request -> request.requestPath().equals(GET_ALL_REQUEST_PATH))))
                .thenReturn(Mono.just(cbsConfigJsonObject));
        when(cbsJsonToPropertyMapConverter.convertToMap(cbsConfigJsonObject)).thenReturn(cbsConfigMap);

        cbsPropertySourceLocator.locate(environment);

        verify(cbsConfiguration).parseCBSConfig(cbsConfigJsonObject);
    }


    @Test
    void shouldPropagateExceptionWhenCbsConfigurationParsingFails() {
        when(cbsClient.get(any(CbsRequest.class))).thenReturn(Mono.just(cbsConfigJsonObject));

        RuntimeException someCbsConfigParsingException = new RuntimeException("boom!");
        doThrow(someCbsConfigParsingException).when(cbsConfiguration).parseCBSConfig(cbsConfigJsonObject);

        assertThatThrownBy(() -> cbsPropertySourceLocator.locate(environment))
                .isSameAs(someCbsConfigParsingException);
    }

    @Test
    void shouldRetryFetchingConfigFromCbsInCaseOfFailure() {
        assumeThat(cbsProperties.getFetchRetries().getMaxAttempts()).isGreaterThan(1);
        when(cbsClient.get(any(CbsRequest.class)))
                .thenReturn(Mono.defer(() -> {
                        virtualTimeScheduler.advanceTimeBy(cbsProperties.getFetchRetries().getMaxBackoff());
                        return Mono.error(new RuntimeException("some connection failure"));
                }))
                .thenReturn(Mono.just(cbsConfigJsonObject));
        when(cbsJsonToPropertyMapConverter.convertToMap(cbsConfigJsonObject)).thenReturn(cbsConfigMap);

        PropertySource<?> propertySource = cbsPropertySourceLocator.locate(environment);

        assertThat(propertySource).extracting(s -> s.getProperty("foo")).isEqualTo("bar");
    }

    @Test
    void shouldFailAfterExhaustingAllOfConfiguredRetryAttempts() {
        assumeThat(cbsProperties.getFetchRetries().getMaxAttempts()).isGreaterThan(1);
        when(cbsClient.get(any(CbsRequest.class)))
                .thenReturn(Mono.defer(() -> {
                    virtualTimeScheduler.advanceTimeBy(cbsProperties.getFetchRetries().getMaxBackoff());
                    return Mono.error(new RuntimeException("some connection failure"));
                }));

        assertThatThrownBy(() -> cbsPropertySourceLocator.locate(environment))
                .hasMessageContaining("Retries exhausted")
                .hasMessageContaining(cbsProperties.getFetchRetries().getMaxAttempts().toString());
    }
}
