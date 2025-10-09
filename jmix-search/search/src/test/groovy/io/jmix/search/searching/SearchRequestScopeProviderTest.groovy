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

import static java.util.Collections.emptyList

class SearchRequestScopeProviderTest extends Specification {

    def "getIndexesWithFields. all entities have configuration. User hav full access to the entities"() {
        given:
        IndexConfiguration configuration1 = Mock()
        configuration1.getIndexName() >> "index1"
        IndexConfiguration configuration2 = Mock()
        configuration2.getIndexName() >> "index2"
        IndexConfiguration configuration3 = Mock()
        configuration3.getIndexName() >> "index3"

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getAllIndexedEntities() >> ["entity1", "entity2", "entity3"]
        indexConfigurationManager.getIndexConfigurationByEntityName("entity1") >> configuration1
        indexConfigurationManager.getIndexConfigurationByEntityName("entity2") >> configuration2
        indexConfigurationManager.getIndexConfigurationByEntityName("entity3") >> configuration3

        and:
        SearchSecurityDecorator securityDecorator = Mock()
        securityDecorator.resolveEntitiesAllowedToSearch(_) >> List.of("entity1", "entity2", "entity3")

        and:
        def fieldsResolver = Mock(SearchFieldsProvider)
        def subfieldsProvider = Mock(VirtualSubfieldsProvider)
        fieldsResolver.resolveFields(configuration1, subfieldsProvider) >> Set.of("field1_1", "field1_2", "field1_3")
        fieldsResolver.resolveFields(configuration2, subfieldsProvider) >> Set.of("field2_1", "field2_2", "field2_3")
        fieldsResolver.resolveFields(configuration3, subfieldsProvider) >> Set.of("field3_1", "field3_2", "field3_3")

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)


        when:
        def fields = analyzer.getSearchRequestScope(
                ["entity1", "entity2", "entity3", "entity4"],
                subfieldsProvider
        )

