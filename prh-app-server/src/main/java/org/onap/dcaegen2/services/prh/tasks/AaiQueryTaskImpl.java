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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiHttpClient;
import org.onap.dcaegen2.services.prh.adapter.aai.model.AaiServiceInstanceQueryModel;
import org.onap.dcaegen2.services.prh.adapter.aai.model.ImmutableAaiServiceInstanceQueryModel;
import org.onap.dcaegen2.services.prh.model.AaiPnfResultModel;
import org.onap.dcaegen2.services.prh.model.AaiServiceInstanceResultModel;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.Relationship;
import org.onap.dcaegen2.services.prh.model.RelationshipData;
import org.onap.dcaegen2.services.prh.model.RelationshipDict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AaiQueryTaskImpl implements AaiQueryTask {
    static final String ACTIVE_STATUS = "Active";
    static final String RELATED_TO = "service-instance";
    static final String CUSTOMER = "customer.global-customer-id";
    static final String SERVICE_TYPE = "service-subscription.service-type";
    static final String SERVICE_INSTANCE_ID = "service-instance.service-instance-id";

    private final AaiHttpClient<ConsumerDmaapModel, AaiPnfResultModel> getPnfModelClient;
    private final AaiHttpClient<AaiServiceInstanceQueryModel, AaiServiceInstanceResultModel> getServiceClient;

    @Autowired
    public AaiQueryTaskImpl(
            final AaiHttpClient<ConsumerDmaapModel, AaiPnfResultModel> getPnfModelClient,
            final AaiHttpClient<AaiServiceInstanceQueryModel, AaiServiceInstanceResultModel> getServiceClient) {
        this.getPnfModelClient = getPnfModelClient;
        this.getServiceClient = getServiceClient;
    }

    @Override
    public Mono<Boolean> execute(ConsumerDmaapModel aaiModel) {
        return getPnfModelClient
                .getAaiResponse(aaiModel)
                .flatMap(this::checkIfPnfHasRelationToService)
                .flatMap(getServiceClient::getAaiResponse)
                .map(this::checkIfRelatedServiceInstanceIsActive)
                .defaultIfEmpty(false);
    }

    private Mono<AaiServiceInstanceQueryModel> checkIfPnfHasRelationToService(final AaiPnfResultModel model) {
        return Mono
                .justOrEmpty(model.getRelationshipList())
                .map(this::findRelatedTo)
                .flatMap(Mono::justOrEmpty)
                .map(RelationshipDict::getRelationshipData)
                .flatMap(x -> {
                    final Optional<String> customer = findValue(x, CUSTOMER);
                    final Optional<String> serviceType = findValue(x, SERVICE_TYPE);
                    final Optional<String> serviceInstanceId= findValue(x, SERVICE_INSTANCE_ID);

                    return customer.isPresent() && serviceType.isPresent() && serviceInstanceId.isPresent()
                            ? Mono.just(ImmutableAaiServiceInstanceQueryModel
                            .builder()
                            .customerId(customer.get())
                            .serviceType(serviceType.get())
                            .serviceInstanceId(serviceInstanceId.get())
                            .build())
                            : Mono.empty();
                });
    }

    private Boolean checkIfRelatedServiceInstanceIsActive(final AaiServiceInstanceResultModel model) {
        return ACTIVE_STATUS.equalsIgnoreCase(model.getOrchestrationStatus());
    }

    private Optional<RelationshipDict> findRelatedTo(final Relationship data) {
        return Optional.ofNullable(data.getRelationship())
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .flatMap(List::stream)
                .filter(x -> RELATED_TO.equals(x.getRelatedTo()))
                .findFirst();
    }

    private Optional<String> findValue(final List<RelationshipData> data, final String key) {
        return data
                .stream()
                .filter(y -> key.equals(y.getRelationshipKey()))
                .findFirst()
                .map(RelationshipData::getRelationshipValue);
    }
}
