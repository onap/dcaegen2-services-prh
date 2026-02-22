/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
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

package org.onap.dcaegen2.services.prh.tasks.commit;

import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.impl.PnfReadyJsonBodyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * Kafka-native publisher that sends PNF registration events directly to Kafka,
 * replacing the DMaaP Message Router HTTP-based publishing for the
 * autoCommitDisabled profile.
 */
@Profile("autoCommitDisabled")
@Component
public class KafkaPublisherTaskImpl implements KafkaPublisherTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPublisherTaskImpl.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PnfReadyJsonBodyBuilder pnfReadyJsonBodyBuilder = new PnfReadyJsonBodyBuilder();

    public KafkaPublisherTaskImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void execute(String topic, ConsumerDmaapModel consumerDmaapModel) {
        String correlationId = consumerDmaapModel.getCorrelationId();
        String jsonBody = pnfReadyJsonBodyBuilder.createJsonBody(consumerDmaapModel).toString();

        LOGGER.info("Publishing to Kafka topic '{}' for PNF '{}': {}", topic, correlationId, jsonBody);

        ListenableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, correlationId, jsonBody);

        future.addCallback(
                result -> LOGGER.info("Successfully published to topic '{}' for PNF '{}', offset: {}",
                        topic, correlationId, result.getRecordMetadata().offset()),
                ex -> LOGGER.error("Failed to publish to topic '{}' for PNF '{}': {}",
                        topic, correlationId, ex.getMessage(), ex)
        );
    }
}
