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

import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.core.Metadata
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.security.constraint.PolicyStore
import io.jmix.security.constraint.SecureOperations
import spock.lang.Specification

class SearchSecurityDecoratorTest extends Specification {

    def isEntityAttrReadPermitted() {
        given:
        def secureOperations = Mock(SecureOperations)

        and:
        def policyStore = Stub(PolicyStore)

        and:
        def metaPropertyPath = Mock(MetaPropertyPath)

        and:
        def decorator = new SearchSecurityDecorator(Mock(Metadata), secureOperations, policyStore);

        when:
        secureOperations.isEntityAttrReadPermitted(metaPropertyPath, policyStore) >> operationsResult
        def actualResult = decorator.isEntityAttrReadPermitted(metaPropertyPath)

        then:
        actualResult == expectedResult

        where:
        operationsResult | expectedResult
        true             | true
        false            | false
    }

    def isEntityReadPermitted() {
        given:
        def secureOperations = Mock(SecureOperations)

        and:
        def policyStore = Stub(PolicyStore)

        and:
        def metaClass = Mock(MetaClass)

        and:
        def decorator = new SearchSecurityDecorator(Mock(Metadata), secureOperations, policyStore);

        when:
        secureOperations.isEntityReadPermitted(metaClass, policyStore) >> operationsResult
        def actualResult = decorator.isEntityReadPermitted(metaClass)

        then:
        actualResult == expectedResult

        where:
        operationsResult | expectedResult
        true             | true
        false            | false

    }

    def "ResolveEntitiesAllowedToSearch: a user has rights for all entities"() {
        given:
        def metadata = Mock(Metadata)
        def metaClass1 = Mock(MetaClass)
        def metaClass2 = Mock(MetaClass)
        def metaClass3 = Mock(MetaClass)
        metadata.getClass("entity1") >> metaClass1
        metadata.getClass("entity2") >> metaClass2
        metadata.getClass("entity3") >> metaClass3

        and:
        def secureOperations = Mock(SecureOperations)
        def policyStore = Stub(PolicyStore)
        secureOperations.isEntityReadPermitted(metaClass1, policyStore) >> true
        secureOperations.isEntityReadPermitted(metaClass2, policyStore) >> true
        secureOperations.isEntityReadPermitted(metaClass3, policyStore) >> true

        and:
        SearchSecurityDecorator decorator = new SearchSecurityDecorator(metadata, secureOperations, policyStore)

        when:
        def actualResult = decorator.resolveEntitiesAllowedToSearch(["entity1", "entity2", "entity3"])

        then:
        actualResult == ["entity1", "entity2", "entity3"]
    }

    def "ResolveEntitiesAllowedToSearch: a user has rights not for all entities"() {
        given:
        def metadata = Mock(Metadata)
        def metaClass1 = Mock(MetaClass)
        def metaClass2 = Mock(MetaClass)
        def metaClass3 = Mock(MetaClass)
        metadata.getClass("entity1") >> metaClass1
        metadata.getClass("entity2") >> metaClass2
        metadata.getClass("entity3") >> metaClass3

        and:
        def secureOperations = Mock(SecureOperations)
        def policyStore = Stub(PolicyStore)
        secureOperations.isEntityReadPermitted(metaClass1, policyStore) >> false
        secureOperations.isEntityReadPermitted(metaClass2, policyStore) >> true
        secureOperations.isEntityReadPermitted(metaClass3, policyStore) >> false

        and:
        SearchSecurityDecorator decorator = new SearchSecurityDecorator(metadata, secureOperations, policyStore)

        when:
        def actualResult = decorator.resolveEntitiesAllowedToSearch(["entity1", "entity2", "entity3"])

        then:
        actualResult == ["entity2"]
    }

    def "ResolveEntitiesAllowedToSearch: a user has not rights for all entities"() {
        given:
        def metadata = Mock(Metadata)
        def metaClass1 = Mock(MetaClass)
        def metaClass2 = Mock(MetaClass)
        def metaClass3 = Mock(MetaClass)
        metadata.getClass("entity1") >> metaClass1
        metadata.getClass("entity2") >> metaClass2
        metadata.getClass("entity3") >> metaClass3

        and:
        def secureOperations = Mock(SecureOperations)
        def policyStore = Stub(PolicyStore)
        secureOperations.isEntityReadPermitted(metaClass1, policyStore) >> false
        secureOperations.isEntityReadPermitted(metaClass2, policyStore) >> false
        secureOperations.isEntityReadPermitted(metaClass3, policyStore) >> false

        and:
        SearchSecurityDecorator decorator = new SearchSecurityDecorator(metadata, secureOperations, policyStore)

        when:
        def actualResult = decorator.resolveEntitiesAllowedToSearch(["entity1", "entity2", "entity3"])

        then:
        actualResult == []
    }
}
