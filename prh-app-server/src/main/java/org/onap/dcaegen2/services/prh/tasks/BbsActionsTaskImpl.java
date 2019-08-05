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

import static reactor.core.publisher.Mono.just;

import static org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiRelation.create;

import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutableLogicalLinkComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutablePnfRequired;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BbsActionsTaskImpl implements BbsActionsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbsActionsTaskImpl.class);
    private static final String ATTACHMENT_POINT = "attachment-point";

    private AaiGetAction<PnfRequired, PnfComplete> getPnf;
    private AaiCreateAction<LogicalLinkComplete> createLogicalLink;
    private AaiDeleteAction<LogicalLinkComplete> deleteLogicalLink;
    private AaiAddRelationAction<LogicalLinkRequired, PnfRequired> addRelationToPnf;
    private AaiGetRelationAction<PnfComplete, LogicalLinkComplete> getRelationToLogicalLink;

    @Autowired
    BbsActionsTaskImpl(
            Config config,
            AaiGetAction<PnfRequired, PnfComplete> getPnf,
            AaiDeleteAction<LogicalLinkComplete> deleteLogicalLink,
            AaiCreateAction<LogicalLinkComplete> createLogicalLink,
            AaiGetRelationAction<PnfComplete, LogicalLinkComplete> getRelationToLogicalLink,
            AaiAddRelationAction<LogicalLinkRequired, PnfRequired> addRelationToPnf) {

        this.getPnf = getPnf;
        this.createLogicalLink = createLogicalLink;
        this.deleteLogicalLink = deleteLogicalLink;
        this.addRelationToPnf = addRelationToPnf;
        this.getRelationToLogicalLink = getRelationToLogicalLink;
    }

    public Mono<ConsumerDmaapModel> execute(ConsumerDmaapModel consumerDmaapModel) {
        JsonObject additionalFields = consumerDmaapModel.getAdditionalFields();
        if (additionalFields == null || !additionalFields.has(ATTACHMENT_POINT)) {
            return just(consumerDmaapModel);
        }
        String linkName = additionalFields.get(ATTACHMENT_POINT).getAsString();
        if (linkName.isEmpty()) {
            LOGGER.warn("Attachment point is empty! Ignore related actions.");
            return just(consumerDmaapModel);
        }

        PnfRequired pnf = ImmutablePnfRequired
                .builder()
                .pnfName(consumerDmaapModel.getCorrelationId())
                .build();

        LogicalLinkComplete logicalLink = ImmutableLogicalLinkComplete
                .builder()
                .linkType(ATTACHMENT_POINT)
                .linkName(linkName)
                .build();

        return getPnf
                .call(pnf)
                .flux()
                .flatMap(getRelationToLogicalLink::call)
                .map(AaiRelation::to)
                .filter(oldLink -> oldLink.getLinkType().equals(ATTACHMENT_POINT))
                .doOnNext(link -> LOGGER.debug("Deleting logical link in AAI {} ", link))
                .flatMap(deleteLogicalLink::call)
                .then(just(logicalLink))
                .doOnNext(link -> LOGGER.debug("Creating logical link in AAI {} ", link))
                .flatMap(createLogicalLink::call)
                .flatMap(omit -> addRelationToPnf.call(create(logicalLink, pnf)))
                .map(x -> consumerDmaapModel);
    }
}


