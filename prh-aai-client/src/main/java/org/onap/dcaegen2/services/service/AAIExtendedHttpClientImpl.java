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
import org.onap.dcaegen2.services.utils.HttpUtils;
import org.onap.dcaegen2.services.utils.RequestVerbs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

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
    public Optional<String> getHttpResponse(HttpRequestDetails httpRequestDetails) {

        Optional<String> extendedDetails = Optional.empty();

        final URI extendedURI = createAAIExtendedURI(httpRequestDetails.getAaiAPIPath(),
                httpRequestDetails.getQueryParameters());
        final HttpRequestBase request = createHttpRequest(extendedURI, httpRequestDetails);

        if (request == null) {
            return Optional.empty();
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
            return extendedDetails;
        } else {
            return Optional.empty();
        }
    }

    private URI createAAIExtendedURI(final String path, Map<String, String> queryParams) {
        URI extendedURI = null;

        final URIBuilder uriBuilder = new URIBuilder().setScheme(this.aaiProtocol).setHost(this.aaiHost)
                .setPort(this.aaiHostPortNumber)
                .setPath(path);
        final String customQuery = createCustomQuery(queryParams);

        if (StringUtils.isNoneBlank(customQuery)) {
            uriBuilder.setCustomQuery(customQuery);
        }

        try {
            logger.info("Building extended URI");
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
                logger.info("HTTP response successful.");
                final String aaiResponse = EntityUtils.toString(responseEntity);
                return Optional.of(aaiResponse);
            } else {
                String aaiResponse = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                logger.error("HTTP response not successful : {}", aaiResponse);
                return Optional.empty();
            }
        };
    }

    private HttpRequestBase createHttpRequest(URI extendedURI, HttpRequestDetails httpRequestDetails) {
        if (isExtendedURINotNull(extendedURI) && (httpRequestDetails.getHttpVerb().equals(RequestVerbs.GET))) {
            return new HttpGet(extendedURI);
        } else if (isExtendedURINotNull(extendedURI) && (httpRequestDetails.getHttpVerb().equals(RequestVerbs.PUT))) {
            return new HttpPut(extendedURI);
        } else {
            return null;
        }
    }

    private Boolean isExtendedURINotNull(URI extendedURI) {
        return extendedURI != null ? true : false;
    }
}
