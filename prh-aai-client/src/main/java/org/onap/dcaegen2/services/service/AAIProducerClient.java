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
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.dcaegen2.services.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.model.CommonFunctions;
import org.onap.dcaegen2.services.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AAIProducerClient implements AAIExtendedHttpClient {
    Logger logger = LoggerFactory.getLogger(AAIProducerClient.class);

    private final CloseableHttpClient closeableHttpClient;
    private final String aaiHost;
    private final String aaiProtocol;
    private final Integer aaiHostPortNumber;
    private final String aaiPath;
    private final Map<String,String> aaiHeaders;


    public AAIProducerClient(AAIClientConfiguration aaiClientConfiguration) {
        closeableHttpClient = new AAIClientImpl(aaiClientConfiguration).getAAIHttpClient();
        aaiHost = aaiClientConfiguration.aaiHost();
        aaiProtocol = aaiClientConfiguration.aaiProtocol();
        aaiHostPortNumber = aaiClientConfiguration.aaiHostPortNumber();
        aaiPath = aaiClientConfiguration.aaiBasePath() + aaiClientConfiguration.aaiPnfPath();
        aaiHeaders = aaiClientConfiguration.aaiHeaders();
    }


    @Override
    public Optional<Integer> getHttpResponse(ConsumerDmaapModel consumerDmaapModel) throws IOException {
        Optional<HttpRequestBase> request = createRequest(consumerDmaapModel);
        try {
            return closeableHttpClient.execute(request.get(), aaiResponseHandler());
        } catch (IOException e) {
            logger.warn("Exception while executing http client: ", e);
            throw new IOException();
        }
    }

    private URI createAAIExtendedURI(final String pnfName) {
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
            logger.warn("Exception while building extended URI: ", e);
        }
        return extendedURI;
    }

    private ResponseHandler<Optional<Integer>> aaiResponseHandler() {
        return (HttpResponse httpResponse) ->  {
            final Integer responseCode = httpResponse.getStatusLine().getStatusCode();
            logger.trace("Status code of operation: {}", responseCode);
            final HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpUtils.isSuccessfulResponseCode(responseCode)) {
                logger.trace("HTTP response successful.");
                return Optional.of(responseCode);
            } else {
                String aaiResponse = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                logger.warn("HTTP response not successful : {}", aaiResponse);
                return Optional.of(responseCode);
            }
        };
    }

    private HttpRequestBase createHttpRequest(URI extendedURI, ConsumerDmaapModel consumerDmaapModel) {
        String jsonBody = CommonFunctions.createJsonBody(consumerDmaapModel);

        if (isExtendedURINotNull(extendedURI) && jsonBody != null && !"".equals(jsonBody)) {
            return createHttpPatch(extendedURI, Optional.ofNullable(CommonFunctions.createJsonBody(consumerDmaapModel)));
        } else {
            return null;
        }
    }

    private Boolean isExtendedURINotNull(URI extendedURI) {
        return extendedURI != null;
    }


    private Optional<StringEntity> createStringEntity(Optional<String> jsonBody) {
        return Optional.of(parseJson(jsonBody).get());
    }

    private HttpPatch createHttpPatch(URI extendedURI, Optional<String> jsonBody) {
        HttpPatch httpPatch = new HttpPatch(extendedURI);
        Optional<StringEntity> stringEntity = createStringEntity(jsonBody);
        httpPatch.setEntity(stringEntity.get());
        return httpPatch;
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
        final URI extendedURI = createAAIExtendedURI(consumerDmaapModel.getPnfName());
        HttpRequestBase request = createHttpRequest(extendedURI, consumerDmaapModel);
        aaiHeaders.forEach(Objects.requireNonNull(request)::addHeader);
        Objects.requireNonNull(request).addHeader("Content-Type", "application/merge-patch+json");
        return Optional.of(request);
    }
}
