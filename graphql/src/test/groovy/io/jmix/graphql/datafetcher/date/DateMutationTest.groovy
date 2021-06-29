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

package io.jmix.graphql.datafetcher.date

import io.jmix.graphql.AbstractGraphQLTest
import spock.lang.Ignore

class DateMutationTest extends AbstractGraphQLTest {

    def "should create entity with date types"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypes-test-entity.graphql",
                asObjectNode("""{
                "entity":{
                    "dateAttr": "2021-01-01",
                    "dateTimeAttr":"2011-12-03T10:15:30",
                    "timeAttr":"10:33:12",
                    "localDateTimeAttr":"2021-06-06T12:31:00",
                    "localDateAttr":"2021-01-01",
                    "localTimeAttr":"10:33:12",
                    "offsetTimeAttr":"10:33:12+04:00",
                    "id":"6a538099-9dfd-8761-fa32-b496c236dbe9"
                }}"""))

        then:
        getBody(response) == '{"data":{"upsert_scr_DatatypesTestEntity":{' +
                '"id":"6a538099-9dfd-8761-fa32-b496c236dbe9",' +
                '"dateAttr":"2021-01-01",' +
                '"dateTimeAttr":"2011-12-03T10:15:30",' +
                '"timeAttr":"10:33:12",' +
                '"localDateTimeAttr":"2021-06-06T12:31:00",' +
                '"offsetDateTimeAttr":null,' +
                '"localDateAttr":"2021-01-01",' +
                '"localTimeAttr":"10:33:12",' +
                '"offsetTimeAttr":"10:33:12+04:00"' +
                '}}}'
    }

    @Ignore // todo https://github.com/Haulmont/jmix-graphql/issues/162
    def "should create entity with OffsetDateTime attribute"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypes-test-entity.graphql",
                asObjectNode("""{
                "entity":{
                    "offsetDateTimeAttr": "2011-12-03T11:15:30+04:00",
                    "id":"6a538099-9dfd-8761-fa32-b496c236dbe9"
                }}"""))

        then:
        response.get('$.data.upsert_scr_DatatypesTestEntity.offsetDateTimeAttr') == "2011-12-03T11:15:30+04:00"
    }
}
