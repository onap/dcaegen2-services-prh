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

package org.onap.dcaegen2.services.prh.model.queries;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.onap.dcaegen2.services.prh.model.queries.ImmutableNamedNode.Builder;

/**
 * Adapter that reads AAI Custom query format
 * <br/>
 * <br/>
 * Example node from AAI query:
 * <pre>
 * {
 *      "pnf": {
 *          "pnf-name": "foo",
 *          "in-maint": false,
 *          "resource-version": "1559732370039"
 *      }
 * }
 * </pre>
 *
 * @see NamedNode
 * @see <a href="https://docs.onap.org/en/dublin/submodules/aai/aai-common.git/docs/AAI%20REST%20API%20Documentation/customQueries.html">AAI
 *  Custom queries</a></a>
 */
public class NamedNodeAdapter extends TypeAdapter<NamedNode> {

    @Override
    public void write(JsonWriter jsonWriter, NamedNode namedNode) {
        throw new UnsupportedOperationException("This model is read only!");
    }

    @Override
    public NamedNode read(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        Builder nodeBuilder = ImmutableNamedNode.builder().name(jsonReader.nextName());
        readProperties(jsonReader, nodeBuilder);
        jsonReader.endObject();

        return nodeBuilder.build();
    }

    private void readProperties(JsonReader jsonReader, Builder nodeBuilder) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            switch (jsonReader.peek()) {
                case STRING:
                    nodeBuilder.putProperties(key, jsonReader.nextString());
                    break;
                case NUMBER:
                    nodeBuilder.putProperties(key, jsonReader.nextInt());
                    break;
                case BOOLEAN:
                    nodeBuilder.putProperties(key, jsonReader.nextBoolean());
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }
}