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
import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.beans.factory.annotation.Autowired

import java.sql.Time
import java.time.LocalTime
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

class TimeScalarTest extends AbstractGraphQLTest {

    @Autowired
    ScalarTypes scalarTypes
    Coercing coercing


    @SuppressWarnings('unused')
    def setup() {
        coercing = scalarTypes.timeScalar.getCoercing()
    }

    def "time scalar should parse ISO time of format HH:mm" () {
        when:
        def stringDate = new StringValue("11:12")
        Time time = (Time) coercing.parseLiteral(stringDate)

        then:
        time.toString() == "11:12:00"
    }

    def "time scalar should parse ISO time of format HH:mm:ss" () {
        when:
        def stringDate = new StringValue("11:12:13")
        Time time = (Time) coercing.parseLiteral(stringDate)

        then:
        time.toString() == "11:12:13"
    }

    def "time scalar should return null on parse empty string" () {
        when:
        def stringDate = new StringValue("")
        Time time = (Time) coercing.parseLiteral(stringDate)

        then:
        time == null
    }

    def "time scalar should serialize time"() {
        when:
        def localTime = LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse("23:59:59"))
        Time time = Time.valueOf(localTime)
        def serialized = coercing.serialize(time)
        then:
        serialized == "23:59:59"
    }

    def "time scalar should throws exception on serialize object of incorrect type"() {
        when:
        coercing.serialize("")

        then:
        def exception = thrown(CoercingSerializeException)
        exception.message == "Expected type 'Time' but was 'String'."
    }

    def "parseLiteral throws exception on parse object of incorrect type"() {
        when:
        coercing.parseLiteral("")

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'StringValue' but was 'String'."
    }

    def "parseValue throws exception on parse object of incorrect type"() {
        when:
        coercing.parseValue(new StringValue(""))

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Expected type 'String' but was 'StringValue'."
    }

    def "parseLiteral throws exception on call with wrong value"() {
        when:
        coercing.parseLiteral(new StringValue("1"))

        then:
        def exception = thrown(CoercingParseLiteralException)
        exception.message == "Please use the format 'HH:mm:ss' or 'HH:mm'"
    }
}
