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

package org.onap.dcaegen2.services.prh.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;
import java.util.Collections;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerPnfModel;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.service.KafkaConsumerJsonParser;
import org.springframework.kafka.support.Acknowledgment;
import reactor.core.publisher.Flux;

/**
 * Unit tests for {@link KafkaConsumerTaskImpl}, the {@code @KafkaListener} entry point.
 *
 * <p>Focuses on the offset-commit contract that PRH relies on: the offset is committed
 * only when the workflow reports every PNF in the batch was found in AAI; otherwise the
 * batch is left uncommitted so it is re-polled later. Also covers the empty/null batch
 * short-circuit, the {@code @PostConstruct} topic configuration and its CBS-not-yet-ready
 * fallback, and topic extraction from the CBS-provided subscribe URL.
 */
@ExtendWith(MockitoExtension.class)
class KafkaConsumerTaskImplTest {

    private static final String TOPIC = "unauthenticated.VES_PNFREG_OUTPUT";

    @Mock
    private KafkaConsumerJsonParser kafkaConsumerJsonParser;

    @Mock
    private Config config;

    @Mock
    private PrhWorkflowProcessor scheduledTasks;

    @Mock
    private Acknowledgment acknowledgment;

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("kafkaTopic");
        System.clearProperty("groupIdConfig");
    }

    private KafkaConsumerTaskImpl newTask() {
        return new KafkaConsumerTaskImpl(kafkaConsumerJsonParser, config, scheduledTasks);
    }

    private ConsumerRecord<String, String> record(String value) {
        return new ConsumerRecord<>(TOPIC, 0, 0L, "key", value);
    }

    // ==================== onMessage: offset-commit semantics ====================

    @Test
    void whenAllPnfsFound_shouldCommitOffset() {
        Flux<ConsumerPnfModel> models = Flux.empty();
        given(kafkaConsumerJsonParser.getConsumerModelFromKafkaRecords(any())).willReturn(models);
        given(scheduledTasks.processMessages(models)).willReturn(true);

        newTask().onMessage(Collections.singletonList(record("{\"event\":{}}")), acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    @Test
    void whenAnyPnfNotFound_shouldNotCommitOffset() {
        Flux<ConsumerPnfModel> models = Flux.empty();
        given(kafkaConsumerJsonParser.getConsumerModelFromKafkaRecords(any())).willReturn(models);
        given(scheduledTasks.processMessages(models)).willReturn(false);

        newTask().onMessage(Collections.singletonList(record("{\"event\":{}}")), acknowledgment);

        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void onMessage_shouldPassAllRecordValuesToParser() {
        Flux<ConsumerPnfModel> models = Flux.empty();
        given(kafkaConsumerJsonParser.getConsumerModelFromKafkaRecords(any())).willReturn(models);
        given(scheduledTasks.processMessages(models)).willReturn(true);

        newTask().onMessage(Arrays.asList(record("first"), record("second")), acknowledgment);

        verify(kafkaConsumerJsonParser)
                .getConsumerModelFromKafkaRecords(Arrays.asList("first", "second"));
        verify(scheduledTasks).processMessages(models);
        verify(acknowledgment).acknowledge();
    }

    // ==================== onMessage: empty / null batch short-circuit ====================

    @Test
    void whenRecordsEmpty_shouldAckImmediatelyWithoutProcessing() {
        newTask().onMessage(Collections.emptyList(), acknowledgment);

        verify(acknowledgment).acknowledge();
        verifyNoInteractions(kafkaConsumerJsonParser, scheduledTasks);
    }

    @Test
    void whenRecordsNull_shouldAckImmediatelyWithoutProcessing() {
        newTask().onMessage(null, acknowledgment);

        verify(acknowledgment).acknowledge();
        verifyNoInteractions(kafkaConsumerJsonParser, scheduledTasks);
    }

    // ==================== configureKafkaTopic (@PostConstruct) ====================

    @Test
    void configureKafkaTopic_shouldPublishTopicAndGroupToSystemProperties() {
        given(config.getSubscribeTopicUrl())
                .willReturn("http://message-router:3904/events/" + TOPIC);
        given(config.getSubscribeConsumerGroup()).willReturn("OpenDCAE-c12");

        newTask().configureKafkaTopic();

        assertThat(System.getProperty("kafkaTopic")).isEqualTo(TOPIC);
        assertThat(System.getProperty("groupIdConfig")).isEqualTo("OpenDCAE-c12");
    }

    @Test
    void configureKafkaTopic_whenCbsConfigNotYetAvailable_shouldNotThrow() {
        given(config.getSubscribeTopicUrl()).willThrow(new RuntimeException("CBS config missing"));

        KafkaConsumerTaskImpl task = newTask();

        assertThatCode(task::configureKafkaTopic).doesNotThrowAnyException();
        assertThat(System.getProperty("kafkaTopic")).isNull();
    }

    // ==================== extractTopicFromUrl ====================

    @Test
    void extractTopicFromUrl_shouldReturnSegmentAfterEventsPath() {
        assertThat(KafkaConsumerTaskImpl.extractTopicFromUrl(
                "http://message-router:3904/events/" + TOPIC)).isEqualTo(TOPIC);
    }

    @Test
    void extractTopicFromUrl_shouldStripTrailingSlash() {
        assertThat(KafkaConsumerTaskImpl.extractTopicFromUrl(
                "http://message-router:3904/events/" + TOPIC + "/")).isEqualTo(TOPIC);
    }

    @Test
    void extractTopicFromUrl_whenNoEventsPath_shouldReturnInputUnchanged() {
        assertThat(KafkaConsumerTaskImpl.extractTopicFromUrl(TOPIC)).isEqualTo(TOPIC);
    }
}
