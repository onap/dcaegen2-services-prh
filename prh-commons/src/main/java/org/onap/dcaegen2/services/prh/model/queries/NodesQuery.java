package org.onap.dcaegen2.services.prh.model.queries;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * Model for AAI Custom query
 * <br/>
 * <br/>
 * Example response:
 * <br/>
 * <pre>
 *     {
 *     "results": [
 *         {
 *             "pnf": {
 *                 "pnf-name": "foo",
 *                 "in-maint": false,
 *                 "resource-version": "1559732370039"
 *             }
 *         },
 *         {
 *             "logical-link": {
 *                 "link-name": "bar",
 *                 "in-maint": false,
 *                 "link-type": "attachment-point",
 *                 "resource-version": "1559732357143"
 *             }
 *         },
 *         {
 *             "service-instance": {
 *                 "service-instance-id": "baz",
 *                 "service-instance-name": "pnf-service-name",
 *                 "resource-version": "1559732385933",
 *                 "orchestration-status": "ACTIVE"
 *             }
 *         }
 *     ]
 * }
 * </pre>
 * @see NamedNode
 * @see <a href="https://docs.onap.org/en/dublin/submodules/aai/aai-common.git/docs/AAI%20REST%20API%20Documentation/customQueries.html">AAI
 * Custom queries</a></a>
 */
@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true)
public interface NodesQuery {

    @SerializedName(value = "results")
    List<NamedNode> results();
}