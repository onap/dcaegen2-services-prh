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

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers.CloudConfigurationClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.Properties;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 8/9/18
 */
@Configuration
@ComponentScan("org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers")
@EnableConfigurationProperties
@EnableScheduling
@Primary
public class CloudConfiguration extends AppConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudConfiguration.class);
    private CloudConfigurationClient prhConfigurationProvider;

    private AaiClientConfiguration aaiClientCloudConfiguration;
    private DmaapPublisherConfiguration dmaapPublisherCloudConfiguration;
    private DmaapPublisherConfiguration dmaapUpdatePublisherCloudConfiguration;
    private DmaapConsumerConfiguration dmaapConsumerCloudConfiguration;

    @Value("#{systemEnvironment}")
    private Properties systemEnvironment;

    @Autowired
    public void setThreadPoolTaskScheduler(CloudConfigurationClient prhConfigurationProvider) {
        this.prhConfigurationProvider = prhConfigurationProvider;
    }

    public void runTask() {
        Flux.defer(() -> EnvironmentProcessor.evaluate(systemEnvironment))
            .subscribeOn(Schedulers.parallel())
            .subscribe(this::parsingConfigSuccess, this::parsingConfigError);
    }

    private void parsingConfigError(Throwable throwable) {
        LOGGER.warn("Failed to process system environments", throwable);
    }

    private void cloudConfigError(Throwable throwable) {
        LOGGER.warn("Failed to gather configuration from ConfigBindingService/Consul", throwable);
    }

    private void parsingConfigSuccess(EnvProperties envProperties) {
        LOGGER.debug("Fetching PRH configuration from ConfigBindingService/Consul");
        prhConfigurationProvider.callForServiceConfigurationReactive(envProperties)
            .subscribe(this::parseCloudConfig, this::cloudConfigError);
    }

    private void parseCloudConfig(JsonObject jsonObject) {
        LOGGER.info("Received application configuration: {}", jsonObject);
        CloudConfigParser cloudConfigParser = new CloudConfigParser(jsonObject);
        dmaapPublisherCloudConfiguration = cloudConfigParser.getDmaapPublisherConfig();
        dmaapUpdatePublisherCloudConfiguration = cloudConfigParser.getDmaapUpdatePublisherConfig();
        aaiClientCloudConfiguration = ImmutableAaiClientConfiguration.copyOf(cloudConfigParser.getAaiClientConfig())
            .withAaiHeaders(aaiClientConfiguration.aaiHeaders());
        dmaapConsumerCloudConfiguration = cloudConfigParser.getDmaapConsumerConfig();
    }

    @Override
    public DmaapPublisherConfiguration getDmaapPublisherConfiguration() {
        return Optional.ofNullable(dmaapPublisherCloudConfiguration).orElse(super.getDmaapPublisherConfiguration());
    }

    @Override
    public DmaapPublisherConfiguration getDmaapUpdatePublisherConfiguration() {
        return Optional.ofNullable(dmaapUpdatePublisherCloudConfiguration).orElse(super.getDmaapPublisherConfiguration());
    }

    @Override
    public AaiClientConfiguration getAaiClientConfiguration() {
        return Optional.ofNullable(aaiClientCloudConfiguration).orElse(super.getAaiClientConfiguration());
    }

    @Override
    public DmaapConsumerConfiguration getDmaapConsumerConfiguration() {
        return Optional.ofNullable(dmaapConsumerCloudConfiguration).orElse(super.getDmaapConsumerConfiguration());
    }
}
