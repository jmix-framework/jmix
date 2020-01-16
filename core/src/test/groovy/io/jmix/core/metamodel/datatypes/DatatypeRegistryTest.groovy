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

package io.jmix.core.metamodel.datatypes


import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.metamodel.datatypes.impl.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime

@ContextConfiguration(classes = [JmixCoreConfiguration])
class DatatypeRegistryTest extends Specification {

    @Autowired
    ApplicationContext context

    @Inject
    DatatypeRegistry registry

    def "context contains beans"() {
        when:

        context.getBean('jmix_BooleanDatatype', BooleanDatatype)
        context.getBean('jmix_IntegerDatatype', IntegerDatatype)
        context.getBean('jmix_LongDatatype', LongDatatype)
        context.getBean('jmix_BigDecimalDatatype', BigDecimalDatatype)
        context.getBean('jmix_DoubleDatatype', DoubleDatatype)
        context.getBean('jmix_StringDatatype', StringDatatype)
        context.getBean('jmix_DateTimeDatatype', DateTimeDatatype)
        context.getBean('jmix_DateDatatype', DateDatatype)
        context.getBean('jmix_TimeDatatype', TimeDatatype)
        context.getBean('jmix_LocalDateTimeDatatype', LocalDateTimeDatatype)
        context.getBean('jmix_LocalDateDatatype', LocalDateDatatype)
        context.getBean('jmix_LocalTimeDatatype', LocalTimeDatatype)
        context.getBean('jmix_OffsetDateTimeDatatype', OffsetDateTimeDatatype)
        context.getBean('jmix_OffsetTimeDatatype', OffsetTimeDatatype)
        context.getBean('jmix_UuidDatatype', UuidDatatype)
        context.getBean('jmix_ByteArrayDatatype', ByteArrayDatatype)

        then:

        noExceptionThrown()
    }

    def "test"() {

        def booleanDatatype = context.getBean('jmix_BooleanDatatype', BooleanDatatype)
        def integerDatatype = context.getBean('jmix_IntegerDatatype', IntegerDatatype)
        def longDatatype = context.getBean('jmix_LongDatatype', LongDatatype)
        def bigDecimalDatatype = context.getBean('jmix_BigDecimalDatatype', BigDecimalDatatype)
        def doubleDatatype = context.getBean('jmix_DoubleDatatype', DoubleDatatype)
        def stringDatatype = context.getBean('jmix_StringDatatype', StringDatatype)
        def dateTimeDatatype = context.getBean('jmix_DateTimeDatatype', DateTimeDatatype)
        def dateDatatype = context.getBean('jmix_DateDatatype', DateDatatype)
        def timeDatatype = context.getBean('jmix_TimeDatatype', TimeDatatype)
        def localDateTimeDatatype = context.getBean('jmix_LocalDateTimeDatatype', LocalDateTimeDatatype)
        def localDateDatatype = context.getBean('jmix_LocalDateDatatype', LocalDateDatatype)
        def localTimeDatatype = context.getBean('jmix_LocalTimeDatatype', LocalTimeDatatype)
        def offsetDateTimeDatatype = context.getBean('jmix_OffsetDateTimeDatatype', OffsetDateTimeDatatype)
        def offsetTimeDatatype = context.getBean('jmix_OffsetTimeDatatype', OffsetTimeDatatype)
        def uuidDatatype = context.getBean('jmix_UuidDatatype', UuidDatatype)
        def byteArrayDatatype = context.getBean('jmix_ByteArrayDatatype', ByteArrayDatatype)

        expect:

        registry.get('boolean') == booleanDatatype
        registry.get('int') == integerDatatype
        registry.get('long') == longDatatype
        registry.get('decimal') == bigDecimalDatatype
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

        registry.get(Boolean) == booleanDatatype
        registry.get(Long) == longDatatype
        registry.get(BigDecimal) == bigDecimalDatatype
        registry.get(Double) == doubleDatatype
        registry.get(String) == stringDatatype
        registry.get(Date) == dateTimeDatatype
        registry.get(java.sql.Date) == dateDatatype
        registry.get(java.sql.Time) == timeDatatype
        registry.get(LocalDateTime) == localDateTimeDatatype
        registry.get(LocalDate) == localDateDatatype
        registry.get(LocalTime) == localTimeDatatype
        registry.get(OffsetDateTime) == offsetDateTimeDatatype
        registry.get(OffsetTime) == offsetTimeDatatype
        registry.get(UUID) == uuidDatatype
        registry.get(byte[]) == byteArrayDatatype
    }
}
