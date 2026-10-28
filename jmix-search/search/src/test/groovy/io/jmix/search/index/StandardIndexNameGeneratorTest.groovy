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

package io.jmix.search.index

import io.jmix.search.SearchProperties
import io.jmix.search.exception.IndexDefinitionRejectedException
import spock.lang.Specification

class StandardIndexNameGeneratorTest extends Specification {

    protected IndexConfiguration configuration(String entityName, String declaredPattern = null) {
        def configuration = Stub(IndexConfiguration)
        configuration.getEntityName() >> entityName
        configuration.getIndexNamePattern() >> declaredPattern
        return configuration
    }

    def "GenerateIndexName.Tenantless"() {
        given:
        def properties = Mock(SearchProperties)

        when:
        properties.getTenantlessIndexNamePattern() >> pattern
        properties.getTenantIndexNamePattern() >> "{entityName}__{tenantId}"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()
        def generateIndexName = generator.generateIndexName(configuration(entityName), null)

        then:
        generateIndexName == result

        where:
        pattern              | entityName || result
        "idx_{entityName}__" | "name1"    || "idx_name1__"
        "{entityName}__"     | "name2"    || "name2__"
        "idx_{entityName}"   | "name3"    || "idx_name3"
    }

    def "GenerateIndexName.Tenantless.Empty value"() {
        given:
        def properties = Mock(SearchProperties)

        when:
        properties.getTenantlessIndexNamePattern() >> "idx_{entityName}__"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()
        generator.generateIndexName(configuration(entityName), null)

        then:
        def illegalArgumentException = thrown(IllegalArgumentException)
        illegalArgumentException.getMessage() == "Entity name must not be blank or null."

        where:
        entityName << ["", null, " "]
    }

    def "GenerateIndexName.With tenants"() {
        given:
        def properties = Mock(SearchProperties)

        when:
        properties.getTenantlessIndexNamePattern() >> "{entityName}"
        properties.getTenantIndexNamePattern() >> pattern
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()
        def generateIndexName = generator.generateIndexName(configuration(entityName), tenantId)

        then:
        generateIndexName == result

        where:
        pattern                         | entityName | tenantId  || result
        "idx_{entityName}__{tenantId}__" | "name1"   | "tenant1" || "idx_name1__tenant1__"
        "{entityName}__{tenantId}__"    | "name2"    | "tenant1" || "name2__tenant1__"
        "{entityName}__{tenantId}"      | "name3"    | "tenant1" || "name3__tenant1"
        "{entityName}{tenantId}"        | "name4"    | "tenant1" || "name4tenant1"
        "idx_{tenantId}__{entityName}__" | "name5"   | "tenant2" || "idx_tenant2__name5__"
        "{tenantId}__{entityName}__"    | "name5"    | "tenant2" || "tenant2__name5__"
        "{tenantId}__{entityName}"      | "name7"    | "tenant2" || "tenant2__name7"
        "{tenantId}{entityName}"        | "name8"    | "tenant2" || "tenant2name8"
    }

    def "GenerateIndexName.Tenantless.Pattern is not set: name is built from the deprecated prefix"() {
        given:
        def properties = Mock(SearchProperties)

        when:
        properties.getTenantlessIndexNamePattern() >> pattern
        properties.getSearchIndexNamePrefix() >> "search_index_"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        then:
        generator.generateIndexName(configuration("test_Order"), null) == "search_index_test_order"

        where:
        pattern << [null, "", "   "]
    }

    def "GenerateIndexName: generated name is lowercased"() {
        given:
        def properties = Mock(SearchProperties)

        when:
        properties.getTenantlessIndexNamePattern() >> "Prefix_{entityName}"
        properties.getTenantIndexNamePattern() >> "Prefix_{entityName}_{tenantId}"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        then:
        generator.generateIndexName(configuration("test_Order"), null) == "prefix_test_order"
        generator.generateIndexName(configuration("test_Order"), "Tenant_A") == "prefix_test_order_tenant_a"
    }

    def "GenerateIndexName.With tenants.Pattern is not set: name is built from the deprecated prefix"() {
        given:
        def properties = Mock(SearchProperties)

        when:
        properties.getSearchIndexNamePrefix() >> "search_index_"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        then:
        generator.generateIndexName(configuration("test_Order"), null) == "search_index_test_order"
        generator.generateIndexName(configuration("test_Order"), "tenantA") == "search_index_test_order_tenanta"
    }

    def "GenerateIndexName: tenant id makes the name invalid"() {
        given:
        def properties = Mock(SearchProperties)

        when:
        properties.getSearchIndexNamePrefix() >> "search_index_"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()
        generator.generateIndexName(configuration("test_Order"), tenantId)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.getMessage().contains(tenantId)

        where:
        tenantId << ["tenant #1", "tenant a", "tenant/a", "tenant,a"]
    }

    def "GenerateIndexName: name is validated against the engine naming rules"() {
        given:
        def properties = Mock(SearchProperties)

        when:
        properties.getTenantlessIndexNamePattern() >> pattern
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        then:
        def exception = thrown(IllegalArgumentException)
        exception.getMessage().contains("jmix.search.tenantless-index-name-pattern")

        where:
        pattern << ["_{entityName}", "-{entityName}", "+{entityName}", "{entityName} name", "{entityName}#1"]
    }

