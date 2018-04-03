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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.dcaegen2.services.config.AAIHttpClientConfiguration;
import org.onap.dcaegen2.services.utils.HttpRequestDetails;
import org.onap.dcaegen2.services.utils.RequestVerbs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.onap.dcaegen2.services.utils.HttpUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

public class AAIExtendedHttpClientImpl implements AAIExtendedHttpClient {

    Logger logger = LoggerFactory.getLogger(AAIExtendedHttpClientImpl.class);

    private final CloseableHttpClient closeableHttpClient;
    private final String aaiHost;
    private final String aaiProtocol;
    private final Integer aaiHostPortNumber;

    @Autowired
    public AAIExtendedHttpClientImpl (AAIHttpClientConfiguration aaiHttpClientConfiguration) {
        final AAIHttpClient aaiHttpClient = new AAIHttpClientImpl(aaiHttpClientConfiguration);
        closeableHttpClient = aaiHttpClient.getAAIHttpClient();
        aaiHost = aaiHttpClientConfiguration.aaiHost();
        aaiProtocol = aaiHttpClientConfiguration.aaiProtocol();
        aaiHostPortNumber = aaiHttpClientConfiguration.aaiHostPortNumber();
    }

    @Override
    public String getHttpResponse(HttpRequestDetails httpRequestDetails) {

        final HttpRequestBase request;
        final URI extendedURI;
        Optional<String> extendedDetails = Optional.empty();

        extendedURI = createAAIExtendedURI(aaiProtocol, aaiHost, aaiHostPortNumber,
                httpRequestDetails.getAaiAPIPath(), httpRequestDetails.getQueryParameters());

        if (extendedURI == null) {
            return null;
        }

        if (httpRequestDetails.getHttpVerb().equals(RequestVerbs.GET)) {
            request = new HttpGet(extendedURI);
        } else if (httpRequestDetails.getHttpVerb().equals(RequestVerbs.PUT)) {
            request = new HttpPut(extendedURI);
        } else {
            return null;
        }

        for (Map.Entry<String, String> headersEntry : httpRequestDetails.getHeaders().entrySet()) {
            request.addHeader(headersEntry.getKey(), headersEntry.getValue());
        }

        try {
            extendedDetails = closeableHttpClient.execute(request, aaiResponseHandler());
        } catch (IOException e) {
            logger.error("Exception while executing HTTP request: {}", e);
        }

        if (extendedDetails.isPresent()) {
            return extendedDetails.get();
        } else {
            return null;
        }
    }

    private URI createAAIExtendedURI(final String protocol, final String hostName,  final Integer portNumber,
                                     final String path, Map<String, String> queryParams) {
        final URIBuilder uriBuilder;
        URI extendedURI = null;

        uriBuilder = new URIBuilder().setScheme(protocol).setHost(hostName).setPort(portNumber).setPath(path);

        final String customQuery = createCustomQuery(queryParams);
        if (StringUtils.isNoneBlank(customQuery)) {
            uriBuilder.setCustomQuery(customQuery);
        }

        try {
            extendedURI = uriBuilder.build();
        } catch (URISyntaxException e) {
            logger.error("Exception while building extended URI: {}", e);
        }

        return extendedURI;
    }

    private String createCustomQuery(@Nonnull final Map<String, String> queryParams) {
        final StringBuilder queryStringBuilder = new StringBuilder("");
        final Iterator<Map.Entry<String, String>> queryParamIterator = queryParams.entrySet().iterator();

        while (queryParamIterator.hasNext()) {
            final Map.Entry<String, String> queryParamsEntry = queryParamIterator.next();
            queryStringBuilder.append(queryParamsEntry.getKey()).append("=").append(queryParamsEntry.getValue());
            if (queryParamIterator.hasNext()) {
                queryStringBuilder.append("&");
            }
        }

        return queryStringBuilder.toString();
    }

    private ResponseHandler<Optional<String>> aaiResponseHandler() {
        return httpResponse ->  {
            final int responseCode = httpResponse.getStatusLine().getStatusCode();
            final HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpUtils.isSuccessfulResponseCode(responseCode) && responseEntity != null) {
                final String aaiResponse = EntityUtils.toString(responseEntity);
                return Optional.of(aaiResponse);
            } else {
                String aaiResponse = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                logger.error("HTTP response not successful : {}", aaiResponse);
                return Optional.empty();
            }
        };
    }
}
