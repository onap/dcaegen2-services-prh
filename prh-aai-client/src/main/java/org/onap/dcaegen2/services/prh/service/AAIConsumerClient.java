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

package org.onap.dcaegen2.services.prh.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.dcaegen2.services.prh.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AAIConsumerClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CloseableHttpClient closeableHttpClient;
    private final String aaiHost;
    private final String aaiProtocol;
    private final Integer aaiHostPortNumber;
    private final String aaiPath;
    private final Map<String,String> aaiHeaders;


    public AAIConsumerClient(AAIClientConfiguration aaiClientConfiguration) {
        closeableHttpClient = new AAIClientImpl(aaiClientConfiguration).getAAIHttpClient();
        aaiHost = aaiClientConfiguration.aaiHost();
        aaiProtocol = aaiClientConfiguration.aaiProtocol();
        aaiHostPortNumber = aaiClientConfiguration.aaiHostPortNumber();
        aaiPath = aaiClientConfiguration.aaiBasePath() + aaiClientConfiguration.aaiPnfPath();
        aaiHeaders = aaiClientConfiguration.aaiHeaders();
    }

    public Optional<String> getHttpResponse(ConsumerDmaapModel consumerDmaapModel) throws IOException {
        Optional<HttpRequestBase> request = createRequest(consumerDmaapModel);
        try {
            return closeableHttpClient.execute(request.get(), aaiResponseHandler());
        } catch (IOException e) {
            logger.warn("Exception while executing http client: ", e);
            throw new IOException();
        }
    }

    private URI createAAIExtendedURI(String pnfName) {

        URI extendedURI = null;

        final URIBuilder uriBuilder = new URIBuilder()
                .setScheme(aaiProtocol)
                .setHost(aaiHost)
                .setPort(aaiHostPortNumber)
                .setPath(aaiPath + "/" + pnfName);

        try {
            extendedURI = uriBuilder.build();
            logger.trace("Building extended URI: {}", extendedURI);
        } catch (URISyntaxException e) {
            logger.warn("Exception while building extended URI: {}", e);
        }

        return extendedURI;
    }

    private ResponseHandler<Optional<String>> aaiResponseHandler() {
        return httpResponse ->  {
            final int responseCode = httpResponse.getStatusLine().getStatusCode();
            logger.info("Status code of operation: {}", responseCode);
            final HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpUtils.isSuccessfulResponseCode(responseCode) ) {
                logger.trace("HTTP response successful.");
                final String aaiResponse = EntityUtils.toString(responseEntity);
                return Optional.of(aaiResponse);
            } else {
                String aaiResponse = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                logger.warn("HTTP response not successful : {}", aaiResponse);
                return Optional.of(String.valueOf(responseCode));
            }
        };
    }

    private HttpRequestBase createHttpRequest(URI extendedURI) {
        return isExtendedURINotNull(extendedURI) ? new HttpGet(extendedURI) : null;
    }

    private Boolean isExtendedURINotNull(URI extendedURI) {
        return extendedURI != null;
    }

    private Optional<HttpRequestBase> createRequest(ConsumerDmaapModel consumerDmaapModel) {
        final URI extendedURI = createAAIExtendedURI(consumerDmaapModel.getPnfName());
        HttpRequestBase request = createHttpRequest(extendedURI);
        aaiHeaders.forEach(Objects.requireNonNull(request)::addHeader);
        Objects.requireNonNull(request).addHeader("Content-Type", "application/json");
        return Optional.of(request);
    }
}
