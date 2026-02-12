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

    def "should upsert time fields with seconds"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypes-test-entity.graphql",
                asObjectNode(
                        """{"entity":{
                                    "timeAttr":"11:22:33",
                                    "localTimeAttr":"22:11:01",
                                    "offsetTimeAttr":"12:21:10+04:00"
                            }}"""))
        then:
        response.get('$.data.upsert_scr_DatatypesTestEntity.timeAttr') == "11:22:33"
        response.get('$.data.upsert_scr_DatatypesTestEntity.localTimeAttr') == "22:11:01"
        response.get('$.data.upsert_scr_DatatypesTestEntity.offsetTimeAttr') == "12:21:10+04:00"
    }

    def "should upsert time fields without seconds"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypes-test-entity.graphql",
                asObjectNode(
                        """{"entity":{
                                    "timeAttr":"11:22",
                                    "localTimeAttr":"22:11",
                                    "offsetTimeAttr":"12:21+04:00"
                            }}"""))
        then:
        response.get('$.data.upsert_scr_DatatypesTestEntity.timeAttr') == "11:22:00"
        response.get('$.data.upsert_scr_DatatypesTestEntity.localTimeAttr') == "22:11:00"
        response.get('$.data.upsert_scr_DatatypesTestEntity.offsetTimeAttr') == "12:21:00+04:00"
    }

    def "should create entity with date types"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypes-test-entity.graphql",
                asObjectNode("""{
                "entity":{
                    "dateAttr": "2021-01-01",
                    "dateTimeAttr":"2011-12-03T10:15:30",
                    "localDateAttr":"2021-01-01",
                    "localDateTimeAttr":"2021-06-06T12:31:00"
                }}"""))

        then:
        response.get('$.data.upsert_scr_DatatypesTestEntity.dateAttr') == "2021-01-01"
        response.get('$.data.upsert_scr_DatatypesTestEntity.dateTimeAttr') == "2011-12-03T10:15:30"
        response.get('$.data.upsert_scr_DatatypesTestEntity.localDateAttr') == "2021-01-01"
        response.get('$.data.upsert_scr_DatatypesTestEntity.localDateTimeAttr') == "2021-06-06T12:31:00"
        // OffsetDateTime is checked in test below
        response.get('$.data.upsert_scr_DatatypesTestEntity.offsetDateTimeAttr') == null
    }

    @Ignore // todo https://github.com/Haulmont/jmix-graphql/issues/162
    def "should create entity with OffsetDateTime attribute"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypes-test-entity.graphql",
                asObjectNode("""{"entity":{"offsetDateTimeAttr": "2011-12-03T11:15:30+04:00"}}"""))
        then:
        response.get('$.data.upsert_scr_DatatypesTestEntity.offsetDateTimeAttr') == "2011-12-03T11:15:30+04:00"
    }
}
