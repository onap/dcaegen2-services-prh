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

package org.onap.dcaegen2.services.prh.tasks;

import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.AaiNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.AaiJsonBodyBuilderImpl;
import org.onap.dcaegen2.services.prh.model.utils.HttpUtils;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.AaiHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.patch.AaiHttpPatchClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Component
public class AaiProducerTaskImpl extends AaiProducerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiProducerTaskImpl.class);

    private final Config config;
    private AaiHttpPatchClient aaiHttpPatchClient;

    @Autowired
    public AaiProducerTaskImpl(Config config) {
        this.config = config;
    }

    @Override
    Mono<ConsumerDmaapModel> publish(ConsumerDmaapModel consumerDmaapModel) {
        Mono<HttpClientResponse> resposne =  aaiHttpPatchClient.getAaiResponse(consumerDmaapModel);
        return resposne.flatMap(response -> {
            if (HttpUtils.isSuccessfulResponseCode(response.status().code())) {
                return Mono.just(consumerDmaapModel);
            }
            return Mono
                    .error(new AaiNotFoundException("Incorrect response code for continuation of tasks workflow" + response.status().code()));
        });
    }

    @Override
    AaiHttpPatchClient resolveClient() {
        return new AaiHttpPatchClient(resolveConfiguration(),
                new AaiJsonBodyBuilderImpl(), new CloudHttpClient());
    }

    @Override
    protected AaiClientConfiguration resolveConfiguration() {
        return config.getAaiClientConfiguration();
    }

    @Override
    protected Mono<ConsumerDmaapModel> execute(ConsumerDmaapModel consumerDmaapModel) throws PrhTaskException {
        if (consumerDmaapModel == null) {
            throw new DmaapNotFoundException("Invoked null object to DMaaP task");
        }
        aaiHttpPatchClient = resolveClient();
        LOGGER.debug("Method called with arg {}", consumerDmaapModel);
        return publish(consumerDmaapModel);
    }
}