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
import java.util.Map;
import javax.net.ssl.SSLException;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.ssl.SslFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;


public class AaiReactiveWebClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiReactiveWebClient.class);

    private final String aaiUserName;
    private final String aaiUserPassword;
    private final Map<String, String> aaiHeaders;
    private final Boolean enableAaiCertAuth;
    private final String trustStorePath;
    private final String trustStorePasswordPath;
    private final String keyStorePath;
    private final String keyStorePasswordPath;
    private final SslFactory sslFactory;

    /**
     * Creating AaiReactiveWebClient.
     *
     * @param configuration - configuration object
     * @param sslFactory - factory for ssl setup
     */
    public AaiReactiveWebClient(SslFactory sslFactory, AaiClientConfiguration configuration) {
        this.aaiUserName = configuration.aaiUserName();
        this.aaiUserPassword = configuration.aaiUserPassword();
        this.aaiHeaders = configuration.aaiHeaders();
        this.trustStorePath = configuration.trustStorePath();
        this.trustStorePasswordPath = configuration.trustStorePasswordPath();
        this.keyStorePath = configuration.keyStorePath();
        this.keyStorePasswordPath = configuration.keyStorePasswordPath();
        this.enableAaiCertAuth = configuration.enableAaiCertAuth();
        this.sslFactory = sslFactory;
    }

    /**
     * Construct Reactive WebClient with appropriate settings.
     *
     * @return WebClient
     */
    public WebClient build() throws SSLException {
        LOGGER.debug("Setting ssl context");
        
        SslContext sslContext = createSslContext();
        
        ClientHttpConnector reactorClientHttpConnector = new ReactorClientHttpConnector(
            HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext)));

        return WebClient.builder()
            .clientConnector(reactorClientHttpConnector)
            .defaultHeaders(httpHeaders -> httpHeaders.setAll(aaiHeaders))
            .filter(basicAuthentication(aaiUserName, aaiUserPassword))
            .filter(logRequest())
            .filter(logResponse())
            .build();
    }

    private SslContext createSslContext() throws SSLException {
        if (enableAaiCertAuth) {
            return sslFactory.createSecureContext(
                keyStorePath,
                keyStorePasswordPath,
                trustStorePath,
                trustStorePasswordPath
            );
        }
        return sslFactory.createInsecureContext();
    }
    
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            MDC.put(SERVICE_NAME, String.valueOf(clientRequest.url()));
            LOGGER.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                .forEach((name, values) -> values.forEach(value -> LOGGER.info("{}={}", name, value)));
            MDC.remove(SERVICE_NAME);
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            MDC.put(RESPONSE_CODE, String.valueOf(clientResponse.statusCode()));
            LOGGER.info("Response Status {}", clientResponse.statusCode());
            MDC.remove(RESPONSE_CODE);
            return Mono.just(clientResponse);
        });
    }
}
