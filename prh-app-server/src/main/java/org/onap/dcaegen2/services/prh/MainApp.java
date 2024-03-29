/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh;

import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.INVOCATION_ID;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on
 *         3/23/18
 */
@SpringBootApplication(exclude = { JacksonAutoConfiguration.class })
@EnableScheduling
@EnableConfigurationProperties
public class MainApp {

    
    public static void main(String[] args) {
       
        SpringApplication.run(MainApp.class, args);

    }

    @Bean
    Map<String, String> mdcContextMap() {
        MDC.put(INVOCATION_ID, UUID.randomUUID().toString());
        return MDC.getCopyOfContextMap();
    }

    @Bean
    TaskScheduler concurrentTaskScheduler() {
        return new ConcurrentTaskScheduler();
    }

}
