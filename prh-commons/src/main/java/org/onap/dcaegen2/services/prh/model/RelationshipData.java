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
 * RelationshipData
 */
public class RelationshipData {
    @SerializedName("relationship-key")
    private String relationshipKey = null;

    @SerializedName("relationship-value")
    private String relationshipValue = null;

    /**
     * A keyword provided by A&amp;AI to indicate an attribute.
     *
     * @return relationshipKey
     **/
    public String getRelationshipKey() {
        return relationshipKey;
    }

    public void setRelationshipKey(String relationshipKey) {
        this.relationshipKey = relationshipKey;
    }

    /**
     * Value of the attribute.
     *
     * @return relationshipValue
     **/
    public String getRelationshipValue() {
        return relationshipValue;
    }

    public void setRelationshipValue(String relationshipValue) {
        this.relationshipValue = relationshipValue;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RelationshipData {\n");

        sb.append("    relationshipKey: ").append(toIndentedString(relationshipKey)).append("\n");
        sb.append("    relationshipValue: ").append(toIndentedString(relationshipValue)).append("\n");
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