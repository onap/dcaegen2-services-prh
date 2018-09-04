/*
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

import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.RESPONSE_CODE;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.SERVICE_NAME;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.Map;
import javax.net.ssl.SSLException;

import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


public class AaiReactiveWebClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiReactiveWebClient.class);

    private String aaiUserName;
    private String aaiUserPassword;
    private Map<String, String> aaiHeaders;

    /**
     * Creating AaiReactiveWebClient.
     *
     * @param configuration - configuration object
     * @return AaiReactiveWebClient
     */
    public AaiReactiveWebClient fromConfiguration(AaiClientConfiguration configuration) {
        this.aaiUserName = configuration.aaiUserName();
        this.aaiUserPassword = configuration.aaiUserPassword();
        this.aaiHeaders = configuration.aaiHeaders();
        return this;
    }

    /**
     * Construct Reactive WebClient with appropriate settings.
     *
     * @return WebClient
     */
    public WebClient build() throws SSLException {
        SslContext sslContext;
        sslContext = SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
        LOGGER.debug("Setting ssl context");

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(clientOptions -> {
                clientOptions.sslContext(sslContext);
                clientOptions.disablePool();
            }))
            .defaultHeaders(httpHeaders -> httpHeaders.setAll(aaiHeaders))
            .filter(basicAuthentication(aaiUserName, aaiUserPassword))
            .filter(logRequest())
            .filter(logResponse())
            .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            MDC.put(SERVICE_NAME, String.valueOf(clientRequest.url()));
            LOGGER.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                .forEach((name, values) -> values.forEach(value -> LOGGER.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            MDC.put(RESPONSE_CODE, String.valueOf(clientResponse.statusCode()));
            LOGGER.info("Response Status {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
