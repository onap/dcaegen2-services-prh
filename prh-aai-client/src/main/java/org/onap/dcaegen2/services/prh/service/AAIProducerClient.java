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

import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.onap.dcaegen2.services.prh.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.model.CommonFunctions;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class AAIProducerClient implements AAIExtendedHttpClient {

    private static final String EXCEPTION_MESSAGE = "Exception while executing http client: ";
    private Logger logger = LoggerFactory.getLogger(AAIProducerClient.class);

    private final CloseableHttpClient closeableHttpClient;
    private final String aaiHost;
    private final String aaiProtocol;
    private final Integer aaiHostPortNumber;
    private final String aaiPath;
    private final Map<String,String> aaiHeaders;
    private final String aaiUserName;
    private final String aaiUserPassword;


    public AAIProducerClient(AAIClientConfiguration aaiClientConfiguration) {
        closeableHttpClient = new AAIClientImpl(aaiClientConfiguration).getAAIHttpClient();
        aaiHost = aaiClientConfiguration.aaiHost();
        aaiProtocol = aaiClientConfiguration.aaiProtocol();
        aaiHostPortNumber = aaiClientConfiguration.aaiHostPortNumber();
        aaiPath = aaiClientConfiguration.aaiBasePath() + aaiClientConfiguration.aaiPnfPath();
        aaiHeaders = aaiClientConfiguration.aaiHeaders();
        aaiUserName = aaiClientConfiguration.aaiUserName();
        aaiUserPassword = aaiClientConfiguration.aaiUserPassword();
    }


    @Override
    public Optional<Integer> getHttpResponse(ConsumerDmaapModel consumerDmaapModel) throws URISyntaxException {
        return createRequest(consumerDmaapModel).flatMap(httpRequestBase -> {
            try {
                return closeableHttpClient.execute(httpRequestBase, CommonFunctions::handleResponse);
            } catch (IOException e) {
                logger.warn(EXCEPTION_MESSAGE, e);
                return Optional.empty();
            }
        });

    }

    private Optional<HttpRequestBase> createRequest(ConsumerDmaapModel consumerDmaapModel) throws URISyntaxException {
        final URI extendedURI = createAAIExtendedURI(consumerDmaapModel.getPnfName());
        return createHttpRequest(extendedURI, consumerDmaapModel);
    }

    private URI createAAIExtendedURI(final String pnfName) throws URISyntaxException {
        return new URIBuilder()
            .setScheme(aaiProtocol)
            .setHost(aaiHost)
            .setPort(aaiHostPortNumber)
            .setPath(aaiPath + "/" + pnfName).build();
    }

    Optional<HttpRequestBase> createHttpRequest(URI extendedURI, ConsumerDmaapModel consumerDmaapModel) {
        return Optional.ofNullable(CommonFunctions.createJsonBody(consumerDmaapModel)).filter(x-> !x.isEmpty()).flatMap(myJson -> {
            try {
                return Optional.of(createHttpPatch(extendedURI, myJson));
            } catch (UnsupportedEncodingException e) {
                logger.warn(EXCEPTION_MESSAGE, e);
            }
            return Optional.empty();
        });
    }

    private HttpPatch createHttpPatch(URI extendedURI, String jsonBody) throws UnsupportedEncodingException {
        HttpPatch httpPatch = new HttpPatch(extendedURI);
        httpPatch.setEntity( new StringEntity(jsonBody));
        aaiHeaders.forEach(httpPatch::addHeader);
        httpPatch.addHeader("Content-Type", "application/merge-patch+json");
        httpPatch.addHeader("Authorization", "Basic " + encode());
        return httpPatch;
    }

    private String encode() throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString((this.aaiUserName + ":" + this.aaiUserPassword)
                .getBytes("UTF-8"));
    }
}
