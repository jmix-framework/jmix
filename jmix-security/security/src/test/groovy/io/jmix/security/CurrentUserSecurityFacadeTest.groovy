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

package io.jmix.security

import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.security.constraint.PolicyStore
import io.jmix.security.constraint.SecureOperations
import spock.lang.Specification

class CurrentUserSecurityFacadeTest extends Specification {

    def "CanAttributeBeRead"() {
        given:
        def secureOperations = Mock(SecureOperations)

        and:
        def policyStore = Stub(PolicyStore)

        and:
        def metaPropertyPath = Mock(MetaPropertyPath)

        and:
        def facade = new CurrentUserSecurityFacade(secureOperations, policyStore);

        when:
        secureOperations.isEntityAttrReadPermitted(metaPropertyPath, policyStore) >> operationsResult
        def actualResult = facade.canAttributeBeRead(metaPropertyPath)

        then:
        actualResult == expectedResult

        where:
        operationsResult | expectedResult
        true             | true
        false            | false
    }

    def "CanEntityBeRead"() {
        given:
        def secureOperations = Mock(SecureOperations)

        and:
        def policyStore = Stub(PolicyStore)

        and:
        def metaClass = Mock(io.jmix.core.metamodel.model.MetaClass)

        and:
        def facade = new CurrentUserSecurityFacade(secureOperations, policyStore);

        when:
        secureOperations.isEntityReadPermitted(metaClass, policyStore) >> operationsResult
        def actualResult = facade.canEntityBeRead(metaClass)

        then:
        actualResult == expectedResult

        where:
        operationsResult | expectedResult
        true             | true
        false            | false

    }
}
