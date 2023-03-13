/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property.All rights reserved.
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

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Profile("autoCommitDisabled")
@EnableKafka
@Configuration
public class KafkaConfig
{
    String kafkaBoostrapServerConfig = System.getenv("kafkaBoostrapServerConfig");

    String groupIdConfig = System.getenv("groupIdConfig");


    String kafkaSecurityProtocol = System.getenv("kafkaSecurityProtocol");

    String kafkaSaslMechanism = System.getenv("kafkaSaslMechanism");

    String kafkaUsername = System.getenv("kafkaUsername");

    String kafkaPassword = System.getenv("kafkaPassword");

    String kafkaJaasConfig = System.getenv("JAAS_CONFIG");

    String kafkaLoginModuleClassConfig = System.getenv("Login_Module_Class");

    @Bean
    public ConsumerFactory<String, String> consumerFactory()
    {
        Map<String,Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaBoostrapServerConfig);
        config.put(ConsumerConfig.GROUP_ID_CONFIG,groupIdConfig);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        if(kafkaJaasConfig == null) {
            kafkaJaasConfig = kafkaLoginModuleClassConfig + " required username=\""
                + kafkaUsername + "\" password=\"" + kafkaPassword + "\";";
        }
        if(kafkaSecurityProtocol==null ) kafkaSecurityProtocol="SASL_PLAINTEXT";
        config.put("security.protocol", kafkaSecurityProtocol);
        if(kafkaSaslMechanism==null ) kafkaSaslMechanism="SCRAM-SHA-512";
        config.put("sasl.mechanism", kafkaSaslMechanism);

        config.put("sasl.jaas.config", kafkaJaasConfig);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
