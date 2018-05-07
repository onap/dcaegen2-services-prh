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

package org.onap.dcaegen2.services.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.dcaegen2.services.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.utils.HttpRequestDetails;
import org.onap.dcaegen2.services.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

public class AAIConsumerClient implements AAIExtendedHttpClient {

    Logger logger = LoggerFactory.getLogger(AAIConsumerClient.class);

    private final CloseableHttpClient closeableHttpClient;
    private final String aaiHost;
    private final String aaiProtocol;
    private final Integer aaiHostPortNumber;
    private final String path;


    public AAIConsumerClient(AAIClientConfiguration aaiHttpClientConfiguration) {
        closeableHttpClient = new AAIClientImpl(aaiHttpClientConfiguration).getAAIHttpClient();
        aaiHost = aaiHttpClientConfiguration.aaiHost();
        aaiProtocol = aaiHttpClientConfiguration.aaiProtocol();
        aaiHostPortNumber = aaiHttpClientConfiguration.aaiHostPortNumber();
        path = aaiHttpClientConfiguration.aaiBasePath() + aaiHttpClientConfiguration.aaiPnfPath();
    }

    @Override
    public Optional<String> getHttpResponse(HttpRequestDetails requestDetails) {

        Optional<String> extendedDetails = Optional.empty();
        Optional<HttpRequestBase> request = createRequest(requestDetails);

        try {
            extendedDetails = closeableHttpClient.execute(request.get(), aaiResponseHandler());
        } catch (IOException e) {
            logger.error("Exception while executing HTTP request: {}", e);
        }

        return extendedDetails;
    }


    private URI createAAIExtendedURI(String pnfName) {

        URI extendedURI = null;

        final URIBuilder uriBuilder = new URIBuilder()
                .setScheme(aaiProtocol)
                .setHost(aaiHost)
                .setPort(aaiHostPortNumber)
                .setPath(path + "/" + pnfName);

        try {
            extendedURI = uriBuilder.build();
            logger.info("Building extended URI: {}", extendedURI);
        } catch (URISyntaxException e) {
            logger.error("Exception while building extended URI: {}", e);
        }

        return extendedURI;
    }

    private ResponseHandler<Optional<String>> aaiResponseHandler() {
        return httpResponse ->  {
            final int responseCode = httpResponse.getStatusLine().getStatusCode();
            logger.info("Status code of operation: {}", responseCode);
            final HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpUtils.isSuccessfulResponseCode(responseCode) ) {
                logger.info("HTTP response successful.");
                final String aaiResponse = EntityUtils.toString(responseEntity);
                return Optional.of(aaiResponse);
            } else {
                String aaiResponse = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                logger.error("HTTP response not successful : {}", aaiResponse);
                return Optional.of("" + responseCode);
            }
        };
    }

    private HttpRequestBase createHttpRequest(URI extendedURI) {

        if (isExtendedURINotNull(extendedURI)) {
            return new HttpGet(extendedURI);
        } else {
            return null;
        }
    }

    private Boolean isExtendedURINotNull(URI extendedURI) {
        return extendedURI != null;
    }

    private Optional<HttpRequestBase> createRequest(HttpRequestDetails requestDetails) {

        final URI extendedURI = createAAIExtendedURI(requestDetails.pnfName());
        HttpRequestBase request = createHttpRequest(extendedURI);
        requestDetails.headers().forEach(Objects.requireNonNull(request)::addHeader);
        return Optional.of(request);
    }
}
