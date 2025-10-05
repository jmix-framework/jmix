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
import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.core.metamodel.model.Range
import spock.lang.Specification

class SearchFieldSubstituteTest extends Specification {

    def "Resolve fields for index by property path"() {

        given:
        SearchFieldSubstitute fieldSubstitute = new SearchFieldSubstitute()

        when:
        def metaPropertyPath = createMetaPropertyPath(isDatatype, dataType, isClass)
        def actualResult = fieldSubstitute.getFieldsForPath(metaPropertyPath, "fieldName")

        then:
        actualResult == expectedResult

        where:
        isDatatype | dataType              | isClass || expectedResult
        true       | Mock(FileRefDatatype) | false || Set.of("fieldName._content", "fieldName._file_name")
        false      | null                  | true    || Set.of("fieldName._instance_name")
        false      | null                  | false   || Set.of("fieldName")
        true       | Mock(Datatype) | false || Set.of("fieldName")
    }

    MetaPropertyPath createMetaPropertyPath(boolean isDatatype, Datatype<?> dataType, boolean isClass) {
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
        return mock
    }
}
