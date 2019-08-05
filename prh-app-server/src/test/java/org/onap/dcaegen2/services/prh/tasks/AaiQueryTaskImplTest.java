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
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutableServiceInstanceRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiRelation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AaiQueryTaskImplTest {
    private static final ServiceInstanceRequired SERVICE_REFERENCE =
            ImmutableServiceInstanceRequired
                    .builder()
                    .globalCustomerId("Baz")
                    .serviceInstanceId("Foo")
                    .serviceType("Bar")
                    .build();

    @Mock
    private AaiGetAction<PnfRequired, PnfComplete> getPnfModelClient;

    @Mock
    private AaiGetRelationAction<PnfComplete, ServiceInstanceComplete> getRelationToServiceInstance;

    @Mock
    private PnfComplete pnfResultModel;

    @Mock
    private ServiceInstanceComplete serviceModel;

    private AaiQueryTask sut;

    private final PnfRequired aaiModel = () -> "SomePNF";

    @BeforeEach
    void setUp() {
        sut = new AaiQueryTaskImpl(getPnfModelClient, getRelationToServiceInstance);
    }

    @Test
    void whenPnfIsUnavailable_ShouldThrowException() {
        //given
        given(getPnfModelClient.call(aaiModel)).willReturn(Mono.error(new Exception("404")));

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertThrows(Exception.class, task::block);
    }

    @Test
    void whenPnfIsAvailableButServiceReferenceIsEmpty_ShouldReturnFalse() {
        //given
        given(getRelationToServiceInstance.call(any())).willReturn(Flux.empty());

        configurePnfClient(aaiModel, pnfResultModel);

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertFalse(task::block);
    }

    @Test
    void whenPnfIsAvailableAndServiceRelationIsCompleteButServiceIsInactive_ShouldReturnFalse() {
        //given
        given(getRelationToServiceInstance.call(any()))
                .willReturn(Flux.just(AaiRelation.create(pnfResultModel, serviceModel)));
        given(serviceModel.getOrchestrationStatus()).willReturn("Inactive");

        configurePnfClient(aaiModel, pnfResultModel);

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertFalse(task::block);
    }

    @Test
    void whenPnfIsAvailableAndServiceRelationIsCompleteButServiceIsActive_ShouldReturnFalse() {
        //given
        given(getRelationToServiceInstance.call(any()))
                .willReturn(Flux.just(AaiRelation.create(pnfResultModel, serviceModel)));
        given(serviceModel.getOrchestrationStatus()).willReturn("Active");

        configurePnfClient(aaiModel, pnfResultModel);

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertTrue(task::block);
    }

    private void configurePnfClient(final PnfRequired aaiModel, final PnfComplete pnfResultModel) {
        given(getPnfModelClient.call(aaiModel)).willReturn(Mono.just(pnfResultModel));
    }
}
