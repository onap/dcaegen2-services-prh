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

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.LogicalLinkComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.LogicalLinkRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpActionFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiAddRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiUpdateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory.AaiActionFactory;
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
    public AaiCreateAction<PnfComplete> createPnf() {
        return x -> createFactory().createPnf().call(x);
    }

    @Bean
    public AaiUpdateAction<PnfComplete> updatePnf() {
        return x -> createFactory().updatePnf().call(x);
    }

    @Bean
    public AaiGetAction<PnfRequired, PnfComplete> getPnf() {
        return x -> createFactory().getPnf().call(x);
    }

    @Bean
    public AaiGetAction<ServiceInstanceRequired, ServiceInstanceComplete> getService() {
        return x -> createFactory().getServiceInstance().call(x);
    }

    @Bean
    public AaiGetAction<LogicalLinkRequired, LogicalLinkComplete> getLogicalLink() {
        return x -> createFactory().getLogicalLink().call(x);
    }

    @Bean
    public AaiCreateAction<LogicalLinkComplete> createLogicalLink() {
        return x -> createFactory().createLogicalLink().call(x);
    }

    @Bean
    public AaiDeleteAction<LogicalLinkComplete> deleteLogicalLink() {
        return x -> createFactory().deleteLogicalLink().call(x);
    }

    @Bean
    public AaiGetRelationAction<PnfComplete, LogicalLinkComplete> getRelationToLogicalLink() {
        return x -> createFactory().<PnfComplete>getRelationToLogicalLink().call(x);
    }

    @Bean
    public AaiGetRelationAction<PnfComplete, ServiceInstanceComplete> getRelationToServiceInstance() {
        return x -> createFactory().<PnfComplete>getRelationToServiceInstance().call(x);
    }

    @Bean
    public AaiAddRelationAction<LogicalLinkRequired, PnfRequired> addRelationToPnf() {
        return x -> createFactory().addRelationFromLogicalLink(PnfRequired.class).call(x);
    }
};