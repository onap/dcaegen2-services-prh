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

package org.onap.dcaegen2.services.service.producer;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.dcaegen2.services.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.model.CommonFunctions;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.service.DmaapHttpClientImpl;
import org.onap.dcaegen2.services.service.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class ExtendedDmaapProducerHttpClientImpl {

    private static Logger logger = LoggerFactory.getLogger(ExtendedDmaapProducerHttpClientImpl.class);

    private final CloseableHttpClient closeableHttpClient;
    private final String dmaapHostName;
    private final String dmaapProtocol;
    private final Integer dmaapPortNumber;
    private final String dmaapTopicName;
    private final String dmaapContentType;


    public ExtendedDmaapProducerHttpClientImpl(DmaapPublisherConfiguration configuration) {
        this.closeableHttpClient = new DmaapHttpClientImpl(configuration).getHttpClient();
        this.dmaapHostName = configuration.dmaapHostName();
        this.dmaapProtocol = configuration.dmaapProtocol();
        this.dmaapPortNumber = configuration.dmaapPortNumber();
        this.dmaapTopicName = configuration.dmaapTopicName();
        this.dmaapContentType = configuration.dmaapContentType();
    }

    public Optional<String> getHttpProducerResponse(ConsumerDmaapModel consumerDmaapModel) {
        Optional<String> extendedDetails = Optional.empty();
        Optional<HttpRequestBase> request = createRequest(consumerDmaapModel);
        try {
            extendedDetails = closeableHttpClient.execute(request.get(), dmaapProducerResponseHandler());
        } catch (IOException | NullPointerException e) {
            logger.warn("Exception while executing HTTP request: ", e);
        }
        return extendedDetails;
    }
    
    private Optional<StringEntity> createStringEntity(Optional<String> jsonBody) {
        return Optional.of(parseJson(jsonBody).get());
    }

    private Optional<StringEntity> parseJson(Optional<String> jsonBody) {
        Optional<StringEntity> stringEntity = Optional.empty();
        try {
            stringEntity = Optional.of(new StringEntity(jsonBody.get()));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Exception while parsing JSON: ", e);
        }
        return stringEntity;
    }

    private Optional<HttpRequestBase> createRequest(ConsumerDmaapModel consumerDmaapModel) {
        Optional<HttpRequestBase> request = Optional.empty();
        final URI extendedURI = createDmaapPublisherExtendedURI();

        if ("application/json".equals(dmaapContentType)) {
            request = Optional.ofNullable(createRequest(extendedURI, consumerDmaapModel));
            request.get().addHeader("Content-type", dmaapContentType);
        }

        return request;
    }

    private URI createDmaapPublisherExtendedURI() {
        URI extendedURI = null;
        final URIBuilder uriBuilder = new URIBuilder()
            .setScheme(dmaapProtocol)
            .setHost(dmaapHostName)
            .setPort(dmaapPortNumber)
            .setPath(dmaapTopicName);
        try {
            extendedURI = uriBuilder.build();
            logger.trace("Building extended URI: {}", extendedURI);
        } catch (URISyntaxException e) {
            logger.warn("Exception while building extended URI: ", e);
        }
        return extendedURI;
    }

    private HttpRequestBase createRequest(URI extendedURI, ConsumerDmaapModel consumerDmaapModel) {
        if (extendedURI != null) {
            return createHttpPost(extendedURI, Optional.ofNullable(CommonFunctions.createJsonBody(consumerDmaapModel)));
        } else {
            return null;
        }
    }

    private HttpPost createHttpPost(URI extendedURI, Optional<String> jsonBody) {
        HttpPost post = new HttpPost(extendedURI);
        Optional<StringEntity> stringEntity = createStringEntity(jsonBody);
        post.setEntity(stringEntity.get());
        return post;
    }

    private ResponseHandler<Optional<String>> dmaapProducerResponseHandler() {
        return httpResponse -> {
            final int responseCode = httpResponse.getStatusLine().getStatusCode();
            final HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpUtils.isSuccessfulResponseCode(responseCode)) {
                logger.trace("HTTP response successful.");
                return Optional.of("" + responseCode);
            } else {
                String response = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                logger.warn("HTTP response not successful : {}", response);
                return Optional.of("" + responseCode);
            }
        };
    }
}
