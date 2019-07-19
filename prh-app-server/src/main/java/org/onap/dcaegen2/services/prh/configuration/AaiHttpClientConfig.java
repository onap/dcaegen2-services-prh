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

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.common.Unit;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.LogicalLink;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.LogicalLinkRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.Pnf;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.service.ServiceInstance;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.service.ServiceInstanceRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.AaiActionFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.actions.AaiUpdateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpActionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AaiHttpClientConfig {
    @Autowired
    private CbsConfiguration cbsConfiguration;

    private AaiActionFactory createFactory() {
        return new AaiHttpActionFactory(cbsConfiguration.getAaiClientConfiguration());
    }

    @Bean
    public AaiCreateAction<Pnf, Unit> createPnf() {
        return x -> createFactory().createPnf().call(x);
    }

    @Bean
    public AaiUpdateAction<Pnf, Unit> updatePnf() {
        return x -> createFactory().updatePnf().call(x);
    }

    @Bean
    public AaiGetAction<PnfRequired, Pnf> getPnf() {
        return x -> createFactory().getPnf().call(x);
    }

    @Bean
    public AaiGetAction<ServiceInstanceRequired, ServiceInstance> getService() {
        return x -> createFactory().getService().call(x);
    }

    @Bean
    public AaiGetAction<LogicalLinkRequired, LogicalLink> getLogicalLink() {
        return x -> createFactory().getLogicalLink().call(x);
    }

    @Bean
    public AaiCreateAction<LogicalLink, Unit> createLogicalLink() {
        return x -> createFactory().createLogicalLink().call(x);
    }

    @Bean
    public AaiDeleteAction<LogicalLink, Unit> deleteLogicalLink() {
        return x -> createFactory().deleteLogicalLink().call(x);
    }
};