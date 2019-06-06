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

package org.onap.dcaegen2.services.prh.model.bulk;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.prh.model.bulk.Transaction.Action.PUT;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.model.utils.PrhModelAwareGsonBuilder;
import org.springframework.util.StreamUtils;

class TransactionsTest {

    @Test
    void shouldBuildTransactionRequest() throws IOException {
        // given
        JsonObject payload = new JsonObject();
        payload.addProperty("link-name", "foo");

        ImmutableTransactions transactions = ImmutableTransactions
            .builder()
            .addOperations(ImmutableTransaction.builder()
                .action(PUT)
                .uri("/network/logical-links/logical-link/foo")
                .body(payload)
                .build())
            .build();

        // when
        Gson gson = PrhModelAwareGsonBuilder.createGson();
        String transaction = gson.toJson(transactions);

        // then
        String expectedJson = StreamUtils.copyToString(getJsonFromFile("transaction.json"), UTF_8);
        assertThat(transaction).isEqualToIgnoringWhitespace(expectedJson);
    }

    private InputStream getJsonFromFile(String file) {
        return requireNonNull(TransactionsTest.class.getClassLoader().getResourceAsStream(file));
    }
}