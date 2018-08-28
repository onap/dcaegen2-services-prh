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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.prh.model.EnvProperties;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 8/10/18
 */

@Service
public class HttpClientExecutorService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Gson gson = new Gson();

    public Mono<String> callConsulForConfigBindingServiceEndpoint(EnvProperties envProperties) {

        return HttpGetClient.callHttpGet(
            "http://" + envProperties.consulHost() + ":" + envProperties.consulPort() + "/v1/catalog/service/" + envProperties
                .cbsName())
            .flatMap(this::getJsonArrayFromRequest)
            .flatMap(this::createConfigBindingServiceURL);
    }

    public Publisher<JsonObject> callConfigBindingServiceForPrhConfiguration(EnvProperties envProperties,
        Mono<String> configBindingServiceUri) {
        return configBindingServiceUri
            .flatMap(uri -> HttpGetClient.callHttpGet("http://" + uri + "/service_component/" + envProperties.appName()))
            .flatMap(this::getJsonConfiguration);
    }

    private Mono<? extends JsonObject> getJsonConfiguration(String body) {
        JsonElement jsonElement = parseJson(body, JsonElement.class);
        try {
            return Mono.just(jsonElement.getAsJsonObject());
        } catch (IllegalStateException e) {
            return Mono.error(e);
        }
    }

    private Mono<String> createConfigBindingServiceURL(JsonArray jsonArray) {
        JsonObject jsonObject;
        try {
            Optional<JsonElement> node = Lists.newArrayList(jsonArray.iterator()).stream().filter(element ->
                "dev-consul-server-1".equals(element.getAsJsonObject().get("Node").getAsString())
            ).findFirst();
            jsonObject = node.get().getAsJsonObject();
        } catch (IllegalStateException e) {
            return Mono.error(e);
        }
        return Mono.just(jsonObject.get("ServiceAddress").getAsString().replaceAll("\"","") + ":" + jsonObject.get("ServicePort").getAsInt());
    }


    private Mono<? extends JsonArray> getJsonArrayFromRequest(String body) {
        try {
            return Mono.just(parseJson(body, JsonArray.class));
        } catch (IllegalStateException e) {
            logger.warn("Converting string to jsonArray threw error: " + e);
            return Mono.error(e);
        }
    }

    private <T> T  parseJson(String body, Class<T> tClass){
        return gson.fromJson(body, tClass);
    }

    private static class HttpGetClient {

        private static final Logger logger = LoggerFactory.getLogger(HttpGetClient.class);

        private static WebClient webClient;

        private HttpGetClient() {
        }

        private static Mono<String> callHttpGet(String url) {
            return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                    Mono.error(new Exception("Request for cloud config failed: HTTP 400")))
                .onStatus(HttpStatus::is5xxServerError, response ->
                    Mono.error(new Exception("Request for cloud config failed: HTTP 500")))
                .bodyToMono(String.class);
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

        static {
            webClient = WebClient.builder().filter(logRequest()).filter(logResponse()).build();
        }


    }
}
