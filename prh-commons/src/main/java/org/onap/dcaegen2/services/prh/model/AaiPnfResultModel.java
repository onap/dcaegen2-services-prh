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

/**
 * PNF represents a physical network function. typically equipment used in the D1 world. in 1607, this will be populated by SDN-C to represent a premises router that a uCPE connects to. But this can be used to represent any physical device that is not an AIC node or uCPE. ###### Related Nodes - TO complex( pnf LocatedIn complex, MANY2ONE) - TO instance-group( pnf MemberOf instance-group, MANY2MANY) - TO zone( pnf LocatedIn zone, MANY2ONE) - FROM configuration( configuration AppliesTo pnf, ONE2MANY) - FROM esr-thirdparty-sdnc( esr-thirdparty-sdnc AppliesTo pnf, ONE2MANY) - FROM generic-vnf( generic-vnf HostedOn pnf, MANY2MANY) - FROM lag-interface (CHILD of pnf, lag-interface BindsTo pnf, MANY2ONE)(1) - FROM logical-link( logical-link BridgedTo pnf, MANY2MANY) - FROM p-interface (CHILD of pnf, p-interface BindsTo pnf, MANY2ONE)(1) - FROM service-instance( service-instance ComposedOf pnf, ONE2MANY)  -(1) IF this PNF node is deleted, this FROM node is DELETED also
 */

public class AaiPnfResultModel {
    @SerializedName("pnf-name")
    private String pnfName = null;

    @SerializedName("pnf-name2")
    private String pnfName2 = null;

    @SerializedName("selflink")
    private String selflink = null;

    @SerializedName("pnf-name2-source")
    private String pnfName2Source = null;

    @SerializedName("pnf-id")
    private String pnfId = null;

    @SerializedName("equip-type")
    private String equipType = null;

    @SerializedName("equip-vendor")
    private String equipVendor = null;

    @SerializedName("equip-model")
    private String equipModel = null;

    @SerializedName("management-option")
    private String managementOption = null;

    @SerializedName("ipaddress-v4-oam")
    private String ipaddressV4Oam = null;

    @SerializedName("sw-version")
    private String swVersion = null;

    @SerializedName("in-maint")
    private Boolean inMaint = null;

    @SerializedName("frame-id")
    private String frameId = null;

    @SerializedName("serial-number")
    private String serialNumber = null;

    @SerializedName("ipaddress-v4-loopback-0")
    private String ipaddressV4Loopback0 = null;

    @SerializedName("ipaddress-v6-loopback-0")
    private String ipaddressV6Loopback0 = null;

    @SerializedName("ipaddress-v4-aim")
    private String ipaddressV4Aim = null;

    @SerializedName("ipaddress-v6-aim")
    private String ipaddressV6Aim = null;

    @SerializedName("ipaddress-v6-oam")
    private String ipaddressV6Oam = null;

    @SerializedName("inv-status")
    private String invStatus = null;

    @SerializedName("resource-version")
    private String resourceVersion = null;

    @SerializedName("prov-status")
    private String provStatus = null;

    @SerializedName("nf-role")
    private String nfRole = null;

    @SerializedName("relationship-list")
    private Relationship relationshipList = null;

    /**
     * unique name of Physical Network Function.
     *
     * @return pnfName
     **/
    public String getPnfName() {
        return pnfName;
    }

    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

    /**
     * name of Physical Network Function.
     *
     * @return pnfName2
     **/
    public String getPnfName2() {
        return pnfName2;
    }

    public void setPnfName2(String pnfName2) {
        this.pnfName2 = pnfName2;
    }

    /**
     * URL to endpoint where AAI can get more details.
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
     * source of name2
     *
     * @return pnfName2Source
     **/
    public String getPnfName2Source() {
        return pnfName2Source;
    }

    public void setPnfName2Source(String pnfName2Source) {
        this.pnfName2Source = pnfName2Source;
    }

    /**
     * id of pnf
     *
     * @return pnfId
     **/
    public String getPnfId() {
        return pnfId;
    }

    public void setPnfId(String pnfId) {
        this.pnfId = pnfId;
    }

    /**
     * Equipment type.  Source of truth should define valid values.
     *
     * @return equipType
     **/
    public String getEquipType() {
        return equipType;
    }

    public void setEquipType(String equipType) {
        this.equipType = equipType;
    }

    /**
     * Equipment vendor.  Source of truth should define valid values.
     *
     * @return equipVendor
     **/
    public String getEquipVendor() {
        return equipVendor;
    }

    public void setEquipVendor(String equipVendor) {
        this.equipVendor = equipVendor;
    }

    /**
     * Equipment model.  Source of truth should define valid values.
     *
     * @return equipModel
     **/
    public String getEquipModel() {
        return equipModel;
    }

    public void setEquipModel(String equipModel) {
        this.equipModel = equipModel;
    }

    /**
     * identifier of managed by ATT or customer
     *
     * @return managementOption
     **/
    public String getManagementOption() {
        return managementOption;
    }

    public void setManagementOption(String managementOption) {
        this.managementOption = managementOption;
    }

    /**
     * ipv4-oam-address with new naming convention for IP addresses
     *
     * @return ipaddressV4Oam
     **/
    public String getIpaddressV4Oam() {
        return ipaddressV4Oam;
    }

    public void setIpaddressV4Oam(String ipaddressV4Oam) {
        this.ipaddressV4Oam = ipaddressV4Oam;
    }

