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

import java.nio.charset.StandardCharsets;

import java.util.function.BiFunction;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiHttpClient;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiPnfResultModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiServiceInstanceQueryModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiServiceInstanceResultModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.impl.AaiGetServiceInstanceClient;
import org.onap.dcaegen2.services.prh.adapter.aai.impl.AaiHttpGetClient;
import org.onap.dcaegen2.services.prh.adapter.aai.impl.AaiHttpPatchClient;
import org.onap.dcaegen2.services.prh.adapter.aai.impl.AaiJsonBodyBuilderImpl;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiHttpClientFactory;
import org.onap.dcaegen2.services.prh.model.utils.PrhModelAwareGsonBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AaiHttpClientConfig {

    @Autowired
    private Config config;

    @Bean
    public AaiHttpClient<ConsumerDmaapModel, HttpResponse> getPatchClientFactory() {
        return createLazyConfigClient(
                (config, client) -> new AaiHttpPatchClient(config, new AaiJsonBodyBuilderImpl(), client));
    }

    @Bean
    public AaiHttpClient<AaiServiceInstanceQueryModel, AaiServiceInstanceResultModel> getServiceInstanceClient() {
        return createLazyConfigClient(
                (config, client) -> new AaiGetServiceInstanceClient(config, client).map(httpResponse -> {
                    httpResponse.throwIfUnsuccessful();
                    return httpResponse.bodyAsJson(StandardCharsets.UTF_8, PrhModelAwareGsonBuilder.createGson(),
                            AaiServiceInstanceResultModel.class);
                }));
    }

    @Bean
    public AaiHttpClient<ConsumerDmaapModel, AaiPnfResultModel> getGetClient() {



        return createLazyConfigClient((config, client) -> new AaiHttpGetClient(config, client).map(httpResponse -> {
            httpResponse.throwIfUnsuccessful();
            return httpResponse.bodyAsJson(StandardCharsets.UTF_8, PrhModelAwareGsonBuilder.createGson(),
                    AaiPnfResultModel.class);
        }));
    }

    private <T, U> AaiHttpClient<T, U> createLazyConfigClient(
            final BiFunction<AaiClientConfiguration, RxHttpClient, AaiHttpClient<T, U>> factoryMethod) {
//        System.out.println("pnf url in AAIClientConfiguration is: " + config.getAaiClientConfiguration().pnfUrl());
//        System.out.println("base url in AAIClientConfiguration is: " + config.getAaiClientConfiguration().baseUrl());
        return x -> factoryMethod.apply(config.getAaiClientConfiguration(),
                new AaiHttpClientFactory(config.getAaiClientConfiguration()).build()).getAaiResponse(x);

    }
}
