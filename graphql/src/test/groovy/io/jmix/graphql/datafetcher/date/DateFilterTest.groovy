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
import spock.lang.Ignore

@TestPropertySource(properties = ["eclipselink.logging.level.sql = FINE"])
class DateFilterTest extends AbstractGraphQLTest {

    def "_eq for date"() {
        when:
        //where dateAttr = "2020-03-03"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_eq": "2020-03-03"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_eq for time"() {
        when:
        //where timeAttr = "03:03:03"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"timeAttr": {"_eq": "03:03:03"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_eq for dateTime"() {
        when:
        //where dateTimeAttr = "2020-03-03T03:03:03"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_eq": "2020-03-03T03:03:03"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_eq for localDate"() {
        when:
        //where localDateAttr = "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_eq": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_eq for localDateTime"() {
        when:
        //where localDateTimeAttr = "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_eq": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_eq for localTime"() {
        when:
        //where localTimeAttr = "22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_eq": "22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_eq for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr = "2020-02-02T22:22:22+04:00"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_eq": "2020-02-02T22:22:22+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_eq for offsetTime"() {
        when:
        //where offsetTimeAttr = "11:11:11+04:00"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetTimeAttr": {"_eq": "11:11:11+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    def "_neq for date"() {
        when:
        //where dateAttr <> "2020-03-03"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_neq": "2020-03-03"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_neq for time"() {
        when:
        //where timeAttr <> "03:03:03"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"timeAttr": {"_neq": "03:03:03"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_neq for dateTime"() {
        when:
        //where dateTimeAttr <> "2020-03-03T03:03:03"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_neq": "2020-03-03T03:03:03"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_neq for localDate"() {
        when:
        //where localDateAttr <> "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_neq": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_neq for localDateTime"() {
        when:
        //where localDateTimeAttr <> "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_neq": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_neq for localTime"() {
        when:
        //where localTimeAttr <> "22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_neq": "22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_neq for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr <> "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_neq": "2020-02-02T22:22:22+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_neq for offsetTime"() {
        when:
        //where offsetTimeAttr <> "11:11:11+04:00"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetTimeAttr": {"_neq": "11:11:11+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_gt for date"() {
        when:
        //where dateAttr > "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_gt": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_gt for time"() {
        when:
        //where timeAttr > "11:11:11"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"timeAttr": {"_gt": "11:11:11"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_gt for dateTime"() {
        when:
        //where dateTimeAttr > "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_gt": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_gt for localDate"() {
        when:
        //where localDateAttr > "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_gt": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_gt for localDateTime"() {
        when:
        //where localDateTimeAttr > "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_gt": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_gt for localTime"() {
        when:
        //where localTimeAttr > "11:11:11"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_gt": "11:11:11"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_gt for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr > "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_gt": "2020-02-02T22:22:22+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_gt for offsetTime"() {
        when:
        //where offsetTimeAttr > "11:11:11+04:00"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetTimeAttr": {"_gt": "11:11:11+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_gte for date"() {
        when:
        //where dateAttr >= "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_gte": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_gte for time"() {
        when:
        //where timeAttr >= "11:11:11"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"timeAttr": {"_gte": "11:11:11"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_gte for dateTime"() {
        when:
        //where dateTimeAttr >= "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_gte": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_gte for localDate"() {
        when:
        //where localDateAttr >= "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_gte": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_gte for localDateTime"() {
        when:
        //where localDateTimeAttr >= "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_gte": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_gte for localTime"() {
        when:
        //where localTimeAttr >= "11:11:11"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_gte": "11:11:11"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_gte for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr >= "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_gte": "2020-02-02T22:22:22+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_gte for offsetTime"() {
        when:
        //where offsetTimeAttr >= "11:11:11+04:00"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetTimeAttr": {"_gte": "11:11:11+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_lt for date"() {
        when:
        //where dateAttr < "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_lt": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    def "_lt for time"() {
        when:
        //where timeAttr < "11:11:11"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"timeAttr": {"_lt": "11:11:11"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_lt for dateTime"() {
        when:
        //where dateTimeAttr < "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_lt": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    def "_lt for localDate"() {
        when:
        //where localDateAttr < "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_lt": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    def "_lt for localDateTime"() {
        when:
        //where localDateTimeAttr < "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_lt": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    def "_lt for localTime"() {
        when:
        //where localTimeAttr < "11:11:11"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_lt": "11:11:11"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_lt for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr < "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_lt": "2020-02-02T22:22:22+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_lt for offsetTime"() {
        when:
        //where offsetTimeAttr < "11:11:11+04:00"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetTimeAttr": {"_lt": "11:11:11+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_lte for date"() {
        when:
        //where dateAttr <= "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_lte": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_lte for time"() {
        when:
        //where timeAttr <= "11:11:11"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"timeAttr": {"_lte": "11:11:11"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_lte for dateTime"() {
        when:
        //where dateTimeAttr <= "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_lte": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_lte for localDate"() {
        when:
        //where localDateAttr <= "2020-02-02"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_lte": "2020-02-02"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_lte for localDateTime"() {
        when:
        //where localDateTimeAttr <= "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_lte": "2020-02-02T22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_lte for localTime"() {
        when:
        //where localTimeAttr <= "11:11:11"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_lte": "11:11:11"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_lte for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr <= "2020-02-02T22:22:22"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_lte": "2020-02-02T22:22:22+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_lte for offsetTime"() {
        when:
        //where offsetTimeAttr <= "11:11:11+04:00"
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetTimeAttr": {"_lte": "11:11:11+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_in for date"() {
        when:
        //where dateAttr in ("2020-02-02", "2020-03-03")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_in": ["2020-02-02", "2020-03-03"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_in for dateTime"() {
        when:
        //where dateTimeAttr in ("2020-02-02T22:22:22", "2020-03-03T03:03:03")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_in": ["2020-02-02T22:22:22", "2020-03-03T03:03:03"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_in for localDate"() {
        when:
        //where localDateAttr in ("2020-03-03", "2020-02-02")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_in": ["2020-03-03", "2020-02-02"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_in for localDateTime"() {
        when:
        //where localDateTimeAttr in ("2020-02-02T22:22:22", "2020-01-01T11:11:11")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_in": ["2020-02-02T22:22:22", "2020-01-01T11:11:11"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_in for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr in ("2020-02-02T22:22:22+04:00", "2020-01-01T11:11:11+04:00")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_in": ["2020-02-02T22:22:22+04:00", "2020-01-01T11:11:11+04:00"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_notIn for date"() {
        when:
        //where dateAttr not in ("2020-02-02", "2020-03-03")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_notIn": ["2020-02-02", "2020-03-03"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    def "_notIn for dateTime"() {
        when:
        //where dateTimeAttr not in ("2020-02-02T22:22:22", "2020-03-03T03:03:03")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_notIn": ["2020-02-02T22:22:22", "2020-03-03T03:03:03"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    def "_notIn for localDate"() {
        when:
        //where localDateAttr in ("2020-03-03", "2020-02-02")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_notIn": ["2020-03-03", "2020-02-02"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    def "_notIn for localDateTime"() {
        when:
        //where localDateTimeAttr in ("2020-02-02T22:22:22", "2020-01-01T11:11:11")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_notIn": ["2020-02-02T22:22:22", "2020-01-01T11:11:11"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    //todo will work after bug fixed in jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
    @Ignore
    def "_notIn for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr not in ("2020-02-02T22:22:22+04:00", "2020-01-01T11:11:11+04:00")
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_notIn": ["2020-02-02T22:22:22+04:00", "2020-01-01T11:11:11+04:00"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (true) for date"() {
        when:
        //where dateAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_isNull": true}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for time"() {
        when:
        //where timeAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"timeAttr": {"_isNull": true}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for dateTime"() {
        when:
        //where dateTimeAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_isNull": true}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for localDate"() {
        when:
        //where localDateAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_isNull": true}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for localDateTime"() {
        when:
        //where localDateTimeAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_isNull": true}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for localTime"() {
        when:
        //where localTimeAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_isNull": true}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_isNull": true}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for offsetTime"() {
        when:
        //where offsetTimeAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetTimeAttr": {"_isNull": true}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (false) for date"() {
        when:
        //where dateAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateAttr": {"_isNull": false}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (false) for time"() {
        when:
        //where timeAttr is not null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"timeAttr": {"_isNull": false}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (false) for dateTime"() {
        when:
        //where dateTimeAttr is not null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"dateTimeAttr": {"_isNull": false}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (false) for localDate"() {
        when:
        //where localDateAttr is not null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateAttr": {"_isNull": false}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (false) for localDateTime"() {
        when:
        //where localDateTimeAttr is not null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_isNull": false}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (false) for localTime"() {
        when:
        //where localTimeAttr is null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_isNull": false}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (false) for offsetDateTime"() {
        when:
        //where offsetDateTimeAttr is not null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_isNull": false}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (false) for offsetTime"() {
        when:
        //where offsetTimeAttr is not null
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetTimeAttr": {"_isNull": false}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }
}
