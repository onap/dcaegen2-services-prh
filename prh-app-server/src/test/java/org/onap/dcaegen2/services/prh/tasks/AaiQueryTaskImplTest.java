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

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.onap.dcaegen2.services.prh.model.*;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiServiceInstanceQueryModel;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)   // TODO remove unnecessary stubbing
class AaiQueryTaskImplTest {
    @Mock
    private AaiHttpClient<AaiModel, AaiPnfResultModel> getPnfModelClient;

    @Mock
    private AaiHttpClient<AaiServiceInstanceQueryModel, AaiServiceInstanceResultModel> getServiceClient;

    @Mock
    private AaiPnfResultModel pnfResultModel;

    @Mock
    private Relationship pnfRelationships;

    @Mock
    private RelationshipDict pnfRelation;

    @Mock
    private AaiServiceInstanceResultModel serviceModel;

    @Mock
    private RelationshipData customer;

    @Mock
    private RelationshipData serviceType;

    @Mock
    private RelationshipData serviceInstanceId;

    private List<RelationshipData> allRelationData;

    private AaiQueryTask sut;

    private final AaiModel aaiModel = () -> "SomePNF";

    @BeforeEach
    void setUp() {
        when(customer.getRelationshipKey()).thenReturn(AaiQueryTaskImpl.CUSTOMER);
        when(customer.getRelationshipValue()).thenReturn("Foo");

        when(serviceType.getRelationshipKey()).thenReturn(AaiQueryTaskImpl.SERVICE_TYPE);
        when(serviceType.getRelationshipValue()).thenReturn("Bar");

        when(serviceInstanceId.getRelationshipKey()).thenReturn(AaiQueryTaskImpl.SERVICE_INSTANCE_ID);
        when(serviceInstanceId.getRelationshipValue()).thenReturn("Baz");

        allRelationData = Lists.list(customer, serviceType, serviceInstanceId);

        sut = new AaiQueryTaskImpl(getPnfModelClient, getServiceClient);
    }

    @Test
    void whenPnfIsUnavailable_ShouldThrowException() {
        //given
        given(getPnfModelClient.getAaiResponse(aaiModel)).willReturn(Mono.error(new Exception("404")));

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertThrows(Exception.class, task::block);
    }

    @Test
    void whenPnfIsAvailableButRelationshipIsNull_ShouldReturnFalse() {
        //given
        given(pnfResultModel.getRelationshipList()).willReturn(null);

        configurePnfClient(aaiModel, pnfResultModel);

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertFalse(task::block);
    }

    @Test
    void whenPnfIsAvailableButRelationshipIsEmpty_ShouldReturnFalse() {
        //given
        given(pnfRelationships.getRelationship()).willReturn(Collections.emptyList());
        given(pnfResultModel.getRelationshipList()).willReturn(pnfRelationships);
        configurePnfClient(aaiModel, pnfResultModel);

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertFalse(task::block);
    }

    @Test
    void whenPnfIsAvailableButServiceRelationIsMissing_ShouldReturnFalse() {
        //given
        given(pnfRelation.getRelatedTo()).willReturn("some-other-relation");
        given(pnfRelationships.getRelationship()).willReturn(Collections.singletonList(pnfRelation));
        given(pnfResultModel.getRelationshipList()).willReturn(pnfRelationships);

        configurePnfClient(aaiModel, pnfResultModel);

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertFalse(task::block);
    }

    @Test
    void whenPnfIsAvailableButServiceRelationIsMissingRequiredKey_ShouldReturnFalse() {
        //given
        Collections.shuffle(allRelationData);
        allRelationData.remove(0);

        given(pnfRelation.getRelatedTo()).willReturn(AaiQueryTaskImpl.RELATED_TO);
        given(pnfRelation.getRelationshipData()).willReturn(allRelationData);
        given(pnfRelationships.getRelationship()).willReturn(Collections.singletonList(pnfRelation));
        given(pnfResultModel.getRelationshipList()).willReturn(pnfRelationships);

        configurePnfClient(aaiModel, pnfResultModel);

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertFalse(task::block);
    }

    @Test
    void whenPnfIsAvailableAndServiceRelationIsCompleteButServiceIsInactive_ShouldReturnFalse() {
        //given
        given(serviceModel.getOrchestrationStatus()).willReturn("Inactive");
        given(getServiceClient.getAaiResponse(any())).willReturn(Mono.just(serviceModel));

        given(pnfRelation.getRelatedTo()).willReturn(AaiQueryTaskImpl.RELATED_TO);
        given(pnfRelation.getRelationshipData()).willReturn(allRelationData);
        given(pnfRelationships.getRelationship()).willReturn(Collections.singletonList(pnfRelation));
        given(pnfResultModel.getRelationshipList()).willReturn(pnfRelationships);

        configurePnfClient(aaiModel, pnfResultModel);

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertFalse(task::block);
    }

    @Test
    void whenPnfIsAvailableAndServiceRelationIsCompleteButServiceIsActive_ShouldReturnFalse() {
        //given
        given(serviceModel.getOrchestrationStatus()).willReturn("Active");
        given(getServiceClient.getAaiResponse(any())).willReturn(Mono.just(serviceModel));

        given(pnfRelation.getRelatedTo()).willReturn(AaiQueryTaskImpl.RELATED_TO);
        given(pnfRelation.getRelationshipData()).willReturn(allRelationData);
        given(pnfRelationships.getRelationship()).willReturn(Collections.singletonList(pnfRelation));
        given(pnfResultModel.getRelationshipList()).willReturn(pnfRelationships);

        configurePnfClient(aaiModel, pnfResultModel);

        //when
        final Mono<Boolean> task = sut.execute(aaiModel);

        //then
        Assertions.assertTrue(task::block);
    }

    private void configurePnfClient(final AaiModel aaiModel, final AaiPnfResultModel pnfResultModel) {
        given(getPnfModelClient.getAaiResponse(aaiModel)).willReturn(Mono.just(pnfResultModel));
    }
}
