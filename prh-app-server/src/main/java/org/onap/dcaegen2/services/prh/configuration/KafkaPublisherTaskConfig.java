/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

import org.onap.dcaegen2.services.prh.tasks.KafkaPublisherTask;
import org.onap.dcaegen2.services.prh.tasks.KafkaPublisherTaskImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaPublisherTaskConfig {
    @Bean(name = "ReadyPublisherTask")
    @Autowired
    public KafkaPublisherTask getReadyPublisherTask(KafkaTemplate<String, String> kafkaTemplate, Config config) {
        return new KafkaPublisherTaskImpl(kafkaTemplate,
                () -> extractTopicName(config.getPublishTopicUrl()));
    }

    @Bean(name = "UpdatePublisherTask")
    @Autowired
    public KafkaPublisherTask getUpdatePublisherTask(KafkaTemplate<String, String> kafkaTemplate, Config config) {
        return new KafkaPublisherTaskImpl(kafkaTemplate,
                () -> extractTopicName(config.getUpdatePublishTopicUrl()));
    }

    static String extractTopicName(String topicUrl) {
        return topicUrl.substring(topicUrl.lastIndexOf('/') + 1);
    }
}
