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

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.onap.dcaegen2.services.prh.adapter.aai.impl.PnfReadyJsonBodyBuilder;
import org.onap.dcaegen2.services.prh.exceptions.KafkaNotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaPublisherTaskImpl implements KafkaPublisherTask {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Supplier<String> topicNameSupplier;
    private final PnfReadyJsonBodyBuilder pnfReadyJsonBodyBuilder = new PnfReadyJsonBodyBuilder();

    @Override
    public Mono<String> execute(ConsumerPnfModel consumerPnfModel) throws KafkaNotFoundException {
        if (consumerPnfModel == null) {
            throw new KafkaNotFoundException("Invoked null object to Kafka task");
        }
        String topicName = topicNameSupplier.get();
        log.info("Publishing to topic {} with arg {}", topicName, consumerPnfModel);
        String jsonBody = pnfReadyJsonBodyBuilder.createJsonBody(consumerPnfModel).toString();
        return Mono.create(sink ->
            kafkaTemplate.send(topicName, jsonBody).addCallback(
                result -> {
                    log.info("Successfully published to {}", topicName);
                    sink.success(topicName);
                },
                error -> {
                    log.error("Failed to publish to {}", topicName, error);
                    sink.error(error);
                }
            )
        );
    }
}
