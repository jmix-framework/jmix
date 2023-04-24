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

package navigation

import io.jmix.flowui.view.navigation.UrlParamSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sec.RoleType
import test_support.spec.FlowuiTestSpecification

import java.sql.Time
import java.time.*

@SpringBootTest
class UrlParamSerializerTest extends FlowuiTestSpecification {

    @Autowired
    UrlParamSerializer urlParamSerializer

    def "Positive serialization cases"() {
        def bigDecimalValue = new BigDecimal("10.5")
        def bigIntegerValue = new BigInteger("1234567890")
        def boolValue = true
        def charValue = ('j' as char)
        def dateValue = new java.sql.Date(1672516800000) // 2023-01-01
        def dateTimeValue = new Date(1672516800000) // 2023-01-01 00:00:00
        def doubleValue = 3.14159265358979d
        def enumValue = RoleType.READONLY
        def floatValue = 3.14f
        def intValue = 42
        def localDateValue = LocalDate.of(2023, Month.JANUARY, 1)
        def localDateTimeValue = LocalDateTime.of(2023, Month.JANUARY, 1, 12, 15, 17)
        def localTimeValue = LocalTime.of(12, 15, 17)
        def longValue = 12041961l
        def offsetDateTimeValue = OffsetDateTime.of(localDateTimeValue, ZoneOffset.ofHours(1))
        def offsetTimeValue = OffsetTime.of(localTimeValue, ZoneOffset.ofHours(1))
        def shortValue = (42 as short)
        def stringValue = 'someString'
        def timeValue = new Time(29717000) // 12:15:17
        def uuidValue = UUID.fromString('79c08841-8063-4f85-86d0-25b3410a857c')


        when: "suitable object are passed as id"

        def bigDecimalSerialized = urlParamSerializer.serialize(bigDecimalValue)
        def bigIntegerSerialized = urlParamSerializer.serialize(bigIntegerValue)
        def boolSerialized = urlParamSerializer.serialize(boolValue)
        def charSerialized = urlParamSerializer.serialize(charValue)
        def dateSerialized = urlParamSerializer.serialize(dateValue)
        def dateTimeSerialized = urlParamSerializer.serialize(dateTimeValue)
        def doubleSerialized = urlParamSerializer.serialize(doubleValue)
        def enumSerialized = urlParamSerializer.serialize(enumValue)
        def floatSerialized = urlParamSerializer.serialize(floatValue)
        def intSerialized = urlParamSerializer.serialize(intValue)
        def localDateSerialized = urlParamSerializer.serialize(localDateValue)
        def localDateTimeSerialized = urlParamSerializer.serialize(localDateTimeValue)
        def localTimeSerialized = urlParamSerializer.serialize(localTimeValue)
        def longSerialized = urlParamSerializer.serialize(longValue)
        def offsetDateTimeSerialized = urlParamSerializer.serialize(offsetDateTimeValue)
        def offsetTimeSerialized = urlParamSerializer.serialize(offsetTimeValue)
        def shortSerialized = urlParamSerializer.serialize(shortValue)
        def stringSerialized = urlParamSerializer.serialize(stringValue)
        def timeSerialized = urlParamSerializer.serialize(timeValue)
        def uuidSerialized = urlParamSerializer.serialize(uuidValue)

        then: "ok"

        bigDecimalSerialized == '10.5'
        bigIntegerSerialized == '1234567890'
        boolSerialized == 'true'
        charSerialized == 'j'
        dateSerialized == '1672516800000'
        dateTimeSerialized == '1672516800000'
        doubleSerialized == '3.14159265358979'
        enumSerialized == 'READONLY'
        floatSerialized == '3.14'
        intSerialized == '42'
        localDateSerialized == '2023-01-01'
        localDateTimeSerialized == '2023-01-01T12-15-17'
        localTimeSerialized == '12-15-17'
        longSerialized == '12041961'
        offsetDateTimeSerialized == '2023-01-01T12-15-17+0100'
        offsetTimeSerialized == '12-15-17+0100'
        shortSerialized == '42'
        stringSerialized == stringValue
        timeSerialized == '29717000'
        uuidSerialized == '79c08841-8063-4f85-86d0-25b3410a857c'
    }

    def "Negative serialization cases"() {
        def nullId = null
        def array = [1, 2, 3]

        when: "null passed as id"
        urlParamSerializer.serialize(nullId)

        then: "fail"
        thrown IllegalArgumentException

        when: "array is passed as id"
        urlParamSerializer.serialize(array)

        then: "fail"
        thrown IllegalArgumentException
    }

    def "Negative deserialization cases"() {
        def badIntId = "4b2"
        def badLongId = "120a41q96s1"

        when: "null type and null id are passed for deserialization"
        urlParamSerializer.deserialize(null, null)

        then: "fail"
        thrown IllegalArgumentException

        when: "null id with type are passed for deserialization"
        urlParamSerializer.deserialize(Integer.class, null)

        then: "fail"
        thrown IllegalArgumentException

        when: "id without type is passed for deserialization"
        urlParamSerializer.deserialize(null, "randomString")

        then: "fail"
        thrown IllegalArgumentException

        when: "not int value is passed with int type"
        urlParamSerializer.deserialize(Integer.class, badIntId)

        then: "fail"
        thrown RuntimeException

        when: "not long value is passed with long type"
        urlParamSerializer.deserialize(Long.class, badLongId)

        then: "fail"
        thrown RuntimeException
    }

