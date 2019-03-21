/*
 * ============LICENSE_START=======================================================
 * PROJECT
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
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableLogicalLink;
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableRelationship;
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableRelationshipList;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BbsLogicalLinkTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbsLogicalLinkTask.class);
    private static final String ATTACHMENT_POINT = "attachmentPoint";

    private final AaiClientConfiguration aaiConfig;

    @Autowired
    public BbsLogicalLinkTask(Config config) {
        this.aaiConfig = config.getAaiClientConfiguration();
    }

    public Mono<ConsumerDmaapModel> execute(ConsumerDmaapModel consumerDmaapModel) {
        JsonObject additionalFields = consumerDmaapModel.getAdditionalFields();
        if (additionalFields != null && additionalFields.has(ATTACHMENT_POINT)) {
            String linkName = additionalFields.get(ATTACHMENT_POINT).getAsString();
            if (linkName.isEmpty()) {
                LOGGER.warn("Attachment point is empty! Ignore related actions.");
                return Mono.just(consumerDmaapModel);
            }
            String pnfName = consumerDmaapModel.getCorrelationId();
            createLogicalLinkInAai(linkName, pnfName);
        }
        return Mono.just(consumerDmaapModel);
    }

    private void createLogicalLinkInAai(String linkName, String pnfName) {
        String logicalLinkUri = "/v14/network/logical-links/logical-link/";
        String uri = new URIBuilder()
            .scheme(aaiConfig.aaiProtocol())
            .host(aaiConfig.aaiHost())
            .port(aaiConfig.aaiPort())
            .path(aaiConfig.aaiBasePath() + logicalLinkUri + linkName)
            .build()
            .toString();

        ImmutableLogicalLink logicalLink = buildModel(linkName, pnfName);

        // FIXME aai client has incompatible and hardcoded model
//        new AaiHttpPutClient(aaiConfig, new LogicalLinkJsonBuilder(), uri, new CloudHttpClient()).getAaiResponse();
    }

    private ImmutableLogicalLink buildModel(String linkName, String pnfName) {
        ImmutableRelationshipList relationshipList = buildRelationToPnf(pnfName);

        return ImmutableLogicalLink
            .builder()
            .linkName(linkName)
            .linkType(ATTACHMENT_POINT)
            .relationshipList(relationshipList)
            .build();
    }

    private ImmutableRelationshipList buildRelationToPnf(String pnfName) {
        return ImmutableRelationshipList
                .builder()
                .addRelationship(ImmutableRelationship
                    .builder()
                    // TODO
//                    .addAllRelatedToProperty()
                    .build()
                )
                .build();
    }
}