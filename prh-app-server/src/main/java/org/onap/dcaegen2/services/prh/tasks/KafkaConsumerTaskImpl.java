/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.tasks;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.service.KafkaConsumerJsonParser;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;


/**
 * Consumes PNF registration events from Kafka and processes them through the
 * PRH workflow pipeline directly. Offsets are committed only after successful processing.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaConsumerTaskImpl {
    private static final String EVENTS_PATH = "/events/";

    private final KafkaConsumerJsonParser kafkaConsumerJsonParser;
    private final Config config;
    private final PrhWorkflowProcessor scheduledTasks;

    @PostConstruct
    void configureKafkaTopic() {
        try {
            String topicUrl = config.getSubscribeTopicUrl();
            String topic = extractTopicFromUrl(topicUrl);
            String consumerGroup = config.getSubscribeConsumerGroup();

            System.setProperty("kafkaTopic", topic);
            System.setProperty("groupIdConfig", consumerGroup);
            log.info("Configured Kafka consumer for topic: {}, consumerGroup: {}", topic, consumerGroup);
        } catch (RuntimeException e) {
            log.warn("CBS config not yet available, Kafka topic will be configured later: {}", e.getMessage());
        }
    }

    @KafkaListener(id = "prhKafkaListener", topics = "${kafkaTopic:unset}", groupId = "${groupIdConfig:unset}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        if (records == null || records.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        log.info("Received {} records from Kafka", records.size());
        List<String> values = records.stream()
                .map(ConsumerRecord::value)
                .collect(Collectors.toList());

        Flux<ConsumerPnfModel> models = kafkaConsumerJsonParser.getConsumerModelFromKafkaRecords(values);
        boolean shouldCommit = scheduledTasks.processMessages(models);

        if (shouldCommit) {
            acknowledgment.acknowledge();
            log.info("Committed Kafka offset");
        } else {
            log.info("Offset not committed — will retry on next poll");
        }
    }

    static String extractTopicFromUrl(String topicUrl) {
        int idx = topicUrl.indexOf(EVENTS_PATH);
        if (idx < 0) {
            return topicUrl;
        }
        String topic = topicUrl.substring(idx + EVENTS_PATH.length());
        if (topic.endsWith("/")) {
            topic = topic.substring(0, topic.length() - 1);
        }
        return topic;
    }
}