    /**
     * sw-version is the version of SW for the hosted application on the PNF.
     *
     * @return swVersion
     **/
    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    /**
     * Used to indicate whether or not this object is in maintenance mode (maintenance mode &#x3D; true). This field (in conjunction with prov-status) is used to suppress alarms and vSCL on VNFs/VMs.
     *
     * @return inMaint
     **/
    public Boolean isInMaint() {
        return inMaint;
    }

    public void setInMaint(Boolean inMaint) {
        this.inMaint = inMaint;
    }

    /**
     * ID of the physical frame (relay rack) where pnf is installed.
     *
     * @return frameId
     **/
    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    /**
     * Serial number of the device
     *
     * @return serialNumber
     **/
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * IPV4 Loopback 0 address
     *
     * @return ipaddressV4Loopback0
     **/
    public String getIpaddressV4Loopback0() {
        return ipaddressV4Loopback0;
    }

    public void setIpaddressV4Loopback0(String ipaddressV4Loopback0) {
        this.ipaddressV4Loopback0 = ipaddressV4Loopback0;
    }

    /**
     * IPV6 Loopback 0 address
     *
     * @return ipaddressV6Loopback0
     **/
    public String getIpaddressV6Loopback0() {
        return ipaddressV6Loopback0;
    }

    public void setIpaddressV6Loopback0(String ipaddressV6Loopback0) {
        this.ipaddressV6Loopback0 = ipaddressV6Loopback0;
    }

    /**
     * IPV4 AIM address
     *
     * @return ipaddressV4Aim
     **/
    public String getIpaddressV4Aim() {
        return ipaddressV4Aim;
    }

    public void setIpaddressV4Aim(String ipaddressV4Aim) {
        this.ipaddressV4Aim = ipaddressV4Aim;
    }

    /**
     * IPV6 AIM address
     *
     * @return ipaddressV6Aim
     **/
    public String getIpaddressV6Aim() {
        return ipaddressV6Aim;
    }

    public void setIpaddressV6Aim(String ipaddressV6Aim) {
        this.ipaddressV6Aim = ipaddressV6Aim;
    }

    /**
     * IPV6 OAM address
     *
     * @return ipaddressV6Oam
     **/
    public String getIpaddressV6Oam() {
        return ipaddressV6Oam;
    }

    public void setIpaddressV6Oam(String ipaddressV6Oam) {
        this.ipaddressV6Oam = ipaddressV6Oam;
    }

    /**
     * CANOPI&#39;s inventory status.  Only set with values exactly as defined by CANOPI.
     *
     * @return invStatus
     **/
    public String getInvStatus() {
        return invStatus;
    }

    public void setInvStatus(String invStatus) {
        this.invStatus = invStatus;
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
     * Prov Status of this device (not under canopi control) Valid values [PREPROV/NVTPROV/PROV]
     *
     * @return provStatus
     **/
    public String getProvStatus() {
        return provStatus;
    }

    public void setProvStatus(String provStatus) {
        this.provStatus = provStatus;
    }

    /**
     * Nf Role is the role performed by this instance in the network.
     *
     * @return nfRole
     **/
    public String getNfRole() {
        return nfRole;
    }

    public void setNfRole(String nfRole) {
        this.nfRole = nfRole;
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
        sb.append("class AaiPnfResultModel  {\n");

        sb.append("    pnfName: ").append(toIndentedString(pnfName)).append("\n");
        sb.append("    pnfName2: ").append(toIndentedString(pnfName2)).append("\n");
        sb.append("    selflink: ").append(toIndentedString(selflink)).append("\n");
        sb.append("    pnfName2Source: ").append(toIndentedString(pnfName2Source)).append("\n");
        sb.append("    pnfId: ").append(toIndentedString(pnfId)).append("\n");
        sb.append("    equipType: ").append(toIndentedString(equipType)).append("\n");
        sb.append("    equipVendor: ").append(toIndentedString(equipVendor)).append("\n");
        sb.append("    equipModel: ").append(toIndentedString(equipModel)).append("\n");
        sb.append("    managementOption: ").append(toIndentedString(managementOption)).append("\n");
        sb.append("    ipaddressV4Oam: ").append(toIndentedString(ipaddressV4Oam)).append("\n");
        sb.append("    swVersion: ").append(toIndentedString(swVersion)).append("\n");
        sb.append("    inMaint: ").append(toIndentedString(inMaint)).append("\n");
        sb.append("    frameId: ").append(toIndentedString(frameId)).append("\n");
        sb.append("    serialNumber: ").append(toIndentedString(serialNumber)).append("\n");
        sb.append("    ipaddressV4Loopback0: ").append(toIndentedString(ipaddressV4Loopback0)).append("\n");
        sb.append("    ipaddressV6Loopback0: ").append(toIndentedString(ipaddressV6Loopback0)).append("\n");
        sb.append("    ipaddressV4Aim: ").append(toIndentedString(ipaddressV4Aim)).append("\n");
        sb.append("    ipaddressV6Aim: ").append(toIndentedString(ipaddressV6Aim)).append("\n");
        sb.append("    ipaddressV6Oam: ").append(toIndentedString(ipaddressV6Oam)).append("\n");
        sb.append("    invStatus: ").append(toIndentedString(invStatus)).append("\n");
        sb.append("    resourceVersion: ").append(toIndentedString(resourceVersion)).append("\n");
        sb.append("    provStatus: ").append(toIndentedString(provStatus)).append("\n");
        sb.append("    nfRole: ").append(toIndentedString(nfRole)).append("\n");
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

