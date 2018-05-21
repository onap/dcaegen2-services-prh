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
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.dcaegen2.services.prh.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.model.CommonFunctions;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.utils.HttpUtils;
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
    private Logger logger = LoggerFactory.getLogger(AAIProducerClient.class);

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
        try {
            return closeableHttpClient.execute(createRequest(consumerDmaapModel), aaiResponseHandler());
        } catch (IOException e) {
            logger.warn("Exception while executing http client: ", e);
            throw e;
        }
    }

    private Optional<URI> createAAIExtendedURI(final String pnfName) {
        final URIBuilder uriBuilder = new URIBuilder()
                .setScheme(aaiProtocol)
                .setHost(aaiHost)
                .setPort(aaiHostPortNumber)
                .setPath(aaiPath + "/" + pnfName);
        try {
            return Optional.of(uriBuilder.build());
        } catch (URISyntaxException e) {
            logger.warn("Exception while building extended URI: ", e);
            return Optional.empty();
        }
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

        if (extendedURI != null && jsonBody != null && !"".equals(jsonBody)) {
            return createHttpPatch(extendedURI, CommonFunctions.createJsonBody(consumerDmaapModel));
        } else {
            return null;
        }
    }

    private HttpPatch createHttpPatch(URI extendedURI, String jsonBody) {
        HttpPatch httpPatch = new HttpPatch(extendedURI);
        Optional<StringEntity> stringEntity = parseJson(jsonBody);
        stringEntity.ifPresent(httpPatch::setEntity);
        return httpPatch;
    }

    private Optional<StringEntity> parseJson(String jsonBody) {
        Optional<StringEntity> stringEntity = Optional.empty();
        try {
            return Optional.of(new StringEntity(jsonBody));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Exception while parsing JSON: ", e);
            return stringEntity;
        }
    }

    private HttpRequestBase createRequest(ConsumerDmaapModel consumerDmaapModel) {
        final URI extendedURI = createAAIExtendedURI(consumerDmaapModel.getPnfName()).get();
        HttpRequestBase request = createHttpRequest(extendedURI, consumerDmaapModel);
        aaiHeaders.forEach(Objects.requireNonNull(request)::addHeader);
        Objects.requireNonNull(request).addHeader("Content-Type", "application/merge-patch+json");
        return request;
    }
}