    def "Serialized and deserialized comparison"() {
        def bigDecimalValue = new BigDecimal("10.5")
        def bigIntegerValue = new BigInteger("1234567890")
        def boolValue = true
        def charValue = ('j' as char)
        def dateValue = new java.sql.Date(1672516800000) // 2023-01-01
        def dateTimeValue = new Date(1672516800000) // 2023-01-01 00:00:00
        def doubleValue = 3.14159265358979d
        def enumValue = RoleType.READONLY
        def floatValue = 3.14f
        def intValue = 42
        def localDateValue = LocalDate.of(2023, Month.JANUARY, 1)
        def localDateTimeValue = LocalDateTime.of(2023, Month.JANUARY, 1, 12, 15, 17)
        def localTimeValue = LocalTime.of(12, 15, 17)
        def longValue = 12041961l
        def offsetDateTimeValue = OffsetDateTime.of(localDateTimeValue, ZoneOffset.ofHours(1))
        def offsetTimeValue = OffsetTime.of(localTimeValue, ZoneOffset.ofHours(1))
        def shortValue = (42 as short)
        def stringValue = 'someString'
        def timeValue = new Time(29717000) // 12:15:17
        def uuidValue = UUID.fromString('79c08841-8063-4f85-86d0-25b3410a857c')

        when: "sequence of serialization and deserialization"

        def bigDecimalSerialized = urlParamSerializer.serialize(bigDecimalValue)
        def bigDecimalDeserialized = urlParamSerializer.deserialize(BigDecimal.class, bigDecimalSerialized)

        def bigIntegerSerialized = urlParamSerializer.serialize(bigIntegerValue)
        def bigIntegerDeserialized = urlParamSerializer.deserialize(BigInteger.class, bigIntegerSerialized)

        def boolSerialized = urlParamSerializer.serialize(boolValue)
        def boolDeserialized = urlParamSerializer.deserialize(Boolean.class, boolSerialized)

        def charSerialized = urlParamSerializer.serialize(charValue)
        def charDeserialized = urlParamSerializer.deserialize(Character.class, charSerialized)

        def dateSerialized = urlParamSerializer.serialize(dateValue)
        def dateDeserialized = urlParamSerializer.deserialize(java.sql.Date.class, dateSerialized)

        def dateTimeSerialized = urlParamSerializer.serialize(dateTimeValue)
        def dateTimeDeserialized = urlParamSerializer.deserialize(Date.class, dateTimeSerialized)

        def doubleSerialized = urlParamSerializer.serialize(doubleValue)
        def doubleDeserialized = urlParamSerializer.deserialize(Double.class, doubleSerialized)

        def enumSerialized = urlParamSerializer.serialize(enumValue)
        def enumDeserialized = urlParamSerializer.deserialize(RoleType.class, enumSerialized)

        def floatSerialized = urlParamSerializer.serialize(floatValue)
        def floatDeserialized = urlParamSerializer.deserialize(Float.class, floatSerialized)

        def intSerialized = urlParamSerializer.serialize(intValue)
        def intDeserialized = urlParamSerializer.deserialize(Integer.class, intSerialized)

        def localDateSerialized = urlParamSerializer.serialize(localDateValue)
        def localDateDeserialized = urlParamSerializer.deserialize(LocalDate.class, localDateSerialized)

        def localDateTimeSerialized = urlParamSerializer.serialize(localDateTimeValue)
        def localDateTimeDeserialized = urlParamSerializer.deserialize(LocalDateTime.class, localDateTimeSerialized)

        def localTimeSerialized = urlParamSerializer.serialize(localTimeValue)
        def localTimeDeserialized = urlParamSerializer.deserialize(LocalTime.class, localTimeSerialized)

        def longSerialized = urlParamSerializer.serialize(longValue)
        def longDeserialized = urlParamSerializer.deserialize(Long.class, longSerialized)

        def offsetDateTimeSerialized = urlParamSerializer.serialize(offsetDateTimeValue)
        def offsetDateTimeDeserialized = urlParamSerializer.deserialize(OffsetDateTime.class, offsetDateTimeSerialized)

        def offsetTimeSerialized = urlParamSerializer.serialize(offsetTimeValue)
        def offsetTimeDeserialized = urlParamSerializer.deserialize(OffsetTime.class, offsetTimeSerialized)

        def shortSerialized = urlParamSerializer.serialize(shortValue)
        def shortDeserialized = urlParamSerializer.deserialize(Short.class, shortSerialized)

        def stringSerialized = urlParamSerializer.serialize(stringValue)
        def stringDeserialized = urlParamSerializer.deserialize(String.class, stringSerialized)

        def timeSerialized = urlParamSerializer.serialize(timeValue)
        def timeDeserialized = urlParamSerializer.deserialize(Time.class, timeSerialized)

        def uuidSerialized = urlParamSerializer.serialize(uuidValue)
        def uuidDeserialized = urlParamSerializer.deserialize(UUID.class, uuidSerialized)

        then: "deserialized values should be equal to initial values"

        bigDecimalValue == bigDecimalDeserialized
        bigIntegerValue == bigIntegerDeserialized
        boolValue == boolDeserialized
        charValue == charDeserialized
        dateValue == dateDeserialized
        dateTimeValue == dateTimeDeserialized
        doubleValue == doubleDeserialized
        enumValue == enumDeserialized
        floatValue == floatDeserialized
        intValue == intDeserialized
        localDateValue == localDateDeserialized
        localDateTimeValue == localDateTimeDeserialized
        localTimeValue == localTimeDeserialized
        longValue == longDeserialized
        offsetDateTimeValue == offsetDateTimeDeserialized
        offsetTimeValue == offsetTimeDeserialized
        shortValue == shortDeserialized
        stringValue == stringDeserialized
        timeValue == timeDeserialized
        uuidValue == uuidDeserialized
    }
}
