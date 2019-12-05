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

import static java.lang.ClassLoader.getSystemResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.TestAppConfiguration;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.adapter.aai.main.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.ContentType;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;


class ConsulConfigurationParserTest {

    private final String correctJson =
        new String(Files.readAllBytes(Paths.get(getSystemResource("configurationFromCbs.json").toURI())));
    private final ImmutableAaiClientConfiguration correctAaiClientConfig =
        TestAppConfiguration.createDefaultAaiClientConfiguration();

    private final JsonObject correctConfig = new Gson().fromJson(correctJson, JsonObject.class);
    private final CbsContentParser consulConfigurationParser = new CbsContentParser(correctConfig);

    ConsulConfigurationParserTest() throws Exception {
    }

    @Test
    void shouldCreateAaiConfigurationCorrectly() {
        // when
        AaiClientConfiguration aaiClientConfig = consulConfigurationParser.getAaiClientConfig();

        // then
        assertThat(aaiClientConfig).isNotNull();
        assertThat(aaiClientConfig).isEqualToComparingFieldByField(correctAaiClientConfig);
    }

    @Test
    void shouldCreateMessageRouterSubscribeRequestCorrectly() {
        // given
        MessageRouterSubscribeRequest messageRouterSubscribeRequest = consulConfigurationParser
            .getMessageRouterSubscribeRequest();

        // then
        assertThat(messageRouterSubscribeRequest.sourceDefinition().topicUrl())
            .isEqualTo("http://dmaap-mr:2222/events/unauthenticated.VES_PNFREG_OUTPUT");
        assertThat(messageRouterSubscribeRequest.consumerGroup()).isEqualTo("OpenDCAE-c12");
        assertThat(messageRouterSubscribeRequest.consumerId()).isEqualTo("c12");
        assertThat(messageRouterSubscribeRequest.timeout()).isEqualTo(Duration.ofMillis(-1));
    }

    @Test
    void shouldCreateMessageRouterPublishConfigurationCorrectly() {
        // when
        MessageRouterPublishRequest messageRouterPublishRequest = consulConfigurationParser
            .getMessageRouterPublishRequest();

        // then
        assertThat(messageRouterPublishRequest.contentType()).isEqualTo(ContentType.APPLICATION_JSON);
        assertThat(messageRouterPublishRequest.sinkDefinition().topicUrl())
            .isEqualTo("http://dmaap-mr:2222/events/unauthenticated.PNF_READY");
    }

    @Test
    void shouldCreateMessageRouterUpdatePublishConfigurationCorrectly() {
        // when
        MessageRouterPublishRequest messageRouterPublishRequest = consulConfigurationParser
            .getMessageRouterUpdatePublishRequest();

        // then
        assertThat(messageRouterPublishRequest.contentType()).isEqualTo(ContentType.APPLICATION_JSON);
        assertThat(messageRouterPublishRequest.sinkDefinition().topicUrl())
            .isEqualTo("http://dmaap-mr:2222/events/unauthenticated.PNF_UPDATE");
    }

    @Test
    void whenDmaapCertAuthIsDisabled_MessageRouterPublisherConfigSecurityKeysShouldBeIgnored() {
        assumeFalse(correctConfig.getAsJsonObject("config").get("security.enableDmaapCertAuth").getAsBoolean());

        MessageRouterPublisherConfig messageRouterPublisherConfig = consulConfigurationParser
            .getMessageRouterPublisherConfig();

        assertThat(messageRouterPublisherConfig.securityKeys()).isNull();
    }

    @Test
    void whenDmaapCertAuthIsDisabled_MessageRouterSubscriberConfigSecurityKeysShouldBeIgnored() {
        assumeFalse(correctConfig.getAsJsonObject("config").get("security.enableDmaapCertAuth").getAsBoolean());

        MessageRouterSubscriberConfig messageRouterSubscriberConfig = consulConfigurationParser
            .getMessageRouterSubscriberConfig();

        assertThat(messageRouterSubscriberConfig.securityKeys()).isNull();
    }


    @Test
    void whenDmaapCertAuthIsEnabled_MessageRouterPublisherConfigSecurityKeysShouldBeLoaded() {
        CbsContentParser consulConfigurationParser = new CbsContentParser(getConfigWithSslEnabled(correctJson));

        MessageRouterPublisherConfig messageRouterPublisherConfig = consulConfigurationParser
            .getMessageRouterPublisherConfig();

        verifySecurityKeys(messageRouterPublisherConfig.securityKeys());
    }


    @Test
    void whenDmaapCertAuthIsEnabled_MessageRouterSubscriberConfigSecurityKeysShouldBeLoaded() {
        CbsContentParser consulConfigurationParser = new CbsContentParser(getConfigWithSslEnabled(correctJson));

        MessageRouterSubscriberConfig messageRouterSubscriberConfig = consulConfigurationParser
            .getMessageRouterSubscriberConfig();

        verifySecurityKeys(messageRouterSubscriberConfig.securityKeys());
    }

    private static void verifySecurityKeys(@Nullable SecurityKeys securityKeys) {
        assertThat(securityKeys).isNotNull();
        assertThat(securityKeys.trustStore().path().endsWith("org.onap.dcae.trust.jks")).isTrue();
        assertThat(securityKeys.keyStore().path().endsWith("org.onap.dcae.jks")).isTrue();
        securityKeys.trustStorePassword()
            .use(chars -> assertThat(new String(chars)).isEqualTo("*TQH?Lnszprs4LmlAj38yds("));
        securityKeys.keyStorePassword()
            .use(chars -> assertThat(new String(chars)).isEqualTo("mYHC98!qX}7h?W}jRv}MIXTJ"));
    }

    private static JsonObject getConfigWithSslEnabled(String configJsonString) {
        JsonObject configJson = new Gson().fromJson(configJsonString, JsonObject.class);
        JsonObject config = configJson.getAsJsonObject("config");
        config.addProperty("security.enableDmaapCertAuth", true);
        config.addProperty("security.enableAaiCertAuth", true);
        config.addProperty("security.trustStorePath", testResourceToPath("/org.onap.dcae.trust.jks"));
        config.addProperty("security.trustStorePasswordPath", testResourceToPath("/truststore.password"));
        config.addProperty("security.keyStorePath", testResourceToPath("/org.onap.dcae.jks"));
        config.addProperty("security.keyStorePasswordPath", testResourceToPath("/keystore.password"));
        return configJson;
    }


    private static String testResourceToPath(String resource) {
        try {
            return Paths.get(ConsulConfigurationParserTest.class.getResource(resource).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed resolving test resource path", e);
        }
    }

}