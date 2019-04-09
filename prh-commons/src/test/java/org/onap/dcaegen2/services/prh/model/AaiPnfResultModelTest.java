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

class AaiPnfResultModelTest {

    private static final String SOME_AAI_PNF_JSON = "{" +
            "\"pnf-name\":\"some pnfName\"," +
            "\"pnf-name2\":\"some pnfName2\"," +
            "\"selflink\":\"some selflink\"," +
            "\"pnf-name2-source\":\"some pnfName2Source\"," +
            "\"pnf-id\":\"some pnfId\"," +
            "\"equip-type\":\"some equipType\"," +
            "\"equip-vendor\":\"some equipVendor\"," +
            "\"equip-model\":\"some equipModel\"," +
            "\"management-option\":\"some managementOption\"," +
            "\"ipaddress-v4-oam\":\"some ipaddressV4Oam\"," +
            "\"sw-version\":\"some swVersion\"," +
            "\"in-maint\":false," +
            "\"frame-id\":\"some frameId\"," +
            "\"serial-number\":\"some serialNumber\"," +
            "\"ipaddress-v4-loopback-0\":\"some ipaddressV4Loopback0\"," +
            "\"ipaddress-v6-loopback-0\":\"some ipaddressV6Loopback0\"," +
            "\"ipaddress-v4-aim\":\"some ipaddressV4Aim\"," +
            "\"ipaddress-v6-aim\":\"some ipaddressV6Aim\"," +
            "\"ipaddress-v6-oam\":\"some ipaddressV6Oam\"," +
            "\"inv-status\":\"some invStatus\"," +
            "\"resource-version\":\"some resourceVersion\"," +
            "\"prov-status\":\"some provStatus\"," +
            "\"nf-role\":\"some nfRole\"," +
            "\"relationship-list\":{\"relationship\":[{}]}" +
            "}";

    private static AaiPnfResultModel getAaiPnfResultModel() {
        AaiPnfResultModel aaiPnf = new AaiPnfResultModel();
        aaiPnf.setPnfName("some pnfName");
        aaiPnf.setPnfName2("some pnfName2");
        aaiPnf.setSelflink("some selflink");
        aaiPnf.setPnfName2Source("some pnfName2Source");
        aaiPnf.setPnfId("some pnfId");
        aaiPnf.setEquipType("some equipType");
        aaiPnf.setEquipVendor("some equipVendor");
        aaiPnf.setEquipModel("some equipModel");
        aaiPnf.setManagementOption("some managementOption");
        aaiPnf.setIpaddressV4Oam("some ipaddressV4Oam");
        aaiPnf.setSwVersion("some swVersion");
        aaiPnf.setInMaint(false);
        aaiPnf.setFrameId("some frameId");
        aaiPnf.setSerialNumber("some serialNumber");
        aaiPnf.setIpaddressV4Loopback0("some ipaddressV4Loopback0");
        aaiPnf.setIpaddressV6Loopback0("some ipaddressV6Loopback0");
        aaiPnf.setIpaddressV4Aim("some ipaddressV4Aim");
        aaiPnf.setIpaddressV6Aim("some ipaddressV6Aim");
        aaiPnf.setIpaddressV6Oam("some ipaddressV6Oam");
        aaiPnf.setInvStatus("some invStatus");
        aaiPnf.setResourceVersion("some resourceVersion");
        aaiPnf.setProvStatus("some provStatus");
        aaiPnf.setNfRole("some nfRole");
        aaiPnf.setRelationshipList(getSomeRelationshipList());
        return aaiPnf;
    }

    private static Relationship getSomeRelationshipList() {
        return new Relationship().addRelationshipItem(
                new RelationshipDict());
    }

    @Test
    void shouldSerializeAaiPnf() {
        String json = new GsonBuilder().create().toJson(getAaiPnfResultModel());

        assertEquals(SOME_AAI_PNF_JSON, json);
    }

    @Test
    void shouldParseAaiPnf() {
        AaiPnfResultModel pnf = new GsonBuilder().create().fromJson(SOME_AAI_PNF_JSON, AaiPnfResultModel.class);

        assertEquals("some pnfName", pnf.getPnfName());
        assertEquals("some pnfName2", pnf.getPnfName2());
        assertEquals("some selflink", pnf.getSelflink());
        assertEquals("some pnfName2Source", pnf.getPnfName2Source());
        assertEquals("some pnfId", pnf.getPnfId());
        assertEquals("some equipType", pnf.getEquipType());
        assertEquals("some equipVendor", pnf.getEquipVendor());
        assertEquals("some equipModel", pnf.getEquipModel());
        assertEquals("some managementOption", pnf.getManagementOption());
        assertEquals("some ipaddressV4Oam", pnf.getIpaddressV4Oam());
        assertEquals("some swVersion", pnf.getSwVersion());
        assertEquals(false, pnf.isInMaint());
        assertEquals("some frameId", pnf.getFrameId());
        assertEquals("some serialNumber", pnf.getSerialNumber());
        assertEquals("some ipaddressV4Loopback0", pnf.getIpaddressV4Loopback0());
        assertEquals("some ipaddressV6Loopback0", pnf.getIpaddressV6Loopback0());
        assertEquals("some ipaddressV4Aim", pnf.getIpaddressV4Aim());
        assertEquals("some ipaddressV6Aim", pnf.getIpaddressV6Aim());
        assertEquals("some ipaddressV6Oam", pnf.getIpaddressV6Oam());
        assertEquals("some invStatus", pnf.getInvStatus());
        assertEquals("some resourceVersion", pnf.getResourceVersion());
        assertEquals("some provStatus", pnf.getProvStatus());
        assertEquals("some nfRole", pnf.getNfRole());
        assertEquals(1, pnf.getRelationshipList().getRelationship().size());
    }

    @Test
    void shouldBePrintable() {
        String s = getAaiPnfResultModel().toString();
        assertThat(s).contains("some pnfName");
        assertThat(s).contains("some pnfName2");
        assertThat(s).contains("some selflink");
        assertThat(s).contains("some pnfName2Source");
        assertThat(s).contains("some pnfId");
        assertThat(s).contains("some equipType");
        assertThat(s).contains("some equipVendor");
        assertThat(s).contains("some equipModel");
        assertThat(s).contains("some managementOption");
        assertThat(s).contains("some ipaddressV4Oam");
        assertThat(s).contains("some swVersion");
        assertThat(s).contains("false");
        assertThat(s).contains("some frameId");
        assertThat(s).contains("some serialNumber");
        assertThat(s).contains("some ipaddressV4Loopback0");
        assertThat(s).contains("some ipaddressV6Loopback0");
        assertThat(s).contains("some ipaddressV4Aim");
        assertThat(s).contains("some ipaddressV6Aim");
        assertThat(s).contains("some ipaddressV6Oam");
        assertThat(s).contains("some invStatus");
        assertThat(s).contains("some resourceVersion");
        assertThat(s).contains("some provStatus");
        assertThat(s).contains("some nfRole");
    }


}