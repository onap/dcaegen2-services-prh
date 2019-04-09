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

package org.onap.dcaegen2.services.prh.model;

import com.google.gson.annotations.SerializedName;

public class AaiServiceInstanceResultModel {
    @SerializedName("service-instance-id")
    private String serviceInstanceId = null;

    @SerializedName("service-instance-name")
    private String serviceInstanceName = null;

    @SerializedName("service-type")
    private String serviceType = null;

    @SerializedName("service-role")
    private String serviceRole = null;

    @SerializedName("environment-context")
    private String environmentContext = null;

    @SerializedName("workload-context")
    private String workloadContext = null;

    @SerializedName("created-at")
    private String createdAt = null;

    @SerializedName("updated-at")
    private String updatedAt = null;

    @SerializedName("description")
    private String description = null;

    @SerializedName("model-invariant-id")
    private String modelInvariantId = null;

    @SerializedName("model-version-id")
    private String modelVersionId = null;

    @SerializedName("persona-model-version")
    private String personaModelVersion = null;

    @SerializedName("widget-model-id")
    private String widgetModelId = null;

    @SerializedName("widget-model-version")
    private String widgetModelVersion = null;

    @SerializedName("bandwidth-total")
    private String bandwidthTotal = null;

    @SerializedName("bandwidth-up-wan1")
    private String bandwidthUpWan1 = null;

    @SerializedName("bandwidth-down-wan1")
    private String bandwidthDownWan1 = null;

    @SerializedName("bandwidth-up-wan2")
    private String bandwidthUpWan2 = null;

    @SerializedName("bandwidth-down-wan2")
    private String bandwidthDownWan2 = null;

    @SerializedName("vhn-portal-url")
    private String vhnPortalUrl = null;

    @SerializedName("service-instance-location-id")
    private String serviceInstanceLocationId = null;

    @SerializedName("resource-version")
    private String resourceVersion = null;

    @SerializedName("selflink")
    private String selflink = null;

    @SerializedName("orchestration-status")
    private String orchestrationStatus = null;

    @SerializedName("relationship-list")
    private Relationship relationshipList = null;

    /**
     * Uniquely identifies this instance of a service
     *
     * @return serviceInstanceId
     **/
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    /**
     * This field will store a name assigned to the service-instance.
     *
     * @return serviceInstanceName
     **/
    public String getServiceInstanceName() {
        return serviceInstanceName;
    }

    public void setServiceInstanceName(String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
    }

    /**
     * String capturing type of service.
     *
     * @return serviceType
     **/
    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * String capturing the service role.
     *
     * @return serviceRole
     **/
    public String getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(String serviceRole) {
        this.serviceRole = serviceRole;
    }

    /**
     * This field will store the environment context assigned to the service-instance.
     *
     * @return environmentContext
     **/
    public String getEnvironmentContext() {
        return environmentContext;
    }

    public void setEnvironmentContext(String environmentContext) {
        this.environmentContext = environmentContext;
    }

    /**
     * This field will store the workload context assigned to the service-instance.
     *
     * @return workloadContext
     **/
    public String getWorkloadContext() {
        return workloadContext;
    }

