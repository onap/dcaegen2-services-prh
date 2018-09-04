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

import javax.net.ssl.SSLException;
import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.configuration.Config;
import org.onap.dcaegen2.services.prh.exceptions.AaiNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.utils.HttpUtils;
import org.onap.dcaegen2.services.prh.service.producer.AaiProducerReactiveHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Component
public class AaiProducerTaskImpl extends
    AaiProducerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiProducerTaskImpl.class);

    private final Config config;
    private AaiProducerReactiveHttpClient aaiProducerReactiveHttpClient;

    @Autowired
    public AaiProducerTaskImpl(Config config) {
        this.config = config;
    }

    @Override
    Mono<ConsumerDmaapModel> publish(ConsumerDmaapModel consumerDmaapModel) {
        LOGGER.info("Publish to AAI DmaapModel");
        return aaiProducerReactiveHttpClient.getAaiProducerResponse(consumerDmaapModel)
            .flatMap(response -> {
                if (HttpUtils.isSuccessfulResponseCode(response.statusCode().value())) {
                    return Mono.just(consumerDmaapModel);
                }
                return Mono
                    .error(new AaiNotFoundException("Incorrect response code for continuation of tasks workflow"));
            });
    }

    @Override
    AaiProducerReactiveHttpClient resolveClient() throws SSLException {
        return new AaiProducerReactiveHttpClient(resolveConfiguration()).createAaiWebClient(buildWebClient());
    }

    @Override
    protected AaiClientConfiguration resolveConfiguration() {
        return config.getAaiClientConfiguration();
    }

    @Override
    protected Mono<ConsumerDmaapModel> execute(ConsumerDmaapModel consumerDmaapModel)
        throws PrhTaskException, SSLException {
        if (consumerDmaapModel == null) {
            throw new DmaapNotFoundException("Invoked null object to DMaaP task");
        }
        aaiProducerReactiveHttpClient = resolveClient();
        LOGGER.debug("Method called with arg {}", consumerDmaapModel);
        return publish(consumerDmaapModel);

    }
}