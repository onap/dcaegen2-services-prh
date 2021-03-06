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

package org.onap.dcaegen2.services.prh.adapter.aai.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.model.RelationshipData;
import org.onap.dcaegen2.services.prh.model.RelationshipDict;
import org.onap.dcaegen2.services.prh.model.utils.PrhModelAwareGsonBuilder;

class AaiPnfResultModelTest {

    @Test
    void shouldParseAaiPnf() {
        Gson gson = PrhModelAwareGsonBuilder.createGson();
        AaiPnfResultModel pnf = gson.fromJson(new InputStreamReader(Objects.requireNonNull(
                ClassLoader.getSystemResourceAsStream("some_aai_pnf.json"))), AaiPnfResultModel.class);

        assertThat(pnf.getPnfName()).isEqualTo("some pnfName");
        assertThat(pnf.getPnfName2()).isEqualTo("some pnfName2");
        assertThat(pnf.getSelflink()).isEqualTo("some selflink");
        assertThat(pnf.getPnfName2Source()).isEqualTo("some pnfName2Source");
        assertThat(pnf.getPnfId()).isEqualTo("some pnfId");
        assertThat(pnf.getEquipType()).isEqualTo("some equipType");
        assertThat(pnf.getEquipVendor()).isEqualTo("some equipVendor");
        assertThat(pnf.getEquipModel()).isEqualTo("some equipModel");
        assertThat(pnf.getManagementOption()).isEqualTo("some managementOption");
        assertThat(pnf.getIpaddressV4Oam()).isEqualTo("some ipaddressV4Oam");
        assertThat(pnf.getSwVersion()).isEqualTo("some swVersion");
        assertThat(pnf.isInMaint()).isFalse();
        assertThat(pnf.getFrameId()).isEqualTo("some frameId");
        assertThat(pnf.getSerialNumber()).isEqualTo("some serialNumber");
        assertThat(pnf.getIpaddressV4Loopback0()).isEqualTo("some ipaddressV4Loopback0");
        assertThat(pnf.getIpaddressV6Loopback0()).isEqualTo("some ipaddressV6Loopback0");
        assertThat(pnf.getIpaddressV4Aim()).isEqualTo("some ipaddressV4Aim");
        assertThat(pnf.getIpaddressV6Aim()).isEqualTo("some ipaddressV6Aim");
        assertThat(pnf.getIpaddressV6Oam()).isEqualTo("some ipaddressV6Oam");
        assertThat(pnf.getInvStatus()).isEqualTo("some invStatus");
        assertThat(pnf.getResourceVersion()).isEqualTo("some resourceVersion");
        assertThat(pnf.getProvStatus()).isEqualTo("some provStatus");
        assertThat(pnf.getNfRole()).isEqualTo("some nfRole");

        assertThat(pnf.getRelationshipList().getRelationship()).hasSize(1);
        RelationshipDict relationshipDict = pnf.getRelationshipList().getRelationship().get(0);
        assertThat(relationshipDict.getRelatedTo()).isEqualTo("some relatedTo");
        assertThat(relationshipDict.getRelationshipData()).hasSize(1);
        RelationshipData relationshipData = relationshipDict.getRelationshipData().get(0);
        assertThat(relationshipData.getRelationshipKey()).isEqualTo("some relationshipKey");
        assertThat(relationshipData.getRelationshipValue()).isEqualTo("some relationshipValue");
    }

    @Test
    void shouldProvideEmptyRelationshipListForEmptyJson() {
        Gson gson = PrhModelAwareGsonBuilder.createGson();
        AaiPnfResultModel pnf = gson.fromJson("{}", AaiPnfResultModel.class);
        assertThat(pnf.getRelationshipList()).isNotNull();
        assertThat(pnf.getRelationshipList().getRelationship()).isEmpty();
    }

    @Test
    void shouldIgnoreUnexpectedFieldsInJson() {
        Gson gson = PrhModelAwareGsonBuilder.createGson();
        gson.fromJson("{\"foo\":\"bar\"}", AaiPnfResultModel.class);
    }

}