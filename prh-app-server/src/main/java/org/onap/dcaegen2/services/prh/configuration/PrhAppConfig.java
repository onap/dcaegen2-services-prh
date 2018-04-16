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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import org.onap.dcaegen2.services.config.AAIHttpClientConfiguration;
import org.onap.dcaegen2.services.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.config.ImmutableAAIHttpClientConfiguration;
import org.onap.dcaegen2.services.config.ImmutableDmaapConsumerConfiguration;
import org.onap.dcaegen2.services.config.ImmutableDmaapPublisherConfiguration;
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
public class PrhAppConfig implements AppConfig {

    private static final String CONFIG = "configs";
    private static final String AAI = "aai";
    private static final String DMAAP = "dmaap";
    private static final String AAI_CONFIG = "aaiHttpClientConfiguration";
    private static final String DMAAP_PRODUCER = "dmaapProducerConfiguration";
    private static final String DMAAP_CONSUMER = "dmaapConsumerConfiguration";

    private static final Logger logger = LoggerFactory.getLogger(PrhAppConfig.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private AAIHttpClientConfiguration aaiHttpClientConfiguration;

    private DmaapConsumerConfiguration dmaapConsumerConfiguration;

    private DmaapPublisherConfiguration dmaapPublisherConfiguration;

    @NotEmpty
    private String filepath;

    public void initFileStreamReader() {

        ObjectMapper jsonObjectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        JsonNode jsonNode;
        try (InputStream inputStream = getInputStream(filepath)) {
            ObjectNode root = (ObjectNode) jsonObjectMapper.readTree(inputStream);
            jsonNode = Optional.ofNullable(root.get(CONFIG).get(AAI).get(AAI_CONFIG)).orElse(NullNode.getInstance());
            aaiHttpClientConfiguration = jsonObjectMapper
                .treeToValue(jsonNode, ImmutableAAIHttpClientConfiguration.class);
            jsonNode = Optional.ofNullable(root.get(CONFIG).get(DMAAP).get(DMAAP_CONSUMER))
                .orElse(NullNode.getInstance());
            dmaapConsumerConfiguration = jsonObjectMapper
                .treeToValue(jsonNode, ImmutableDmaapConsumerConfiguration.class);
            jsonNode = Optional.ofNullable(root.get(CONFIG).get(DMAAP).get(DMAAP_PRODUCER))
                .orElse(NullNode.getInstance());
            dmaapPublisherConfiguration = jsonObjectMapper
                .treeToValue(jsonNode, ImmutableDmaapPublisherConfiguration.class);
        } catch (FileNotFoundException e) {
            logger
                .error(
                    "Configuration PrhAppConfig initFileStreamReader()::FileNotFoundException :: Execution Time - {}:{}",
                    dateTimeFormatter.format(
                        LocalDateTime.now()), e);
        } catch (JsonParseException e) {
            logger
                .error(
                    "Configuration PrhAppConfig initFileStreamReader()::JsonParseException :: Execution Time - {}:{}",
                    dateTimeFormatter.format(
                        LocalDateTime.now()), e);
        } catch (JsonMappingException e) {
            logger
                .error(
                    "Configuration PrhAppConfig initFileStreamReader()::JsonMappingException :: Execution Time - {}:{}",
                    dateTimeFormatter.format(
                        LocalDateTime.now()), e);
        } catch (IOException e) {
            logger
                .error(
                    "Configuration PrhAppConfig initFileStreamReader()::IOException :: Execution Time - {}:{}",
                    dateTimeFormatter.format(
                        LocalDateTime.now()), e);
        }
    }

    InputStream getInputStream(String filepath) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(filepath));
    }

    String getFilepath() {
        return this.filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

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
}