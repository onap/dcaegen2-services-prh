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

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.common.*;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.ImmutableLogicalLink;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.LogicalLink;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.LogicalLinkRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.ImmutablePnf;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.Pnf;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiGetAction;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.onap.dcaegen2.services.prh.TestAppConfiguration.createDefaultAaiClientConfiguration;
import static reactor.core.publisher.Mono.just;

@ExtendWith(MockitoExtension.class)
class BbsActionsTaskTest {
    private static final String PNF_NAME = "Nokia123";
    private static final String LINK_NAME= "some-link";
    private static final String ATTACHMENT_POINT = "attachment-point";
    private static final LogicalLink LOGICAL_LINK =
            ImmutableLogicalLink
                    .builder()
                    .linkName(LINK_NAME)
                    .linkType(ATTACHMENT_POINT)
                    .build();

    private static final Pnf PNF =
            ImmutablePnf
                    .builder()
                    .pnfName(PNF_NAME)
                    .build();

    private static final Relationship PNF_TO_LINK =
            ImmutableRelationship
                .builder()
                .relatedLink("/some/path")
                    .relatedTo("logical-link")
                    .relationshipData(Collections.singletonList(
                            ImmutableRelationshipData
                                    .builder()
                                    .relationshipKey("logical-link.link-name")
                                    .relationshipValue(LINK_NAME)
                                    .build())
                    ).build();

    private final CbsConfiguration cbsConfiguration = mock(CbsConfiguration.class);
    private final AaiClientConfiguration aaiClientConfiguration = createDefaultAaiClientConfiguration();

    @Mock
    private AaiGetAction<PnfRequired, Pnf> getPnf;
    @Mock
    private AaiDeleteAction<LogicalLink, Unit> deleteLogicalLink;
    @Mock
    private AaiCreateAction<LogicalLink, Unit> createLogicalLink;
    @Mock
    private AaiGetAction<LogicalLinkRequired, LogicalLink> getLogicalLink;

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
                getLogicalLink);
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
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);
        given(createLogicalLink.call(field(LogicalLink::getLinkName, LINK_NAME))).willReturn(just(Unit.UNIT));
        given(getPnf.call(field(PnfRequired::getPnfName, PNF_NAME))).willReturn(just(PNF));

        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        // when
        Mono<ConsumerDmaapModel> response = createCut().execute(consumerDmaapModel);

        // then
        assertEquals(consumerDmaapModel, response.block());
        assertEquals(getBodyJson(LOGICAL_LINK_BODY), extractBodyFromRequest(linkPut));
    }

    @Test
    void whenPassedObjectHasLogicalLink_and_pnfHasLogicalLink_deleteOldLogicalLink_and_createLogicalLink_and_associateWithPnf_and_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);
        given(getLogicalLink.call(field(LogicalLinkRequired::getLinkName, LINK_NAME))).willReturn(just(LOGICAL_LINK));
        given(createLogicalLink.call(field(LogicalLink::getLinkName, LINK_NAME))).willReturn(just(Unit.UNIT));
        given(deleteLogicalLink.call(field(LogicalLink::getLinkName, LINK_NAME))).willReturn(just(Unit.UNIT));
        given(getPnf.call(field(PnfRequired::getPnfName, PNF_NAME))).willReturn(just(
                ImmutablePnf
                        .copyOf(PNF)
                        .withRelationshipList(
                                ImmutableRelationshipList
                                        .builder()
                                        .relationship(Collections.singletonList(PNF_TO_LINK))
                                        .build())));


        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        // when
        Mono<ConsumerDmaapModel> response = createCut().execute(consumerDmaapModel);

        // then
        assertEquals(consumerDmaapModel, response.block());
        assertEquals(getBodyJson(LOGICAL_LINK_BODY), extractBodyFromRequest(linkPut));
    }

    @Test
    void whenPassedObjectHasLogicalLink_butAaiQueryFails_returnError() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);
        given(getPnf.call(field(PnfRequired::getPnfName, PNF_NAME)))
                .willReturn(Mono.error(new AaiServiceConnectionException(500)));

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

    private String extractBodyFromRequest(HttpRequest request) {
        return Mono.from(request.body().contents()).block().toString(StandardCharsets.UTF_8);
}