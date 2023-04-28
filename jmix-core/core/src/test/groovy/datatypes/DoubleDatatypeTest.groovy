/*
 * Copyright 2022 Haulmont.
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

import format_strings.TestFormatStringsRegistry
import io.jmix.core.metamodel.datatype.impl.DoubleDatatype
import properties.TestCoreProperties
import spock.lang.Specification

import java.text.ParseException

class DoubleDatatypeTest  extends Specification {

    def "format/parse without locale, without rounding"() {
        def datatype = new DoubleDatatype()
        datatype.coreProperties = TestCoreProperties.builder()
                .setDecimalValueRoundByFormat(false)
                .build()

        expect:

        datatype.format(Double.valueOf('12345678.12345678')) == '12345678.123'
        datatype.parse('12345678.12345678') == Double.valueOf('12345678.12345678')
        datatype.parse('12345678.456789') == Double.valueOf('12345678.456789')
    }

    def "format/parse without locale, with rounding"() {
        def datatype = new DoubleDatatype()
        datatype.coreProperties = TestCoreProperties.builder()
                .setDecimalValueRoundByFormat(true)
                .build()

        expect:

        datatype.format(Double.valueOf('12345678.12345678')) == '12345678.123'
        datatype.parse('12345678.12345678') == Double.valueOf('12345678.123')
        datatype.parse('12345678.456789') == Double.valueOf('12345678.457')
    }

    def "format/parse with locale, without decimal rounding"() {
        def datatype = new DoubleDatatype()
        datatype.formatStringsRegistry = new TestFormatStringsRegistry()
        datatype.coreProperties = TestCoreProperties.builder()
                .setDecimalValueRoundByFormat(false)
                .build()

        expect:

        datatype.format(Double.valueOf('12345678.12345678'), Locale.ENGLISH) == '12,345,678.123'
        datatype.parse('12345678.12345678', Locale.ENGLISH) == Double.valueOf('12345678.12345678')
        datatype.parse('12345678.456789', Locale.ENGLISH) == Double.valueOf('12345678.456789')
        datatype.parse('12,345,678.12345678', Locale.ENGLISH) == Double.valueOf('12345678.12345678')

    }

    def "format/parse with locale, with decimal rounding"() {
        def datatype = new DoubleDatatype()
        datatype.formatStringsRegistry = new TestFormatStringsRegistry()
        datatype.coreProperties = TestCoreProperties.builder()
                .setDecimalValueRoundByFormat(true)
                .build()

        expect:

        datatype.format(Double.valueOf('12345678.12345678'), Locale.ENGLISH) == '12,345,678.123'
        datatype.parse('12345678.12345678', Locale.ENGLISH) == Double.valueOf('12345678.123')
        datatype.parse('12345678.456789', Locale.ENGLISH) == Double.valueOf('12345678.457')
        datatype.parse('12,345,678.12345678', Locale.ENGLISH) == Double.valueOf('12345678.123')
    }

    def "parse error due to unknown separators"() {
        def datatype = new DoubleDatatype()
        datatype.formatStringsRegistry = new TestFormatStringsRegistry()

        when:

        datatype.parse('12 345 678,12345678', Locale.ENGLISH) == Double.valueOf('12345678.12345678')

        then:

        thrown(ParseException)
    }
}
