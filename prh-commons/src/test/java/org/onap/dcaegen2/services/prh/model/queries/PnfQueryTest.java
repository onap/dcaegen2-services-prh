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
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.model.utils.PrhModelAwareGsonBuilder;

class PnfQueryTest {

    @Test
    void shouldParseToCorrectJson() throws IOException {
        // given
        PnfQuery pnfQuery = new PnfQuery("foo");

        // when
        Gson gson = PrhModelAwareGsonBuilder.createGson();

        // then
        String json = gson.toJson(pnfQuery);
        assertThat(json).isEqualToIgnoringWhitespace(getJsonFromFile("pnf_query_request.json"));
    }

    private String getJsonFromFile(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
            requireNonNull(getClass().getClassLoader().getResourceAsStream(file))))) {
            return br.lines().collect(joining(System.lineSeparator()));
        }
    }
    
}