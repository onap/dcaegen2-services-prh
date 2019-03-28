/*
 * ============LICENSE_START=======================================================
 * PROJECT
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

package org.onap.dcaegen2.services.prh.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.PnfReadyJsonBodyBuilderImpl;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DMaaPPublisherReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.DmaaPRestTemplateFactory;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer.PublisherReactiveHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
public class DmaapPublisherTaskImpl implements DmaapPublisherTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapPublisherTaskImpl.class);
    private final Supplier<DmaapPublisherConfiguration> config;
    private final PnfReadyJsonBodyBuilderImpl pnfReadyJsonBodyBuilder = new PnfReadyJsonBodyBuilderImpl();
    private final PublisherReactiveHttpClientFactory httpClientFactory;

    public DmaapPublisherTaskImpl(final Supplier<DmaapPublisherConfiguration> config) {
        this(config, new PublisherReactiveHttpClientFactory(
                new DmaaPRestTemplateFactory(),
                new PnfReadyJsonBodyBuilderImpl()));
    }

    DmaapPublisherTaskImpl(
            Supplier<DmaapPublisherConfiguration> config,
            PublisherReactiveHttpClientFactory httpClientFactory) {
        this.config = config;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public Mono<org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse>
        execute(ConsumerDmaapModel consumerDmaapModel) throws DmaapNotFoundException, SSLException {
        if (consumerDmaapModel == null) {
            throw new DmaapNotFoundException("Invoked null object to DMaaP task");
        }
        DMaaPPublisherReactiveHttpClient dmaapPublisherReactiveHttpClient = resolveClient();
        LOGGER.info("Method called with arg {}", consumerDmaapModel);
        return dmaapPublisherReactiveHttpClient.getDMaaPProducerResponse(consumerDmaapModel, Optional.empty());
    }

    @Override
    public DMaaPPublisherReactiveHttpClient resolveClient() throws SSLException {
        return httpClientFactory.create(config.get());

    }

    /**
     *
     * Does not work reactive version with DMaaP MR  - to be investigated why in future
     * As WA plesae use Mono<HttpResponse> executeWithApache(ConsumerDmaapModel consumerDmaapModel);
     * */
    @Override
    public Mono<org.apache.http.HttpResponse> executeWithApache(ConsumerDmaapModel consumerDmaapModel) {
        String json = pnfReadyJsonBodyBuilder.createJsonBody(consumerDmaapModel);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(getUrl());
        try {
            StringEntity input = new StringEntity(json);
            input.setContentType(config.get().dmaapContentType());
            postRequest.setEntity(input);
            HttpResponse response = httpClient.execute(postRequest);
            return Mono.just(response);
        } catch (Exception e) {
            LOGGER.warn("Publishing to DMaaP MR failed: {}", e);
            return Mono.error(e);
        }
    }

    private String getUrl() {
        return (new URI.URIBuilder()).scheme(config.get().dmaapProtocol())
            .host(config.get().dmaapHostName())
            .port(config.get().dmaapPortNumber()).path(this.createRequestPath()).build()
            .toString();
    }

    private String createRequestPath() {
        return "/" + config.get().dmaapTopicName();
    }
}