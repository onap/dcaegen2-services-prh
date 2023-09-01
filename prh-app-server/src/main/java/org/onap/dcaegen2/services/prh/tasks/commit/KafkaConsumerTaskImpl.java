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
import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.configuration.CbsConfigurationForAutoCommitDisabledMode;
import org.onap.dcaegen2.services.prh.service.DmaapConsumerJsonParser;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ajinkya-patil@t-systems.com">Ajinkya Patil</a> on
 *         3/13/23
 */

@Profile("autoCommitDisabled")
@Component
public class KafkaConsumerTaskImpl implements KafkaConsumerTask, BatchAcknowledgingMessageListener<String, String> {
   
   
    private DmaapConsumerJsonParser dmaapConsumerJsonParser;

    private EpochDateTimeConversion epochDateTimeConversion;

    private CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode;

    private List<String> jsonEvent = new ArrayList<>();

    public List<String> getJsonEvent() {
        return jsonEvent;
    }

    private Acknowledgment offset;

    public Acknowledgment getOffset() {
        return offset;
    }

    static String commonInURL = "/events/";

    String kafkaTopic;

    String groupIdConfig;
    
    
    public KafkaConsumerTaskImpl(CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode
            ,DmaapConsumerJsonParser dmaapConsumerJsonParser,EpochDateTimeConversion epochDateTimeConversion) {
        this.cbsConfigurationForAutoCommitDisabledMode = cbsConfigurationForAutoCommitDisabledMode;
        this.dmaapConsumerJsonParser = dmaapConsumerJsonParser;
        this.epochDateTimeConversion = epochDateTimeConversion;
        String kafkaTopicURL = this.cbsConfigurationForAutoCommitDisabledMode.getMessageRouterSubscribeRequest()
                .sourceDefinition().topicUrl();
        kafkaTopic = getTopicFromTopicUrl(kafkaTopicURL);
        groupIdConfig = cbsConfigurationForAutoCommitDisabledMode.getMessageRouterSubscribeRequest().consumerGroup();

        System.setProperty("kafkaTopic", kafkaTopic);
        System.setProperty("groupIdConfig", groupIdConfig);

    }

    @Override
    @KafkaListener(topics = "${kafkaTopic}", groupId = "${groupIdConfig}")
    public void onMessage(List<ConsumerRecord<String, String>> list, Acknowledgment acknowledgment) {

        if (list != null && !list.isEmpty()) {
            list.stream().filter(
                    consumerRecord -> consumerRecord.timestamp() >= epochDateTimeConversion.getStartDateOfTheDay()
                            && consumerRecord.timestamp() <= epochDateTimeConversion.getEndDateOfTheDay())
                    .map(ConsumerRecord::value).forEach(value -> {
                        jsonEvent.add(value);
                    });

        }

        offset = acknowledgment;
    }

    @Override
    public Flux<ConsumerDmaapModel> execute() throws JSONException {
        return dmaapConsumerJsonParser.getConsumerDmaapModelFromKafkaConsumerRecord(jsonEvent);
    }

    public void setJsonEvent(List<String> jsonEvent) {
        this.jsonEvent = jsonEvent;
    }

    @Override
    public void commitOffset() {
        if (!jsonEvent.isEmpty()) {
            jsonEvent.clear();
        }
        if (offset != null) {
            offset.acknowledge();
        }
    }

    public String getTopicFromTopicUrl(String topicUrl) {
        if (topicUrl.endsWith("/")) {
            return topicUrl.substring(topicUrl.indexOf(commonInURL) + commonInURL.length(), topicUrl.lastIndexOf("/"));
        }
        return topicUrl.substring(topicUrl.indexOf(commonInURL) + commonInURL.length());
    }

}
