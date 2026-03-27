/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * Copyright (C) 2023-2026 Deutsche Telekom Intellectual Property. All rights reserved.
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

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamPredicates.streamWithName;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.adapter.aai.main.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.DataStreams;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParsers;

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

    String getPublishTopicUrl() {
        return getSinkTopicUrl(PNF_READY);
    }

    String getUpdatePublishTopicUrl() {
        return getSinkTopicUrl(PNF_UPDATE);
    }

    private String getSinkTopicUrl(String streamName) {
        RawDataStream<JsonObject> sink = DataStreams.namedSinks(jsonObject).find(streamWithName(streamName)).get();
        MessageRouterSink parsedSink = StreamFromGsonParsers.messageRouterSinkParser().unsafeParse(sink);
        return parsedSink.topicUrl();
    }

    AaiClientConfiguration getAaiClientConfig() {
        return new ImmutableAaiClientConfiguration.Builder()
            .pnfUrl(jsonObject.get("aai.aaiClientConfiguration.pnfUrl").getAsString())
            .baseUrl(jsonObject.get("aai.aaiClientConfiguration.baseUrl").getAsString())
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

    String getSubscribeTopicUrl() {
        RawDataStream<JsonObject> source = DataStreams.namedSources(jsonObject).find(streamWithName(VES_REG_OUTPUT)).get();
        MessageRouterSource parsedSource = StreamFromGsonParsers.messageRouterSourceParser().unsafeParse(source);
        return parsedSource.topicUrl();
    }

    String getSubscribeConsumerGroup() {
        return jsonObject.get("dmaap.dmaapConsumerConfiguration.consumerGroup").getAsString();
    }
}

