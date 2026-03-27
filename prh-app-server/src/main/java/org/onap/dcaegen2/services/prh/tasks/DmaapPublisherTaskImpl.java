/*
 * ============LICENSE_START=======================================================
 * PROJECT
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

import java.util.function.Supplier;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.impl.PnfReadyJsonBodyBuilder;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
public class DmaapPublisherTaskImpl implements DmaapPublisherTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapPublisherTaskImpl.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Supplier<String> topicNameSupplier;
    private final PnfReadyJsonBodyBuilder pnfReadyJsonBodyBuilder = new PnfReadyJsonBodyBuilder();

    public DmaapPublisherTaskImpl(KafkaTemplate<String, String> kafkaTemplate,
                                  Supplier<String> topicNameSupplier) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicNameSupplier = topicNameSupplier;
    }

    @Override
    public Mono<String> execute(ConsumerDmaapModel consumerDmaapModel) throws DmaapNotFoundException {
        if (consumerDmaapModel == null) {
            throw new DmaapNotFoundException("Invoked null object to DMaaP task");
        }
        String topicName = topicNameSupplier.get();
        LOGGER.info("Publishing to topic {} with arg {}", topicName, consumerDmaapModel);
        String jsonBody = pnfReadyJsonBodyBuilder.createJsonBody(consumerDmaapModel).toString();
        return Mono.create(sink ->
            kafkaTemplate.send(topicName, jsonBody).addCallback(
                result -> {
                    LOGGER.info("Successfully published to {}", topicName);
                    sink.success(topicName);
                },
                error -> {
                    LOGGER.error("Failed to publish to {}", topicName, error);
                    sink.error(error);
                }
            )
        );
    }
}
