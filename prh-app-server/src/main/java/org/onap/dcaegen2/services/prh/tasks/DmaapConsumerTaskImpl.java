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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;


/**
 * Consumes PNF registration events from Kafka topic using Spring KafkaListener.
 * Replaces the previous SDK MessageRouterSubscriber-based implementation.
 */
@Profile("!autoCommitDisabled")
@Component
public class DmaapConsumerTaskImpl implements DmaapConsumerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapConsumerTaskImpl.class);
    private static final String EVENTS_PATH = "/events/";

    private final DmaapConsumerJsonParser dmaapConsumerJsonParser;
    private final Config config;
    private final List<String> messageBuffer = Collections.synchronizedList(new ArrayList<>());

    public DmaapConsumerTaskImpl(Config config, DmaapConsumerJsonParser dmaapConsumerJsonParser) {
        this.dmaapConsumerJsonParser = dmaapConsumerJsonParser;
        this.config = config;
    }

    @PostConstruct
    void configureKafkaTopic() {
        try {
            String topicUrl = config.getMessageRouterSubscribeRequest().sourceDefinition().topicUrl();
            String topic = extractTopicFromUrl(topicUrl);
            String consumerGroup = config.getMessageRouterSubscribeRequest().consumerGroup();

            System.setProperty("kafkaTopic", topic);
            System.setProperty("groupIdConfig", consumerGroup);
            LOGGER.info("Configured Kafka consumer for topic: {}, consumerGroup: {}", topic, consumerGroup);
        } catch (RuntimeException e) {
            LOGGER.warn("CBS config not yet available, Kafka topic will be configured later: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "${kafkaTopic:unset}", groupId = "${groupIdConfig:unset}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        if (records != null && !records.isEmpty()) {
            LOGGER.info("Received {} records from Kafka", records.size());
            records.stream()
                   .map(ConsumerRecord::value)
                   .forEach(messageBuffer::add);
        }
        acknowledgment.acknowledge();
    }

    @Override
    public Flux<ConsumerDmaapModel> execute() {
        if (messageBuffer.isEmpty()) {
            return Flux.empty();
        }
        List<String> batch;
        synchronized (messageBuffer) {
            batch = new ArrayList<>(messageBuffer);
            messageBuffer.clear();
        }
        LOGGER.info("Processing batch of {} PNF registration events", batch.size());
        return dmaapConsumerJsonParser.getConsumerDmaapModelFromKafkaConsumerRecord(batch);
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
