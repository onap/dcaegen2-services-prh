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

package org.onap.dcaegen2.services.prh.configuration;

import org.onap.dcaegen2.services.prh.model.AaiJsonBodyBuilderImpl;
import org.onap.dcaegen2.services.prh.model.AaiPnfResultModel;
import org.onap.dcaegen2.services.prh.model.AaiServiceInstanceResultModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.AaiHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.get.AaiGetServiceInstanceClient;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.get.AaiHttpGetClient;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.patch.AaiHttpPatchClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiServiceInstanceQueryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Configuration
public class AaiHttpClientConfig {
    private final static String filterHeader = "Content-Type";

    @Autowired
    private CloudConfiguration cloudConfig;

    private final Supplier<AaiClientConfiguration> patchConfig = () -> cloudConfig.getAaiClientConfiguration();

    private final Supplier<AaiClientConfiguration> getConfig = () ->
            ImmutableAaiClientConfiguration
                    .copyOf(cloudConfig.getAaiClientConfiguration())
                    .withAaiHeaders(
                            cloudConfig
                                    .getAaiClientConfiguration()
                                    .aaiHeaders()
                                    .entrySet()
                                    .stream()
                                    .filter(x -> !x.getKey().equals(filterHeader))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    @Bean
    public AaiHttpClient<AaiModel, HttpResponse> getPatchClientFactory() {
        return createLazyConfigClient(
                patchConfig,
                (config, client) -> new AaiHttpPatchClient(config, new AaiJsonBodyBuilderImpl(), client));
    }

    @Bean
    public AaiHttpClient<AaiServiceInstanceQueryModel, AaiServiceInstanceResultModel> getServiceInstanceClient() {
        return createLazyConfigClient(
                getConfig,
                (config, client) -> new AaiGetServiceInstanceClient(config, client)
                        .map(x -> x.bodyAsJson(AaiServiceInstanceResultModel.class)));
    }

    @Bean
    public AaiHttpClient<AaiModel, AaiPnfResultModel> getGetClient() {
        return createLazyConfigClient(
                getConfig,
                (config, client) -> new AaiHttpGetClient(config, client)
                        .map(x -> x.bodyAsJson(AaiPnfResultModel.class)));
    }

    private <T, U> AaiHttpClient<T, U> createLazyConfigClient(
            final Supplier<AaiClientConfiguration> lazyConfig,
            final BiFunction<AaiClientConfiguration, CloudHttpClient, AaiHttpClient<T, U>> factoryMethod) {

        return x -> factoryMethod.apply(
                lazyConfig.get(),
                new AaiHttpClientFactory(lazyConfig.get()).build()
        ).getAaiResponse(x);
    }
}
