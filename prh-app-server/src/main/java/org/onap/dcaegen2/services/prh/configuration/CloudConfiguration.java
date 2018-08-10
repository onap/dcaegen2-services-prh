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

import java.util.Properties;
import org.onap.dcaegen2.services.prh.model.EnvProperties;
import org.onap.dcaegen2.services.prh.service.HttpClientExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 8/9/18
 */
@Configuration
@EnableConfigurationProperties
@EnableScheduling
public class CloudConfiguration extends AppConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private HttpClientExecutorService httpClientExecutorService;

    TaskScheduler cloudTaskScheduler;

    @Value("#{systemEnvironment}")
    private Properties systemEnvironment;


    @Autowired
    public void setThreadPoolTaskScheduler(ThreadPoolTaskScheduler threadPoolTaskScheduler,
        HttpClientExecutorService httpClientExecutorService) {
        this.cloudTaskScheduler = threadPoolTaskScheduler;
        this.httpClientExecutorService = httpClientExecutorService;
    }

    protected void runTask() {
        Flux.defer(() -> EnvironmentProcessor.evaluate(systemEnvironment))
            .subscribeOn(Schedulers.parallel())
            .subscribe(this::doOnSucces, this::doOnError);
    }

    private void doOnError(Throwable throwable) {
        logger.warn("Error in case of processing system environment.%nMore details below:%n ", throwable);
    }

    private void doOnSucces(EnvProperties envProperties) {
        logger.info("Fetching PRH configuration from ConfigBindingService/Consul");
        Flux.just(httpClientExecutorService.callConsulForConfigBindingServiceEndpoint(envProperties))
            .flatMap(configBindingServiceUri -> httpClientExecutorService.callConfigBindingServiceForPrhConfiguration(envProperties,
                configBindingServiceUri)).subscribe();
    }
}
