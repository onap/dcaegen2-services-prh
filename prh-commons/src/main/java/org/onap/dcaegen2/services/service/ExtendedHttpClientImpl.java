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
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.dcaegen2.services.config.*;
import org.onap.dcaegen2.services.utils.HttpRequestDetails;
import org.onap.dcaegen2.services.utils.HttpUtils;
import org.onap.dcaegen2.services.utils.RequestVerbs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;


public class ExtendedHttpClientImpl {

    private static Logger logger = LoggerFactory.getLogger(ExtendedHttpClientImpl.class);

    private ExtendedHttpClientImpl() {}


    public static <T,S> Optional<String> getHttpResponse(
            T clientConfig, S httpRequestDetails) {

        Optional<String> extendedDetails = Optional.empty();
        CloseableHttpClient closeableHttpClient = HttpClientImpl.getHttpClient(clientConfig);
        Optional<HttpRequestBase> request = createRequest(clientConfig, httpRequestDetails);

        try {
            extendedDetails = closeableHttpClient.execute(request.get(), aaiResponseHandler());
        } catch (IOException  | NullPointerException e) {
            logger.error("Exception while executing HTTP request: {}", e);
        }

        if (extendedDetails.isPresent()) {
            return extendedDetails;
        } else {
            return Optional.empty();
        }
    }

    private static URI createExtendedURI(final String path, Map<String, String> queryParams, String protocol,
                                            String host, Integer port) {
        URI extendedURI = null;

        final URIBuilder uriBuilder = new URIBuilder().setScheme(protocol).setHost(host).setPort(port).setPath(path);
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

    private static String createCustomQuery(@Nonnull final Map<String, String> queryParams) {
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

    private static ResponseHandler<Optional<String>> aaiResponseHandler() {
        return httpResponse ->  {
            final int responseCode = httpResponse.getStatusLine().getStatusCode();
            final HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpUtils.isSuccessfulResponseCode(responseCode) && responseEntity != null) {
                logger.info("HTTP response successful.");
                return Optional.of(EntityUtils.toString(responseEntity));
            } else {

                logger.error("HTTP response not successful : {}",
                        responseEntity != null ? EntityUtils.toString(responseEntity) : "");
                return Optional.empty();
            }
        };
    }

    private static HttpRequestBase createHttpRequest(URI extendedURI, HttpRequestDetails httpRequestDetails) {
        if (isExtendedURINotNull(extendedURI) && (httpRequestDetails.requestVerb().equals(RequestVerbs.GET))) {
            return new HttpGet(extendedURI);
        } else if (isExtendedURINotNull(extendedURI) && (httpRequestDetails.requestVerb().equals(RequestVerbs.PUT))) {
            return new HttpPut(extendedURI);
        } else if (isExtendedURINotNull(extendedURI) &&
                isPatchRequestValid(httpRequestDetails.requestVerb(),httpRequestDetails.jsonBody())) {
            return createHttpPatch(extendedURI, httpRequestDetails.jsonBody());
        } else {
            return null;
        }
    }

    private static Boolean isExtendedURINotNull(URI extendedURI) {
        return extendedURI != null;
    }

    private static Optional<StringEntity> createStringEntity(Optional<String> jsonBody) {
        return Optional.of(parseJson(jsonBody).get());
    }

    private static HttpPatch createHttpPatch(URI extendedURI, Optional<String> jsonBody) {
        HttpPatch httpPatch = new HttpPatch(extendedURI);
        Optional<StringEntity> stringEntity = createStringEntity(jsonBody);
        httpPatch.setEntity(stringEntity.get());
        return httpPatch;
    }

    private static Optional<StringEntity> parseJson(Optional<String> jsonBody) {
        Optional<StringEntity> stringEntity = Optional.empty();

        try {
            stringEntity = Optional.of(new StringEntity(jsonBody.get()));
        } catch (UnsupportedEncodingException e) {
            logger.error("Exception while parsing JSON: {}", e);
        }

        return stringEntity;
    }

    private static Boolean isPatchRequestValid(RequestVerbs requestVerb, Optional<String> jsonBody) {
        return requestVerb == RequestVerbs.PATCH && jsonBody.isPresent();
    }

    private static <T,S> Optional<HttpRequestBase> createRequest (T t, S s) {

        if (t instanceof AAIClientConfiguration && s instanceof HttpRequestDetails) {

            AAIClientConfiguration aaiClientConfig = (AAIClientConfiguration) t;
            HttpRequestDetails httpRequestDetails = (HttpRequestDetails) s;

            final URI extendedURI = createExtendedURI(httpRequestDetails.aaiAPIPath(),
                    httpRequestDetails.queryParameters(), aaiClientConfig.aaiProtocol(), aaiClientConfig.aaiHost(),
                    aaiClientConfig.aaiHostPortNumber());
            HttpRequestBase request = createHttpRequest(extendedURI, httpRequestDetails);

            httpRequestDetails.headers().forEach(request::addHeader);

            return Optional.of(request);

        } else if (t instanceof DmaapPublisherConfiguration ) {

            DmaapPublisherConfiguration dmaapPublisher = (DmaapPublisherConfiguration) t;

            //ToDo http request class for dmaap publisher

            return Optional.empty();


        } else if (t instanceof DmaapConsumerConfiguration) {

            DmaapConsumerConfiguration dmaapConsumer = (DmaapConsumerConfiguration) t;

            //ToDo request class for dmaap consumer probably not needed

            return Optional.empty();

        } else {
            return Optional.empty();
        }
    }
}
