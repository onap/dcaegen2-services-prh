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
import java.util.Objects;

/**
 * RelationshipDict
 */
public class RelationshipDict {
    @SerializedName("related-to")
    private String relatedTo = null;

    @SerializedName("relationship-label")
    private String relationshipLabel = null;

    @SerializedName("related-link")
    private String relatedLink = null;

    @SerializedName("relationship-data")
    private List<RelationshipData> relationshipData = null;

    public RelationshipDict relatedTo(String relatedTo) {
        this.relatedTo = relatedTo;
        return this;
    }

    /**
     * A keyword provided by A&amp;AI to indicate type of node.
     *
     * @return relatedTo
     **/
    public String getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(String relatedTo) {
        this.relatedTo = relatedTo;
    }

    public RelationshipDict relationshipLabel(String relationshipLabel) {
        this.relationshipLabel = relationshipLabel;
        return this;
    }

    /**
     * The edge label for this relationship.
     *
     * @return relationshipLabel
     **/
    public String getRelationshipLabel() {
        return relationshipLabel;
    }

    public void setRelationshipLabel(String relationshipLabel) {
        this.relationshipLabel = relationshipLabel;
    }

    public RelationshipDict relatedLink(String relatedLink) {
        this.relatedLink = relatedLink;
        return this;
    }

    /**
     * URL to the object in A&amp;AI.
     *
     * @return relatedLink
     **/
    public String getRelatedLink() {
        return relatedLink;
    }

    public void setRelatedLink(String relatedLink) {
        this.relatedLink = relatedLink;
    }

    public RelationshipDict relationshipData(List<RelationshipData> relationshipData) {
        this.relationshipData = relationshipData;
        return this;
    }

    public RelationshipDict addRelationshipDataItem(RelationshipData relationshipDataItem) {
        if (this.relationshipData == null) {
            this.relationshipData = new ArrayList<RelationshipData>();
        }
        this.relationshipData.add(relationshipDataItem);
        return this;
    }

    /**
     * Get relationshipData
     *
     * @return relationshipData
     **/
    public List<RelationshipData> getRelationshipData() {
        return relationshipData;
    }

    public void setRelationshipData(List<RelationshipData> relationshipData) {
        this.relationshipData = relationshipData;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelationshipDict relationshipDict = (RelationshipDict) o;
        return Objects.equals(this.relatedTo, relationshipDict.relatedTo) &&
                Objects.equals(this.relationshipLabel, relationshipDict.relationshipLabel) &&
                Objects.equals(this.relatedLink, relationshipDict.relatedLink) &&
                Objects.equals(this.relationshipData, relationshipDict.relationshipData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relatedTo, relationshipLabel, relatedLink, relationshipData);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RelationshipDict {\n");

        sb.append("    relatedTo: ").append(toIndentedString(relatedTo)).append("\n");
        sb.append("    relationshipLabel: ").append(toIndentedString(relationshipLabel)).append("\n");
        sb.append("    relatedLink: ").append(toIndentedString(relatedLink)).append("\n");
        sb.append("    relationshipData: ").append(toIndentedString(relationshipData)).append("\n");
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