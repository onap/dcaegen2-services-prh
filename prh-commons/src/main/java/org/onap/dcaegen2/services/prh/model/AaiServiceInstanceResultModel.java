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

import java.util.Objects;

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

    public AaiServiceInstanceResultModel serviceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
        return this;
    }

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

    public AaiServiceInstanceResultModel serviceInstanceName(String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
        return this;
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

    public AaiServiceInstanceResultModel serviceType(String serviceType) {
        this.serviceType = serviceType;
        return this;
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

    public AaiServiceInstanceResultModel serviceRole(String serviceRole) {
        this.serviceRole = serviceRole;
        return this;
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

    public AaiServiceInstanceResultModel environmentContext(String environmentContext) {
        this.environmentContext = environmentContext;
        return this;
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

    public AaiServiceInstanceResultModel workloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
        return this;
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

    public AaiServiceInstanceResultModel createdAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
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

    public AaiServiceInstanceResultModel updatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
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

    public AaiServiceInstanceResultModel description(String description) {
        this.description = description;
        return this;
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

    public AaiServiceInstanceResultModel modelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
        return this;
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

    public AaiServiceInstanceResultModel modelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
        return this;
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

    public AaiServiceInstanceResultModel personaModelVersion(String personaModelVersion) {
        this.personaModelVersion = personaModelVersion;
        return this;
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

    public AaiServiceInstanceResultModel widgetModelId(String widgetModelId) {
        this.widgetModelId = widgetModelId;
        return this;
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

    public AaiServiceInstanceResultModel widgetModelVersion(String widgetModelVersion) {
        this.widgetModelVersion = widgetModelVersion;
        return this;
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

    public AaiServiceInstanceResultModel bandwidthTotal(String bandwidthTotal) {
        this.bandwidthTotal = bandwidthTotal;
        return this;
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

    public AaiServiceInstanceResultModel bandwidthUpWan1(String bandwidthUpWan1) {
        this.bandwidthUpWan1 = bandwidthUpWan1;
        return this;
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

    public AaiServiceInstanceResultModel bandwidthDownWan1(String bandwidthDownWan1) {
        this.bandwidthDownWan1 = bandwidthDownWan1;
        return this;
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

    public AaiServiceInstanceResultModel bandwidthUpWan2(String bandwidthUpWan2) {
        this.bandwidthUpWan2 = bandwidthUpWan2;
        return this;
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

    public AaiServiceInstanceResultModel bandwidthDownWan2(String bandwidthDownWan2) {
        this.bandwidthDownWan2 = bandwidthDownWan2;
        return this;
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

    public AaiServiceInstanceResultModel vhnPortalUrl(String vhnPortalUrl) {
        this.vhnPortalUrl = vhnPortalUrl;
        return this;
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

    public AaiServiceInstanceResultModel serviceInstanceLocationId(String serviceInstanceLocationId) {
        this.serviceInstanceLocationId = serviceInstanceLocationId;
        return this;
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

    public AaiServiceInstanceResultModel resourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
        return this;
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

    public AaiServiceInstanceResultModel selflink(String selflink) {
        this.selflink = selflink;
        return this;
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

    public AaiServiceInstanceResultModel orchestrationStatus(String orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
        return this;
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

    public AaiServiceInstanceResultModel relationshipList(Relationship relationshipList) {
        this.relationshipList = relationshipList;
        return this;
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
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AaiServiceInstanceResultModel serviceInstance = (AaiServiceInstanceResultModel) o;
        return Objects.equals(this.serviceInstanceId, serviceInstance.serviceInstanceId) &&
                Objects.equals(this.serviceInstanceName, serviceInstance.serviceInstanceName) &&
                Objects.equals(this.serviceType, serviceInstance.serviceType) &&
                Objects.equals(this.serviceRole, serviceInstance.serviceRole) &&
                Objects.equals(this.environmentContext, serviceInstance.environmentContext) &&
                Objects.equals(this.workloadContext, serviceInstance.workloadContext) &&
                Objects.equals(this.createdAt, serviceInstance.createdAt) &&
                Objects.equals(this.updatedAt, serviceInstance.updatedAt) &&
                Objects.equals(this.description, serviceInstance.description) &&
                Objects.equals(this.modelInvariantId, serviceInstance.modelInvariantId) &&
                Objects.equals(this.modelVersionId, serviceInstance.modelVersionId) &&
                Objects.equals(this.personaModelVersion, serviceInstance.personaModelVersion) &&
                Objects.equals(this.widgetModelId, serviceInstance.widgetModelId) &&
                Objects.equals(this.widgetModelVersion, serviceInstance.widgetModelVersion) &&
                Objects.equals(this.bandwidthTotal, serviceInstance.bandwidthTotal) &&
                Objects.equals(this.bandwidthUpWan1, serviceInstance.bandwidthUpWan1) &&
                Objects.equals(this.bandwidthDownWan1, serviceInstance.bandwidthDownWan1) &&
                Objects.equals(this.bandwidthUpWan2, serviceInstance.bandwidthUpWan2) &&
                Objects.equals(this.bandwidthDownWan2, serviceInstance.bandwidthDownWan2) &&
                Objects.equals(this.vhnPortalUrl, serviceInstance.vhnPortalUrl) &&
                Objects.equals(this.serviceInstanceLocationId, serviceInstance.serviceInstanceLocationId) &&
                Objects.equals(this.resourceVersion, serviceInstance.resourceVersion) &&
                Objects.equals(this.selflink, serviceInstance.selflink) &&
                Objects.equals(this.orchestrationStatus, serviceInstance.orchestrationStatus) &&
                Objects.equals(this.relationshipList, serviceInstance.relationshipList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInstanceId, serviceInstanceName, serviceType, serviceRole, environmentContext, workloadContext, createdAt, updatedAt, description, modelInvariantId, modelVersionId, personaModelVersion, widgetModelId, widgetModelVersion, bandwidthTotal, bandwidthUpWan1, bandwidthDownWan1, bandwidthUpWan2, bandwidthDownWan2, vhnPortalUrl, serviceInstanceLocationId, resourceVersion, selflink, orchestrationStatus, relationshipList);
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