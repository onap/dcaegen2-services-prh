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

package org.onap.dcaegen2.services.prh.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.onap.dcaegen2.services.prh.TestAppConfiguration.createDefaultAaiClientConfiguration;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.Unit.UNIT;
import static reactor.core.publisher.Flux.empty;
import static reactor.core.publisher.Mono.just;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutableLogicalLinkComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutablePnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.LogicalLinkComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.LogicalLinkRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiAddRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiRelation;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiServiceConnectionException;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipList;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class BbsActionsTaskTest {
    private static final String PNF_NAME = "Nokia123";
    private static final String LINK_NAME = "some-link";
    private static final String ATTACHMENT_POINT = "attachment-point";
    private static final LogicalLinkComplete LOGICAL_LINK =
            ImmutableLogicalLinkComplete
                    .builder()
                    .linkType(ATTACHMENT_POINT)
                    .linkName(LINK_NAME)
                    .build();

    private static final PnfComplete PNF =
            ImmutablePnfComplete
                    .builder()
                    .pnfName(PNF_NAME)
                    .build();

    private static final Relationship PNF_TO_LINK =
            ImmutableRelationship
                    .builder()
                    .relatedLink("/some/path")
                    .relatedTo("logical-link")
                    .addAllRelationshipData(Collections.singletonList(
                            ImmutableRelationshipData
                                    .builder()
                                    .relationshipKey("logical-link.link-name")
                                    .relationshipValue(LINK_NAME)
                                    .build())
                    ).build();

    private final CbsConfiguration cbsConfiguration = mock(CbsConfiguration.class);
    private final AaiClientConfiguration aaiClientConfiguration = createDefaultAaiClientConfiguration();

    @Mock
    private AaiGetAction<PnfRequired, PnfComplete> getPnf;
    @Mock
    private AaiDeleteAction<LogicalLinkComplete> deleteLogicalLink;
    @Mock
    private AaiCreateAction<LogicalLinkComplete> createLogicalLink;
    @Mock
    private AaiGetRelationAction<PnfComplete, LogicalLinkComplete> getRelationToLogicalLink;
    @Mock
    private AaiAddRelationAction<LogicalLinkRequired, PnfRequired> addRelationToPnf;

    private final JsonObject additionalFields = new JsonObject();

    private static <T, R> T field(Function<T, R> map, @NotNull R value) {
        return argThat(x -> x != null && value.equals(map.apply(x)));
    }

    public BbsActionsTaskTest() {
        additionalFields.addProperty(ATTACHMENT_POINT, LINK_NAME);
    }

    private BbsActionsTask createCut() {
        return new BbsActionsTaskImpl(
                cbsConfiguration,
                getPnf,
                deleteLogicalLink,
                createLogicalLink,
                getRelationToLogicalLink,
                addRelationToPnf);
    }

    @Test
    void whenPassedObjectDoesntHaveAdditionalFields_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);

        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(null);

        // when
        ConsumerDmaapModel result = createCut().execute(consumerDmaapModel).block();

        // then
        assertThat(result).isEqualTo(consumerDmaapModel);
    }

    @Test
    void whenPassedObjectHasEmptyLogicalLink_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);

        JsonObject additionalFields = new JsonObject();
        additionalFields.addProperty(ATTACHMENT_POINT, "");

        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        // when
        ConsumerDmaapModel result = createCut().execute(consumerDmaapModel).block();

        // then
        assertThat(result).isEqualTo(consumerDmaapModel);
    }

    @Test
    void whenPassedObjectHasLogicalLink_and_pnfHasNoLogicalLink_createLogicalLink_and_associateWithPnf_and_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration())
                .willReturn(aaiClientConfiguration);

        given(getRelationToLogicalLink.call(field(PnfRequired::getPnfName, PNF_NAME)))
                .willReturn(empty());

        given(addRelationToPnf.call(argThat(arg -> PNF_NAME.equals(arg.to().getPnfName()))))
                .willReturn(just(UNIT));

        given(createLogicalLink.call(field(LogicalLinkRequired::getLinkName, LINK_NAME)))
                .willReturn(just(UNIT));

        given(getPnf.call(field(PnfRequired::getPnfName, PNF_NAME)))
                .willReturn(just(PNF));

        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        // when
        Mono<ConsumerDmaapModel> response = createCut().execute(consumerDmaapModel);

        // then
        assertEquals(consumerDmaapModel, response.block());
    }

    @Test
    void whenPassedObjectHasLogicalLink_and_pnfHasLogicalLink_deleteOldLogicalLink_and_createLogicalLink_and_associateWithPnf_and_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration())
                .willReturn(aaiClientConfiguration);

        given(getRelationToLogicalLink.call(field(PnfRequired::getPnfName, PNF_NAME)))
                .willReturn(Flux.just(AaiRelation.create(PNF, LOGICAL_LINK)));

        given(createLogicalLink.call(field(LogicalLinkRequired::getLinkName, LINK_NAME)))
                .willReturn(just(UNIT));

        given(deleteLogicalLink.call(field(LogicalLinkRequired::getLinkName, LINK_NAME)))
                .willReturn(just(UNIT));

        given(addRelationToPnf.call(argThat(arg -> PNF_NAME.equals(arg.to().getPnfName()))))
                .willReturn(just(UNIT));

        given(getPnf.call(field(PnfRequired::getPnfName, PNF_NAME))).willReturn(just(
                ImmutablePnfComplete
                        .copyOf(PNF)
                        .withRelationshipList(
                                ImmutableRelationshipList
                                        .builder()
                                        .addRelationship(PNF_TO_LINK)
                                        .build())));


        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        // when
        Mono<ConsumerDmaapModel> response = createCut().execute(consumerDmaapModel);

        // then
        assertEquals(consumerDmaapModel, response.block());
    }

    @Test
    void whenPassedObjectHasLogicalLink_butAaiQueryFails_returnError() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);
        given(getPnf.call(field(PnfRequired::getPnfName, PNF_NAME)))
                .willReturn(Mono.error(new AaiServiceConnectionException(500, "Some exception")));

        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        // when
        Mono<ConsumerDmaapModel> response = createCut().execute(consumerDmaapModel);

        // then
        assertThatThrownBy(response::block)
                .hasCauseInstanceOf(AaiServiceConnectionException.class);
    }

    private ConsumerDmaapModel buildConsumerDmaapModel(JsonObject additionalFields) {
        return ImmutableConsumerDmaapModel.builder()
                .ipv4("10.16.123.234")
                .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
                .correlationId(PNF_NAME)
                .serialNumber("QTFCOC540002E")
                .equipVendor("nokia")
                .equipModel("3310")
                .equipType("type")
                .nfRole("role")
                .swVersion("v4.5.0.1")
                .additionalFields(additionalFields)
                .build();
    }
}