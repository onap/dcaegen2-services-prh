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

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AaiServiceInstanceResultModelTest {

    private static final String SOME_AAI_SERVICE_INSTANCE_JSON = "{" +
            "\"service-instance-id\":\"some serviceInstanceId\"," +
            "\"service-instance-name\":\"some serviceInstanceName\"," +
            "\"service-type\":\"some serviceType\"," +
            "\"service-role\":\"some serviceRole\"," +
            "\"environment-context\":\"some environmentContext\"," +
            "\"workload-context\":\"some workloadContext\"," +
            "\"created-at\":\"some createdAt\"," +
            "\"updated-at\":\"some updatedAt\"," +
            "\"description\":\"some description\"," +
            "\"model-invariant-id\":\"some modelInvariantId\"," +
            "\"model-version-id\":\"some modelVersionId\"," +
            "\"persona-model-version\":\"some personaModelVersion\"," +
            "\"widget-model-id\":\"some widgetModelId\"," +
            "\"widget-model-version\":\"some widgetModelVersion\"," +
            "\"bandwidth-total\":\"some bandwidthTotal\"," +
            "\"bandwidth-up-wan1\":\"some bandwidthUpWan1\"," +
            "\"bandwidth-down-wan1\":\"some bandwidthDownWan1\"," +
            "\"bandwidth-up-wan2\":\"some bandwidthUpWan2\"," +
            "\"bandwidth-down-wan2\":\"some bandwidthDownWan2\"," +
            "\"vhn-portal-url\":\"some vhnPortalUrl\"," +
            "\"service-instance-location-id\":\"some serviceInstanceLocationId\"," +
            "\"resource-version\":\"some resourceVersion\"," +
            "\"selflink\":\"some selflink\"," +
            "\"orchestration-status\":\"some orchestrationStatus\"," +
            "\"relationship-list\":{\"relationship\":[{}]}" +
            "}";

    private static AaiServiceInstanceResultModel getSomeAaiServiceInstance() {
        AaiServiceInstanceResultModel serviceInstance = new AaiServiceInstanceResultModel();
        serviceInstance.setServiceInstanceId("some serviceInstanceId");
        serviceInstance.setServiceInstanceName("some serviceInstanceName");
        serviceInstance.setServiceType("some serviceType");
        serviceInstance.setServiceRole("some serviceRole");
        serviceInstance.setEnvironmentContext("some environmentContext");
        serviceInstance.setWorkloadContext("some workloadContext");
        serviceInstance.setCreatedAt("some createdAt");
        serviceInstance.setUpdatedAt("some updatedAt");
        serviceInstance.setDescription("some description");
        serviceInstance.setModelInvariantId("some modelInvariantId");
        serviceInstance.setModelVersionId("some modelVersionId");
        serviceInstance.setPersonaModelVersion("some personaModelVersion");
        serviceInstance.setWidgetModelId("some widgetModelId");
        serviceInstance.setWidgetModelVersion("some widgetModelVersion");
        serviceInstance.setBandwidthTotal("some bandwidthTotal");
        serviceInstance.setBandwidthUpWan1("some bandwidthUpWan1");
        serviceInstance.setBandwidthDownWan1("some bandwidthDownWan1");
        serviceInstance.setBandwidthUpWan2("some bandwidthUpWan2");
        serviceInstance.setBandwidthDownWan2("some bandwidthDownWan2");
        serviceInstance.setVhnPortalUrl("some vhnPortalUrl");
        serviceInstance.setServiceInstanceLocationId("some serviceInstanceLocationId");
        serviceInstance.setResourceVersion("some resourceVersion");
        serviceInstance.setSelflink("some selflink");
        serviceInstance.setOrchestrationStatus("some orchestrationStatus");
        serviceInstance.setRelationshipList(getSomeRelationshipList());
        return serviceInstance;
    }

    private static Relationship getSomeRelationshipList() {
        return new Relationship().addRelationshipItem(
                new RelationshipDict());
    }

    @Test
    void shouldSerializeAaiServiceInstance() {
        String json = new GsonBuilder().create().toJson(getSomeAaiServiceInstance());
        assertEquals(SOME_AAI_SERVICE_INSTANCE_JSON, json);
    }

    @Test
    void shouldParseAaiServiceInstance() {
        AaiServiceInstanceResultModel serviceInstance = new GsonBuilder().create()
                .fromJson(SOME_AAI_SERVICE_INSTANCE_JSON, AaiServiceInstanceResultModel.class);

        assertEquals("some serviceInstanceId", serviceInstance.getServiceInstanceId());
        assertEquals("some serviceInstanceName", serviceInstance.getServiceInstanceName());
        assertEquals("some serviceType", serviceInstance.getServiceType());
        assertEquals("some serviceRole", serviceInstance.getServiceRole());
        assertEquals("some environmentContext", serviceInstance.getEnvironmentContext());
        assertEquals("some workloadContext", serviceInstance.getWorkloadContext());
        assertEquals("some createdAt", serviceInstance.getCreatedAt());
        assertEquals("some updatedAt", serviceInstance.getUpdatedAt());
        assertEquals("some description", serviceInstance.getDescription());
        assertEquals("some modelInvariantId", serviceInstance.getModelInvariantId());
        assertEquals("some modelVersionId", serviceInstance.getModelVersionId());
        assertEquals("some personaModelVersion", serviceInstance.getPersonaModelVersion());
        assertEquals("some widgetModelId", serviceInstance.getWidgetModelId());
        assertEquals("some widgetModelVersion", serviceInstance.getWidgetModelVersion());
        assertEquals("some bandwidthTotal", serviceInstance.getBandwidthTotal());
        assertEquals("some bandwidthUpWan1", serviceInstance.getBandwidthUpWan1());
        assertEquals("some bandwidthDownWan1", serviceInstance.getBandwidthDownWan1());
        assertEquals("some bandwidthUpWan2", serviceInstance.getBandwidthUpWan2());
        assertEquals("some bandwidthDownWan2", serviceInstance.getBandwidthDownWan2());
        assertEquals("some vhnPortalUrl", serviceInstance.getVhnPortalUrl());
        assertEquals("some serviceInstanceLocationId", serviceInstance.getServiceInstanceLocationId());
        assertEquals("some resourceVersion", serviceInstance.getResourceVersion());
        assertEquals("some selflink", serviceInstance.getSelflink());
        assertEquals("some orchestrationStatus", serviceInstance.getOrchestrationStatus());
        assertEquals(1, serviceInstance.getRelationshipList().getRelationship().size());
    }

    @Test
    void shouldBePrintable() {
        String s = getSomeAaiServiceInstance().toString();
        assertThat(s).contains("some serviceInstanceId");
        assertThat(s).contains("some serviceInstanceName");
        assertThat(s).contains("some serviceType");
        assertThat(s).contains("some serviceRole");
        assertThat(s).contains("some environmentContext");
        assertThat(s).contains("some workloadContext");
        assertThat(s).contains("some createdAt");
        assertThat(s).contains("some updatedAt");
        assertThat(s).contains("some description");
        assertThat(s).contains("some modelInvariantId");
        assertThat(s).contains("some modelVersionId");
        assertThat(s).contains("some personaModelVersion");
        assertThat(s).contains("some widgetModelId");
        assertThat(s).contains("some widgetModelVersion");
        assertThat(s).contains("some bandwidthTotal");
        assertThat(s).contains("some bandwidthUpWan1");
        assertThat(s).contains("some bandwidthDownWan1");
        assertThat(s).contains("some bandwidthUpWan2");
        assertThat(s).contains("some bandwidthDownWan2");
        assertThat(s).contains("some vhnPortalUrl");
        assertThat(s).contains("some serviceInstanceLocationId");
        assertThat(s).contains("some resourceVersion");
        assertThat(s).contains("some selflink");
        assertThat(s).contains("some orchestrationStatus");
    }
}