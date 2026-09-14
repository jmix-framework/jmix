/*
 * Copyright 2026 Haulmont.
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

package facet.url_query_parameters

import io.jmix.flowui.facet.urlqueryparameters.FilterUrlQueryParametersSupport
import spock.lang.Specification

/**
 * Unit test for the shared parameter tokenizer used by the generic filter, property filter and
 * data grid filter URL binders: {@code headTokens} structural tokens plus the remainder, which may
 * itself contain separator characters and may be empty.
 */
class FilterUrlQueryParametersSupportTest extends Specification {

    def support = new FilterUrlQueryParametersSupport(null, null, null)

    def "splits a parameter into head tokens and the remainder"() {
        expect:
        support.splitParameter(parameter, headTokens) == expected

        where:
        parameter                       | headTokens | expected
        "name_contains_John"            | 2          | ["name", "contains", "John"]
        "name_contains_a_b_c"           | 2          | ["name", "contains", "a_b_c"]
        "name_equal_"                   | 2          | ["name", "equal", ""]
        "operation_value"               | 1          | ["operation", "value"]
        "key_property_operation_va_lue" | 3          | ["key", "property", "operation", "va_lue"]
        "whole"                         | 0          | ["whole"]
    }

    def "a parameter with fewer separators than head tokens is rejected"() {
        when:
        support.splitParameter("name_contains", 2)

        then:
        def e = thrown(IllegalStateException)
        e.message.contains("name_contains")
    }
}
