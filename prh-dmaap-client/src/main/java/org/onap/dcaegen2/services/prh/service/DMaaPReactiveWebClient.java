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

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/4/18
 */
public class DMaaPReactiveWebClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private DMaaPReactiveWebClient() {
    }

    private WebClient create(WebClientBuilder webClientBuilder) {
        return WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, webClientBuilder.dMaaPContentType)
            .filter(basicAuthentication(webClientBuilder.dMaaPUserName, webClientBuilder.dMaaPUserPassword))
            .filter(logRequest())
            .filter(logResponse())
            .build();
    }

    ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("Response Status {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                .forEach((name, values) -> values.forEach(value -> logger.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    public static class WebClientBuilder {

        private String dMaaPContentType;
        private String dMaaPUserName;
        private String dMaaPUserPassword;

        public WebClientBuilder() {
        }

        public WebClientBuilder dmaapContentType(String dmaapContentType) {
            this.dMaaPContentType = dmaapContentType;
            return this;
        }

        public WebClientBuilder dmaapUserName(String dmaapUserName) {
            this.dMaaPUserName = dmaapUserName;
            return this;
        }

        public WebClientBuilder dmaapUserPassword(String dmaapUserPassword) {
            this.dMaaPUserPassword = dmaapUserPassword;
            return this;
        }

        public WebClient build() {
            return new DMaaPReactiveWebClient().create(this);
        }
    }
}
