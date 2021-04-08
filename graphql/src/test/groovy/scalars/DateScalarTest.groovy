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

package scalars

import graphql.language.StringValue
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import io.jmix.graphql.schema.scalar.DateScalar
import org.apache.commons.lang3.time.DateUtils
import spock.lang.Specification

import java.time.Instant
import java.time.format.DateTimeFormatter

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

class DateScalarTest extends Specification {

    def "date scalar test"() {
        given:
        def scalar = new DateScalar()
        def stringDate = new StringValue("2021-01-01T23:59:59Z")
        def temporalAccessor = DateTimeFormatter.ISO_DATE_TIME.parse(stringDate.getValue())
        def date = Date.from(Instant.from(temporalAccessor))
        def coercing = scalar.getCoercing()
        def parsedLiteral
        def parsedValue
        def serialized
        def nullParsedLiteral
        def nullParsedValue

        when:
        parsedLiteral = (Date) coercing.parseLiteral(stringDate)
        parsedValue = (Date) coercing.parseValue(stringDate)
        serialized = coercing.serialize(date)
        nullParsedLiteral = (Date) coercing.parseLiteral(new StringValue(""))
        nullParsedValue = (Date) coercing.parseLiteral(new StringValue(""))

        then:
        DateUtils.isSameDay(parsedLiteral, date)
        DateUtils.isSameDay(parsedValue, date)
        serialized == stringDate.getValue()
        DateUtils.isSameDay(nullParsedLiteral, Date.from(Instant.EPOCH))
        DateUtils.isSameDay(nullParsedValue, Date.from(Instant.EPOCH))
    }

    def "date scalar coercing throws CoercingSerializeException"() {
        given:
        def scalar = new DateScalar()
        def coercing = scalar.getCoercing()

        when:
        coercing.serialize("")

        then:
        def exception = thrown(CoercingSerializeException)
        exception.message == "Expected type 'Date' but was 'String'."
    }

    def "date scalar coercing throws CoercingParseLiteralException with parseLiteral"() {
        given:
        def scalar = new DateScalar()
        def coercing = scalar.getCoercing()

        when:
        coercing.parseLiteral("")

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'StringValue' but was 'String'."
    }

    def "date scalar coercing throws CoercingParseLiteralException with parseValue"() {
        given:
        def scalar = new DateScalar()
        def coercing = scalar.getCoercing()

        when:
        coercing.parseValue("")

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'StringValue' but was 'String'."
    }
}
