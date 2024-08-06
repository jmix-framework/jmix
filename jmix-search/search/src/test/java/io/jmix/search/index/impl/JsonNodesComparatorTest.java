/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.search.index.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonNodesComparatorTest {
    static String JSON_STRING_TWO_PARAMETERS = "{\n" +
            "  \"aField\": \"1234\",\n" +
            "  \"bField\": \"some text\"\n" +
            "}";
    static String JSON_STRING_THREE_PARAMETERS = "{\n" +
            "  \"aField\": \"1234\",\n" +
            "  \"bField\": \"some text\",\n" +
            "  \"cField\": \"some text 2\"\n" +
            "}";

    @Test
    void nodeContains() {

        JsonNodesComparator comparator = new JsonNodesComparator();
        assertTrue(comparator.nodeContains(toObjectNode(JSON_STRING_TWO_PARAMETERS), toObjectNode(JSON_STRING_TWO_PARAMETERS)));
    }

    static ObjectNode toObjectNode(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return (ObjectNode) mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}