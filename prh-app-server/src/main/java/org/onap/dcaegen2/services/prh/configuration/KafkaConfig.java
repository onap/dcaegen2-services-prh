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

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

 /**
  *  * @author <a href="mailto:PRANIT.KAPDULE@t-systems.com">Pranit Kapdule</a> on
  *   *        24/08/23
  *    */

@Profile("autoCommitDisabled")
@EnableKafka
@Configuration
public class KafkaConfig {

    CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode;

    public String kafkaBoostrapServerConfig;
    public String groupIdConfig;
    public String kafkaSecurityProtocol;
    public String kafkaSaslMechanism;
    public String kafkaUsername;
    public String kafkaPassword;
    public String kafkaJaasConfigName;
    public String kafkaLoginModuleClassConfig;
    public String kafkaJaasConfig;

    public final String DEFAULT_KAFKA_SECURITY_PROTOCOL = "SASL_PLAINTEXT";
    public final String DEFAULT_KAFKA_SASL_MECHANISM = "SCRAM-SHA-512";
    
    public KafkaConfig() {
        
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode) {
        this.cbsConfigurationForAutoCommitDisabledMode = cbsConfigurationForAutoCommitDisabledMode;
        kafkaBoostrapServerConfig = cbsConfigurationForAutoCommitDisabledMode.getKafkaConfig()
                .kafkaBoostrapServerConfig();
        groupIdConfig = cbsConfigurationForAutoCommitDisabledMode.getKafkaConfig().groupIdConfig();
        kafkaSecurityProtocol = cbsConfigurationForAutoCommitDisabledMode.getKafkaConfig().kafkaSecurityProtocol();
        kafkaSaslMechanism = cbsConfigurationForAutoCommitDisabledMode.getKafkaConfig().kafkaSaslMechanism();
        kafkaJaasConfig = cbsConfigurationForAutoCommitDisabledMode.getKafkaConfig().kafkaJaasConfig();

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBoostrapServerConfig);

        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupIdConfig);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        if (kafkaSecurityProtocol == null)
            kafkaSecurityProtocol = DEFAULT_KAFKA_SECURITY_PROTOCOL;
        config.put("security.protocol", kafkaSecurityProtocol);
        if (kafkaSaslMechanism == null)
            kafkaSaslMechanism = DEFAULT_KAFKA_SASL_MECHANISM;
        config.put("sasl.mechanism", kafkaSaslMechanism);

        config.put("sasl.jaas.config", kafkaJaasConfig);

        return new DefaultKafkaConsumerFactory<>(config);

    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            CbsConfigurationForAutoCommitDisabledMode cbsConfigurationForAutoCommitDisabledMode) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(cbsConfigurationForAutoCommitDisabledMode));
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

}
