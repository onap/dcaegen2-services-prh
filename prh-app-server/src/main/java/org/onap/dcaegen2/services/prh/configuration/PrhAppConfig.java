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

import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.INVOCATION_ID;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.REQUEST_ID;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapterFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ServiceLoader;
import javax.validation.constraints.NotNull;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/9/18
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("app")
public abstract class PrhAppConfig implements Config {

    private static final String CONFIG = "configs";
    private static final String AAI = "aai";
    private static final String DMAAP = "dmaap";
    private static final String AAI_CONFIG = "aaiClientConfiguration";
    private static final String DMAAP_PRODUCER = "dmaapProducerConfiguration";
    private static final String DMAAP_CONSUMER = "dmaapConsumerConfiguration";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    AaiClientConfiguration aaiClientConfiguration;

    DmaapConsumerConfiguration dmaapConsumerConfiguration;

    DmaapPublisherConfiguration dmaapPublisherConfiguration;

    @Value("classpath:prh_endpoints.json")
    private Resource resourceFile;

    @Override
    public DmaapConsumerConfiguration getDmaapConsumerConfiguration() {
        return dmaapConsumerConfiguration;
    }

    @Override
    public AaiClientConfiguration getAaiClientConfiguration() {
        return aaiClientConfiguration;
    }

    @Override
    public DmaapPublisherConfiguration getDmaapPublisherConfiguration() {
        return dmaapPublisherConfiguration;
    }

    @Override
    public void initFileStreamReader() {

        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject;

        try (InputStream inputStream = resourceFile.getInputStream()) {
            JsonElement rootElement = getJsonElement(parser, inputStream);
            if (rootElement.isJsonObject()) {
                jsonObject = rootElement.getAsJsonObject();
                aaiClientConfiguration = deserializeType(gsonBuilder,
                    jsonObject.getAsJsonObject(CONFIG).getAsJsonObject(AAI).getAsJsonObject(AAI_CONFIG),
                    AaiClientConfiguration.class);

                dmaapConsumerConfiguration = deserializeType(gsonBuilder,
                    jsonObject.getAsJsonObject(CONFIG).getAsJsonObject(DMAAP).getAsJsonObject(DMAAP_CONSUMER),
                    DmaapConsumerConfiguration.class);

                dmaapPublisherConfiguration = deserializeType(gsonBuilder,
                    jsonObject.getAsJsonObject(CONFIG).getAsJsonObject(DMAAP).getAsJsonObject(DMAAP_PRODUCER),
                    DmaapPublisherConfiguration.class);
            }
        } catch (IOException e) {
            logger.warn("Problem with file loading, file ", e);
        } catch (JsonSyntaxException e) {
            logger.warn("Problem with Json deserialization", e);
        }
    }

    JsonElement getJsonElement(JsonParser parser, InputStream inputStream) {
        return parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private <T> T deserializeType(@NotNull GsonBuilder gsonBuilder, @NotNull JsonObject jsonObject,
        @NotNull Class<T> type) {
        return gsonBuilder.create().fromJson(jsonObject, type);
    }

    void setResourceFile(Resource resourceFile) {
        this.resourceFile = resourceFile;
    }

    InputStream getInputStream(@NotNull String filepath) throws IOException {
        return new BufferedInputStream(new FileInputStream(filepath));
    }

    public void setXOnapRequestId(String xonaprequestid) {
        MDC.put(REQUEST_ID, xonaprequestid);
    }

    public void setXInvocationId(String xinvocationid) {
        MDC.put(INVOCATION_ID, xinvocationid);
    }
}