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
import io.jmix.search.index.impl.IndexLayout
import io.jmix.search.index.impl.MultitenancyAdapter
import io.jmix.search.index.mapping.IndexConfigurationManager
import spock.lang.Specification

import static java.util.Collections.emptyList

class SearchRequestScopeProviderTest extends Specification {

    def "getIndexesWithFields. all entities have configuration. User hav full access to the entities"() {
        given:
        IndexConfiguration configuration1 = Mock()
        IndexConfiguration configuration2 = Mock()
        IndexConfiguration configuration3 = Mock()

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
        def indexLayout = Mock(IndexLayout)
        indexLayout.indexName(configuration1, _) >> "index1"
        indexLayout.indexName(configuration2, _) >> "index2"
        indexLayout.indexName(configuration3, _) >> "index3"

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)
        analyzer.indexLayout = indexLayout
        analyzer.multitenancyAdapter = Mock(MultitenancyAdapter)

        when:
        def fields = analyzer.getSearchRequestScope(
                ["entity1", "entity2", "entity3", "entity4"],
                subfieldsProvider
        )

        then:
        fields == List.of(
                new IndexSearchRequestScope(configuration1, Set.of("field1_1", "field1_2", "field1_3"), "index1"),
                new IndexSearchRequestScope(configuration2, Set.of("field2_1", "field2_2", "field2_3"), "index2"),
                new IndexSearchRequestScope(configuration3, Set.of("field3_1", "field3_2", "field3_3"), "index3")
        )
    }

    def "getIndexNamesWithFields. Empty list of required entities"() {
        given:
        IndexConfiguration configuration1 = Mock()
        IndexConfiguration configuration2 = Mock()
        IndexConfiguration configuration3 = Mock()

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
        def indexLayout = Mock(IndexLayout)
        indexLayout.indexName(configuration1, _) >> "index1"
        indexLayout.indexName(configuration2, _) >> "index2"
        indexLayout.indexName(configuration3, _) >> "index3"

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)
        analyzer.indexLayout = indexLayout
        analyzer.multitenancyAdapter = Mock(MultitenancyAdapter)

        when:
        def fields = analyzer.getSearchRequestScope(
                emptyList(),
                subfieldsProvider
        )

        then:
        fields == List.of(
                new IndexSearchRequestScope(configuration1, Set.of("field1_1", "field1_2", "field1_3"), "index1"),
                new IndexSearchRequestScope(configuration2, Set.of("field2_1", "field2_2", "field2_3"), "index2"),
                new IndexSearchRequestScope(configuration3, Set.of("field3_1", "field3_2", "field3_3"), "index3")
        )
    }

    def "getIndexNamesWithFields. Empty list of allowed entities"() {
        given:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getAllIndexedEntities() >> ["entity1", "entity2", "entity3"]

        and:
        SearchSecurityDecorator securityDecorator = Mock()
        securityDecorator.resolveEntitiesAllowedToSearch(_) >> emptyList()

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, Mock(SearchFieldsProvider))
        analyzer.indexLayout = Mock(IndexLayout)
        analyzer.multitenancyAdapter = Mock(MultitenancyAdapter)

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
        IndexConfiguration configuration2 = Mock()
        IndexConfiguration configuration3 = Mock()

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
        def indexLayout = Mock(IndexLayout)
        indexLayout.indexName(configuration1, _) >> "index1"
        indexLayout.indexName(configuration2, _) >> "index2"
        indexLayout.indexName(configuration3, _) >> "index3"

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)
        analyzer.indexLayout = indexLayout
        analyzer.multitenancyAdapter = Mock(MultitenancyAdapter)

        when:
        def fields = analyzer.getSearchRequestScope(
                List.of("entity1", "entity2", "entity3", "entity4"),
                subfieldsProvider
        )

        then:
        fields == List.of(
                new IndexSearchRequestScope(configuration1, Set.of("field1_1", "field1_2", "field1_3"), "index1"),
                new IndexSearchRequestScope(configuration3, Set.of("field3_1", "field3_2", "field3_3"), "index3")
        )
    }

    def "getIndexNamesWithFields. All entities haven't allowed fields"() {
        given:
        IndexConfiguration configuration1 = Mock()
        IndexConfiguration configuration2 = Mock()
        IndexConfiguration configuration3 = Mock()

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
        def indexLayout = Mock(IndexLayout)
        indexLayout.indexName(_, _) >> "index"

        and:
        def configurator = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)
        configurator.indexLayout = indexLayout
        configurator.multitenancyAdapter = Mock(MultitenancyAdapter)

        when:
        def fields = configurator.getSearchRequestScope(
                List.of("entity1", "entity2", "entity3", "entity4"),
                subfieldsProvider
        )

        then:
        fields.isEmpty()
    }

    def "getIndexNamesWithFields. Tenantless and tenant-specific index names are resolved"() {
        given:
        IndexConfiguration tenantlessConfiguration = Mock()
        tenantlessConfiguration.isTenantAware() >> false
        IndexConfiguration tenantSpecificConfiguration = Mock()
        tenantSpecificConfiguration.isTenantAware() >> true

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getAllIndexedEntities() >> ["entity1", "entity2"]
        indexConfigurationManager.getIndexConfigurationByEntityName("entity1") >> tenantlessConfiguration
        indexConfigurationManager.getIndexConfigurationByEntityName("entity2") >> tenantSpecificConfiguration

        and:
        SearchSecurityDecorator securityDecorator = Mock()
        securityDecorator.resolveEntitiesAllowedToSearch(_) >> List.of("entity1", "entity2")

        and:
        def fieldsResolver = Mock(SearchFieldsProvider)
        def subfieldsProvider = Mock(VirtualSubfieldsProvider)
        fieldsResolver.resolveFields(tenantlessConfiguration, subfieldsProvider) >> Set.of("field1")
        fieldsResolver.resolveFields(tenantSpecificConfiguration, subfieldsProvider) >> Set.of("field2")

        and:
        def indexLayout = Mock(IndexLayout)
        indexLayout.indexName(tenantlessConfiguration, _) >> "index_public"
        indexLayout.indexName(tenantSpecificConfiguration, _) >> "index_tenant_a"

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)
        analyzer.indexLayout = indexLayout
        analyzer.multitenancyAdapter = Mock(MultitenancyAdapter)

        when:
        def fields = analyzer.getSearchRequestScope(
                ["entity1", "entity2"],
                subfieldsProvider
        )

        then:
        fields == List.of(
                new IndexSearchRequestScope(tenantlessConfiguration, Set.of("field1"), "index_public"),
                new IndexSearchRequestScope(tenantSpecificConfiguration, Set.of("field2"), "index_tenant_a")
        )
    }

    def "GetEntitiesWithConfiguration"() {
        given:
        IndexConfigurationManager indexConfigurationManager = Mock()

        and:
        def analyzer = new SearchRequestScopeProvider(Mock(SearchSecurityDecorator), indexConfigurationManager, Mock(SearchFieldsProvider))
        analyzer.indexLayout = Mock(IndexLayout)
        analyzer.multitenancyAdapter = Mock(MultitenancyAdapter)

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

    def "getIndexNamesWithFields. User without a tenant does not search tenant-aware entities"() {
        given:
        IndexConfiguration tenantlessConfiguration = Mock()
        IndexConfiguration tenantAwareConfiguration = Mock()

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getAllIndexedEntities() >> ["shared", "tenantAware"]
        indexConfigurationManager.getIndexConfigurationByEntityName("shared") >> tenantlessConfiguration
        indexConfigurationManager.getIndexConfigurationByEntityName("tenantAware") >> tenantAwareConfiguration

        and:
        SearchSecurityDecorator securityDecorator = Mock()
        securityDecorator.resolveEntitiesAllowedToSearch(_) >> List.of("shared", "tenantAware")

        and:
        def fieldsResolver = Mock(SearchFieldsProvider)
        def subfieldsProvider = Mock(VirtualSubfieldsProvider)
        fieldsResolver.resolveFields(tenantlessConfiguration, subfieldsProvider) >> Set.of("field1")
        fieldsResolver.resolveFields(tenantAwareConfiguration, subfieldsProvider) >> Set.of("field2")

        and: "the current user has no tenant, so the tenant-aware configuration resolves to no indexes"
        def multitenancyAdapter = Mock(MultitenancyAdapter)
        multitenancyAdapter.getCurrentUserTenantId() >> null
        def indexLayout = Mock(IndexLayout)
        indexLayout.indexName(tenantlessConfiguration, null) >> "shared_index"
        indexLayout.indexName(tenantAwareConfiguration, null) >> null

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)
        analyzer.indexLayout = indexLayout
        analyzer.multitenancyAdapter = multitenancyAdapter

        when:
        def scopes = analyzer.getSearchRequestScope(List.of(), subfieldsProvider)

        then: "only the shared entity is searched"
        scopes == List.of(new IndexSearchRequestScope(tenantlessConfiguration, Set.of("field1"), "shared_index"))
    }

    def "getIndexNamesWithFields. User of a tenant searches the index of that tenant"() {
        given:
        IndexConfiguration tenantAwareConfiguration = Mock()

        and:
        IndexConfigurationManager indexConfigurationManager = Mock()
        indexConfigurationManager.getAllIndexedEntities() >> ["tenantAware"]
        indexConfigurationManager.getIndexConfigurationByEntityName("tenantAware") >> tenantAwareConfiguration

        and:
        SearchSecurityDecorator securityDecorator = Mock()
        securityDecorator.resolveEntitiesAllowedToSearch(_) >> List.of("tenantAware")

        and:
        def fieldsResolver = Mock(SearchFieldsProvider)
        def subfieldsProvider = Mock(VirtualSubfieldsProvider)
        fieldsResolver.resolveFields(tenantAwareConfiguration, subfieldsProvider) >> Set.of("field1")

        and:
        def multitenancyAdapter = Mock(MultitenancyAdapter)
        multitenancyAdapter.getCurrentUserTenantId() >> "tenantA"
        def indexLayout = Mock(IndexLayout)
        indexLayout.indexName(tenantAwareConfiguration, "tenantA") >> "index_tenant_a"

        and:
        def analyzer = new SearchRequestScopeProvider(securityDecorator, indexConfigurationManager, fieldsResolver)
        analyzer.indexLayout = indexLayout
        analyzer.multitenancyAdapter = multitenancyAdapter

        when:
        def scopes = analyzer.getSearchRequestScope(List.of(), subfieldsProvider)

        then:
        scopes == List.of(new IndexSearchRequestScope(tenantAwareConfiguration, Set.of("field1"), "index_tenant_a"))
    }
}
