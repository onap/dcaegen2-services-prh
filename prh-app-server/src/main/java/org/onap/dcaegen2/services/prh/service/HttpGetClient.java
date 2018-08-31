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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class HttpGetClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpGetClient.class);

    private final WebClient webClient;
    private final Gson gson;

    HttpGetClient() {
        this(WebClient.builder().filter(logRequest()).filter(logResponse()).build());
    }

    HttpGetClient(WebClient webClient) {
        this.webClient = webClient;
        this.gson = new Gson();
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("Response status {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> logger.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    <T> Mono<T> callHttpGet(String url, Class<T> tClass) {
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(getException(response)))
                .onStatus(HttpStatus::is5xxServerError, response -> Mono.error(getException(response)))
                .bodyToMono(String.class)
                .flatMap(body -> getJsonFromRequest(body, tClass));
    }

    private RuntimeException getException(ClientResponse response) {
        return new RuntimeException(String.format("Request for cloud config failed: HTTP %d",
                response.statusCode().value()));
    }

    private <T> Mono<T> getJsonFromRequest(String body, Class<T> tClass) {
        try {
            return Mono.just(parseJson(body, tClass));
        } catch (JsonSyntaxException | IllegalStateException e) {
            return Mono.error(e);
        }
    }

    private <T> T parseJson(String body, Class<T> tClass) {
        return gson.fromJson(body, tClass);
    }
}
