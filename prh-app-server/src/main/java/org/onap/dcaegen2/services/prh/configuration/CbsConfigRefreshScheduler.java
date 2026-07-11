/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2026 Deutsche Telekom Intellectual Property. All rights reserved.
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

import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.services.bootstrap.CbsConfigFetcher;
import org.onap.dcaegen2.services.bootstrap.CbsProperties;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;

@Slf4j
@Component
public class CbsConfigRefreshScheduler {

    private static final Duration NO_UPDATES = Duration.ZERO;

    private final CbsConfigFetcher cbsConfigFetcher;
    private final CbsProperties cbsProperties;
    private final Scheduler scheduler;
    private volatile Disposable refreshEventsStreamHandle;


    public CbsConfigRefreshScheduler(CbsConfigFetcher cbsConfigFetcher, CbsProperties cbsProperties) {
        this.cbsConfigFetcher = cbsConfigFetcher;
        this.cbsProperties = cbsProperties;
        this.scheduler = Schedulers.newBoundedElastic(
                Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
                Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
                "conf-updates");
    }

    @PostConstruct
    public void startPollingForCbsUpdates() {
        startPollingForCbsUpdates(getCbsUpdatesInterval());
    }

    void startPollingForCbsUpdates(Duration updatesInterval) {
        if (!updatesInterval.equals(NO_UPDATES)) {
            log.info("Configuring pulling for CBS updates in every {}", updatesInterval);
            refreshEventsStreamHandle = Flux.interval(updatesInterval, scheduler)
                    .doOnNext(i -> {
                        log.debug("Requesting CBS configuration refresh");
                        cbsConfigFetcher.fetchAndParse();
                    })
                    .onErrorContinue((e, o) -> log.error("Failed fetching config updates from CBS", e))
                    .subscribe();
        }
    }

    private Duration getCbsUpdatesInterval() {
        Duration interval = cbsProperties.getUpdatesInterval();
        return interval != null ? interval : NO_UPDATES;
    }

    @PreDestroy
    void stopPollingForCbsUpdates() {
        if (refreshEventsStreamHandle != null) {
            log.debug("Stopping pulling for CBS updates");
            refreshEventsStreamHandle.dispose();
        }
    }

}
