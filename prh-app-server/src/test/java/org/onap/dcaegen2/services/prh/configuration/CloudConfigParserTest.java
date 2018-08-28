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
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class CloudConfigParserTest {

    private static final String correctJson =
        "{\"aai.aaiClientConfiguration.aaiIgnoreSslCertificateErrors\": true, "
            + "\"dmaap.dmaapProducerConfiguration.dmaapTopicName\": \"/events/unauthenticated.PNF_READY\", "
            + "\"dmaap.dmaapConsumerCon"
            + "figuration.timeoutMS\": -1, \"dmaap.dmaapConsumerConfiguration.dmaapHostName\": "
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

    private CloudConfigParser cloudConfigParser = new CloudConfigParser(new Gson().fromJson(correctJson, JsonObject.class));

    @Test
    public void shouldCreateAaiConfigurationCorrectly(){
        // when
        AaiClientConfiguration aaiClientConfig = cloudConfigParser.getAaiClientConfig();

        // then
        assertThat(aaiClientConfig).isNotNull();
    }


    @Test
    public void shouldCreateDmaapConsumerConfigurationCorrectly(){
        // when
        DmaapConsumerConfiguration dmaapConsumerConfig = cloudConfigParser.getDmaapConsumerConfig();

        // then
        assertThat(dmaapConsumerConfig).isNotNull();
    }


    @Test
    public void shouldCreateDmaapPublisherConfigurationCorrectly(){
        // when
        DmaapPublisherConfiguration dmaapPublisherConfig = cloudConfigParser.getDmaapPublisherConfig();

        // then
        assertThat(dmaapPublisherConfig).isNotNull();
    }
}