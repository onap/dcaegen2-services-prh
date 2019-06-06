package org.onap.dcaegen2.services.prh.model.queries;

import java.util.Map;
import org.immutables.value.Value;

/**
 * @see NamedNodeAdapter
 * @see NodesQuery
 */
@Value.Immutable
public interface NamedNode {
    String name();
    Map<String, Object> properties();
}