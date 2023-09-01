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
import org.springframework.context.annotation.Profile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author <a href="mailto:PRANIT.KAPDULE@t-systems.com">Pranit Kapdule</a> on
 *        24/08/23
 */
@Profile("autoCommitDisabled")
public class CbsConfigurationForAutoCommitDisabledMode extends CbsConfiguration {

    protected KafkaConfiguration kafkaConfiguration;
    
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
        
 }

    public KafkaConfiguration getKafkaConfig() {
        return Optional.ofNullable(kafkaConfiguration).orElseThrow(() -> new RuntimeException(CBS_CONFIG_MISSING));
    }

    public void setKafkaConfiguration(KafkaConfiguration kafkaConfiguration) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

}
