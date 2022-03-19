/*
 * Copyright 2020 Haulmont.
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

package commons


import spock.lang.Specification

import static io.jmix.core.common.util.StringHelper.underscoreToCamelCase

class StringHelperTest extends Specification {

    def "test underscoreToCamelCase"() {
        expect:
        underscoreToCamelCase('foo_bar') == 'fooBar'
        underscoreToCamelCase('FOO_BAR') == 'fooBar'
        underscoreToCamelCase('foo_bar_baz') == 'fooBarBaz'
        underscoreToCamelCase('foo__bar') == 'fooBar'
        underscoreToCamelCase('_foo_bar') == '_fooBar'
        underscoreToCamelCase('foo') == 'foo'
        underscoreToCamelCase('') == ''
    }
}
