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
package org.onap.dcaegen2.services.prh.configuration;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Kafka producer configuration for the autoCommitDisabled profile.
 * Provides a KafkaTemplate that publishes PNF events directly to Kafka,
 * replacing the DMaaP Message Router HTTP-based publishing.
 */
@Profile("autoCommitDisabled")
@Configuration
public class KafkaProducerConfig {

    private final CbsConfigurationForAutoCommitDisabledMode cbsConfig;

    public KafkaProducerConfig(CbsConfigurationForAutoCommitDisabledMode cbsConfig) {
        this.cbsConfig = cbsConfig;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                cbsConfig.getKafkaConfig().kafkaBoostrapServerConfig());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        String kafkaSecurityProtocol = cbsConfig.getKafkaConfig().kafkaSecurityProtocol();
        String kafkaSaslMechanism = cbsConfig.getKafkaConfig().kafkaSaslMechanism();
        String kafkaJaasConfig = cbsConfig.getKafkaConfig().kafkaJaasConfig();

        if (kafkaSecurityProtocol != null && !kafkaSecurityProtocol.isEmpty()) {
            config.put("security.protocol", kafkaSecurityProtocol);
        }
        if (kafkaSaslMechanism != null && !kafkaSaslMechanism.isEmpty()) {
            config.put("sasl.mechanism", kafkaSaslMechanism);
        }
        if (kafkaJaasConfig != null && !kafkaJaasConfig.isEmpty()) {
            config.put("sasl.jaas.config", kafkaJaasConfig);
        }

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
