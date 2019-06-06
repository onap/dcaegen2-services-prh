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