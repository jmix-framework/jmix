/*
 * Copyright 2019 Haulmont.
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

package datatypes

import io.jmix.core.metamodel.datatype.impl.BigDecimalDatatype
import format_strings.TestFormatStringsRegistry
import spock.lang.Specification

import java.text.ParseException

class BigDecimalDatatypeTest extends Specification {

    def "format/parse without locale"() {
        def datatype = new BigDecimalDatatype()

        expect:

        datatype.format(new BigDecimal('12345678.12345678')) == '12345678.1235'
        datatype.parse('12345678.12345678') == new BigDecimal('12345678.12345678')
    }

    def "format/parse with locale"() {
        def datatype = new BigDecimalDatatype()
        datatype.formatStringsRegistry = new TestFormatStringsRegistry()

        expect:

        datatype.format(new BigDecimal('12345678.12345678'), Locale.ENGLISH) == '12,345,678.12'
        datatype.parse('12345678.12345678', Locale.ENGLISH) == new BigDecimal('12345678.12345678')
        datatype.parse('12,345,678.12345678', Locale.ENGLISH) == new BigDecimal('12345678.12345678')

    }

    def "parse error due to unknown separators"() {
        def datatype = new BigDecimalDatatype()
        datatype.formatStringsRegistry = new TestFormatStringsRegistry()

        when:

        datatype.parse('12 345 678,12345678', Locale.ENGLISH) == new BigDecimal('12345678.12345678')

        then:

        thrown(ParseException)
    }
}
