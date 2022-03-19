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
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["eclipselink.logging.level.sql = FINE"])
class DateQueryTest extends AbstractGraphQLTest {

    def "should return a correct formatted date"() {
        when:
        def response = query(
                "datafetcher/date/testEntity-dates-with-filter.gql",
                asObjectNode('{"filter": {"id": {"_eq": "032fd8a5-e042-4828-a802-36cbd2ce12de"}}}'))

        then:
        response.get('$.data.scr_DatatypesTestEntityList[0].dateAttr') == "2020-01-01"
        response.get('$.data.scr_DatatypesTestEntityList[0].timeAttr') == "11:11:11"
        response.get('$.data.scr_DatatypesTestEntityList[0].dateTimeAttr') == "2020-01-01T11:11:11"
        response.get('$.data.scr_DatatypesTestEntityList[0].localDateAttr') == "2020-01-01"
        response.get('$.data.scr_DatatypesTestEntityList[0].localTimeAttr') == "11:11:11"
        response.get('$.data.scr_DatatypesTestEntityList[0].localDateTimeAttr') == "2020-01-01T11:11:11"
        response.get('$.data.scr_DatatypesTestEntityList[0].offsetDateTimeAttr') == "2020-01-01T11:11:11+04:00"
        response.get('$.data.scr_DatatypesTestEntityList[0].offsetTimeAttr') == "11:11:11+04:00"
    }

}
