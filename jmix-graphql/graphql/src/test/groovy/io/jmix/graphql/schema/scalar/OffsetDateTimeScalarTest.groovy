/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql.schema.scalar

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeScalarTest extends Specification {

    private final OffsetDateTimeScalar scalar = new OffsetDateTimeScalar()
    private Coercing coercing

    @SuppressWarnings('unused')
    def setup() {
        coercing = scalar.getCoercing()
    }

    def "OffsetDateTime scalar test"() {
        given:
        def stringDate = new StringValue("2021-01-01T23:59:59+04:00")
        def offsetDateTime = OffsetDateTime.from(
                DateTimeFormatter
                        .ISO_OFFSET_DATE_TIME
                        .parse(stringDate.getValue())
        )
        def parsedLiteral
        def parsedValue
        def serialized

        when:
        parsedLiteral = (OffsetDateTime) coercing.parseLiteral(stringDate)
        parsedValue = (OffsetDateTime) coercing.parseValue(stringDate.getValue())
        serialized = coercing.serialize(offsetDateTime)

        then:
        parsedLiteral.isEqual(offsetDateTime)
        parsedValue.isEqual(offsetDateTime)
        serialized == stringDate.getValue()
    }

    def "offsetDateTime scalar should return null on parse empty string" () {
        when:
        def date = coercing.parseLiteral(new StringValue(""))
        then:
        date == null
    }

    def "OffsetDateTime scalar throws CoercingSerializeException"() {
        when:
        coercing.serialize("")

        then:
        def exception = thrown(CoercingSerializeException)
        exception.message == "Expected type 'OffsetDateTime' but was 'String'."
    }

    def "OffsetDateTime scalar throws CoercingParseLiteralException with parseLiteral"() {
        when:
        coercing.parseLiteral("")

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'StringValue' but was 'String'."
    }

    def "OffsetDateTime scalar throws CoercingParseLiteralException with parseValue"() {
        when:
        coercing.parseValue(new StringValue(""))

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'String' but was 'StringValue'."
    }

    def "OffsetDateTime scalar throws CoercingParseLiteralException with wrong value"() {
        when:
        coercing.parseLiteral(new StringValue("1"))

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Please use the format 'yyyy-MM-dd'T'HH:mm:ss+hh:mm'"
    }
}
