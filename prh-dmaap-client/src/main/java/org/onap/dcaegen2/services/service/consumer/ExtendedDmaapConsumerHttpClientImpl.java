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

package org.onap.dcaegen2.services.service.consumer;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.onap.dcaegen2.services.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.service.CommonMethods;
import org.onap.dcaegen2.services.service.DmaapHttpClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;


public class ExtendedDmaapConsumerHttpClientImpl {

    private static Logger logger = LoggerFactory.getLogger(ExtendedDmaapConsumerHttpClientImpl.class);

    private final CloseableHttpClient closeableHttpClient;
    private final String dmaapHostName;
    private final String dmaapProtocol;
    private final Integer dmaapPortNumber;
    private final String dmaapTopicName;
    private final String consumerGroup;
    private final String consumerId;
    private final String dmaapContentType;


    ExtendedDmaapConsumerHttpClientImpl(DmaapConsumerConfiguration configuration) {
        this.closeableHttpClient = new DmaapHttpClientImpl(configuration).getHttpClient();
        this.dmaapHostName = configuration.dmaapHostName();
        this.dmaapProtocol = configuration.dmaapProtocol();
        this.dmaapPortNumber = configuration.dmaapPortNumber();
        this.dmaapTopicName = configuration.dmaapTopicName();
        this.consumerGroup = configuration.consumerGroup();
        this.consumerId = configuration.consumerId();
        this.dmaapContentType = configuration.dmaapContentType();
    }

    public Optional<String> getHttpConsumerResponse() {

        Optional<String> extendedDetails = Optional.empty();
        Optional<HttpRequestBase> request = createRequest();

        try {
            extendedDetails = closeableHttpClient.execute(request.get(), CommonMethods.dmaapResponseHandler());
        } catch (IOException | NullPointerException e) {
            logger.error("Exception while executing HTTP request: {}", e);
        }

        return extendedDetails;
    }

    private static HttpRequestBase createHttpRequest(URI extendedURI) {
        if (isExtendedURINotNull(extendedURI)) {
            return new HttpGet(extendedURI);
        }

        return null;
    }

    private static Boolean isExtendedURINotNull(URI extendedURI) {
        return extendedURI != null;
    }

    private Optional<HttpRequestBase> createRequest() {

        Optional<HttpRequestBase> request = Optional.empty();
        final URI extendedURI = createDmaapConsumerExtendedURI();

        if ("application/json".equals(dmaapContentType)) {
            request = Optional.ofNullable(createHttpRequest(extendedURI));
            request.get().addHeader("Content-type", dmaapContentType);
        }

        return request;
    }

    private String createRequestPath() {
        return dmaapTopicName + "/" + consumerGroup + "/" + consumerId;
    }

    private URI createDmaapConsumerExtendedURI() {
        URI extendedURI = null;

        final URIBuilder uriBuilder = new URIBuilder()
                .setScheme(dmaapProtocol)
                .setHost(dmaapHostName)
                .setPort(dmaapPortNumber)
                .setPath(createRequestPath());

        try {
            extendedURI = uriBuilder.build();
            logger.info("Building extended URI: {}", extendedURI);
        } catch (URISyntaxException e) {
            logger.error("Exception while building extended URI: {}", e);
        }

        return extendedURI;
    }
}


