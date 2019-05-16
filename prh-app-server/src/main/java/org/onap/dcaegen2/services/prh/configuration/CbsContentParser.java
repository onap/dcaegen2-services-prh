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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.DataStreams;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParsers;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;

import java.time.Duration;
import java.util.Map;

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamPredicates.streamWithName;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 8/21/18
 */
class CbsContentParser {
    private static final String SECURITY_TRUST_STORE_PATH = "security.trustStorePath";
    private static final String SECURITY_TRUST_STORE_PASS_PATH = "security.trustStorePasswordPath";
    private static final String SECURITY_KEY_STORE_PATH = "security.keyStorePath";
    private static final String SECURITY_KEY_STORE_PASS_PATH = "security.keyStorePasswordPath";
    private static final String SECURITY_ENABLE_DMAAP_CERT_AUTH = "security.enableDmaapCertAuth";
    private static final String CONFIG = "config";
    private static final String PNF_UPDATE = "pnf-update";
    private static final String PNF_READY = "pnf-ready";
    private static final String VES_REG_OUTPUT = "ves-reg-output";

    private final JsonObject jsonObject;

    CbsContentParser(JsonObject jsonObject) {
        this.jsonObject = jsonObject.getAsJsonObject(CONFIG);
    }

    MessageRouterPublishRequest getMessageRouterPublishRequest() {
        RawDataStream<JsonObject> sink = DataStreams.namedSinks(jsonObject).find(streamWithName(PNF_READY)).get();
        MessageRouterSink parsedSink = StreamFromGsonParsers.messageRouterSinkParser().unsafeParse(sink);

        return ImmutableMessageRouterPublishRequest.builder()
                .contentType(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapContentType").getAsString())
                .sinkDefinition(parsedSink)
                .build();
//        return new ImmutableDmaapPublisherConfiguration.Builder()
//            .endpointUrl(parsedSink.topicUrl())
//            .dmaapTopicName(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapTopicName").getAsString())
//            .dmaapUserPassword(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapUserPassword").getAsString())
//            .dmaapPortNumber(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapPortNumber").getAsInt())
//            .dmaapProtocol(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapProtocol").getAsString())
//            .dmaapContentType(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapContentType").getAsString())
//            .dmaapHostName(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapHostName").getAsString())
//            .dmaapUserName(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapUserName").getAsString())
//            .dmaapUserPassword(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapUserPassword").getAsString())
//            .trustStorePath(jsonObject.get(SECURITY_TRUST_STORE_PATH).getAsString())
//            .trustStorePasswordPath(jsonObject.get(SECURITY_TRUST_STORE_PASS_PATH).getAsString())
//            .keyStorePath(jsonObject.get(SECURITY_KEY_STORE_PATH).getAsString())
//            .keyStorePasswordPath(jsonObject.get(SECURITY_KEY_STORE_PASS_PATH).getAsString())
//            .enableDmaapCertAuth(jsonObject.get(SECURITY_ENABLE_DMAAP_CERT_AUTH).getAsBoolean())
//            .build();
    }

    MessageRouterPublishRequest getMessageRouterUpdatePublishRequest() {
        RawDataStream<JsonObject> sink = DataStreams.namedSinks(jsonObject).find(streamWithName(PNF_UPDATE)).get();
        MessageRouterSink parsedSink = StreamFromGsonParsers.messageRouterSinkParser().unsafeParse(sink);

        return ImmutableMessageRouterPublishRequest.builder()
                .contentType(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapContentType").getAsString())
                .sinkDefinition(parsedSink)
                .build();

//        return new ImmutableDmaapPublisherConfiguration.Builder()
//            .endpointUrl(parsedSink.topicUrl())
//            .dmaapTopicName(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapTopicName").getAsString())
//            .dmaapUserPassword(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapUserPassword").getAsString())
//            .dmaapPortNumber(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapPortNumber").getAsInt())
//            .dmaapProtocol(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapProtocol").getAsString())
//            .dmaapContentType(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapContentType").getAsString())
//            .dmaapHostName(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapHostName").getAsString())
//            .dmaapUserName(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapUserName").getAsString())
//            .dmaapUserPassword(jsonObject.get("dmaap.dmaapUpdateProducerConfiguration.dmaapUserPassword").getAsString())
//            .trustStorePath(jsonObject.get(SECURITY_TRUST_STORE_PATH).getAsString())
//            .trustStorePasswordPath(jsonObject.get(SECURITY_TRUST_STORE_PASS_PATH).getAsString())
//            .keyStorePath(jsonObject.get(SECURITY_KEY_STORE_PATH).getAsString())
//            .keyStorePasswordPath(jsonObject.get(SECURITY_KEY_STORE_PASS_PATH).getAsString())
//            .enableDmaapCertAuth(jsonObject.get(SECURITY_ENABLE_DMAAP_CERT_AUTH).getAsBoolean())
//            .build();
    }

    AaiClientConfiguration getAaiClientConfig() {
        return new ImmutableAaiClientConfiguration.Builder()
            .pnfUrl(jsonObject.get("aai.aaiClientConfiguration.pnfUrl").getAsString())
            .aaiHost(jsonObject.get("aai.aaiClientConfiguration.aaiHost").getAsString())
            .aaiPort(jsonObject.get("aai.aaiClientConfiguration.aaiHostPortNumber").getAsInt())
            .aaiUserName(jsonObject.get("aai.aaiClientConfiguration.aaiUserName").getAsString())
            .aaiPnfPath(jsonObject.get("aai.aaiClientConfiguration.aaiPnfPath").getAsString())
            .aaiServiceInstancePath(jsonObject.get("aai.aaiClientConfiguration.aaiServiceInstancePath").getAsString())
            .aaiIgnoreSslCertificateErrors(
                    jsonObject.get("aai.aaiClientConfiguration.aaiIgnoreSslCertificateErrors").getAsBoolean())
            .aaiUserPassword(jsonObject.get("aai.aaiClientConfiguration.aaiUserPassword").getAsString())
            .aaiProtocol(jsonObject.get("aai.aaiClientConfiguration.aaiProtocol").getAsString())
            .aaiBasePath(jsonObject.get("aai.aaiClientConfiguration.aaiBasePath").getAsString())
            .trustStorePath(jsonObject.get(SECURITY_TRUST_STORE_PATH).getAsString())
            .trustStorePasswordPath(jsonObject.get(SECURITY_TRUST_STORE_PASS_PATH).getAsString())
            .keyStorePath(jsonObject.get(SECURITY_KEY_STORE_PATH).getAsString())
            .keyStorePasswordPath(jsonObject.get(SECURITY_KEY_STORE_PASS_PATH).getAsString())
            .enableAaiCertAuth(jsonObject.get("security.enableAaiCertAuth").getAsBoolean())
            .aaiHeaders(new Gson().fromJson(jsonObject.get("aai.aaiClientConfiguration.aaiHeaders"),
                    new TypeToken<Map<String, String>>(){}.getType()))
            .build();
    }

    MessageRouterSubscribeRequest getMessageRouterSubscribeRequest() {
        RawDataStream<JsonObject> source = DataStreams.namedSources(jsonObject).find(streamWithName(VES_REG_OUTPUT)).get();
        MessageRouterSource parsedSource = StreamFromGsonParsers.messageRouterSourceParser().unsafeParse(source);

        return ImmutableMessageRouterSubscribeRequest.builder()
                .consumerGroup(jsonObject.get("dmaap.dmaapConsumerConfiguration.consumerGroup").getAsString())
                .consumerId(jsonObject.get("dmaap.dmaapConsumerConfiguration.consumerId").getAsString())
                .sourceDefinition(parsedSource)
                .timeout(Duration.ofMillis(jsonObject.get("dmaap.dmaapConsumerConfiguration.timeoutMs").getAsLong()))
                .build();

//        return new ImmutableDmaapConsumerConfiguration.Builder()
//            .endpointUrl(parsedSource.topicUrl())
//            .timeoutMs(jsonObject.get("dmaap.dmaapConsumerConfiguration.timeoutMs").getAsInt())
//            .dmaapHostName(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapHostName").getAsString())
//            .dmaapUserName(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapUserName").getAsString())
//            .dmaapUserPassword(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapUserPassword").getAsString())
//            .dmaapTopicName(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapTopicName").getAsString())
//            .dmaapPortNumber(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapPortNumber").getAsInt())
//            .dmaapContentType(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapContentType").getAsString())
//            .messageLimit(jsonObject.get("dmaap.dmaapConsumerConfiguration.messageLimit").getAsInt())
//            .dmaapProtocol(jsonObject.get("dmaap.dmaapConsumerConfiguration.dmaapProtocol").getAsString())
//            .consumerId(jsonObject.get("dmaap.dmaapConsumerConfiguration.consumerId").getAsString())
//            .consumerGroup(jsonObject.get("dmaap.dmaapConsumerConfiguration.consumerGroup").getAsString())
//            .trustStorePath(jsonObject.get(SECURITY_TRUST_STORE_PATH).getAsString())
//            .trustStorePasswordPath(jsonObject.get(SECURITY_TRUST_STORE_PASS_PATH).getAsString())
//            .keyStorePath(jsonObject.get(SECURITY_KEY_STORE_PATH).getAsString())
//            .keyStorePasswordPath(jsonObject.get(SECURITY_KEY_STORE_PASS_PATH).getAsString())
//            .enableDmaapCertAuth(jsonObject.get(SECURITY_ENABLE_DMAAP_CERT_AUTH).getAsBoolean())
//            .build();
    }
}