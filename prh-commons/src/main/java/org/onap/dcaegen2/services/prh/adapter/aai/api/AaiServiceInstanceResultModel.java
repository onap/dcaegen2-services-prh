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

package org.onap.dcaegen2.services.prh.adapter.aai.api;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.onap.dcaegen2.services.prh.model.ImmutableRelationship;
import org.onap.dcaegen2.services.prh.model.Relationship;
import org.springframework.lang.Nullable;

@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true)
public interface AaiServiceInstanceResultModel {

    /**
     * Uniquely identifies this instance of a service
     **/
    @Nullable
    @SerializedName("service-instance-id")
    String getServiceInstanceId();

    /**
     * This field will store a name assigned to the service-instance.
     **/
    @Nullable
    @SerializedName("service-instance-name")
    String getServiceInstanceName();

    /**
     * String capturing type of service.
     **/
    @Nullable
    @SerializedName("service-type")
    String getServiceType();

    /**
     * String capturing the service role.
     **/
    @Nullable
    @SerializedName("service-role")
    String getServiceRole();

    /**
     * This field will store the environment context assigned to the service-instance.
    **/
    @Nullable
    @SerializedName("environment-context")
    String getEnvironmentContext();

    /**
     * This field will store the workload context assigned to the service-instance.
    **/
    @Nullable
    @SerializedName("workload-context")
    String getWorkloadContext();

    /**
     * createGson time of Network Service.
    **/
    @Nullable
    @SerializedName("created-at")
    String getCreatedAt();

    /**
     * last update of Network Service.
    **/
    @Nullable
    @SerializedName("updated-at")
    String getUpdatedAt();

    /**
     * short description for service-instance.
    **/
    @Nullable
    @SerializedName("description")
    String getDescription();

    /**
     * the ASDC model id for this resource or service model.
    **/
    @Nullable
    @SerializedName("model-invariant-id")
    String getModelInvariantId();

    /**
     * the ASDC model version for this resource or service model.
    **/
    @Nullable
    @SerializedName("model-version-id")
    String getModelVersionId();

    /**
     * the ASDC model version for this resource or service model.
    **/
    @Nullable
    @SerializedName("persona-model-version")
    String getPersonaModelVersion();

    /**
     * the ASDC data dictionary widget model. This maps directly to the A&amp;AI widget.
    **/
    @Nullable
    @SerializedName("widget-model-id")
    String getWidgetModelId();

    /**
     * the ASDC data dictionary version of the widget model.This maps directly to the A&amp;AI version of the widget.
    **/
    @Nullable
    @SerializedName("widget-model-version")
    String getWidgetModelVersion();

    /**
     * Indicates the total bandwidth to be used for this service.
    **/
    @Nullable
    @SerializedName("bandwidth-total")
    String getBandwidthTotal();

    /**
     * indicates the upstream bandwidth this service will use on the WAN1 port of the physical device.
    **/
    @Nullable
    @SerializedName("bandwidth-up-wan1")
    String getBandwidthUpWan1();

    /**
     * indicates the downstream bandwidth this service will use on the WAN1 port of the physical device.
    **/
    @Nullable
    @SerializedName("bandwidth-down-wan1")
    String getBandwidthDownWan1();

    /**
     * indicates the upstream bandwidth this service will use on the WAN2 port of the physical device.
    **/
    @Nullable
    @SerializedName("bandwidth-up-wan2")
    String getBandwidthUpWan2();

    /**
     * indicates the downstream bandwidth this service will use on the WAN2 port of the physical device.
    **/
    @Nullable
    @SerializedName("bandwidth-down-wan2")
    String getBandwidthDownWan2();

    /**
     * URL customers will use to access the vHN Portal.
    **/
    @Nullable
    @SerializedName("vhn-portal-url")
    String getVhnPortalUrl();

    /**
     * An identifier that customers assign to the location where this service is being used.
    **/
    @Nullable
    @SerializedName("service-instance-location-id")
    String getServiceInstanceLocationId();

    /**
     * Used for optimistic concurrency.  Must be empty on createGson, valid on update and delete.
    **/
    @Nullable
    @SerializedName("resource-version")
    String getResourceVersion();

    /**
     * Path to the controller object.
    **/
    @Nullable
    @SerializedName("selflink")
    String getSelflink();

    /**
     * Orchestration status of this service.
    **/
    @Nullable
    @SerializedName("orchestration-status")
    String getOrchestrationStatus();

    /**
     * Get relationshipList
    **/
    @SerializedName("relationship-list")
    @Value.Default
    default Relationship getRelationshipList() {
        return ImmutableRelationship.builder().build();
    }

}