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

    static String JSON_STRING_WITH_NULL = "{\n" +
            "  \"aField\": \"1234\",\n" +
            "  \"bField\": null\n" +
            "}";
    static String JSON_STRING_THREE_PARAMETERS = "{\n" +
            "  \"aField\": \"1234\",\n" +
            "  \"bField\": \"some text\",\n" +
            "  \"cField\": \"some text 2\"\n" +
            "}";

    static String BIGGER_JSON = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"_instance_name\": {\n" +
            "        \"type\": \"text\"\n" +
            "      },\n" +
            "      \"customer\": {\n" +
            "        \"properties\": {\n" +
            "          \"lastName\": {\n" +
            "            \"type\": \"text\"\n" +
            "          },\n" +
            "          \"status\": {\n" +
            "            \"type\": \"text\"\n" +
            "          }\n" +
            "        }\n" +
            "      },\n" +
            "      \"description\": {\n" +
            "        \"type\": \"text\"\n" +
            "      },\n" +
            "      \"number\": {\n" +
            "        \"type\": \"text\"\n" +
            "      },\n" +
            "      \"product\": {\n" +
            "        \"type\": \"text\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    static String SMALLER_JSON = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"_instance_name\": {\n" +
            "        \"type\": \"text\"\n" +
            "      },\n" +
            "      \"customer\": {\n" +
            "        \"properties\": {\n" +
            "          \"lastName\": {\n" +
            "            \"type\": \"text\"\n" +
            "          },\n" +
            "          \"status\": {\n" +
            "            \"type\": \"text\"\n" +
            "          }\n" +
            "        }\n" +
            "      },\n" +
            "      \"number\": {\n" +
            "        \"type\": \"text\"\n" +
            "      },\n" +
            "      \"product\": {\n" +
            "        \"type\": \"text\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @Test
    void nodeContains() {

        JsonNodesComparator comparator = new JsonNodesComparator();
        assertTrue(comparator.nodeContains(toObjectNode(JSON_STRING_TWO_PARAMETERS), toObjectNode(JSON_STRING_TWO_PARAMETERS)));
    }

    @Test
    void nodeContains_2() {

        JsonNodesComparator comparator = new JsonNodesComparator();
        assertTrue(comparator.nodeContains(toObjectNode(JSON_STRING_THREE_PARAMETERS), toObjectNode(JSON_STRING_TWO_PARAMETERS)));
    }

    @Test
    void nodeContains_3() {

        JsonNodesComparator comparator = new JsonNodesComparator();
        assertFalse(comparator.nodeContains(toObjectNode(JSON_STRING_TWO_PARAMETERS), toObjectNode(JSON_STRING_THREE_PARAMETERS)));
    }

    @Test
    void nodeContains_4() {
        JsonNodesComparator comparator = new JsonNodesComparator();
        assertTrue(comparator.nodeContains(toObjectNode(BIGGER_JSON), toObjectNode(BIGGER_JSON)));
    }

    @Test
    void nodeContains_5() {
        JsonNodesComparator comparator = new JsonNodesComparator();
        assertTrue(comparator.nodeContains(toObjectNode(BIGGER_JSON), toObjectNode(SMALLER_JSON)));
    }

    @Test
    void nodeContains_6() {
        JsonNodesComparator comparator = new JsonNodesComparator();
        assertFalse(comparator.nodeContains(toObjectNode(SMALLER_JSON), toObjectNode(BIGGER_JSON)));
    }

    @Test
    void nodeContains_7() {
        JsonNodesComparator comparator = new JsonNodesComparator();
        assertTrue(comparator.nodeContains(toObjectNode(JSON_STRING_WITH_NULL), toObjectNode(JSON_STRING_WITH_NULL)));
    }

    @Test
    void nodeContains_8() {
        JsonNodesComparator comparator = new JsonNodesComparator();
        assertFalse(comparator.nodeContains(toObjectNode(JSON_STRING_WITH_NULL), toObjectNode(JSON_STRING_TWO_PARAMETERS)));
    }

    @Test
    void nodeContains_9() {
        JsonNodesComparator comparator = new JsonNodesComparator();
        assertFalse(comparator.nodeContains(toObjectNode(JSON_STRING_TWO_PARAMETERS), toObjectNode(JSON_STRING_WITH_NULL)));
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