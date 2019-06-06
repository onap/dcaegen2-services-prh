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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.model.utils.PrhModelAwareGsonBuilder;

class NamedNodesTest {

    @Test
    void shouldParseJsonToNamedNodes() {
        // given
        InputStreamReader inputStream = getJsonFromFile("aai_custom_query.json");
        Gson gson = PrhModelAwareGsonBuilder.createGson();

        // when
        NamedNodes nodes = gson.fromJson(inputStream, NamedNodes.class);

        // then
        assertThat(nodes.results().size()).isEqualTo(3);
        assertThat(nodes.results().get(0))
            .matches(node -> "pnf".equals(node.name()))
            .matches(node -> node.properties().get("pnf-name").equals("foo"));
        assertThat(nodes.results().get(1))
            .matches(node -> "logical-link".equals(node.name()))
            .matches(node -> node.properties().get("link-name").equals("bar"));
        assertThat(nodes.results().get(2))
            .matches(node -> "service-instance".equals(node.name()))
            .matches(node -> node.properties().get("service-instance-name").equals("baz"));
    }

    private InputStreamReader getJsonFromFile(String file) {
        return new InputStreamReader(requireNonNull(NamedNodesTest.class.getClassLoader().getResourceAsStream(file)));
    }
}