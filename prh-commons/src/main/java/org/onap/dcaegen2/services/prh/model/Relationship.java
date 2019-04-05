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

import java.util.ArrayList;
import java.util.List;

/**
 * dictionary of relationship
 */
public class Relationship {
    @SerializedName("relationship")
    private List<RelationshipDict> relationship = null;

    public Relationship relationship(List<RelationshipDict> relationship) {
        this.relationship = relationship;
        return this;
    }

    public Relationship addRelationshipItem(RelationshipDict relationshipItem) {
        if (this.relationship == null) {
            this.relationship = new ArrayList<>();
        }
        this.relationship.add(relationshipItem);
        return this;
    }

    /**
     * Get relationship
     *
     * @return relationship
     **/
    public List<RelationshipDict> getRelationship() {
        return relationship;
    }

    public void setRelationship(List<RelationshipDict> relationship) {
        this.relationship = relationship;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Relationship {\n");

        sb.append("    relationship: ").append(toIndentedString(relationship)).append("\n");
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