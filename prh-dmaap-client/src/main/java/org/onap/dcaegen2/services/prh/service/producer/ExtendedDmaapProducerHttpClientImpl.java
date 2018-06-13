/*-
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
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

package org.onap.dcaegen2.services.prh.service.producer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.model.CommonFunctions;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.utils.HttpUtils;
import org.onap.dcaegen2.services.prh.service.DmaapHttpClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class ExtendedDmaapProducerHttpClientImpl {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CloseableHttpClient closeableHttpClient;
    private final String dmaapHostName;
    private final String dmaapProtocol;
    private final Integer dmaapPortNumber;
    private final String dmaapTopicName;
    private final String dmaapContentType;
    private ConsumerDmaapModel consumerDmaapModel;


    public ExtendedDmaapProducerHttpClientImpl(DmaapPublisherConfiguration configuration) {
        this.closeableHttpClient = new DmaapHttpClientImpl(configuration).getHttpClient();
        this.dmaapHostName = configuration.dmaapHostName();
        this.dmaapProtocol = configuration.dmaapProtocol();
        this.dmaapPortNumber = configuration.dmaapPortNumber();
        this.dmaapTopicName = configuration.dmaapTopicName();
        this.dmaapContentType = configuration.dmaapContentType();
    }

    public Optional<Integer> getHttpProducerResponse(ConsumerDmaapModel consumerDmaapModel) {
        this.consumerDmaapModel = consumerDmaapModel;
        try {
            return createRequest()
                .flatMap(this::executeHttpClient);
        } catch (URISyntaxException e) {
            logger.warn("Exception while executing HTTP request: ", e);
        }
        return Optional.empty();
    }

    private Optional<Integer> executeHttpClient(HttpRequestBase httpRequestBase) {
        try {
            return closeableHttpClient.execute(httpRequestBase, this::handleResponse);
        } catch (IOException e) {
            logger.warn("Exception while executing HTTP request: ", e);
        }
        return Optional.empty();
    }

    private Optional<HttpRequestBase> createRequest() throws URISyntaxException {
        return "application/json".equals(dmaapContentType)
            ? createDmaapPublisherExtendedURI().map(this::createHttpPostRequest)
            : Optional.empty();
    }

    private Optional<URI> createDmaapPublisherExtendedURI() throws URISyntaxException {
        return Optional.ofNullable(new URIBuilder()
            .setScheme(dmaapProtocol)
            .setHost(dmaapHostName)
            .setPort(dmaapPortNumber)
            .setPath(dmaapTopicName).build());
    }

    private HttpPost createHttpPostRequest(URI extendedURI) {
        HttpPost post = new HttpPost(extendedURI);
        post.addHeader("Content-type", dmaapContentType);
        createStringEntity().ifPresent(post::setEntity);
        return post;
    }

    private Optional<StringEntity> createStringEntity() {
        try {
            return Optional.of(new StringEntity(CommonFunctions.createJsonBody(consumerDmaapModel)));
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            logger.warn("Exception while parsing JSON: ", e);
        }
        return Optional.empty();
    }

    Optional<Integer> handleResponse(HttpResponse response) throws IOException {

        final Integer responseCode = response.getStatusLine().getStatusCode();
        logger.info("Status code of operation: {}", responseCode);
        final HttpEntity responseEntity = response.getEntity();

        if (HttpUtils.isSuccessfulResponseCode(responseCode)) {
            logger.trace("HTTP response successful.");
            return Optional.of(responseCode);
        } else {
            String aaiResponse = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
            logger.warn("HTTP response not successful : {}", aaiResponse);
            return Optional.of(responseCode);
        }
    }
}