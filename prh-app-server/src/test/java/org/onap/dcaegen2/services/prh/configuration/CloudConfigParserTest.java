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

package org.onap.dcaegen2.services.prh.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.*;

import static org.assertj.core.api.Assertions.assertThat;

public class CloudConfigParserTest {

    private static final String correctJson =
        "{\"aai.aaiClientConfiguration.aaiIgnoreSslCertificateErrors\": true, "
            + "\"dmaap.dmaapProducerConfiguration.dmaapTopicName\": \"/events/unauthenticated.PNF_READY\", "
            + "\"dmaap.dmaapConsumerCon"
            + "figuration.timeoutMs\": -1, \"dmaap.dmaapConsumerConfiguration.dmaapHostName\": "
            + "\"message-router.onap.svc.cluster.local\", \"aai.aaiClientConfiguration.aaiPnfPath\": "
            + "\"/network/pnfs/pnf\", \"aai.aaiClientConfiguration.aai"
            + "UserPassword\": \"AAI\", \"dmaap.dmaapConsumerConfiguration.dmaapUserName\": \"admin\", "
            + "\"aai.aaiClientConfiguration.aaiBasePath\": \"/aai/v12\", "
            + "\"dmaap.dmaapProducerConfiguration.dmaapPortNumber\": 3904, \"aai.aaiClientConf"
            + "iguration.aaiHost\": \"aai.onap.svc.cluster.local\", "
            + "\"dmaap.dmaapConsumerConfiguration.dmaapUserPassword\": \"admin\", "
            + "\"dmaap.dmaapProducerConfiguration.dmaapProtocol\": \"http\", \"dmaap.dmaapProducerConfiguration.dmaapC"
            + "ontentType\": \"application/json\", "
            + "\"dmaap.dmaapConsumerConfiguration.dmaapTopicName\": \"/events/unauthenticated.SEC_OTHER_OUTPUT\", "
            + "\"dmaap.dmaapConsumerConfiguration.dmaapPortNumber\": 3904, \"dmaap.dmaapConsumerConfi"
            + "guration.dmaapContentType\": \"application/json\", \"dmaap.dmaapConsumerConfiguration.messageLimit\": "
            + "-1, \"dmaap.dmaapConsumerConfiguration.dmaapProtocol\": \"http\", "
            + "\"aai.aaiClientConfiguration.aaiUserName\": \"AAI\", \"dm"
            + "aap.dmaapConsumerConfiguration.consumerId\": \"c12\", \"dmaap.dmaapProducerConfiguration.dmaapHostName\""
            + ": \"message-router.onap.svc.cluster.local\", \"aai.aaiClientConfiguration.aaiHostPortNumber\": "
            + "8443, \"dmaap.dmaapConsumerConfiguration.consumerGroup\": \"OpenDCAE-c12\", "
            + "\"aai.aaiClientConfiguration.aaiProtocol\": \"https\", "
            + "\"dmaap.dmaapProducerConfiguration.dmaapUserName\": \"admin\", "
            + "\"dmaap.dmaapProducerConfiguration.dmaapUserPasswor"
            + "d\": \"admin\"}";

    private static final ImmutableAaiClientConfiguration correctAaiClientConfig =
        new ImmutableAaiClientConfiguration.Builder()
        .aaiHost("aai.onap.svc.cluster.local")
        .aaiPort(8443)
        .aaiUserName("AAI")
        .aaiPnfPath("/network/pnfs/pnf")
        .aaiIgnoreSslCertificateErrors(true)
        .aaiUserPassword("AAI")
        .aaiProtocol("https")
        .aaiBasePath("/aai/v12")
        .build();

    private static final ImmutableDmaapConsumerConfiguration correctDmaapConsumerConfig =
        new ImmutableDmaapConsumerConfiguration.Builder()
        .timeoutMs(-1)
        .dmaapHostName("message-router.onap.svc.cluster.local")
        .dmaapUserName("admin")
        .dmaapUserPassword("admin")
        .dmaapTopicName("/events/unauthenticated.SEC_OTHER_OUTPUT")
        .dmaapPortNumber(3904)
        .dmaapContentType("application/json")
        .messageLimit(-1)
        .dmaapProtocol("http")
        .consumerId("c12")
        .consumerGroup("OpenDCAE-c12")
        .build();

    private static final ImmutableDmaapPublisherConfiguration correctDmaapPublisherConfig =
        new ImmutableDmaapPublisherConfiguration.Builder()
        .dmaapTopicName("/events/unauthenticated.PNF_READY")
        .dmaapUserPassword("admin")
        .dmaapPortNumber(3904)
        .dmaapProtocol("http")
        .dmaapContentType("application/json")
        .dmaapHostName("message-router.onap.svc.cluster.local")
        .dmaapUserName("admin")
        .build();

    private CloudConfigParser cloudConfigParser = new CloudConfigParser(new Gson().fromJson(correctJson, JsonObject.class));


    @Test
    public void shouldCreateAaiConfigurationCorrectly(){
        // when
        AaiClientConfiguration aaiClientConfig = cloudConfigParser.getAaiClientConfig();

        // then
        assertThat(aaiClientConfig).isNotNull();
        assertThat(aaiClientConfig).isEqualToComparingFieldByField(correctAaiClientConfig);
    }


    @Test
    public void shouldCreateDmaapConsumerConfigurationCorrectly(){
        // when
        DmaapConsumerConfiguration dmaapConsumerConfig = cloudConfigParser.getDmaapConsumerConfig();

        // then
        assertThat(dmaapConsumerConfig).isNotNull();
        assertThat(dmaapConsumerConfig).isEqualToComparingFieldByField(correctDmaapConsumerConfig);
    }


    @Test
    public void shouldCreateDmaapPublisherConfigurationCorrectly(){
        // when
        DmaapPublisherConfiguration dmaapPublisherConfig = cloudConfigParser.getDmaapPublisherConfig();

        // then
        assertThat(dmaapPublisherConfig).isNotNull();
        assertThat(dmaapPublisherConfig).isEqualToComparingFieldByField(correctDmaapPublisherConfig);
    }
}