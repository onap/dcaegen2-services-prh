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

package org.onap.dcaegen2.services.prh.service.consumer;

import static org.onap.dcaegen2.services.prh.model.logging.MDCVariables.REQUEST_ID;
import static org.onap.dcaegen2.services.prh.model.logging.MDCVariables.X_INVOCATION_ID;
import static org.onap.dcaegen2.services.prh.model.logging.MDCVariables.X_ONAP_REQUEST_ID;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import org.apache.http.client.utils.URIBuilder;
import org.onap.dcaegen2.services.prh.config.DmaapConsumerConfiguration;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 6/26/18
 */
public class DMaaPConsumerReactiveHttpClient {

    private final String dmaapHostName;
    private final String dmaapProtocol;
    private final Integer dmaapPortNumber;
    private final String dmaapTopicName;
    private final String consumerGroup;
    private final String consumerId;
    private final String contentType;
    private WebClient webClient;

    /**
     * Constructor of DMaaPConsumerReactiveHttpClient.
     *
     * @param consumerConfiguration - DMaaP consumer configuration object
     */
    public DMaaPConsumerReactiveHttpClient(DmaapConsumerConfiguration consumerConfiguration) {
        this.dmaapHostName = consumerConfiguration.dmaapHostName();
        this.dmaapProtocol = consumerConfiguration.dmaapProtocol();
        this.dmaapPortNumber = consumerConfiguration.dmaapPortNumber();
        this.dmaapTopicName = consumerConfiguration.dmaapTopicName();
        this.consumerGroup = consumerConfiguration.consumerGroup();
        this.consumerId = consumerConfiguration.consumerId();
        this.contentType = consumerConfiguration.dmaapContentType();
    }

    /**
     * Function for calling DMaaP HTTP consumer - consuming messages from Kafka/DMaaP from topic.
     *
     * @return reactive response from DMaaP in string format
     */
    public Mono<String> getDMaaPConsumerResponse() {
        try {
            return webClient
                .get()
                .uri(getUri())
                .header(X_ONAP_REQUEST_ID, MDC.get(REQUEST_ID))
                .header(X_INVOCATION_ID, UUID.randomUUID().toString())
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse ->
                    Mono.error(new RuntimeException("DmaaPConsumer HTTP " + clientResponse.statusCode()))
                )
                .onStatus(HttpStatus::is5xxServerError, clientResponse ->
                    Mono.error(new RuntimeException("DmaaPConsumer HTTP " + clientResponse.statusCode())))
                .bodyToMono(String.class);
        } catch (URISyntaxException e) {
            return Mono.error(e);
        }
    }

    private String createRequestPath() {
        return dmaapTopicName + "/" + consumerGroup + "/" + consumerId;
    }

    public DMaaPConsumerReactiveHttpClient createDMaaPWebClient(WebClient webClient) {
        this.webClient = webClient;
        return this;
    }

    URI getUri() throws URISyntaxException {
        return new URIBuilder().setScheme(dmaapProtocol).setHost(dmaapHostName).setPort(dmaapPortNumber)
            .setPath(createRequestPath()).build();
    }
}
