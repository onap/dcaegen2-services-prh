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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;
import org.onap.dcaegen2.services.prh.model.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 8/10/18
 */

@Service
public class PrhConfigurationProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpGetClient httpGetClient;

    PrhConfigurationProvider() {
        this(new HttpGetClient());
    }

    PrhConfigurationProvider(HttpGetClient httpGetClient) {
        this.httpGetClient = httpGetClient;
    }

    public Mono<JsonObject> callForPrhConfiguration(EnvProperties envProperties) {
        return callConsulForConfigBindingServiceEndpoint(envProperties)
            .flatMap(this::callConfigBindingServiceForPrhConfiguration);
    }

    private Mono<String> callConsulForConfigBindingServiceEndpoint(EnvProperties envProperties) {
        logger.info("Retrieving Config Binding Service endpoint from Consul");
        try {
            return httpGetClient.callHttpGet(getConsulUrl(envProperties), JsonArray.class)
                .flatMap(jsonArray -> this.createConfigBindingserviceurl(jsonArray, envProperties.appName()));
        } catch (URISyntaxException e) {
            logger.warn("Malformed Consul uri", e);
            return Mono.error(e);
        }
    }

    private String getConsulUrl(EnvProperties envProperties) throws URISyntaxException {
        return getUri(envProperties.consulHost(), envProperties.consulPort(), "/v1/catalog/service",
            envProperties.cbsName());
    }

    private Mono<JsonObject> callConfigBindingServiceForPrhConfiguration(String configBindingServiceUri) {
        logger.info("Retrieving PRH configuration");
        return httpGetClient.callHttpGet(configBindingServiceUri, JsonObject.class);
    }


    private Mono<String> createConfigBindingserviceurl(JsonArray jsonArray, String appName) {
        return getConfigBindingObject(jsonArray)
            .flatMap(jsonObject -> buildConfigBindingserviceurl(jsonObject, appName));
    }

    private Mono<String> buildConfigBindingserviceurl(JsonObject jsonObject, String appName) {
        try {
            return Mono.just(getUri(jsonObject.get("ServiceAddress").getAsString(),
                jsonObject.get("ServicePort").getAsInt(), "/service_component", appName));
        } catch (URISyntaxException e) {
            logger.warn("Malformed Config Binding Service uri", e);
            return Mono.error(e);
        }
    }

    private Mono<JsonObject> getConfigBindingObject(JsonArray jsonArray) {
        try {
            if (jsonArray.size() > 0) {
                return Mono.just(jsonArray.get(0).getAsJsonObject());
            } else {
                throw new IllegalStateException("JSON Array was empty");
            }
        } catch (IllegalStateException e) {
            logger.warn("Failed to retrieve JSON Object from array", e);
            return Mono.error(e);
        }
    }

    private String getUri(String host, Integer port, String... paths) throws URISyntaxException {
        return new URIBuilder()
            .setScheme("http")
            .setHost(host)
            .setPort(port)
            .setPath(String.join("/", paths))
            .build().toString();
    }
}
