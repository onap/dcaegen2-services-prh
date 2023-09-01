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
package org.onap.dcaegen2.services.prh.adapter.kafka;

import java.io.Serializable;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 *  * @author <a href="mailto:PRANIT.KAPDULE@t-systems.com">Pranit Kapdule</a> on
 *   *        24/08/23
 *    */

@Value.Immutable(prehash = true)
@Value.Style(builder = "new")
@Gson.TypeAdapters
@Configuration
@EnableKafka
public abstract class KafkaConfiguration implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Value.Parameter
    public abstract String kafkaBoostrapServerConfig();

    @Value.Parameter
    public abstract String groupIdConfig();

    @Value.Parameter
    public abstract String kafkaSecurityProtocol();

    @Value.Parameter
    public abstract String kafkaSaslMechanism();

    @Value.Parameter
    public abstract String kafkaJaasConfig();

}
