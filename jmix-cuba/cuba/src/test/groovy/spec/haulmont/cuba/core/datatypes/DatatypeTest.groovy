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

package spec.haulmont.cuba.core.datatypes

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.metamodel.datatype.impl.*
import com.haulmont.cuba.core.model.LocalDateTimeEntity
import com.haulmont.cuba.core.model.TestNumberValuesEntity
import io.jmix.core.Metadata
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.core.metamodel.datatype.impl.*
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification
import spock.lang.Ignore

import java.text.ParseException
import java.time.*

class DatatypeTest extends CoreTestSpecification {

    @Autowired
    private Metadata metadata

    private Locale savedLocale

    def setup() {
        savedLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    def cleanup() {
        Locale.setDefault(savedLocale)
    }

    def "standard datatypes"() {

        def datatypes = AppBeans.get(DatatypeRegistry.class)

        expect:

        datatypes.find(String.class).class == StringDatatype
        datatypes.find(Character.class).class == CharacterDatatype
        datatypes.find(Boolean.class).class == BooleanDatatype
        datatypes.find(Integer.class).class == IntegerDatatype
        datatypes.find(Long.class).class == LongDatatype
        datatypes.find(Double.class).class == DoubleDatatype
        datatypes.find(BigDecimal.class).class == BigDecimalDatatype
        datatypes.find(java.util.Date.class).class == CubaDateTimeDatatype
        datatypes.find(java.sql.Date.class).class == CubaDateDatatype
        datatypes.find(java.sql.Time.class).class == CubaTimeDatatype
        datatypes.find(UUID.class).class == UuidDatatype
        datatypes.find(byte[].class).class == ByteArrayDatatype
        datatypes.find(LocalDate.class).class == CubaLocalDateDatatype
        datatypes.find(LocalTime.class).class == CubaLocalTimeDatatype
        datatypes.find(LocalDateTime.class).class == CubaLocalDateTimeDatatype
        datatypes.find(OffsetDateTime.class).class == CubaOffsetDateTimeDatatype
        datatypes.find(OffsetTime.class).class == CubaOffsetTimeDatatype
    }

    def "not supported types"() {

        def datatypes = AppBeans.get(DatatypeRegistry.class)

        expect:

        datatypes.find(Byte.class) == null
    }

    def "adaptive BigDecimal datatype"() {
        def metaClass = metadata.getClass(TestNumberValuesEntity.class)

        def decimalDatatype1 = metaClass.getProperty('decimalField1').getRange().asDatatype()
        def decimalDatatype2 = metaClass.getProperty('decimalField2').getRange().asDatatype()
        def decimalDatatype3 = metaClass.getProperty('decimalField3').getRange().asDatatype()
        def decimalDatatype4 = metaClass.getProperty('decimalField4').getRange().asDatatype()

        expect:

        // #,###.###
        decimalDatatype1.getJavaClass() == BigDecimal
        decimalDatatype1.format(0.1) == '0.1'
        decimalDatatype1.format(12345.6789123) == '12,345.679'
        decimalDatatype1.parse('12,345.6789123') == 12345.6789123
        decimalDatatype1.parse('12,345.6789123') instanceof BigDecimal

        // 0.00
        decimalDatatype2.getJavaClass() == BigDecimal
        decimalDatatype2.format(0.1) == '0.10'
        decimalDatatype2.format(12345.6789123) == '12345.68'
        decimalDatatype2.parse('12345.6789123') == 12345.6789123
        decimalDatatype2.parse('12345.6789123') instanceof BigDecimal

        // #
        decimalDatatype3.getJavaClass() == BigDecimal
        decimalDatatype3.format(0.1) == '0'
        decimalDatatype3.format(12345.6789123) == '12346'
        decimalDatatype3.parse('12345.6789123') == 12345.6789123
        decimalDatatype3.parse('12345.6789123') instanceof BigDecimal

        // #,###.### decimalSeparator = "_", groupingSeparator = "`"
        decimalDatatype4.getJavaClass() == BigDecimal
        decimalDatatype4.format(0.1) == '0_1'
        decimalDatatype4.format(12345.6789123) == '12`345_679'
        decimalDatatype4.parse('12`345_6789123') == 12345.6789123
        decimalDatatype4.parse('12`345_6789123') instanceof BigDecimal
    }

    @Ignore
    def "adaptive BigDecimal localized datatype"() {
        def metaClass = metadata.getClass(TestNumberValuesEntity.class)

        def decimalDatatype1 = metaClass.getProperty('decimalField1').getRange().asDatatype()
        def decimalDatatype4 = metaClass.getProperty('decimalField4').getRange().asDatatype()

        def locale = Locale.forLanguageTag("ru")

        expect:

        // #,###.###
        decimalDatatype1.getJavaClass() == BigDecimal
        decimalDatatype1.format(0.1, locale) == '0,1'
        decimalDatatype1.format(12345.6789123, locale) == '12 345,679'
        decimalDatatype1.parse('12 345,6789123', locale) == 12345.6789123
        decimalDatatype1.parse('12 345,6789123', locale) instanceof BigDecimal

        // #,###.### decimalSeparator = "_", groupingSeparator = "`"
        decimalDatatype4.getJavaClass() == BigDecimal
        decimalDatatype4.format(0.1, locale) == '0_1'
        decimalDatatype4.format(12345.6789123, locale) == '12`345_679'
        decimalDatatype4.parse('12`345_6789123', locale) == 12345.6789123
        decimalDatatype4.parse('12`345_6789123', locale) instanceof BigDecimal
    }

    def "adaptive Double datatype"() {
        def metaClass = metadata.getClass(TestNumberValuesEntity.class)

        def doubleDatatype1 = metaClass.getProperty('doubleField1').getRange().asDatatype()
        def doubleDatatype2 = metaClass.getProperty('doubleField2').getRange().asDatatype()
        def doubleDatatype3 = metaClass.getProperty('doubleField3').getRange().asDatatype()

        expect:

        // #,###.###
        doubleDatatype1.getJavaClass() == Double
        doubleDatatype1.format(0.1) == '0.1'
        doubleDatatype1.format(12345.6789123) == '12,345.679'
        doubleDatatype1.parse('12,345.6789123') == 12345.6789123
        doubleDatatype1.parse('12,345.6789123') instanceof Double

        // 0.00
        doubleDatatype2.getJavaClass() == Double
        doubleDatatype2.format(0.1) == '0.10'
        doubleDatatype2.format(12345.6789123) == '12345.68'
        doubleDatatype2.parse('12345.6789123') == 12345.6789123
        doubleDatatype2.parse('12345.6789123') instanceof Double

        // #
        doubleDatatype3.getJavaClass() == Double
        doubleDatatype3.format(0.1) == '0'
        doubleDatatype3.format(12345.6789123) == '12346'
        doubleDatatype3.parse('12345.6789123') == 12345.6789123
        doubleDatatype3.parse('12345.6789123') instanceof Double
    }

    def "adaptive Float datatype"() {
        def metaClass = metadata.getClass(TestNumberValuesEntity.class)

        def floatDatatype1 = metaClass.getProperty('floatField1').getRange().asDatatype()
        def floatDatatype2 = metaClass.getProperty('floatField2').getRange().asDatatype()
        def floatDatatype3 = metaClass.getProperty('floatField3').getRange().asDatatype()

        expect:

        // #,###.###
        floatDatatype1.getJavaClass() == Float
        floatDatatype1.format(0.1) == '0.1'
        floatDatatype1.format(12345.6789123) == '12,345.679'
        floatDatatype1.parse('12,345.6789123') == 12345.679f
        floatDatatype1.parse('12,345.6789123') instanceof Float

        // 0.00
        floatDatatype2.getJavaClass() == Float
        floatDatatype2.format(0.1) == '0.10'
        floatDatatype2.format(12345.6789123) == '12345.68'
        floatDatatype2.parse('12345.6789123') == 12345.679f
        floatDatatype2.parse('12345.6789123') instanceof Float

        // #
        floatDatatype3.getJavaClass() == Float
        floatDatatype3.format(0.1) == '0'
        floatDatatype3.format(12345.6789123) == '12346'
        floatDatatype3.parse('12345.6789123') == 12345.679f
        floatDatatype3.parse('12345.6789123') instanceof Float
    }

    def "adaptive Integer datatype"() {
        def metaClass = metadata.getClass(TestNumberValuesEntity.class)

        def intDatatype1 = metaClass.getProperty('intField1').getRange().asDatatype()
        def intDatatype2 = metaClass.getProperty('intField2').getRange().asDatatype()

        expect:

        // #
        intDatatype1.getJavaClass() == Integer
        intDatatype1.format(12345) == '12345'
        intDatatype1.format(0.1) == '0'
        intDatatype1.format(12345.6789123) == '12346'
        intDatatype1.parse('12345') == 12345
        intDatatype1.parse('12345') instanceof Integer

        // #,##0
        intDatatype2.getJavaClass() == Integer
        intDatatype2.format(12345) == '12,345'
        intDatatype2.format(0.1) == '0'
        intDatatype2.format(12345.6789123) == '12,346'
        intDatatype2.parse('12345') == 12345
        intDatatype2.parse('12345') instanceof Integer
    }

    def "adaptive Long datatype"() {
        def metaClass = metadata.getClass(TestNumberValuesEntity.class)

        def longDatatype1 = metaClass.getProperty('longField1').getRange().asDatatype()
        def longDatatype2 = metaClass.getProperty('longField2').getRange().asDatatype()

        expect:

        // #
        longDatatype1.getJavaClass() == Long
        longDatatype1.format(12345) == '12345'
        longDatatype1.format(0.1) == '0'
        longDatatype1.format(12345.6789123) == '12346'
        longDatatype1.parse('12345') == 12345
        longDatatype1.parse('12345') instanceof Long

        // #,##0
        longDatatype2.getJavaClass() == Long
        longDatatype2.format(12345) == '12,345'
        longDatatype2.format(0.1) == '0'
        longDatatype2.format(12345.6789123) == '12,346'
        longDatatype2.parse('12345') == 12345
        longDatatype2.parse('12345') instanceof Long
    }

    def "adaptive percent datatype"() {
        def metaClass = metadata.getClass(TestNumberValuesEntity.class)

        def percentDatatype = metaClass.getProperty('percentField').getRange().asDatatype()

        expect:

        percentDatatype.getJavaClass() == BigDecimal
        percentDatatype.format(0.123) == '12%'
        percentDatatype.format(0.126) == '13%'
        percentDatatype.format(0) == '0%'
        percentDatatype.parse('12%') == 0.12
        percentDatatype.parse('12%') instanceof BigDecimal

        when:

        percentDatatype.parse('12')

        then:

        thrown(ParseException)
    }

    @Ignore
    def "LocalDate/LocalDateTime/LocalTime/OffsetDateTime/OffsetTime datatypes"() {
        def metaClass = metadata.getClass(LocalDateTimeEntity.class)

        def localDateDatatype = metaClass.getProperty('localDate').getRange().asDatatype()
        def localTimeDatatype = metaClass.getProperty('localTime').getRange().asDatatype()
        def localDateTimeDatatype = metaClass.getProperty('localDateTime').getRange().asDatatype()
        def offsetDateTimeDatatype = metaClass.getProperty('offsetDateTime').getRange().asDatatype()
        def offsetTimeDatatype = metaClass.getProperty('offsetTime').getRange().asDatatype()

        expect:

        localDateDatatype.getJavaClass() == LocalDate
        localDateDatatype.format(LocalDate.of(1987, 10, 01)) == '1987-10-01'
        localDateDatatype.parse('1987-10-01') == LocalDate.of(1987, 10, 01)

        localTimeDatatype.getJavaClass() == LocalTime
        localTimeDatatype.format(LocalTime.of(10, 20, 40)) == '10:20:40'
        localTimeDatatype.parse('10:20:40') == LocalTime.of(10, 20, 40)

        localDateTimeDatatype.getJavaClass() == LocalDateTime
        localDateTimeDatatype.format(LocalDateTime.of(1987, 10, 01, 10, 20, 40)) == '1987-10-01 10:20:40.000'
        localDateTimeDatatype.parse('1987-10-01 10:20:40.000') == LocalDateTime.of(1987, 10, 01, 10, 20, 40)

        offsetTimeDatatype.getJavaClass() == OffsetTime
        offsetTimeDatatype.format(OffsetTime.of(LocalTime.of(10, 20, 40), ZoneOffset.of('Z'))) == '10:20:40 +0000'
        offsetTimeDatatype.parse('10:20:40 +0000') == OffsetTime.of(LocalTime.of(10, 20, 40), ZoneOffset.of('Z'))

        offsetDateTimeDatatype.getJavaClass() == OffsetDateTime
        def offsetDateTime = OffsetDateTime.of(LocalDate.of(1987, 10, 01),
                LocalTime.of(10, 20, 40),
                ZoneOffset.of('Z'))
        offsetDateTimeDatatype.format(offsetDateTime) == '1987-10-01 10:20:40.000 +0000'
        offsetDateTimeDatatype.parse('1987-10-01 10:20:40.000 +0000') == offsetDateTime
    }
}