        then:
        fields == List.of(
                new IndexSearchRequestScope(configuration1, Set.of("field1_1", "field1_2", "field1_3")),
                new IndexSearchRequestScope(configuration2, Set.of("field2_1", "field2_2", "field2_3")),
                new IndexSearchRequestScope(configuration3, Set.of("field3_1", "field3_2", "field3_3"))
        )
    }

    def "getIndexNamesWithFields. Empty list of required entities"() {
        given:
        IndexConfiguration configuration1 = Mock()
        configuration1.getIndexName() >> "index1"
        IndexConfiguration configuration2 = Mock()
        configuration2.getIndexName() >> "index2"
        IndexConfiguration configuration3 = Mock()
        configuration3.getIndexName() >> "index3"

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getAllIndexedEntities() >> ["entity1", "entity2", "entity3"]
        indexConfigurationManager.getIndexConfigurationByEntityName("entity1") >> configuration1
        indexConfigurationManager.getIndexConfigurationByEntityName("entity2") >> configuration2
        indexConfigurationManager.getIndexConfigurationByEntityName("entity3") >> configuration3

        and:
        SearchSecurityDecorator securityDecorator = Mock()
        securityDecorator.resolveEntitiesAllowedToSearch(_) >> List.of("entity1", "entity2", "entity3")

        and:
        def fieldsResolver = Mock(SearchFieldsProvider)
        def subfieldsProvider = Mock(VirtualSubfieldsProvider)
        fieldsResolver.resolveFields(configuration1, subfieldsProvider) >> Set.of("field1_1", "field1_2", "field1_3")
        fieldsResolver.resolveFields(configuration2, subfieldsProvider) >> Set.of("field2_1", "field2_2", "field2_3")
        fieldsResolver.resolveFields(configuration3, subfieldsProvider) >> Set.of("field3_1", "field3_2", "field3_3")

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)


        when:
        def fields = analyzer.getSearchRequestScope(
                emptyList(),
                subfieldsProvider
        )

        then:
        fields == List.of(
                new IndexSearchRequestScope(configuration1, Set.of("field1_1", "field1_2", "field1_3")),
                new IndexSearchRequestScope(configuration2, Set.of("field2_1", "field2_2", "field2_3")),
                new IndexSearchRequestScope(configuration3, Set.of("field3_1", "field3_2", "field3_3"))
        )
    }

    def "getIndexNamesWithFields. Empty list of allowed entities"() {
        given:
        IndexConfiguration configuration1 = Mock()
        configuration1.getIndexName() >> "index1"
        IndexConfiguration configuration2 = Mock()
        configuration2.getIndexName() >> "index2"
        IndexConfiguration configuration3 = Mock()
        configuration3.getIndexName() >> "index3"

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getAllIndexedEntities() >> ["entity1", "entity2", "entity3"]

        and:
        SearchSecurityDecorator securityDecorator = Mock()
        securityDecorator.resolveEntitiesAllowedToSearch(_) >> emptyList()

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, Mock(SearchFieldsProvider))


        when:
        def fields = analyzer.getSearchRequestScope(
                ["entity1", "entity2", "entity3"],
                Mock(VirtualSubfieldsProvider)
        )

        then:
        fields.isEmpty()
    }

    def "getIndexNamesWithFields. Some entities haven't allowed fields"() {
        given:
        IndexConfiguration configuration1 = Mock()
        configuration1.getIndexName() >> "index1"
        IndexConfiguration configuration2 = Mock()
        configuration2.getIndexName() >> "index2"
        IndexConfiguration configuration3 = Mock()
        configuration3.getIndexName() >> "index3"

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getAllIndexedEntities() >> ["entity1", "entity2", "entity3"]
        indexConfigurationManager.getIndexConfigurationByEntityName("entity1") >> configuration1
        indexConfigurationManager.getIndexConfigurationByEntityName("entity2") >> configuration2
        indexConfigurationManager.getIndexConfigurationByEntityName("entity3") >> configuration3

        and:
        SearchSecurityDecorator securityDecorator = Mock()
        securityDecorator.resolveEntitiesAllowedToSearch(_) >> List.of("entity1", "entity2", "entity3")

        and:
        def fieldsResolver = Mock(SearchFieldsProvider)
        def subfieldsProvider = Mock(VirtualSubfieldsProvider)
        fieldsResolver.resolveFields(configuration1, subfieldsProvider) >> Set.of("field1_1", "field1_2", "field1_3")
        fieldsResolver.resolveFields(configuration2, subfieldsProvider) >> Set.of()
        fieldsResolver.resolveFields(configuration3, subfieldsProvider) >> Set.of("field3_1", "field3_2", "field3_3")

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)

        when:
        def fields = analyzer.getSearchRequestScope(
                List.of("entity1", "entity2", "entity3", "entity4"),
                subfieldsProvider
        )

        then:
        fields == List.of(
                new IndexSearchRequestScope(configuration1, Set.of("field1_1", "field1_2", "field1_3")),
                new IndexSearchRequestScope(configuration3, Set.of("field3_1", "field3_2", "field3_3"))
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

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getAllIndexedEntities() >> ["entity1", "entity2", "entity3"]
        indexConfigurationManager.getIndexConfigurationByEntityName("entity1") >> configuration1
        indexConfigurationManager.getIndexConfigurationByEntityName("entity2") >> configuration2
        indexConfigurationManager.getIndexConfigurationByEntityName("entity3") >> configuration3

        and:
        SearchSecurityDecorator securityDecorator = Mock()
        securityDecorator.resolveEntitiesAllowedToSearch(_) >> List.of("entity1", "entity2", "entity3")

        and:
        def fieldsResolver = Mock(SearchFieldsProvider)
        def subfieldsProvider = Mock(VirtualSubfieldsProvider)
        fieldsResolver.resolveFields(configuration1, subfieldsProvider) >> Set.of()
        fieldsResolver.resolveFields(configuration2, subfieldsProvider) >> Set.of()
        fieldsResolver.resolveFields(configuration3, subfieldsProvider) >> Set.of()

        and:
        def configurator = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)

        when:
        def fields = configurator.getSearchRequestScope(
                List.of("entity1", "entity2", "entity3", "entity4"),
                subfieldsProvider
        )

        then:
        fields.isEmpty()
    }

    def "GetEntitiesWithConfiguration"() {
        given:
        IndexConfigurationManager indexConfigurationManager = Mock()

        and:
        def analyzer = new SearchRequestScopeProvider(Mock(SearchSecurityDecorator), indexConfigurationManager, Mock(SearchFieldsProvider))

        when:
        indexConfigurationManager.getAllIndexedEntities() >> allConfigurations

        def entitiesWithConfiguration = analyzer.getEntitiesWithConfiguration(input)

        then:
        entitiesWithConfiguration == result

        where:
        input                             | allConfigurations                || result
        []                                | ["entity1", "entity2", "entity3"] | ["entity1", "entity2", "entity3"]
        []                                | []                                | []
        ["entity1", "entity2", "entity3"] | []                                | []
        ["entity1", "entity2", "entity3"] | ["entity1", "entity3"]            | ["entity1", "entity3"]
        ["entity2", "entity3"]            | ["entity1", "entity2", "entity3"] | ["entity2", "entity3"]
    }


}
