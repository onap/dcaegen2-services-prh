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

import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 8/9/18
 */
@Configuration
@ComponentScan("org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers")
@EnableConfigurationProperties
@EnableScheduling
@Primary
public class ConsulConfiguration extends PrhAppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulConfiguration.class);
    private AaiClientConfiguration aaiClientCBSConfiguration;
    private DmaapPublisherConfiguration dmaapPublisherCBSConfiguration;
    private DmaapConsumerConfiguration dmaapConsumerCBSConfiguration;
    @Autowired
    private ConsulConfigLoader consulConfigLoader;

    public void runTask() {
        Flux.defer(this::resolveEnvProperties)
            .subscribeOn(Schedulers.parallel())
            .subscribe(this::parsingConfigSuccess, this::parsingConfigError);
    }

    private Mono<EnvProperties> resolveEnvProperties() {
        try {
            return Mono.just(EnvProperties.fromEnvironment());
        } catch(Exception e){
            parsingConfigError(e);
            return consulConfigLoader.evaluate();
        }
    }

    private void parsingConfigSuccess(EnvProperties envProperties) {
        LOGGER.debug("Fetching PRH configuration from Consul");
        CbsClientFactory.createCbsClient(envProperties)
            .flatMap(cbsClient -> cbsClient.get(CbsRequests.getAll(RequestDiagnosticContext.create())))
            .subscribe(this::parseCBSConfig, this::cbsConfigError);
    }

    private void parseCBSConfig(JsonObject jsonObject) {
        LOGGER.info("Received application configuration: {}", jsonObject);
        ConsulConfigParser consulConfigParser = new ConsulConfigParser(jsonObject);
        dmaapPublisherCBSConfiguration = consulConfigParser.getDmaapPublisherConfig();
        aaiClientCBSConfiguration = ImmutableAaiClientConfiguration.copyOf(consulConfigParser.getAaiClientConfig())
            .withAaiHeaders(aaiClientConfiguration.aaiHeaders());
        dmaapConsumerCBSConfiguration = consulConfigParser.getDmaapConsumerConfig();
    }

    private void parsingConfigError(Throwable throwable) {
        LOGGER.warn("Failed to process system environments", throwable);
    }

    private void cbsConfigError(Throwable throwable) {
        LOGGER.warn("Failed to gather configuration from ConfigBindingService/Consul", throwable);
    }

    @Override
    public DmaapPublisherConfiguration getDmaapPublisherConfiguration() {
        return dmaapPublisherCBSConfiguration;
    }

    @Override
    public AaiClientConfiguration getAaiClientConfiguration() {
        return aaiClientCBSConfiguration;
    }

    @Override
    public DmaapConsumerConfiguration getDmaapConsumerConfiguration() {
        return dmaapConsumerCBSConfiguration;
    }
}