    def "a tenant pattern that mentions no tenant would put every tenant in one index"() {
        given:
        def properties = Mock(SearchProperties)
        properties.getTenantIndexNamePattern() >> "search_index_{entityName}"

        when:
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        then: """nothing would fail later: the name would simply be the same for everyone, and the data of all
                 the tenants would land in one index without a single error"""
        def exception = thrown(IllegalArgumentException)
        exception.getMessage().contains("jmix.search.tenant-index-name-pattern")
        exception.getMessage().contains("{tenantId}")
    }

    def "a tenantless pattern must not mention a tenant"() {
        given:
        def properties = Mock(SearchProperties)
        properties.getTenantlessIndexNamePattern() >> "search_index_{entityName}_{tenantId}"

        when:
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        then: "there is no tenant to put there, so the placeholder would be replaced with nothing"
        def exception = thrown(IllegalArgumentException)
        exception.getMessage().contains("jmix.search.tenantless-index-name-pattern")
    }

    def "a pattern of either kind must name the entity"() {
        given:
        def properties = Mock(SearchProperties)
        properties.getTenantlessIndexNamePattern() >> tenantlessPattern
        properties.getTenantIndexNamePattern() >> tenantPattern

        when:
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        then: "without it every entity of the application would share one index"
        def exception = thrown(IllegalArgumentException)
        exception.getMessage().contains(expectedProperty)
        exception.getMessage().contains("{entityName}")

        where:
        tenantlessPattern     | tenantPattern                  || expectedProperty
        "search_index"        | ""                             || "jmix.search.tenantless-index-name-pattern"
        ""                    | "search_index_{tenantId}"      || "jmix.search.tenant-index-name-pattern"
    }

    def "the pattern declared by an entity wins over the application-wide one"() {
        given:
        def properties = Mock(SearchProperties)
        properties.getTenantlessIndexNamePattern() >> "idx_{entityName}"
        properties.getTenantIndexNamePattern() >> "idx_{entityName}_{tenantId}"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        expect:
        generator.generateIndexName(configuration("demo_Order", declared), tenantId) == result

        where:
        declared              | tenantId || result
        "orders"              | null     || "orders"
        "orders_{tenantId}"   | "acme"   || "orders_acme"
        "{entityName}_orders" | null     || "demo_order_orders"
    }

    def "an entity without a declared pattern falls back to the application-wide one"() {
        given:
        def properties = Mock(SearchProperties)
        properties.getTenantlessIndexNamePattern() >> "idx_{entityName}"
        properties.getTenantIndexNamePattern() >> "idx_{entityName}_{tenantId}"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        expect:
        generator.generateIndexName(configuration("demo_Order"), null) == "idx_demo_order"
        generator.generateIndexName(configuration("demo_Order"), "acme") == "idx_demo_order_acme"
    }

    def "a declared pattern of a split entity must produce a name per tenant"() {
        given:
        def properties = Mock(SearchProperties)
        properties.getTenantlessIndexNamePattern() >> "idx_{entityName}"
        properties.getTenantIndexNamePattern() >> "idx_{entityName}_{tenantId}"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        when:
        generator.validateEntityIndexNamePattern("orders", "Index definition OrderIndexDefinition", true)

        then: "only this definition is at fault, so only this definition is dropped"
        def exception = thrown(IndexDefinitionRejectedException)
        exception.message.contains("OrderIndexDefinition")
        exception.message.contains("{tenantId}")
    }

    def "a declared pattern of an entity stored in a single index must not mention a tenant"() {
        given:
        def properties = Mock(SearchProperties)
        properties.getTenantlessIndexNamePattern() >> "idx_{entityName}"
        properties.getTenantIndexNamePattern() >> "idx_{entityName}_{tenantId}"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        when:
        generator.validateEntityIndexNamePattern("orders_{tenantId}", "Index definition OrderIndexDefinition", false)

        then:
        thrown(IndexDefinitionRejectedException)
    }

    def "a declared pattern needs no entity name: it belongs to one entity already"() {
        given:
        def properties = Mock(SearchProperties)
        properties.getTenantlessIndexNamePattern() >> "idx_{entityName}"
        properties.getTenantIndexNamePattern() >> "idx_{entityName}_{tenantId}"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        when:
        generator.validateEntityIndexNamePattern(pattern, "Index definition OrderIndexDefinition", splitByTenants)

        then:
        noExceptionThrown()

        where:
        pattern             | splitByTenants
        "orders"            | false
        "orders_{tenantId}" | true
    }

    def "a declared pattern is checked against the engine naming rules"() {
        given:
        def properties = Mock(SearchProperties)
        properties.getTenantlessIndexNamePattern() >> "idx_{entityName}"
        properties.getTenantIndexNamePattern() >> "idx_{entityName}_{tenantId}"
        def generator = new StandardIndexNameGenerator()
        generator.searchProperties = properties
        generator.init()

        when:
        generator.validateEntityIndexNamePattern(pattern, "Index definition OrderIndexDefinition", false)

        then:
        thrown(IndexDefinitionRejectedException)

        where:
        pattern << ["_orders", "-orders", "+orders", "my orders", "orders#1", ""]
    }

}
