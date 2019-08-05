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

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AaiQueryTaskImpl implements AaiQueryTask {
    private final static String ACTIVE_STATUS = "Active";

    private final AaiGetAction<PnfRequired, PnfComplete> getPnfAction;
    private final AaiGetRelationAction<PnfComplete, ServiceInstanceComplete> getRelationToServiceInstance;

    @Autowired
    public AaiQueryTaskImpl(
            final AaiGetAction<PnfRequired, PnfComplete> getPnfAction,
            final AaiGetRelationAction<PnfComplete, ServiceInstanceComplete> getRelationToServiceInstance) {
        this.getPnfAction = getPnfAction;
        this.getRelationToServiceInstance = getRelationToServiceInstance;
    }

    @Override
    public Mono<Boolean> execute(PnfRequired pnf) {
        return getPnfAction
                .call(pnf)
                .flux()
                .flatMap(getRelationToServiceInstance::call)
                .map(AaiRelation::to)
                .any(this::checkIfRelatedServiceInstanceIsActive)
                .defaultIfEmpty(false);
    }

    private Boolean checkIfRelatedServiceInstanceIsActive(final ServiceInstanceComplete model) {
        return ACTIVE_STATUS.equalsIgnoreCase(model.getOrchestrationStatus());
    }
}
