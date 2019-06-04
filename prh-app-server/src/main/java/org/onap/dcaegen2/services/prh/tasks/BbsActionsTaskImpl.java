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
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.AaiFailureException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableLogicalLink;
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableRelationship;
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableRelationshipWrapper;
import org.onap.dcaegen2.services.prh.model.bbs.RelationshipWrapper;
import org.onap.dcaegen2.services.prh.model.utils.GsonSerializer;
import org.onap.dcaegen2.services.prh.model.utils.HttpUtils;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI.URIBuilder;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.PUT;

@Component
public class BbsActionsTaskImpl implements BbsActionsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbsActionsTaskImpl.class);
    private static final String ATTACHMENT_POINT = "attachment-point";
    private static final String LOGICAL_LINK_URI = "/network/logical-links/logical-link/";
    private static final String PNF_URI = "/network/pnfs/pnf/";

    private final Config config;
    private final RxHttpClient httpClient;

    @Autowired
    BbsActionsTaskImpl(Config config) {
        this(config, RxHttpClient.create(new SslFactory().createInsecureClientContext()));
    }

    BbsActionsTaskImpl(Config config, RxHttpClient httpClient) {
        this.config = config;
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
        ImmutableHttpRequest request = buildLogicalLinkRequest(linkName, pnfName);

        return httpClient.call(request);
    }

    private ImmutableHttpRequest buildLogicalLinkRequest(String linkName, String pnfName) {
        String uri = buildLogicalLinkUri(linkName);
        ImmutableLogicalLink logicalLink = buildModel(linkName, pnfName);
        RequestBody requestBody = RequestBody.fromString(GsonSerializer.createJsonBody(logicalLink));

        // FIXME: AAI headers for PUT are different than PATCH (taken from prh_endpoints.json)
        Map<String, String> aaiHeaders = HashMap
                .ofAll(config.getAaiClientConfiguration().aaiHeaders())
                .put("Content-Type", "application/json");

        return ImmutableHttpRequest
            .builder()
            .method(PUT)
            .url(uri)
            .body(requestBody)
            .customHeaders(aaiHeaders)
            .build();
    }

    private ImmutableLogicalLink buildModel(String linkName, String pnfName) {
        List<RelationshipWrapper> relationships = buildRelationLink(pnfName);

        return ImmutableLogicalLink
            .builder()
            .linkName(linkName)
            .linkType(ATTACHMENT_POINT)
            .relationshipList(relationships)
            .build();
    }

    private List<RelationshipWrapper> buildRelationLink(String pnfName) {
        return Arrays.asList(ImmutableRelationshipWrapper
            .builder()
            .relationship(ImmutableRelationship
                .builder()
                .relatedLink(PNF_URI + pnfName)
                .build())
            .build());
    }

    private String buildLogicalLinkUri(String linkName) {
        return new URIBuilder()
            .scheme(config.getAaiClientConfiguration().aaiProtocol())
            .host(config.getAaiClientConfiguration().aaiHost())
            .port(config.getAaiClientConfiguration().aaiPort())
            .path(config.getAaiClientConfiguration().aaiBasePath() + LOGICAL_LINK_URI + linkName)
            .build()
            .toString();
    }
}