    public void setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
    }

    /**
     * create time of Network Service.
     *
     * @return createdAt
     **/
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * last update of Network Service.
     *
     * @return updatedAt
     **/
    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * short description for service-instance.
     *
     * @return description
     **/
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * the ASDC model id for this resource or service model.
     *
     * @return modelInvariantId
     **/
    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    /**
     * the ASDC model version for this resource or service model.
     *
     * @return modelVersionId
     **/
    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    /**
     * the ASDC model version for this resource or service model.
     *
     * @return personaModelVersion
     **/
    public String getPersonaModelVersion() {
        return personaModelVersion;
    }

    public void setPersonaModelVersion(String personaModelVersion) {
        this.personaModelVersion = personaModelVersion;
    }

    /**
     * the ASDC data dictionary widget model. This maps directly to the A&amp;AI widget.
     *
     * @return widgetModelId
     **/
    public String getWidgetModelId() {
        return widgetModelId;
    }

    public void setWidgetModelId(String widgetModelId) {
        this.widgetModelId = widgetModelId;
    }

    /**
     * the ASDC data dictionary version of the widget model.This maps directly to the A&amp;AI version of the widget.
     *
     * @return widgetModelVersion
     **/
    public String getWidgetModelVersion() {
        return widgetModelVersion;
    }

    public void setWidgetModelVersion(String widgetModelVersion) {
        this.widgetModelVersion = widgetModelVersion;
    }

    /**
     * Indicates the total bandwidth to be used for this service.
     *
     * @return bandwidthTotal
     **/
    public String getBandwidthTotal() {
        return bandwidthTotal;
    }

    public void setBandwidthTotal(String bandwidthTotal) {
        this.bandwidthTotal = bandwidthTotal;
    }

    /**
     * indicates the upstream bandwidth this service will use on the WAN1 port of the physical device.
     *
     * @return bandwidthUpWan1
     **/
    public String getBandwidthUpWan1() {
        return bandwidthUpWan1;
    }

    public void setBandwidthUpWan1(String bandwidthUpWan1) {
        this.bandwidthUpWan1 = bandwidthUpWan1;
    }

    /**
     * indicates the downstream bandwidth this service will use on the WAN1 port of the physical device.
     *
     * @return bandwidthDownWan1
     **/
    public String getBandwidthDownWan1() {
        return bandwidthDownWan1;
    }

    public void setBandwidthDownWan1(String bandwidthDownWan1) {
        this.bandwidthDownWan1 = bandwidthDownWan1;
    }

    /**
     * indicates the upstream bandwidth this service will use on the WAN2 port of the physical device.
     *
     * @return bandwidthUpWan2
     **/
    public String getBandwidthUpWan2() {
        return bandwidthUpWan2;
    }

    public void setBandwidthUpWan2(String bandwidthUpWan2) {
        this.bandwidthUpWan2 = bandwidthUpWan2;
    }

    /**
     * indicates the downstream bandwidth this service will use on the WAN2 port of the physical device.
     *
     * @return bandwidthDownWan2
     **/
    public String getBandwidthDownWan2() {
        return bandwidthDownWan2;
    }

    public void setBandwidthDownWan2(String bandwidthDownWan2) {
        this.bandwidthDownWan2 = bandwidthDownWan2;
    }

    /**
     * URL customers will use to access the vHN Portal.
     *
     * @return vhnPortalUrl
     **/
    public String getVhnPortalUrl() {
        return vhnPortalUrl;
    }

    public void setVhnPortalUrl(String vhnPortalUrl) {
        this.vhnPortalUrl = vhnPortalUrl;
    }

    /**
     * An identifier that customers assign to the location where this service is being used.
     *
     * @return serviceInstanceLocationId
     **/
    public String getServiceInstanceLocationId() {
        return serviceInstanceLocationId;
    }

    public void setServiceInstanceLocationId(String serviceInstanceLocationId) {
        this.serviceInstanceLocationId = serviceInstanceLocationId;
    }

    /**
     * Used for optimistic concurrency.  Must be empty on create, valid on update and delete.
     *
     * @return resourceVersion
     **/
    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    /**
     * Path to the controller object.
     *
     * @return selflink
     **/
    public String getSelflink() {
        return selflink;
    }

    public void setSelflink(String selflink) {
        this.selflink = selflink;
    }

    /**
     * Orchestration status of this service.
     *
     * @return orchestrationStatus
     **/
    public String getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(String orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }


    /**
     * Get relationshipList
     *
     * @return relationshipList
     **/
    public Relationship getRelationshipList() {
        return relationshipList;
    }

    public void setRelationshipList(Relationship relationshipList) {
        this.relationshipList = relationshipList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AaiServiceInstanceResultModel {\n");

        sb.append("    serviceInstanceId: ").append(toIndentedString(serviceInstanceId)).append("\n");
        sb.append("    serviceInstanceName: ").append(toIndentedString(serviceInstanceName)).append("\n");
        sb.append("    serviceType: ").append(toIndentedString(serviceType)).append("\n");
        sb.append("    serviceRole: ").append(toIndentedString(serviceRole)).append("\n");
        sb.append("    environmentContext: ").append(toIndentedString(environmentContext)).append("\n");
        sb.append("    workloadContext: ").append(toIndentedString(workloadContext)).append("\n");
        sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
        sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    modelInvariantId: ").append(toIndentedString(modelInvariantId)).append("\n");
        sb.append("    modelVersionId: ").append(toIndentedString(modelVersionId)).append("\n");
        sb.append("    personaModelVersion: ").append(toIndentedString(personaModelVersion)).append("\n");
        sb.append("    widgetModelId: ").append(toIndentedString(widgetModelId)).append("\n");
        sb.append("    widgetModelVersion: ").append(toIndentedString(widgetModelVersion)).append("\n");
        sb.append("    bandwidthTotal: ").append(toIndentedString(bandwidthTotal)).append("\n");
        sb.append("    bandwidthUpWan1: ").append(toIndentedString(bandwidthUpWan1)).append("\n");
        sb.append("    bandwidthDownWan1: ").append(toIndentedString(bandwidthDownWan1)).append("\n");
        sb.append("    bandwidthUpWan2: ").append(toIndentedString(bandwidthUpWan2)).append("\n");
        sb.append("    bandwidthDownWan2: ").append(toIndentedString(bandwidthDownWan2)).append("\n");
        sb.append("    vhnPortalUrl: ").append(toIndentedString(vhnPortalUrl)).append("\n");
        sb.append("    serviceInstanceLocationId: ").append(toIndentedString(serviceInstanceLocationId)).append("\n");
        sb.append("    resourceVersion: ").append(toIndentedString(resourceVersion)).append("\n");
        sb.append("    selflink: ").append(toIndentedString(selflink)).append("\n");
        sb.append("    orchestrationStatus: ").append(toIndentedString(orchestrationStatus)).append("\n");
        sb.append("    relationshipList: ").append(toIndentedString(relationshipList)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}