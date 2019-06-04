/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;

@Component
public class CbsConfigRefreshScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsConfigRefreshScheduler.class);
    private static final String CBS_UPDATES_INTERVAL_PROPERTY = "cbs.updates-interval";
    private static final Duration NO_UPDATES = Duration.ZERO;

    private final ContextRefresher contextRefresher;
    private final Environment environment;
    private final Scheduler scheduler;
    private transient Disposable refreshEventsStreamHandle;


    public CbsConfigRefreshScheduler(ContextRefresher contextRefresher, Environment environment) {
        this.contextRefresher = contextRefresher;
        this.environment = environment;
        this.scheduler = Schedulers.newElastic("conf-updates");
    }

    @PostConstruct
    public void startPollingForCbsUpdates() {
        startPollingForCbsUpdates(getCbsUpdatesInterval());
    }

    private void startPollingForCbsUpdates(Duration updatesInterval) {
        if (!updatesInterval.equals(NO_UPDATES)) {
            LOGGER.info("Configuring pulling for CBS updates in every {}", updatesInterval);
            refreshEventsStreamHandle = Flux.interval(updatesInterval, scheduler)
                    .doOnNext(i -> {
                        LOGGER.debug("Requesting context refresh");
                        contextRefresher.refresh();
                    })
                    .onErrorContinue((e, o) -> LOGGER.error("Failed fetching config updates from CBS", e))
                    .subscribe();
        }
    }

    @EventListener
    public void onEnvironmentChanged(EnvironmentChangeEvent event) {
        if (event.getKeys().contains(CBS_UPDATES_INTERVAL_PROPERTY)) {
            LOGGER.info("CBS config polling interval changed to {}", environment.getProperty(CBS_UPDATES_INTERVAL_PROPERTY));
            stopPollingForCbsUpdates();
            startPollingForCbsUpdates(getCbsUpdatesInterval());
        }
    }

    private Duration getCbsUpdatesInterval() {
        return environment.getProperty(CBS_UPDATES_INTERVAL_PROPERTY, Duration.class, NO_UPDATES);
    }

    @PreDestroy
    private void stopPollingForCbsUpdates() {
        if(refreshEventsStreamHandle != null) {
            LOGGER.debug("Stopping pulling for CBS updates");
            refreshEventsStreamHandle.dispose();
        }
    }

}
