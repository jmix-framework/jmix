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
import io.jmix.core.metamodel.datatype.impl.FloatDatatype
import spock.lang.Specification
import test_support.TestCoreProperties

import java.text.ParseException

class FloatDatatypeTest extends Specification {

    def "format/parse without locale, without rounding"() {
        def datatype = new FloatDatatype()
        datatype.coreProperties = TestCoreProperties.builder()
                .setRoundDecimalValueByFormat(false)
                .build()

        expect:

        datatype.format(Float.valueOf('12345.12')) == '12345.12'
        datatype.parse('12345.12345') == Float.valueOf('12345.12345')
        datatype.parse('12345.45678') == Float.valueOf('12345.45678')
    }

    def "format/parse without locale, with rounding"() {
        def datatype = new FloatDatatype()
        datatype.coreProperties = TestCoreProperties.builder()
                .setRoundDecimalValueByFormat(true)
                .build()

        expect:

        datatype.format(Float.valueOf('12345.12345')) == '12345.123'
        datatype.parse('12345.12345') == Float.valueOf('12345.123')
        datatype.parse('12345.45678') == Float.valueOf('12345.457')
    }

    def "format/parse with locale, without decimal rounding"() {
        def datatype = new FloatDatatype()
        datatype.formatStringsRegistry = new TestFormatStringsRegistry()
        datatype.coreProperties = TestCoreProperties.builder()
                .setRoundDecimalValueByFormat(false)
                .build()

        expect:

        datatype.format(Float.valueOf("12345.12"), Locale.ENGLISH) == '12,345.12'
        datatype.parse('12345.12345', Locale.ENGLISH) == Float.valueOf('12345.12345')
        datatype.parse('12345.45678', Locale.ENGLISH) == Float.valueOf('12345.45678')
        datatype.parse('12,345,678.123', Locale.ENGLISH) == Float.valueOf('12345678.123')

    }

    def "format/parse with locale, with decimal rounding"() {
        def datatype = new FloatDatatype()
        datatype.formatStringsRegistry = new TestFormatStringsRegistry()
        datatype.coreProperties = TestCoreProperties.builder()
                .setRoundDecimalValueByFormat(true)
                .build()

        expect:

        datatype.format(Float.valueOf("12345.12"), Locale.ENGLISH) == '12,345.12'
        datatype.parse('12345.12345', Locale.ENGLISH) == Float.valueOf('12345.123')
        datatype.parse('12345.45678', Locale.ENGLISH) == Float.valueOf('12345.457')
        datatype.parse('12,345,678.123', Locale.ENGLISH) == Float.valueOf('12345678.123')
    }

    def "parse error due to unknown separators"() {
        def datatype = new FloatDatatype()
        datatype.formatStringsRegistry = new TestFormatStringsRegistry()

        when:

        datatype.parse('12 345 678,123', Locale.ENGLISH) == Float.valueOf('12345678.123')

        then:

        thrown(ParseException)
    }
}
