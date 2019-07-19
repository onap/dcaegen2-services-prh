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
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.common.Unit;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.ImmutableLogicalLink;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.LogicalLink;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.LogicalLinkRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.Pnf;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiGetAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static java.lang.String.format;
import static reactor.core.publisher.Mono.defer;

@Component
public class BbsActionsTaskImpl implements BbsActionsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbsActionsTaskImpl.class);
    private static final String ATTACHMENT_POINT = "attachment-point";
    private static final String LOGICAL_LINK_URI = "/network/logical-links/logical-link";
    private static final String PNF_URI = "/network/pnfs/pnf";
    private static final String LINK_KEY = "logical-link.link-name";
    private static final String ERROR_PREFIX = "Incorrect response when performing BBS-related actions: ";
    private static final String LOGICAL_LINK = "logical-link";

    private final Config config;
    private AaiGetAction<PnfRequired, Pnf> getPnf;
    private AaiCreateAction<LogicalLink, Unit> createLogicalLink;
    private AaiDeleteAction<LogicalLink, Unit> deleteLogicalLink;
    private AaiGetAction<LogicalLinkRequired, LogicalLink> getLogicalLink;

    @Autowired
    BbsActionsTaskImpl(
            Config config,
            AaiGetAction<PnfRequired, Pnf> getPnf,
            AaiDeleteAction<LogicalLink, Unit> deleteLogicalLink,
            AaiCreateAction<LogicalLink, Unit> createLogicalLink,
            AaiGetAction<LogicalLinkRequired, LogicalLink> getLogicalLink) {
        this.config = config;
        this.getPnf = getPnf;
        this.getLogicalLink = getLogicalLink;
        this.createLogicalLink = createLogicalLink;
        this.deleteLogicalLink = deleteLogicalLink;
    }

    public Mono<ConsumerDmaapModel> execute(ConsumerDmaapModel consumerDmaapModel) {
        JsonObject additionalFields = consumerDmaapModel.getAdditionalFields();
        if (additionalFields == null || !additionalFields.has(ATTACHMENT_POINT)) {
            return Mono.just(consumerDmaapModel);
        }
        String linkName = additionalFields.get(ATTACHMENT_POINT).getAsString();
        if (linkName.isEmpty()) {
            LOGGER.warn("Attachment point is empty! Ignore related actions.");
            return Mono.just(consumerDmaapModel);
        }
        String pnfName = consumerDmaapModel.getCorrelationId();

        return
            getPnf
                    .call(() -> pnfName)
                    .map(Pnf::getLogicalLinkReference)
                    .flatMap(Mono::justOrEmpty)
                    .flatMap(getLogicalLink::call)
                    .filter(oldLink -> oldLink.getLinkType().equals(ATTACHMENT_POINT))
                    .flatMap(deleteLogicalLink::call)
                    .then(defer(() -> createLogicalLink.call(
                            ImmutableLogicalLink
        LOGGER.debug("Creating logical link in AAI {} ", request);
        LOGGER.debug("Deleting logical link in AAI {} ", request);
                                .builder()
                                .linkName(linkName)
                                .linkType(ATTACHMENT_POINT)
                                .build())
                    )).map(x -> consumerDmaapModel);
    }

//    private ImmutableLogicalLink.Builder prepareModelBuilder(String linkName, String pnfName) {
//        Relationship relationship = org.onap.dcaegen2.services.prh.model.ImmutableRelationship.builder()
//            .addRelationship(
//                ImmutableRelationshipDict.builder().relatedLink(PNF_URI + "/" + pnfName).build()).build();
//
//        return ImmutableLogicalLink
//            .builder()
//            .linkName(linkName)
//            .linkType(ATTACHMENT_POINT)
//            .relationshipList(relationship);
//    }
}

