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
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableMessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableMessageRouterSubscriberConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;

import java.nio.file.Paths;
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
    }

    MessageRouterPublishRequest getMessageRouterUpdatePublishRequest() {
        RawDataStream<JsonObject> sink = DataStreams.namedSinks(jsonObject).find(streamWithName(PNF_UPDATE)).get();
        MessageRouterSink parsedSink = StreamFromGsonParsers.messageRouterSinkParser().unsafeParse(sink);

        return ImmutableMessageRouterPublishRequest.builder()
                .contentType(jsonObject.get("dmaap.dmaapProducerConfiguration.dmaapContentType").getAsString())
                .sinkDefinition(parsedSink)
                .build();
    }

    MessageRouterPublisherConfig getMessageRouterPublisherConfig() {
        return ImmutableMessageRouterPublisherConfig.builder()
                .securityKeys(isDmaapCertAuthEnabled(jsonObject) ? createSecurityKeys(jsonObject) : null)
                .build();
    }

    MessageRouterSubscriberConfig getMessageRouterSubscriberConfig() {
        return ImmutableMessageRouterSubscriberConfig.builder()
                .securityKeys(isDmaapCertAuthEnabled(jsonObject) ? createSecurityKeys(jsonObject) : null)
                .build();
    }

    private SecurityKeys createSecurityKeys(JsonObject config) {
        return ImmutableSecurityKeys.builder()
                .keyStore(ImmutableSecurityKeysStore.of(Paths.get(config.get(SECURITY_KEY_STORE_PATH).getAsString())))
                .keyStorePassword(Passwords.fromPath(Paths.get(config.get(SECURITY_KEY_STORE_PASS_PATH).getAsString())))
                .trustStore(ImmutableSecurityKeysStore.of(Paths.get(config.get(SECURITY_TRUST_STORE_PATH).getAsString())))
                .trustStorePassword(Passwords.fromPath(Paths.get(config.get(SECURITY_TRUST_STORE_PASS_PATH).getAsString())))
                .build();
    }

    private boolean isDmaapCertAuthEnabled(JsonObject config) {
        return config.get("security.enableDmaapCertAuth").getAsBoolean();
    }

    AaiClientConfiguration getAaiClientConfig() {
        return new ImmutableAaiClientConfiguration.Builder()
            .pnfUrl(jsonObject.get("aai.aaiClientConfiguration.pnfUrl").getAsString())
            .aaiUserName(jsonObject.get("aai.aaiClientConfiguration.aaiUserName").getAsString())
            .aaiServiceInstancePath(jsonObject.get("aai.aaiClientConfiguration.aaiServiceInstancePath").getAsString())
            .aaiIgnoreSslCertificateErrors(
                    jsonObject.get("aai.aaiClientConfiguration.aaiIgnoreSslCertificateErrors").getAsBoolean())
            .aaiUserPassword(jsonObject.get("aai.aaiClientConfiguration.aaiUserPassword").getAsString())
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
                .sourceDefinition(parsedSource)
                .consumerId(jsonObject.get("dmaap.dmaapConsumerConfiguration.consumerId").getAsString())
                .timeout(Duration.ofMillis(jsonObject.get("dmaap.dmaapConsumerConfiguration.timeoutMs").getAsLong()))
                .build();
    }
}