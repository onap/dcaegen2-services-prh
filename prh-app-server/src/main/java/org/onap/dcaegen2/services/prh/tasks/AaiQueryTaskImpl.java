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

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.Pnf;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.service.ServiceInstance;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.service.ServiceInstanceRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiGetAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AaiQueryTaskImpl implements AaiQueryTask {
    private final static String ACTIVE_STATUS = "Active";

    private final AaiGetAction<PnfRequired, Pnf> getPnfAction;
    private final AaiGetAction<ServiceInstanceRequired, ServiceInstance> getServiceAction;

    @Autowired
    public AaiQueryTaskImpl(
            final AaiGetAction<PnfRequired, Pnf> getPnfAction,
            final AaiGetAction<ServiceInstanceRequired, ServiceInstance> getServiceAction) {
        this.getPnfAction = getPnfAction;
        this.getServiceAction = getServiceAction;
    }

    @Override
    public Mono<Boolean> execute(PnfRequired pnf) {
        return getPnfAction
                .call(pnf)
                .map(Pnf::getServiceReference)
                .flatMap(Mono::justOrEmpty)
                .flatMap(getServiceAction::call)
                .map(this::checkIfRelatedServiceInstanceIsActive)
                .defaultIfEmpty(false);
    }

    private Boolean checkIfRelatedServiceInstanceIsActive(final ServiceInstance model) {
        return ACTIVE_STATUS.equalsIgnoreCase(model.getOrchestrationStatus());
    }
}
