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

import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.PUT;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.vavr.collection.HashMap;
import java.util.function.Function;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.AaiFailureException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableLogicalLink;
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableRelationship;
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableRelationshipList;
import org.onap.dcaegen2.services.prh.model.utils.GsonSerializer;
import org.onap.dcaegen2.services.prh.model.utils.HttpUtils;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI.URIBuilder;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BbsActionsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbsActionsTask.class);
    private static final String ATTACHMENT_POINT = "attachmentPoint";

    private final AaiClientConfiguration aaiConfig;
    private final RxHttpClient httpClient;

    @Autowired
    BbsActionsTask(Config config, RxHttpClient httpClient) {
        this.aaiConfig = config.getAaiClientConfiguration();
        this.httpClient = httpClient;
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
        return createLogicalLinkInAai(linkName, pnfName).flatMap(handleResponse(consumerDmaapModel));
    }

    private Function<HttpResponse, Mono<ConsumerDmaapModel>> handleResponse(ConsumerDmaapModel consumerDmaapModel) {
        return response -> HttpUtils.isSuccessfulResponseCode(response.statusCode())
            ? Mono.just(consumerDmaapModel)
            : Mono.error(new AaiFailureException(
                "Incorrect response when performing BBS-related actions: " + response.statusCode()));
    }

    private Mono<HttpResponse> createLogicalLinkInAai(String linkName, String pnfName) {
        ImmutableHttpRequest request = buildRequest(linkName, pnfName);

        return httpClient.call(request);
    }

    private ImmutableHttpRequest buildRequest(String linkName, String pnfName) {
        String uri = buildUri(linkName);
        ImmutableLogicalLink logicalLink = buildModel(linkName, pnfName);
        Publisher<ByteBuf> jsonPayload = RequestBody.fromString(GsonSerializer.createJsonBody(logicalLink));

        return ImmutableHttpRequest
            .builder()
            .method(PUT)
            .url(uri)
            .body(jsonPayload)
            .customHeaders(HashMap.ofAll(aaiConfig.aaiHeaders()))
            .build();
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
                .relatedLink("/network/pnfs/pnf/" + pnfName)
                .build())
            .build();
    }

    private String buildUri(String linkName) {
        String logicalLinkUri = "/v14/network/logical-links/logical-link/";
        return new URIBuilder()
            .scheme(aaiConfig.aaiProtocol())
            .host(aaiConfig.aaiHost())
            .port(aaiConfig.aaiPort())
            .path(aaiConfig.aaiBasePath() + logicalLinkUri + linkName)
            .build()
            .toString();
    }
}