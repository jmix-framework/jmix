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

package io.jmix.searchelasticsearch.searching.strategy.impl

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.json.JsonpMapper
import co.elastic.clients.json.JsonpSerializable
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import com.fasterxml.jackson.databind.ObjectMapper
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.searching.IndexSearchRequestScope
import io.jmix.search.searching.SearchRequestScopeProvider
import jakarta.json.spi.JsonProvider
import spock.lang.Specification

import static java.nio.charset.StandardCharsets.*

class ElasticSearchQueryConfigurerTest extends Specification {
    def "configureRequest. multiple indexes"() {
        given:
        List<IndexSearchRequestScope> scopes = List.of(
                createScope("index1", "field1_1"),
                createScope("index2", "field2_1", "field2_2", "field2_3")
        )

        and:
        def configurator = new ElasticSearchQueryConfigurer(Mock(SearchRequestScopeProvider))

        when:
        def query = configurator.createQuery(
                (b, scope) ->
                        b.multiMatch(m ->
                                m.fields(scope.getFieldList()).query("search text").operator(Operator.Or)
                        )
                , scopes).build()

        then:
        jsonEquals(toJson(query), readResourceAsString("request_multiple_indexes"))
    }

    def "configureRequest. single index"() {
        given:
        List<IndexSearchRequestScope> scopes = List.of(
                createScope("index1", "field1_1", "field1_2", "field1_3"),
        )


        and:
        def configurator = new ElasticSearchQueryConfigurer(Mock(SearchRequestScopeProvider))

        when:
        def query = configurator.createQuery(
                (b, scope) ->
                        b.multiMatch(m ->
                                m.fields(scope.getFieldList()).query("search text").operator(Operator.Or)
                        )
                , scopes).build()

        then:
        jsonEquals(toJson(query), readResourceAsString("request_single_index"))
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
        InputStream is = ElasticSearchQueryConfigurerTest.class.classLoader.getResourceAsStream("requests/" + resourcePath + ".json")
        assert is != null: "Resource not found: $resourcePath"
        try {
            return new String(is.readAllBytes(), UTF_8)
        } finally {
            is.close()
        }
    }

    private IndexSearchRequestScope createScope(String indexName, String... fields) {
        IndexConfiguration indexConfiguration = Mock()
        indexConfiguration.getIndexName() >> indexName
        return new IndexSearchRequestScope(indexConfiguration, new LinkedHashSet<>(List.of(fields)))
    }
}
