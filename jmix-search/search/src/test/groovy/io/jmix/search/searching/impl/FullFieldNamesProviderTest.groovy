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

import io.jmix.core.metamodel.datatype.Datatype
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.core.metamodel.model.Range
import io.jmix.search.searching.SearchSecurityDecorator
import spock.lang.Specification

class FullFieldNamesProviderTest extends Specification {

    def "Resolve fields for index by property path"() {

        given:
        SearchSecurityDecorator securityDecorator = Mock()

        and:
        FullFieldNamesProvider provider = new FullFieldNamesProvider(securityDecorator)

        when:
        def metaPropertyPath = createMetaPropertyPath(isDatatype, dataType, isClass, asClass)
        if (isClass) {
            securityDecorator.isEntityReadPermitted(asClass) >> canBeRead
        }

        def actualResult = provider.getFieldNamesForBaseField(metaPropertyPath, "fieldName")

        then:
        actualResult == expectedResult

        where:
        isDatatype | dataType              | isClass | asClass         | canBeRead || expectedResult
        true       | Mock(FileRefDatatype) | false   | null            | null      || Set.of("fieldName._content", "fieldName._file_name")
        false      | null                  | true    | Mock(MetaClass) | false     || Set.of()
        false      | null                  | true    | Mock(MetaClass) | true      || Set.of("fieldName._instance_name")
        false      | null                  | false   | null            | null      || Set.of("fieldName")
        true       | Mock(Datatype)        | false   | null            | null      || Set.of("fieldName")
    }

    MetaPropertyPath createMetaPropertyPath(boolean isDatatype, Datatype<?> dataType, boolean isClass, MetaClass metaClass) {
        def mock = Mock(MetaPropertyPath)
        def rangeMock = Mock(Range)
        mock.getRange() >> rangeMock
        if (!isDatatype) {
            rangeMock.asDatatype() >> { throw new RuntimeException() }
        } else {
            rangeMock.asDatatype() >> dataType
        }
        rangeMock.isDatatype() >> isDatatype
        rangeMock.isClass() >> isClass
        rangeMock.asClass() >> metaClass
        return mock
    }
}
