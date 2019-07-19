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

import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.common.Unit;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.ImmutablePnf;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.Pnf;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static org.onap.dcaegen2.services.prh.model.AaiModelConverter.dmaapConsumerModelToPnf;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
@Component
public class AaiProducerTaskImpl implements AaiProducerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiProducerTaskImpl.class);

    private final AaiUpdateAction<Pnf, Unit> aaiUpdateAction;

    @Autowired
    public AaiProducerTaskImpl(final AaiUpdateAction<Pnf, Unit> aaiUpdateAction) {
        this.aaiUpdateAction = aaiUpdateAction;
    }

    private Mono<ConsumerDmaapModel> publish(ConsumerDmaapModel consumerDmaapModel) {
        return aaiUpdateAction
                .call(dmaapConsumerModelToPnf(consumerDmaapModel))
                .map(x -> consumerDmaapModel);
    }

    @Override
    public Mono<ConsumerDmaapModel> execute(ConsumerDmaapModel consumerDmaapModel) throws PrhTaskException {
        if (consumerDmaapModel == null) {
            throw new DmaapNotFoundException("Invoked null object to DMaaP task");
        }
        LOGGER.debug("Method called with arg {}", consumerDmaapModel);
        return publish(consumerDmaapModel);
    }
}