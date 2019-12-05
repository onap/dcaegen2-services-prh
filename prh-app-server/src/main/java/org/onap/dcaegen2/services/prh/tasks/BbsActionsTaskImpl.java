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

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.DELETE;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.GET;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.PUT;

import com.google.gson.JsonObject;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiPnfResultModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.AaiFailureException;
import org.onap.dcaegen2.services.prh.model.ImmutableRelationshipDict;
import org.onap.dcaegen2.services.prh.model.Relationship;
import org.onap.dcaegen2.services.prh.model.RelationshipDict;
import org.onap.dcaegen2.services.prh.model.bbs.ImmutableLogicalLink;
import org.onap.dcaegen2.services.prh.model.bbs.LogicalLink;
import org.onap.dcaegen2.services.prh.model.utils.HttpUtils;
import org.onap.dcaegen2.services.prh.model.utils.PrhModelAwareGsonBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private final RxHttpClient httpClient;

    @Autowired
    BbsActionsTaskImpl(Config config) {
        this(config, RxHttpClientFactory.createInsecure());
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

        return getLinksByPnf(pnfName)
            .flatMap(x -> Flux.fromIterable(x.getRelationshipData()))
            .filter(x -> LINK_KEY.equals(x.getRelationshipKey()))
            .map(x -> x.getRelationshipValue())
            .flatMap(oldLinkName -> getLogicalLink(oldLinkName))
            .filter(oldLink -> oldLink.getLinkType().equals(ATTACHMENT_POINT))
            .flatMap(oldLink -> deleteLogicalLinkInAai(oldLink.getLinkName(), oldLink.getResourceVersion()))
            .then(createLogicalLinkInAai(linkName, pnfName))
            .flatMap(response -> handleFinalResponse(response, consumerDmaapModel));
    }

    private Flux<RelationshipDict> getLinksByPnf(String pnfName) {
        return httpClient.call(buildGetRequest(PNF_URI + "/" + pnfName))
            .flatMap(response -> handleResponse(response, "GET PNF request. Pnf name: " + pnfName))
            .map(httpResponse -> httpResponse.bodyAsJson(UTF_8,
                PrhModelAwareGsonBuilder.createGson(), AaiPnfResultModel.class))
            .flatMapMany(pnfModel -> Flux.fromStream(pnfModel.getRelationshipList().getRelationship().stream()))
            .filter(x -> LOGICAL_LINK.equals(x.getRelatedTo()));
    }

    private Mono<LogicalLink> getLogicalLink(String linkName) {
        ImmutableHttpRequest request = buildGetRequest(LOGICAL_LINK_URI + "/" + linkName);
        return httpClient.call(request)
            .flatMap(response -> handleResponse(response, "GET LogicalLink request. Link name: " + linkName))
            .map(httpResponse -> httpResponse.bodyAsJson(UTF_8,
                PrhModelAwareGsonBuilder.createGson(), LogicalLink.class));
    }

    private Mono<HttpResponse> createLogicalLinkInAai(String linkName, String pnfName) {
        ImmutableHttpRequest request = buildLogicalLinkPutRequest(linkName, pnfName);
        LOGGER.debug("Creating logical link in AAI {} ", request);
        return httpClient.call(request)
            .flatMap(response -> handleResponse(response, "PUT LogicalLink request. Link name: " + linkName));
    }

    private Mono<HttpResponse> deleteLogicalLinkInAai(String linkName, String resourceVersion) {
        ImmutableHttpRequest request = buildLogicalLinkDeleteRequest(linkName, resourceVersion);
        LOGGER.debug("Deleting logical link in AAI {} ", request);
        return httpClient.call(request)
            .flatMap(response -> handleResponse(response, "DELETE LogicalLink request. Link name:  " + linkName));
    }

    private ImmutableHttpRequest buildGetRequest(String path) {
        String uri = buildUri(path);
        Map<String, String> aaiHeaders = HashMap
            .ofAll(config.getAaiClientConfiguration().aaiHeaders());

        return ImmutableHttpRequest
            .builder()
            .method(GET)
            .url(uri)
            .customHeaders(aaiHeaders)
            .build();
    }

    private ImmutableHttpRequest buildLogicalLinkPutRequest(String linkName, String pnfName) {
        String uri = buildUri(LOGICAL_LINK_URI + "/" + linkName);
        ImmutableLogicalLink logicalLink = prepareModelBuilder(linkName, pnfName).build();
        RequestBody requestBody = RequestBody.fromString(PrhModelAwareGsonBuilder.createGson().toJson(logicalLink));

        // FIXME: AAI headers for PUT are different than PATCH (taken from prh_endpoints.json)
        Map<String, String> aaiHeaders = HashMap
            .ofAll(config.getAaiClientConfiguration().aaiHeaders())
            .put(CONTENT_TYPE, APPLICATION_JSON);

        return ImmutableHttpRequest
            .builder()
            .method(PUT)
            .url(uri)
            .body(requestBody)
            .customHeaders(aaiHeaders)
            .build();
    }

    private ImmutableHttpRequest buildLogicalLinkDeleteRequest(String linkName, String resourceVersion) {
        String uri = buildUri(LOGICAL_LINK_URI + "/" + linkName + "?resource-version=" + resourceVersion);

        Map<String, String> aaiHeaders = HashMap
            .ofAll(config.getAaiClientConfiguration().aaiHeaders())
            .put(CONTENT_TYPE, APPLICATION_JSON);

        return ImmutableHttpRequest
            .builder()
            .method(DELETE)
            .url(uri)
            .customHeaders(aaiHeaders)
            .build();
    }

    private ImmutableLogicalLink.Builder prepareModelBuilder(String linkName, String pnfName) {
        Relationship relationship = org.onap.dcaegen2.services.prh.model.ImmutableRelationship.builder()
            .addRelationship(
                ImmutableRelationshipDict.builder().relatedLink(PNF_URI + "/" + pnfName).build()).build();

        return ImmutableLogicalLink
            .builder()
            .linkName(linkName)
            .linkType(ATTACHMENT_POINT)
            .relationshipList(relationship);
    }

    private Mono<HttpResponse> handleResponse(HttpResponse response, String msg) {
        return HttpUtils.isSuccessfulResponseCode(response.statusCode()) ? Mono.just(response) :
            Mono.error(new AaiFailureException(ERROR_PREFIX + response.statusCode() + ". Occurred in " + msg));
    }

    private Mono<? extends ConsumerDmaapModel> handleFinalResponse(
        HttpResponse response, ConsumerDmaapModel consumerDmaapModel) {
        return HttpUtils.isSuccessfulResponseCode(response.statusCode())
            ? Mono.just(consumerDmaapModel) : Mono.error(new AaiFailureException(ERROR_PREFIX + response.statusCode()));
    }

    private String buildUri(String path) {
        return config.getAaiClientConfiguration().pnfUrl().replace(PNF_URI, path);
    }
}

