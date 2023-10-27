/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
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

package org.onap.dcaegen2.services.prh.tasks.commit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.configuration.CbsConfigurationForAutoCommitDisabledMode;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.springframework.kafka.support.Acknowledgment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import reactor.core.publisher.Flux;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static java.lang.ClassLoader.getSystemResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerTaskImplTest {

    @Mock
    private Acknowledgment acknowledgment;

    private KafkaConsumerTaskImpl kafkaConsumerTask;

    private DmaapConsumerJsonParser dmaapConsumerJsonParser;

    private EpochDateTimeConversion epochDateTimeConversion;

    private CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabled;

    private JsonObject cbsConfigJsonForAutoCommitDisabled;

    @BeforeEach
    void beforeEach() throws JsonSyntaxException, IOException, URISyntaxException {
        cbsConfigJsonForAutoCommitDisabled = new Gson().fromJson(
                new String(Files.readAllBytes(
                        Paths.get(getSystemResource("autoCommitDisabledConfigurationFromCbs2.json").toURI()))),
                JsonObject.class);
        cbsConfigurationForAutoCommitDisabled = new CbsConfigurationForAutoCommitDisabledMode();
        dmaapConsumerJsonParser = new DmaapConsumerJsonParser();
        epochDateTimeConversion = new EpochDateTimeConversion();

    }

    @Test
    void beforeOnMessageTest() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
        .and("BOOTSTRAP_SERVERS", "localhost:9092")
        .execute(() -> {
            cbsConfigurationForAutoCommitDisabled.parseCBSConfig(cbsConfigJsonForAutoCommitDisabled);
            kafkaConsumerTask = new KafkaConsumerTaskImpl(cbsConfigurationForAutoCommitDisabled,
                    dmaapConsumerJsonParser, epochDateTimeConversion);
            List<ConsumerRecord<String, String>> list = new ArrayList<>();
            TimestampType timestampType = null;
            Headers headers = new RecordHeaders();
            epochDateTimeConversion.setDaysForRecords("3");
            ConsumerRecord<String, String> records = new ConsumerRecord<>("test-topic", 1, 1l, 0l, timestampType, 1, 1,
                    "test-key", "test-value", headers, null);
            list.add(records);
            kafkaConsumerTask.onMessage(list, acknowledgment);
            String actualTopicInList = list.get(0).topic();
            String expectedTopicInList = "test-topic";
            assertEquals(expectedTopicInList, actualTopicInList, "topic is not matching");
            assertThat(kafkaConsumerTask.getOffset().equals(acknowledgment));
            assertThat(kafkaConsumerTask.getJsonEvent().contains("test-topic"));
        });
    }

    @Test
    void beforeCommitOffsetTest() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
        .and("BOOTSTRAP_SERVERS", "localhost:9092")
        .execute(() -> {
            cbsConfigurationForAutoCommitDisabled.parseCBSConfig(cbsConfigJsonForAutoCommitDisabled);
            kafkaConsumerTask = new KafkaConsumerTaskImpl(cbsConfigurationForAutoCommitDisabled,
                    dmaapConsumerJsonParser, epochDateTimeConversion);
            kafkaConsumerTask.commitOffset();
        });
    }

    @Test
    void beforeExecuteTest() throws Exception {
        withEnvironmentVariable("JAAS_CONFIG", "jaas_config")
        .and("BOOTSTRAP_SERVERS", "localhost:9092")
        .execute(() -> {
            cbsConfigurationForAutoCommitDisabled.parseCBSConfig(cbsConfigJsonForAutoCommitDisabled);
            kafkaConsumerTask = new KafkaConsumerTaskImpl(cbsConfigurationForAutoCommitDisabled,
                    dmaapConsumerJsonParser, epochDateTimeConversion);
            String event = getResourceContent("integration/event.json");
            java.util.List<String> eventList = new ArrayList<>();
            eventList.add(event);
            kafkaConsumerTask.setJsonEvent(eventList);
            Flux<ConsumerDmaapModel> flux = kafkaConsumerTask.execute();
            String expectedSourceName = "NOK6061ZW8";
            String actualSourceName = flux.blockFirst().getCorrelationId();

            String expectedOamV4IpAddress = "val3";
            String actualOamV4IpAddress = flux.blockFirst().getIpv4();

            String expectedOamV6IpAddress = "val4";
            String actualOamV6IpAddress = flux.blockFirst().getIpv6();

            assertEquals(expectedSourceName, actualSourceName, "SourceName is not matching");
            assertEquals(expectedOamV4IpAddress, actualOamV4IpAddress, "OamV4IpAddress is not matching");
            assertEquals(expectedOamV6IpAddress, actualOamV6IpAddress, "OamV6IpAddress is not matching");
        });
    }

    private static String getResourceContent(String resourceName) {
        try {
            return new String(Files.readAllBytes(Paths.get(getSystemResource(resourceName).toURI())));
        } catch (Exception e) {
            throw new RuntimeException("failed loading content of '" + resourceName + "'", e);
        }
    }

}
