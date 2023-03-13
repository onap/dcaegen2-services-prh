/*
 * ============LICENSE_START=======================================================
 * PROJECT
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;


import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiHttpClient;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiPnfResultModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiServiceInstanceQueryModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiServiceInstanceResultModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableRelationshipData;
import org.onap.dcaegen2.services.prh.model.Relationship;
import org.onap.dcaegen2.services.prh.model.RelationshipData;
import org.onap.dcaegen2.services.prh.model.RelationshipDict;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AaiQueryTaskImplTest {
    @Mock
    private AaiHttpClient<ConsumerDmaapModel, AaiPnfResultModel> getPnfModelClient;

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

    private List<RelationshipData> allRelationData;

    private AaiQueryTask sut;

    private final ConsumerDmaapModel aaiModel = mock(ConsumerDmaapModel.class);

    @BeforeEach
    void setUp() {
        allRelationData = Lists.list(
                ImmutableRelationshipData.builder()
                        .relationshipKey(AaiQueryTaskImpl.CUSTOMER).relationshipValue("Foo").build(),
                ImmutableRelationshipData.builder()
                        .relationshipKey(AaiQueryTaskImpl.SERVICE_TYPE).relationshipValue("Bar").build(),
                ImmutableRelationshipData.builder()
                        .relationshipKey(AaiQueryTaskImpl.SERVICE_INSTANCE_ID).relationshipValue("Baz").build()
        );

        sut = new AaiQueryTaskImpl(getPnfModelClient, getServiceClient);
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

    private void configurePnfClient(final ConsumerDmaapModel aaiModel, final AaiPnfResultModel pnfResultModel) {
        given(getPnfModelClient.getAaiResponse(aaiModel)).willReturn(Mono.just(pnfResultModel));
    }

    @Test
    void testFindPnfInAAIActive(){
        ConsumerDmaapModel model = ImmutableConsumerDmaapModel.builder().correlationId("123").build();
        configurePnfClient(model, pnfResultModel);
        Mono<ConsumerDmaapModel> test = sut.findPnfinAAI(model);
        Assertions.assertNotNull(test);
    }
}
