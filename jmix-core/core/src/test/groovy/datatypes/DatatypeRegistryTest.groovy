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

import io.jmix.core.CoreConfiguration
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.core.metamodel.datatype.impl.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration

import java.time.*

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
class DatatypeRegistryTest extends Specification {

    @Autowired
    ApplicationContext context

    @Autowired
    DatatypeRegistry registry

    def "context contains beans"() {
        when:

        context.getBean('core_BooleanDatatype', BooleanDatatype)
        context.getBean('core_ShortDatatype', ShortDatatype)
        context.getBean('core_IntegerDatatype', IntegerDatatype)
        context.getBean('core_LongDatatype', LongDatatype)
        context.getBean('core_BigIntegerDatatype', BigIntegerDatatype)
        context.getBean('core_BigDecimalDatatype', BigDecimalDatatype)
        context.getBean('core_FloatDatatype', FloatDatatype)
        context.getBean('core_DoubleDatatype', DoubleDatatype)
        context.getBean('core_StringDatatype', StringDatatype)
        context.getBean('core_CharacterDatatype', CharacterDatatype)
        context.getBean('core_DateTimeDatatype', DateTimeDatatype)
        context.getBean('core_DateDatatype', DateDatatype)
        context.getBean('core_TimeDatatype', TimeDatatype)
        context.getBean('core_LocalDateTimeDatatype', LocalDateTimeDatatype)
        context.getBean('core_LocalDateDatatype', LocalDateDatatype)
        context.getBean('core_LocalTimeDatatype', LocalTimeDatatype)
        context.getBean('core_OffsetDateTimeDatatype', OffsetDateTimeDatatype)
        context.getBean('core_OffsetTimeDatatype', OffsetTimeDatatype)
        context.getBean('core_UuidDatatype', UuidDatatype)
        context.getBean('core_ByteArrayDatatype', ByteArrayDatatype)

        then:

        noExceptionThrown()
    }

    def "test"() {

        def booleanDatatype = context.getBean('core_BooleanDatatype', BooleanDatatype)
        def shortDatatype = context.getBean('core_ShortDatatype', ShortDatatype)
        def integerDatatype = context.getBean('core_IntegerDatatype', IntegerDatatype)
        def longDatatype = context.getBean('core_LongDatatype', LongDatatype)
        def bigIntegerDatatype = context.getBean('core_BigIntegerDatatype', BigIntegerDatatype)
        def bigDecimalDatatype = context.getBean('core_BigDecimalDatatype', BigDecimalDatatype)
        def floatDatatype = context.getBean('core_FloatDatatype', FloatDatatype)
        def doubleDatatype = context.getBean('core_DoubleDatatype', DoubleDatatype)
        def stringDatatype = context.getBean('core_StringDatatype', StringDatatype)
        def dateTimeDatatype = context.getBean('core_DateTimeDatatype', DateTimeDatatype)
        def dateDatatype = context.getBean('core_DateDatatype', DateDatatype)
        def timeDatatype = context.getBean('core_TimeDatatype', TimeDatatype)
        def localDateTimeDatatype = context.getBean('core_LocalDateTimeDatatype', LocalDateTimeDatatype)
        def localDateDatatype = context.getBean('core_LocalDateDatatype', LocalDateDatatype)
        def localTimeDatatype = context.getBean('core_LocalTimeDatatype', LocalTimeDatatype)
        def offsetDateTimeDatatype = context.getBean('core_OffsetDateTimeDatatype', OffsetDateTimeDatatype)
        def offsetTimeDatatype = context.getBean('core_OffsetTimeDatatype', OffsetTimeDatatype)
        def uuidDatatype = context.getBean('core_UuidDatatype', UuidDatatype)
        def byteArrayDatatype = context.getBean('core_ByteArrayDatatype', ByteArrayDatatype)

        expect:

        registry.get('boolean') == booleanDatatype
        registry.get('short') == shortDatatype
        registry.get('int') == integerDatatype
        registry.get('long') == longDatatype
        registry.get('bigInteger') == bigIntegerDatatype
        registry.get('decimal') == bigDecimalDatatype
        registry.get('float') == floatDatatype
        registry.get('double') == doubleDatatype
        registry.get('string') == stringDatatype
        registry.get('dateTime') == dateTimeDatatype
        registry.get('date') == dateDatatype
        registry.get('time') == timeDatatype
        registry.get('localDateTime') == localDateTimeDatatype
        registry.get('localDate') == localDateDatatype
        registry.get('localTime') == localTimeDatatype
        registry.get('offsetDateTime') == offsetDateTimeDatatype
        registry.get('offsetTime') == offsetTimeDatatype
        registry.get('uuid') == uuidDatatype
        registry.get('byteArray') == byteArrayDatatype

        registry.find(Boolean) == booleanDatatype
        registry.find(Short) == shortDatatype
        registry.find(Long) == longDatatype
        registry.find(BigInteger) == bigIntegerDatatype
        registry.find(BigDecimal) == bigDecimalDatatype
        registry.find(Float) == floatDatatype
        registry.find(Double) == doubleDatatype
        registry.find(String) == stringDatatype
        registry.find(Date) == dateTimeDatatype
        registry.find(java.sql.Date) == dateDatatype
        registry.find(java.sql.Time) == timeDatatype
        registry.find(LocalDateTime) == localDateTimeDatatype
        registry.find(LocalDate) == localDateDatatype
        registry.find(LocalTime) == localTimeDatatype
        registry.find(OffsetDateTime) == offsetDateTimeDatatype
        registry.find(OffsetTime) == offsetTimeDatatype
        registry.find(UUID) == uuidDatatype
        registry.find(byte[]) == byteArrayDatatype
    }
}
