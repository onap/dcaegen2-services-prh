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
import java.util.Optional;
import java.util.Properties;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.prh.model.EnvProperties;
import org.onap.dcaegen2.services.prh.service.PrhConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 8/9/18
 */
@Configuration
@EnableConfigurationProperties
@EnableScheduling
@Primary
public class CloudConfiguration extends AppConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private PrhConfigurationProvider prhConfigurationProvider;

    private AaiClientConfiguration aaiClientCloudConfiguration;
    private DmaapPublisherConfiguration dmaapPublisherCloudConfiguration;
    private DmaapConsumerConfiguration dmaapConsumerCloudConfiguration;

    @Value("#{systemEnvironment}")
    private Properties systemEnvironment;


    @Autowired
    public void setThreadPoolTaskScheduler(PrhConfigurationProvider prhConfigurationProvider) {
        this.prhConfigurationProvider = prhConfigurationProvider;
    }

    protected void runTask() {
        Flux.defer(() -> EnvironmentProcessor.evaluate(systemEnvironment))
            .subscribeOn(Schedulers.parallel())
            .subscribe(this::parsingConfigSuccess, this::parsingConfigError);
    }

    private void parsingConfigError(Throwable throwable) {
        logger.warn("Error in case of processing system environment, more details below: ", throwable);
    }

    private void cloudConfigError(Throwable throwable) {
        logger.warn("Exception during getting configuration from CONSUL/CONFIG_BINDING_SERVICE ", throwable);
    }

    private void parsingConfigSuccess(EnvProperties envProperties) {
        logger.info("Fetching PRH configuration from ConfigBindingService/Consul");
        prhConfigurationProvider.callForPrhConfiguration(envProperties)
            .subscribe(this::parseCloudConfig, this::cloudConfigError);
    }

    private void parseCloudConfig(JsonObject jsonObject) {
        logger.info("Received application configuration: {}", jsonObject);
        CloudConfigParser cloudConfigParser = new CloudConfigParser(jsonObject);
        dmaapPublisherCloudConfiguration = cloudConfigParser.getDmaapPublisherConfig();
        aaiClientCloudConfiguration = cloudConfigParser.getAaiClientConfig();
        dmaapConsumerCloudConfiguration = cloudConfigParser.getDmaapConsumerConfig();
    }

    @Override
    public DmaapPublisherConfiguration getDmaapPublisherConfiguration() {
        return Optional.ofNullable(dmaapPublisherCloudConfiguration).orElse(super.getDmaapPublisherConfiguration());
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
