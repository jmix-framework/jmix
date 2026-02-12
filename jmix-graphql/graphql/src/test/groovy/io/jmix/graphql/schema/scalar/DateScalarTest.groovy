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
import org.apache.commons.lang3.time.DateUtils
import spock.lang.Specification

import java.time.LocalDate
import java.time.ZoneId

class DateScalarTest extends Specification {

    private final DateScalar scalar = new DateScalar()
    private Coercing coercing

    @SuppressWarnings('unused')
    def setup() {
        coercing = scalar.getCoercing()
    }

    def "date scalar test"() {
        given:
        def stringDate = "2021-01-01"
        def temporalAccessor = LocalDate.parse(stringDate).atStartOfDay(ZoneId.systemDefault()).toInstant()
        def date = Date.from(temporalAccessor)
        def parsedLiteral
        def parsedValue
        def serialized

        when:
        parsedLiteral = (Date) this.coercing.parseLiteral(new StringValue(stringDate))
        parsedValue = (Date) this.coercing.parseValue(stringDate)
        serialized = this.coercing.serialize(date)

        then:
        DateUtils.isSameDay(parsedLiteral, date)
        DateUtils.isSameDay(parsedValue, date)
        serialized == stringDate
    }

    def "date scalar should return null on parse empty string" () {
        when:
        def date = coercing.parseLiteral(new StringValue(""))
        then:
        date == null
    }

    def "date scalar coercing throws CoercingSerializeException"() {
        when:
        coercing.serialize("")

        then:
        def exception = thrown(CoercingSerializeException)
        exception.message == "Expected type 'Date' but was 'String'."
    }

    def "date scalar coercing throws CoercingParseLiteralException with parseLiteral"() {
        when:
        coercing.parseLiteral("")

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'StringValue' but was 'String'."
    }

    def "date scalar coercing throws CoercingParseLiteralException with parseValue"() {
        when:
        coercing.parseValue(new StringValue(""))

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'String' but was 'StringValue'."
    }

    def "date scalar throws CoercingParseLiteralException with wrong value"() {
        when:
        coercing.parseLiteral(new StringValue("1"))

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Please use the format 'yyyy-MM-dd'"
    }
}
