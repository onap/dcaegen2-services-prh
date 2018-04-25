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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapterFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ServiceLoader;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.onap.dcaegen2.services.config.AAIHttpClientConfiguration;
import org.onap.dcaegen2.services.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.config.DmaapPublisherConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
    private static final String AAI_CONFIG = "aaiHttpClientConfiguration";
    private static final String DMAAP_PRODUCER = "dmaapProducerConfiguration";
    private static final String DMAAP_CONSUMER = "dmaapConsumerConfiguration";

    private static final Logger logger = LoggerFactory.getLogger(PrhAppConfig.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    AAIHttpClientConfiguration aaiHttpClientConfiguration;

    DmaapConsumerConfiguration dmaapConsumerConfiguration;

    DmaapPublisherConfiguration dmaapPublisherConfiguration;

    @NotEmpty
    private String filepath;


    @Override
    public DmaapConsumerConfiguration getDmaapConsumerConfiguration() {
        return dmaapConsumerConfiguration;
    }

    @Override
    public AAIHttpClientConfiguration getAAIHttpClientConfiguration() {
        return aaiHttpClientConfiguration;
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
        try (InputStream inputStream = getInputStream(filepath)) {
            JsonElement rootElement = parser.parse(new InputStreamReader(inputStream));
            if (rootElement.isJsonObject()) {
                jsonObject = rootElement.getAsJsonObject();
                aaiHttpClientConfiguration = deserializeType(gsonBuilder,
                    jsonObject.getAsJsonObject(CONFIG).getAsJsonObject(AAI).getAsJsonObject(AAI_CONFIG),
                    AAIHttpClientConfiguration.class);

                dmaapConsumerConfiguration = deserializeType(gsonBuilder,
                    jsonObject.getAsJsonObject(CONFIG).getAsJsonObject(DMAAP).getAsJsonObject(DMAAP_CONSUMER),
                    DmaapConsumerConfiguration.class);

                dmaapPublisherConfiguration = deserializeType(gsonBuilder,
                    jsonObject.getAsJsonObject(CONFIG).getAsJsonObject(DMAAP).getAsJsonObject(DMAAP_PRODUCER),
                    DmaapPublisherConfiguration.class);
            }
        } catch (FileNotFoundException e) {
            logger
                .error(
                    "Configuration PrhAppConfig initFileStreamReader()::FileNotFoundException :: Execution Time - {}:{}",
                    dateTimeFormatter.format(
                        LocalDateTime.now()), e);
        } catch (IOException e) {
            logger
                .error(
                    "Configuration PrhAppConfig initFileStreamReader()::IOException :: Execution Time - {}:{}",
                    dateTimeFormatter.format(
                        LocalDateTime.now()), e);
        } catch (JsonSyntaxException e) {
            logger
                .error(
                    "Configuration PrhAppConfig initFileStreamReader()::JsonSyntaxException :: Execution Time - {}:{}",
                    dateTimeFormatter.format(
                        LocalDateTime.now()), e);
        }
    }

    private <T> T deserializeType(@NotNull GsonBuilder gsonBuilder, @NotNull JsonObject jsonObject,
        @NotNull Class<T> type) {
        return gsonBuilder.create().fromJson(jsonObject, type);
    }

    InputStream getInputStream(@NotNull String filepath) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(filepath));
    }

    String getFilepath() {
        return this.filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

}