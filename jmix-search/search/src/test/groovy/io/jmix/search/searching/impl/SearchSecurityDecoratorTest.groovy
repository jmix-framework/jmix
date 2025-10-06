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

package io.jmix.search.searching.impl

import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.core.Metadata
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.security.CurrentUserSecurityFacade
import spock.lang.Specification

class SearchSecurityDecoratorTest extends Specification {

    def "CanAttributeBeRead: returns the result of the similar CurrentUserSecurityFacade method"() {
        given:
        def securityFacade = Mock(CurrentUserSecurityFacade)

        and:
        SearchSecurityDecorator decorator = new SearchSecurityDecorator(Mock(Metadata), securityFacade)

        and:
        def propertyPath = Mock(MetaPropertyPath)

        when:
        1 * securityFacade.canAttributeBeRead(propertyPath) >> expectedResult
        def actualResult = decorator.canAttributeBeRead(propertyPath)

        then:
        actualResult == expectedResult
        where:
        expectedResult << [true, false]
    }

    def "ResolveEntitiesAllowedToSearch: a user has rights for all entities"() {
        given:
        def securityFacade = Mock(CurrentUserSecurityFacade)

        and:
        def metadata = Mock(Metadata)
        def metaClass1 = Mock(MetaClass)
        def metaClass2 = Mock(MetaClass)
        def metaClass3 = Mock(MetaClass)
        metadata.getClass("entity1") >> metaClass1
        metadata.getClass("entity2") >> metaClass2
        metadata.getClass("entity3") >> metaClass3

        and:
        securityFacade.canEntityBeRead(metaClass1) >> true
        securityFacade.canEntityBeRead(metaClass2) >> true
        securityFacade.canEntityBeRead(metaClass3) >> true

        and:
        SearchSecurityDecorator decorator = new SearchSecurityDecorator(
                metadata,
                securityFacade)

        when:
        def actualResult = decorator.resolveEntitiesAllowedToSearch(["entity1", "entity2", "entity3"])

        then:
        actualResult == ["entity1", "entity2", "entity3"]
    }

    def "ResolveEntitiesAllowedToSearch: a user has rights not for all entities"() {
        given:
        def securityFacade = Mock(CurrentUserSecurityFacade)

        and:
        def metadata = Mock(Metadata)
        def metaClass1 = Mock(MetaClass)
        def metaClass2 = Mock(MetaClass)
        def metaClass3 = Mock(MetaClass)
        metadata.getClass("entity1") >> metaClass1
        metadata.getClass("entity2") >> metaClass2
        metadata.getClass("entity3") >> metaClass3

        and:
        securityFacade.canEntityBeRead(metaClass1) >> false
        securityFacade.canEntityBeRead(metaClass2) >> true
        securityFacade.canEntityBeRead(metaClass3) >> false

        and:
        SearchSecurityDecorator decorator = new SearchSecurityDecorator(metadata, securityFacade)

        when:
        def actualResult = decorator.resolveEntitiesAllowedToSearch(["entity1", "entity2", "entity3"])

        then:
        actualResult == ["entity2"]
    }

    def "ResolveEntitiesAllowedToSearch: a user has not rights for all entities"() {
        given:
        def securityFacade = Mock(CurrentUserSecurityFacade)

        and:
        def metadata = Mock(Metadata)
        def metaClass1 = Mock(MetaClass)
        def metaClass2 = Mock(MetaClass)
        def metaClass3 = Mock(MetaClass)
        metadata.getClass("entity1") >> metaClass1
        metadata.getClass("entity2") >> metaClass2
        metadata.getClass("entity3") >> metaClass3

        and:
        securityFacade.canEntityBeRead(metaClass1) >> false
        securityFacade.canEntityBeRead(metaClass2) >> false
        securityFacade.canEntityBeRead(metaClass3) >> false

        and:
        SearchSecurityDecorator decorator = new SearchSecurityDecorator(metadata, securityFacade)

        when:
        def actualResult = decorator.resolveEntitiesAllowedToSearch(["entity1", "entity2", "entity3"])

        then:
        actualResult == []
    }
}
