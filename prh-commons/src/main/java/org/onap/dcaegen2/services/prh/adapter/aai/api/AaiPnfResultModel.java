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


/**
 * PNF represents a physical network function. typically equipment used in the D1 world. in 1607, this will be populated by SDN-C to represent a premises router that a uCPE connects to. But this can be used to represent any physical device that is not an AIC node or uCPE. ###### Related Nodes - TO complex( pnf LocatedIn complex, MANY2ONE) - TO instance-group( pnf MemberOf instance-group, MANY2MANY) - TO zone( pnf LocatedIn zone, MANY2ONE) - FROM configuration( configuration AppliesTo pnf, ONE2MANY) - FROM esr-thirdparty-sdnc( esr-thirdparty-sdnc AppliesTo pnf, ONE2MANY) - FROM generic-vnf( generic-vnf HostedOn pnf, MANY2MANY) - FROM lag-interface (CHILD of pnf, lag-interface BindsTo pnf, MANY2ONE)(1) - FROM logical-link( logical-link BridgedTo pnf, MANY2MANY) - FROM p-interface (CHILD of pnf, p-interface BindsTo pnf, MANY2ONE)(1) - FROM service-instance( service-instance ComposedOf pnf, ONE2MANY)  -(1) IF this PNF node is deleted, this FROM node is DELETED also
 */
@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true)
public interface AaiPnfResultModel {

    /**
     * unique name of Physical Network Function.
     **/
    @Nullable
    @SerializedName("pnf-name")
    String getPnfName();

    /**
     * name of Physical Network Function.
     **/
    @Nullable
    @SerializedName("pnf-name2")
    String getPnfName2();

    /**
     * URL to endpoint where AAI can get more details.
     **/
    @Nullable
    @SerializedName("selflink")
    String getSelflink();

    /**
     * source of name2
     **/
    @Nullable
    @SerializedName("pnf-name2-source")
    String getPnfName2Source();

    /**
     * id of pnf
     **/
    @Nullable
    @SerializedName("pnf-id")
    String getPnfId();

    /**
     * Equipment type.  Source of truth should define valid values.
     **/
    @Nullable
    @SerializedName("equip-type")
    String getEquipType();

    /**
     * Equipment vendor.  Source of truth should define valid values.
     **/
    @Nullable
    @SerializedName("equip-vendor")
    String getEquipVendor();

    /**
     * Equipment model.  Source of truth should define valid values.
     **/
    @Nullable
    @SerializedName("equip-model")
    String getEquipModel();

    /**
     * identifier of managed by ATT or customer
     **/
    @Nullable
    @SerializedName("management-option")
    String getManagementOption();

    /**
     * ipv4-oam-address with new naming convention for IP addresses
     **/
    @Nullable
    @SerializedName("ipaddress-v4-oam")
    String getIpaddressV4Oam();

    /**
     * sw-version is the version of SW for the hosted application on the PNF.
     **/
    @Nullable
    @SerializedName("sw-version")
    String getSwVersion();

    /**
     * Used to indicate whether or not this object is in maintenance mode (maintenance mode &#x3D; true). This field (in conjunction with prov-status) is used to suppress alarms and vSCL on VNFs/VMs.
     **/
    @Nullable
    @SerializedName("in-maint")
    Boolean isInMaint();

    /**
     * ID of the physical frame (relay rack) where pnf is installed.
     **/
    @Nullable
    @SerializedName("frame-id")
    String getFrameId();

    /**
     * Serial number of the device
     **/
    @Nullable
    @SerializedName("serial-number")
    String getSerialNumber();

    /**
     * IPV4 Loopback 0 address
     **/
    @Nullable
    @SerializedName("ipaddress-v4-loopback-0")
    String getIpaddressV4Loopback0();

    /**
     * IPV6 Loopback 0 address
     **/
    @Nullable
    @SerializedName("ipaddress-v6-loopback-0")
    String getIpaddressV6Loopback0();

    /**
     * IPV4 AIM address
     **/
    @Nullable
    @SerializedName("ipaddress-v4-aim")
    String getIpaddressV4Aim();

    /**
     * IPV6 AIM address
     **/
    @Nullable
    @SerializedName("ipaddress-v6-aim")
    String getIpaddressV6Aim();

    /**
     * IPV6 OAM address
     **/
    @Nullable
    @SerializedName("ipaddress-v6-oam")
    String getIpaddressV6Oam();

    /**
     * CANOPI&#39;s inventory status.  Only set with values exactly as defined by CANOPI.
     **/
    @Nullable
    @SerializedName("inv-status")
    String getInvStatus();

    /**
     * Used for optimistic concurrency.  Must be empty on createGson, valid on update and delete.
     **/
    @Nullable
    @SerializedName("resource-version")
    String getResourceVersion();

    /**
     * Prov Status of this device (not under canopi control) Valid values [PREPROV/NVTPROV/PROV]
     **/
    @Nullable
    @SerializedName("prov-status")
    String getProvStatus();

    /**
     * Nf Role is the role performed by this instance in the network.
     **/
    @Nullable
    @SerializedName("nf-role")
    String getNfRole();

    /**
     * Get relationshipList
     **/
    @SerializedName("relationship-list")
    @Value.Default
    default Relationship getRelationshipList() {
        return ImmutableRelationship.builder().build();
    }
}

