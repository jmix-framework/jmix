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
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import io.jmix.graphql.schema.scalar.TimeScalar
import org.apache.commons.lang3.time.DateUtils
import spock.lang.Specification

import static io.jmix.graphql.schema.scalar.CustomScalars.SERIALIZATION_TIME_FORMAT

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

class TimeScalarTest extends Specification {

    def "time scalar test"() {
        given:
        def scalar = new TimeScalar()
        def stringDate = "23:59:59"
        def time = DateUtils.parseDate(stringDate.trim(), SERIALIZATION_TIME_FORMAT.toPattern());
        def coercing = scalar.getCoercing()
        def parsedLiteral
        def parsedValue
        def serialized
        def nullParsedLiteral
        def nullParsedValue
        def midnight = DateUtils.parseDate("00:00:00", SERIALIZATION_TIME_FORMAT.toPattern());

        when:
        parsedLiteral = (Date) coercing.parseLiteral(new StringValue(stringDate))
        parsedValue = (Date) coercing.parseValue(stringDate)
        serialized = coercing.serialize(time)
        nullParsedLiteral = (Date) coercing.parseLiteral(new StringValue(""))
        nullParsedValue = (Date) coercing.parseLiteral(new StringValue(""))

        then:
        DateUtils.isSameDay(parsedLiteral, time)
        DateUtils.isSameDay(parsedValue, time)
        serialized == stringDate
        DateUtils.isSameDay(nullParsedLiteral, midnight)
        DateUtils.isSameDay(nullParsedValue, midnight)
    }

    def "time scalar coercing throws CoercingSerializeException"() {
        given:
        def scalar = new TimeScalar()
        def coercing = scalar.getCoercing()

        when:
        coercing.serialize("")

        then:
        def exception = thrown(CoercingSerializeException)
        exception.message == "Expected type 'Date' but was 'String'."
    }

    def "time scalar coercing throws CoercingParseLiteralException with parseLiteral"() {
        given:
        def scalar = new TimeScalar()
        def coercing = scalar.getCoercing()

        when:
        coercing.parseLiteral("")

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'StringValue' but was 'String'."
    }

    def "time scalar coercing throws CoercingParseLiteralException with parseValue"() {
        given:
        def scalar = new TimeScalar()
        def coercing = scalar.getCoercing()

        when:
        coercing.parseValue(new StringValue(""))

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'String' but was 'StringValue'."
    }
}
