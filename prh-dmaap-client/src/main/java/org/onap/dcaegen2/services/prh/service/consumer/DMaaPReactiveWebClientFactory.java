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

package org.onap.dcaegen2.services.prh.service.consumer;

import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.RESPONSE_CODE;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.SERVICE_NAME;

import io.netty.handler.ssl.SslContext;
import javax.net.ssl.SSLException;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
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


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/4/18
 */
public class DMaaPReactiveWebClientFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SslFactory sslFactory;

    public DMaaPReactiveWebClientFactory() {
        this(new SslFactory());
    }

    DMaaPReactiveWebClientFactory(SslFactory sslFactory) {
        this.sslFactory = sslFactory;
    }

    /**
     * Construct Reactive WebClient with appropriate settings.
     *
     * @return WebClient
     */
    public WebClient build(DmaapConsumerConfiguration consumerConfiguration) throws SSLException {
        SslContext sslContext = createSslContext(consumerConfiguration);
        ClientHttpConnector reactorClientHttpConnector = new ReactorClientHttpConnector(
                HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext)));
        return WebClient.builder()
                .clientConnector(reactorClientHttpConnector)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private SslContext createSslContext(DmaapConsumerConfiguration consumerConfiguration) throws SSLException {
        if (consumerConfiguration.enableDmaapCertAuth()) {
            return sslFactory.createSecureContext(
                    consumerConfiguration.keyStorePath(), consumerConfiguration.keyStorePasswordPath(),
                    consumerConfiguration.trustStorePath(), consumerConfiguration.trustStorePasswordPath()
            );
        }
        return sslFactory.createInsecureContext();
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            MDC.put(RESPONSE_CODE, String.valueOf(clientResponse.statusCode()));
            logger.info("Response Status {}", clientResponse.statusCode());
            MDC.remove(RESPONSE_CODE);
            return Mono.just(clientResponse);
        });
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            MDC.put(SERVICE_NAME, String.valueOf(clientRequest.url()));
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                .forEach((name, values) -> values.forEach(value -> logger.info("{}={}", name, value)));
            MDC.remove(SERVICE_NAME);
            return Mono.just(clientRequest);
        });
    }

}
