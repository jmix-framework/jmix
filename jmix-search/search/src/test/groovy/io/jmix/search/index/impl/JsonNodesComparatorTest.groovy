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

package io.jmix.search.index.impl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import spock.lang.Specification


class JsonNodesComparatorTest extends Specification {

    private static String JSON_STRING_TWO_PARAMETERS = "{\n" +
            "  \"aField\": \"1234\",\n" +
            "  \"bField\": \"some text\"\n" +
            "}"

    private static String JSON_STRING_WITH_NULL = "{\n" +
            "  \"aField\": \"1234\",\n" +
            "  \"bField\": null\n" +
            "}"
    private static String JSON_STRING_THREE_PARAMETERS = "{\n" +
            "  \"aField\": \"1234\",\n" +
            "  \"bField\": \"some text\",\n" +
            "  \"cField\": \"some text 2\"\n" +
            "}"

    private static String BIGGER_JSON = "{\n" +
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
            "}"

    private static String SMALLER_JSON = "{\n" +
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
            "}"


    def "comparing the same JSONs"() {
        given:
        JsonNodesComparator comparator = new JsonNodesComparator()

        expect:
        comparator.nodeContains(toObjectNode(JSON_STRING_TWO_PARAMETERS), toObjectNode(JSON_STRING_TWO_PARAMETERS))
    }

    def "container node contains an extra node"() {
        given:
        JsonNodesComparator comparator = new JsonNodesComparator();

        expect:
        comparator.nodeContains(toObjectNode(JSON_STRING_THREE_PARAMETERS), toObjectNode(JSON_STRING_TWO_PARAMETERS))
    }

    void "content node contains an extra node"() {
        given:
        JsonNodesComparator comparator = new JsonNodesComparator();

        expect:
        !comparator.nodeContains(toObjectNode(JSON_STRING_TWO_PARAMETERS), toObjectNode(JSON_STRING_THREE_PARAMETERS))
    }

    void "the same complex nodes ara equal"() {
        given:
        JsonNodesComparator comparator = new JsonNodesComparator();

        expect:
        comparator.nodeContains(toObjectNode(BIGGER_JSON), toObjectNode(BIGGER_JSON))
    }

    void "the bigger JSON contains the smaller one"() {
        given:
        JsonNodesComparator comparator = new JsonNodesComparator();

        expect:
        comparator.nodeContains(toObjectNode(BIGGER_JSON), toObjectNode(SMALLER_JSON))
    }

    void "the smaller JSON can't contain the smaller json"() {
        given:
        JsonNodesComparator comparator = new JsonNodesComparator();

        expect:
        !comparator.nodeContains(toObjectNode(SMALLER_JSON), toObjectNode(BIGGER_JSON))
    }

    void "the same jsons with nulls are equal"() {
        given:
        JsonNodesComparator comparator = new JsonNodesComparator();

        expect:
        comparator.nodeContains(toObjectNode(JSON_STRING_WITH_NULL), toObjectNode(JSON_STRING_WITH_NULL))
    }


    void "the json with the null is not equal to the other json without the null"() {
        given:
        JsonNodesComparator comparator = new JsonNodesComparator();

        expect:
        !comparator.nodeContains(toObjectNode(JSON_STRING_WITH_NULL), toObjectNode(JSON_STRING_TWO_PARAMETERS))
    }

    void "the json without the null is not equal to the other json with the null"() {
        given:
        JsonNodesComparator comparator = new JsonNodesComparator();

        expect:
        !comparator.nodeContains(toObjectNode(JSON_STRING_TWO_PARAMETERS), toObjectNode(JSON_STRING_WITH_NULL))
    }

    private static ObjectNode toObjectNode(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper()
            return (ObjectNode) mapper.readTree(json)
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e)
        }
    }

}
