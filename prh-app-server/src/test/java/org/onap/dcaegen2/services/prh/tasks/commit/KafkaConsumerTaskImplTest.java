/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.onap.dcaegen2.services.prh.tasks.commit.EpochDateTimeConversion;
import org.onap.dcaegen2.services.prh.tasks.commit.KafkaConsumerTaskImpl;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.kafka.support.Acknowledgment;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerTaskImplTest {

    @Mock
    private Acknowledgment acknowledgment;

    @Mock
    private DmaapConsumerJsonParser dmaapConsumerJsonParser;

    @Mock
    private EpochDateTimeConversion epochDateTimeConversion;

    @InjectMocks
    private KafkaConsumerTaskImpl kafkaConsumerTask;

    @Test
    public void onMessageTest(){
        List<ConsumerRecord<String, String>> list = new ArrayList<>();
        TimestampType timestampType = null;
        Headers headers = new RecordHeaders();
        epochDateTimeConversion.setDaysForRecords("3");
        ConsumerRecord<String, String> records = new ConsumerRecord<>
                ("test-topic", 1, 1l, 0l, timestampType, 1, 1, "test-key", "test-value", headers
        , null);
        list.add(records);
        kafkaConsumerTask.onMessage(list, acknowledgment);
    }

    @Test
    public void commitOffsetTest(){
        kafkaConsumerTask.commitOffset();
    }

    @Test
    public void executeTest() throws JSONException {
        List<String> jsonEvent = new ArrayList<>();
        ConsumerDmaapModel consumerDmaapModel = ImmutableConsumerDmaapModel.builder().correlationId("123").build();
        when(dmaapConsumerJsonParser.getConsumerDmaapModelFromKafkaConsumerRecord(jsonEvent)).thenReturn(Flux.just(consumerDmaapModel));
        kafkaConsumerTask.execute();
    }
}
