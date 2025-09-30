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

package io.jmix.search.searching

import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.mapping.IndexConfigurationManager
import spock.lang.Specification

import java.util.function.Function


class AbstractSearchQueryConfiguratorTest extends Specification {


    public static final String SAMPLE_MESSAGE_TEXT = "Sample text"

    def "getIndexNamesWithFields. Normal flow"() {
        given:
        IndexConfiguration configuration1 = Mock()
        configuration1.getIndexName() >> "index1"
        IndexConfiguration configuration2 = Mock()
        configuration2.getIndexName() >> "index2"
        IndexConfiguration configuration3 = Mock()
        configuration3.getIndexName() >> "index3"

        Map<IndexConfiguration, Set<String>> fieldsMap = Map.of(
                configuration1, Set.of("field1_1", "field1_2", "field1_3"),
                configuration2, Set.of("field2_1", "field2_2", "field2_3"),
                configuration3, Set.of("field3_1", "field3_2", "field3_3")
        )

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getIndexConfigurationByEntityName("entity1") >> configuration1
        indexConfigurationManager.getIndexConfigurationByEntityName("entity2") >> configuration2
        indexConfigurationManager.getIndexConfigurationByEntityName("entity3") >> configuration3

        and:
        SearchUtils searchUtils = Mock()
        searchUtils.resolveEntitiesAllowedToSearch(_) >> List.of("entity1", "entity2", "entity3")

        and:
        def configurator = new TestSearchQueryConfigurator(searchUtils, indexConfigurationManager)

        when:
        def fields = configurator.getIndexNamesWithFields(
                List.of("entity1", "entity2", "entity3", "entity4"),
                configuration -> fieldsMap.get(configuration)
        )

        then:
        fields == Map.of(
                "index1", Set.of("field1_1", "field1_2", "field1_3"),
                "index2", Set.of("field2_1", "field2_2", "field2_3"),
                "index3", Set.of("field3_1", "field3_2", "field3_3")
        )
    }

    def "getIndexNamesWithFields. Empty list of required entities"() {
        given:
        SearchUtils searchUtils = Mock()
        searchUtils.resolveEntitiesAllowedToSearch(_) >> List.of()

        and:
        def configurator = new TestSearchQueryConfigurator(searchUtils, Mock(IndexConfigurationManager))

        when:
        configurator.getIndexNamesWithFields(
                List.of(),
                configuration -> null
        )

        then:
        def exception = thrown(NoAllowedEntitiesForSearching)
        exception.getMessage() == SAMPLE_MESSAGE_TEXT
    }

    def "getIndexNamesWithFields. Empty list of allowed entities"() {
        given:
        SearchUtils searchUtils = Mock()
        searchUtils.resolveEntitiesAllowedToSearch(_) >> List.of()

        and:
        def configurator = new TestSearchQueryConfigurator(searchUtils, Mock(IndexConfigurationManager))

        when:
        configurator.getIndexNamesWithFields(
                List.of("entity1", "entity2", "entity3", "entity4"),
                configuration -> null
        )

        then:
        def exception = thrown(NoAllowedEntitiesForSearching)
        exception.getMessage() == SAMPLE_MESSAGE_TEXT
    }

    def "getIndexNamesWithFields. Some entities haven't allowed fields"() {
        given:
        IndexConfiguration configuration1 = Mock()
        configuration1.getIndexName() >> "index1"
        IndexConfiguration configuration2 = Mock()
        configuration2.getIndexName() >> "index2"
        IndexConfiguration configuration3 = Mock()
        configuration3.getIndexName() >> "index3"

        Map<IndexConfiguration, Set<String>> fieldsMap = Map.of(
                configuration1, Set.of("field1_1", "field1_2", "field1_3"),
                configuration2, Set.of(),
                configuration3, Set.of("field3_1", "field3_2", "field3_3")
        )

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getIndexConfigurationByEntityName("entity1") >> configuration1
        indexConfigurationManager.getIndexConfigurationByEntityName("entity2") >> configuration2
        indexConfigurationManager.getIndexConfigurationByEntityName("entity3") >> configuration3

        and:
        SearchUtils searchUtils = Mock()
        searchUtils.resolveEntitiesAllowedToSearch(_) >> List.of("entity1", "entity2", "entity3")

        and:
        def configurator = new TestSearchQueryConfigurator(searchUtils, indexConfigurationManager)

        when:
        def fields = configurator.getIndexNamesWithFields(
                List.of("entity1", "entity2", "entity3", "entity4"),
                configuration -> fieldsMap.get(configuration)
        )

        then:
        fields == Map.of(
                "index1", Set.of("field1_1", "field1_2", "field1_3"),
                "index3", Set.of("field3_1", "field3_2", "field3_3")
        )
    }

    def "getIndexNamesWithFields. All entities haven't allowed fields"() {
        given:
        IndexConfiguration configuration1 = Mock()
        configuration1.getIndexName() >> "index1"
        IndexConfiguration configuration2 = Mock()
        configuration2.getIndexName() >> "index2"
        IndexConfiguration configuration3 = Mock()
        configuration3.getIndexName() >> "index3"

        Map<IndexConfiguration, Set<String>> fieldsMap = Map.of(
                configuration1, Set.of(),
                configuration2, Set.of(),
                configuration3, Set.of()
        )

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getIndexConfigurationByEntityName("entity1") >> configuration1
        indexConfigurationManager.getIndexConfigurationByEntityName("entity2") >> configuration2
        indexConfigurationManager.getIndexConfigurationByEntityName("entity3") >> configuration3

        and:
        SearchUtils searchUtils = Mock()
        searchUtils.resolveEntitiesAllowedToSearch(_) >> List.of("entity1", "entity2", "entity3")

        and:
        def configurator = new TestSearchQueryConfigurator(searchUtils, indexConfigurationManager)

        when:
        configurator.getIndexNamesWithFields(
                List.of("entity1", "entity2", "entity3", "entity4"),
                configuration -> fieldsMap.get(configuration)
        )

        then:
        def exception = thrown(NoAllowedEntitiesForSearching)
    }

    private class TestSearchQueryConfigurator extends AbstractSearchQueryConfigurator {
        TestSearchQueryConfigurator(SearchUtils searchUtils, IndexConfigurationManager indexConfigurationManager) {
            super(searchUtils, indexConfigurationManager)
        }

        @Override
        void configureRequest(Object requestBuilder, List entities, Function fieldResolving, TargetQueryBuilder targetQueryBuilder) {

        }
    }
}
