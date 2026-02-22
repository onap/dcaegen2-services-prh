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

/**
 * Task interface for publishing PNF registration events directly to Kafka,
 * replacing the DMaaP Message Router HTTP-based publishing.
 */
public interface KafkaPublisherTask {

    /**
     * Publish a PNF event to the given Kafka topic.
     *
     * @param topic the Kafka topic to publish to
     * @param consumerDmaapModel the PNF event model to publish
     */
    void execute(String topic, ConsumerDmaapModel consumerDmaapModel);
}
