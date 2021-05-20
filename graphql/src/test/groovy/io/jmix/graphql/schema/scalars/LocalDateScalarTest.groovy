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

package io.jmix.graphql.schema.scalars

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import io.jmix.graphql.schema.scalar.LocalDateScalar
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateScalarTest extends Specification {

    private final LocalDateScalar scalar = new LocalDateScalar()
    private Coercing coercing

    @SuppressWarnings('unused')
    def setup() {
        coercing = scalar.getCoercing()
    }

    def "localDate scalar test"() {
        given:
        def stringDate = new StringValue("2021-01-01")
        def localDate = LocalDate.from(
                DateTimeFormatter
                        .ISO_LOCAL_DATE
                        .parse(stringDate.getValue())
        )
        def parsedLiteral
        def parsedValue
        def serialized
        def nullParsedLiteral
        def nullParsedValue

        when:
        parsedLiteral = (LocalDate) coercing.parseLiteral(stringDate)
        parsedValue = (LocalDate) coercing.parseValue(stringDate.getValue())
        serialized = coercing.serialize(localDate)
        nullParsedLiteral = (LocalDate) coercing.parseLiteral(new StringValue(""))
        nullParsedValue = (LocalDate) coercing.parseValue("")

        then:
        parsedLiteral.isEqual(localDate)
        parsedValue.isEqual(localDate)
        serialized == stringDate.getValue()
        nullParsedLiteral.isEqual(LocalDate.MIN)
        nullParsedValue.isEqual(LocalDate.MIN)
    }

    def "localDate scalar throws CoercingSerializeException"() {
        when:
        coercing.serialize("")

        then:
        def exception = thrown(CoercingSerializeException)
        exception.message == "Expected type 'LocalDate' but was 'String'."
    }

    def "localDate scalar throws CoercingParseLiteralException with parseLiteral"() {
        when:
        coercing.parseLiteral("")

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'StringValue' but was 'String'."
    }

    def "localDate scalar throws CoercingParseLiteralException with parseValue"() {
        when:
        coercing.parseValue(new StringValue(""))

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'String' but was 'StringValue'."
    }

    def "localDate scalar throws CoercingParseLiteralException with wrong value"() {
        when:
        coercing.parseLiteral(new StringValue("1"))

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Please use the format 'yyyy-MM-dd'"
    }
}
