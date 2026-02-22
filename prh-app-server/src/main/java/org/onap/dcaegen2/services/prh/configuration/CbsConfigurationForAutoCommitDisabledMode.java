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

package org.onap.dcaegen2.services.prh.configuration;

import java.util.Optional;
import org.onap.dcaegen2.services.prh.adapter.kafka.ImmutableKafkaConfiguration;
import org.onap.dcaegen2.services.prh.adapter.kafka.KafkaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author <a href="mailto:PRANIT.KAPDULE@t-systems.com">Pranit Kapdule</a> on
 *        24/08/23
 */
@Profile("autoCommitDisabled")
public class CbsConfigurationForAutoCommitDisabledMode extends CbsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsConfigurationForAutoCommitDisabledMode.class);
    private static final String EVENTS_PATH = "/events/";

    protected KafkaConfiguration kafkaConfiguration;
    protected String pnfReadyTopic;
    protected String pnfUpdateTopic;
    
    @Override
    public void parseCBSConfig(JsonObject jsonObject) {
        
        super.parseCBSConfig(jsonObject);
        JsonObject jsonObjectforAutoCommitDisabled = jsonObject.getAsJsonObject("config");
        JsonElement jsonObjectOfKafkaConfigurations = jsonObjectforAutoCommitDisabled.get("kafka-configurations");

        kafkaConfiguration = new ImmutableKafkaConfiguration.Builder()
                .kafkaBoostrapServerConfig(
                        ((JsonObject) jsonObjectOfKafkaConfigurations).get("kafkaBoostrapServerConfig").getAsString())
                .groupIdConfig(((JsonObject) jsonObjectOfKafkaConfigurations).get("groupIdConfig").getAsString())
                .kafkaSaslMechanism(
                        ((JsonObject) jsonObjectOfKafkaConfigurations).get("kafkaSaslMechanism").getAsString())
                .kafkaSecurityProtocol(
                        ((JsonObject) jsonObjectOfKafkaConfigurations).get("kafkaSecurityProtocol").getAsString())
                .kafkaJaasConfig(System.getenv("JAAS_CONFIG"))
                .build();

        // Extract Kafka topic names from the DMaaP publish stream URLs
        pnfReadyTopic = extractTopicFromPublishRequest(getMessageRouterPublishRequest());
        pnfUpdateTopic = extractTopicFromPublishRequest(getMessageRouterUpdatePublishRequest());
        LOGGER.info("Resolved Kafka publish topics - pnfReady: {}, pnfUpdate: {}", pnfReadyTopic, pnfUpdateTopic);
 }

    private String extractTopicFromPublishRequest(
            org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest request) {
        String topicUrl = request.sinkDefinition().topicUrl();
        if (topicUrl.contains(EVENTS_PATH)) {
            String afterEvents = topicUrl.substring(topicUrl.indexOf(EVENTS_PATH) + EVENTS_PATH.length());
            return afterEvents.endsWith("/")
                    ? afterEvents.substring(0, afterEvents.length() - 1)
                    : afterEvents;
        }
        return topicUrl;
    }

    public String getPnfReadyTopic() {
        return Optional.ofNullable(pnfReadyTopic).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    public String getPnfUpdateTopic() {
        return Optional.ofNullable(pnfUpdateTopic).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    public KafkaConfiguration getKafkaConfig() {
        return Optional.ofNullable(kafkaConfiguration).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    public void setKafkaConfiguration(KafkaConfiguration kafkaConfiguration) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

}
