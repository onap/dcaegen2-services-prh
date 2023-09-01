/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property. All rights reserved.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.configuration.CbsConfigurationForAutoCommitDisabledMode;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.RequestPath;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import reactor.core.publisher.Mono;
import reactor.test.scheduler.VirtualTimeScheduler;

/**
 *  * @author <a href="mailto:PRANIT.KAPDULE@t-systems.com">Pranit Kapdule</a> on
 *   *        24/08/23
 *    */

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "autoCommitDisabled")
public class CbsPropertySourceLocatorForAutoCommitDisabledTest {
    private static final RequestPath GET_ALL_REQUEST_PATH = CbsRequests.getAll(RequestDiagnosticContext.create())
            .requestPath();

    private CbsProperties cbsProperties = new CbsProperties();
    @Mock
    private CbsJsonToPropertyMapConverter cbsJsonToPropertyMapConverter;
    @Mock
    private CbsClientConfigurationResolver cbsClientConfigurationResolver;
    @Mock
    private CbsClientFactoryFacade cbsClientFactoryFacade;
    @Mock
    private CbsConfiguration cbsConfiguration;
    @Mock
    private CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode;
    @Mock
    private Environment environment;
    @Mock
    private CbsClient cbsClient;
    @Mock
    private JsonObject cbsConfigJsonObject;

    private Map<String, Object> cbsConfigMap = ImmutableMap.of("foo", "bar");

    private VirtualTimeScheduler virtualTimeScheduler;

    private CbsPropertySourceLocatorForAutoCommitDisabled cbsPropertySourceLocatorACDM;

    @BeforeEach
    void setup() {
        virtualTimeScheduler = VirtualTimeScheduler.getOrSet(true);

        cbsPropertySourceLocatorACDM = new CbsPropertySourceLocatorForAutoCommitDisabled(cbsProperties,
                cbsJsonToPropertyMapConverter, cbsClientConfigurationResolver, cbsClientFactoryFacade,
                cbsConfigurationForAutoCommitDisabledMode);

    }

    @AfterEach
    void cleanup() {
        virtualTimeScheduler.dispose();
    }

    @Test
    void cbsProperySourceLocatorForAutoCommitDisabledTest() throws Exception {
       
        Mono<CbsClient> just = Mono.just(cbsClient);
        when(cbsClientFactoryFacade.createCbsClient(any())).thenReturn(just);
        when(cbsClient.get(argThat(request -> request.requestPath().equals(GET_ALL_REQUEST_PATH))))
                .thenReturn(Mono.just(cbsConfigJsonObject));
        when(cbsJsonToPropertyMapConverter.convertToMap(cbsConfigJsonObject)).thenReturn(cbsConfigMap);

        cbsPropertySourceLocatorACDM.locate(environment);
       
        verify(cbsConfigurationForAutoCommitDisabledMode).parseCBSConfig(cbsConfigJsonObject);
       
        
       }

}
