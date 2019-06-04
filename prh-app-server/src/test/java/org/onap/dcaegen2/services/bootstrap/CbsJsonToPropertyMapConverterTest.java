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

package org.onap.dcaegen2.services.bootstrap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CbsJsonToPropertyMapConverterTest {

    private static final JsonObject SOME_JSON_FROM_CBS = asJsonObject("{\n" +
            "  \"config\": {\n" +
            "    \"someStringProp\": \"foo\",\n" +
            "    \"someNumericalProp\": 123,\n" +
            "    \"someBooleanProp\": true,\n" +
            "    \"someObjectProp\": {\n" +
            "      \"bar\": \"baz\"\n" +
            "    }\n" +
            "  }\n" +
            "}"
    );

    private CbsJsonToPropertyMapConverter cbsJsonToPropertyMapConverter = new CbsJsonToPropertyMapConverter();

    @Test
    void shouldExtractPrimitivePropertiesToSimpleJavaTypes() {
        Map<String, Object> map = cbsJsonToPropertyMapConverter.convertToMap(SOME_JSON_FROM_CBS);

        assertThat(map).containsEntry("someStringProp", "foo");
        assertThat(map).containsEntry("someNumericalProp", 123L);
        assertThat(map).containsEntry("someBooleanProp", true);

    }

    @Test
    void shouldLeaveComplexPropertiesAsJsonTypes() {
        Map<String, Object> map = cbsJsonToPropertyMapConverter.convertToMap(SOME_JSON_FROM_CBS);

        assertThat(map).hasEntrySatisfying("someObjectProp", hasClass(JsonObject.class));
    }

    @Test
    void shouldProduceDescriptiveExceptionInCaseExpectedRootElementInCbsJsonIsMissing() {
        assertThatThrownBy(() -> cbsJsonToPropertyMapConverter.convertToMap(asJsonObject("{}")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing expected 'config' property in json from CBS.");
    }


    private static JsonObject asJsonObject(String jsonString) {
        return new JsonParser().parse(jsonString).getAsJsonObject();
    }

    private static Condition<Object> hasClass(Class clazz) {
        return new Condition<Object>(clazz.getCanonicalName()){
            public boolean matches(Object value) {
                return value.getClass().equals(clazz);
            }
        };
    }
}