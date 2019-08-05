/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.model.utils.PrhModelAwareGsonBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationshipData;

class AaiServiceInstanceResultModelTest {

    @Test
    void shouldParseAaiServiceInstance() {
        ServiceInstanceComplete serviceInstance = PrhModelAwareGsonBuilder.createGson().fromJson(
                new InputStreamReader(Objects.requireNonNull(
                        ClassLoader.getSystemResourceAsStream("some_aai_service_instance.json"))),
                ServiceInstanceComplete.class);

        assertThat(serviceInstance.getServiceInstanceId()).isEqualTo("some serviceInstanceId");
        assertThat(serviceInstance.getServiceInstanceName()).isEqualTo("some serviceInstanceName");
        assertThat(serviceInstance.getServiceType()).isEqualTo("some serviceType");
        assertThat(serviceInstance.getServiceRole()).isEqualTo("some serviceRole");
        assertThat(serviceInstance.getEnvironmentContext()).isEqualTo("some environmentContext");
        assertThat(serviceInstance.getWorkloadContext()).isEqualTo("some workloadContext");
        assertThat(serviceInstance.getCreatedAt()).isEqualTo("some createdAt");
        assertThat(serviceInstance.getUpdatedAt()).isEqualTo("some updatedAt");
        assertThat(serviceInstance.getDescription()).isEqualTo("some description");
        assertThat(serviceInstance.getModelInvariantId()).isEqualTo("some modelInvariantId");
        assertThat(serviceInstance.getModelVersionId()).isEqualTo("some modelVersionId");
        assertThat(serviceInstance.getPersonaModelVersion()).isEqualTo("some personaModelVersion");
        assertThat(serviceInstance.getWidgetModelId()).isEqualTo("some widgetModelId");
        assertThat(serviceInstance.getWidgetModelVersion()).isEqualTo("some widgetModelVersion");
        assertThat(serviceInstance.getBandwidthTotal()).isEqualTo("some bandwidthTotal");
        assertThat(serviceInstance.getVhnPortalUrl()).isEqualTo("some vhnPortalUrl");
        assertThat(serviceInstance.getServiceInstanceLocationId()).isEqualTo("some serviceInstanceLocationId");
        assertThat(serviceInstance.getResourceVersion()).isEqualTo("some resourceVersion");
        assertThat(serviceInstance.getSelflink()).isEqualTo("some selflink");
        assertThat(serviceInstance.getOrchestrationStatus()).isEqualTo("some orchestrationStatus");

        Relationship relationshipDict = serviceInstance.getRelationshipList().getRelationship().get(0);
        assertThat(relationshipDict.getRelatedTo()).isEqualTo("some relatedTo");
        assertThat(relationshipDict.getRelationshipData()).hasSize(1);
        RelationshipData relationshipData = relationshipDict.getRelationshipData().get(0);
        assertThat(relationshipData.getRelationshipKey()).isEqualTo("some relationshipKey");
        assertThat(relationshipData.getRelationshipValue()).isEqualTo("some relationshipValue");
    }


    @Test
    void shouldProvideEmptyRelationshipListForEmptyJson() {
        final Gson gson = PrhModelAwareGsonBuilder.createGson();
        final ServiceInstanceComplete serviceInstance = gson.fromJson(
                "{\"service-instance-id\": \"FOO\", \"service-type\": \" BAR\", \"global-customer-id\" : \"BOO\"}",
                ServiceInstanceComplete.class);
        assertThat(serviceInstance.getRelationshipList()).isNotNull();
        assertThat(serviceInstance.getRelationshipList().getRelationship()).isEmpty();
    }

    @Test
    void shouldIgnoreUnexpectedFieldsInJson() {
        final Gson gson = PrhModelAwareGsonBuilder.createGson();
        gson.fromJson(
                "{\"service-instance-id\": \"FOO\", \"service-type\": \" BAR\", " +
                        "\"global-customer-id\" : \"BOO\", \"foo\":\"bar\"}",
                ServiceInstanceComplete.class);
    }

}