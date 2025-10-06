/*
 * Copyright 2025 Haulmont.
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

package io.jmix.searchopensearch.searching.strategy.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.jmix.search.searching.impl.SearchModelAnalyzer
import jakarta.json.spi.JsonProvider
import org.opensearch.client.json.JsonpMapper
import org.opensearch.client.json.JsonpSerializable
import org.opensearch.client.json.jackson.JacksonJsonpMapper
import org.opensearch.client.opensearch._types.query_dsl.Operator
import spock.lang.Specification

import static java.nio.charset.StandardCharsets.UTF_8

class OpenSearchQueryConfiguratorTest extends Specification {

    def "configureRequest. multiple indexes"() {
        given:
        Map<String, Set<String>> indexesWithFields = new LinkedHashMap<>()

        indexesWithFields.put("index1", createLinkedSet("field1_1"))
        indexesWithFields.put("index2", createLinkedSet("field2_1", "field2_2", "field2_3"))

        and:
        def configurator = new OpenSearchQueryConfigurator(Mock(SearchModelAnalyzer))

        when:
        def query = configurator.createQuery(
                (b, fields) ->
                        b.multiMatch(m ->
                                m.fields(fields).query("search text").operator(Operator.Or)
                        )
                , indexesWithFields)
        .build()

        then:
        jsonEquals(toJson(query), readResourceAsString("request_multiple_indexes"))
    }

    def "configureRequest. single index"() {
        given:
        Map<String, Set<String>> indexesWithFields = new LinkedHashMap<>()
        indexesWithFields.put("index1", createLinkedSet("field1_1", "field1_2", "field1_3"))

        and:
        def configurator = new OpenSearchQueryConfigurator(Mock(SearchModelAnalyzer))

        when:
        def query = configurator.createQuery(
                (b, fields) ->
                        b.multiMatch(m ->
                                m.fields(fields).query("search text").operator(Operator.Or)
                        )
                , indexesWithFields).build()

        then:
        jsonEquals(toJson(query), readResourceAsString("request_single_index"))
    }

    private static LinkedHashSet<String> createLinkedSet(String... fields) {
        new LinkedHashSet<>(List.of(fields))
    }

    private static String toJson(JsonpSerializable obj) {
        JsonpMapper mapper = new JacksonJsonpMapper()
        StringWriter sw = new StringWriter()
        def generator = JsonProvider.provider().createGenerator(sw)
        obj.serialize(generator, mapper)
        generator.close()
        return sw.toString()
    }

    private static boolean jsonEquals(String a, String b) {
        def om = new ObjectMapper()
        return om.readTree(a) == om.readTree(b)
    }

    private static String readResourceAsString(String resourcePath) {
        InputStream is = this.getClassLoader().getResourceAsStream("requests/" + resourcePath + ".json")
        assert is != null: "Resource not found: $resourcePath"
        try {
            return new String(is.readAllBytes(), UTF_8)
        } finally {
            is.close()
        }
    }
}
