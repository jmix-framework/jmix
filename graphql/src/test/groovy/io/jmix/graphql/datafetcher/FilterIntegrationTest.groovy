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

package io.jmix.graphql.datafetcher

import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["eclipselink.logging.level.sql = FINE"])
class FilterIntegrationTest extends AbstractGraphQLTest {

    def "_eq for numbers"() {
        when:
        //where capacity = 50
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"capacity": {"_eq": "50"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"}' +
                ']}}'
    }

    def "_eq for string with strict case"() {
        when:
        //where name = "Hillwood City"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_eq": "Hillwood City"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_eq for string with ignore case"() {
        when:
        //where name = "hillwood city"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_eq": "hillwood city"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[]}}'
    }

    def "_eq for boolean"() {
        when:
        //where vanEntry = false
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"vanEntry": {"_eq": "false"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"}' +
                ']}}'
    }

    def "_eq for UUID"() {
        when:
        //where id = "bfe41616-f03d-f287-1397-8619f5dde390"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"id": {"_eq": "bfe41616-f03d-f287-1397-8619f5dde390"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"}' +
                ']}}'
    }

    def "_eq for date"() {
        when:
        //where dateAttr = "2020-03-03"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_eq": "22:22:22"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    //todo uncomment when the bug will be fixed in the jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
//    def "_eq for offsetDateTime"() {
//        when:
//        //where offsetDateTimeAttr = "2020-02-02T22:22:22+04:00"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetDateTimeAttr": {"_eq": "2020-02-02T22:22:22+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
//                ']}}'
//    }
//
//    def "_eq for offsetTime"() {
//        when:
//        //where offsetTimeAttr = "11:11:11+04:00"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetTimeAttr": {"_eq": "11:11:11+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
//                ']}}'
//    }

    def "_neq for numbers"() {
        when:
        //where capacity <> 50
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"capacity": {"_neq": "50"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_neq for string with strict case"() {
        when:
        //where name <> "Hillwood City"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_neq": "Hillwood City"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"}' +
                ']}}'
    }

    def "_neq for string with ignore case"() {
        when:
        //where name <> "hillwood city"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_neq": "hillwood city"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_neq for boolean"() {
        when:
        //where vanEntry <> false
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"vanEntry": {"_neq": "false"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_neq for UUID"() {
        when:
        //where id <> "bfe41616-f03d-f287-1397-8619f5dde390"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"id": {"_neq": "bfe41616-f03d-f287-1397-8619f5dde390"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_neq for date"() {
        when:
        //where dateAttr <> "2020-03-03"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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

    //todo uncomment when the bug will be fixed in the jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
//    def "_neq for offsetDateTime"() {
//        when:
//        //where offsetDateTimeAttr <> "2020-02-02T22:22:22"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetDateTimeAttr": {"_neq": "2020-02-02T22:22:22+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
//                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
//                ']}}'
//    }
//
//    def "_neq for offsetTime"() {
//        when:
//        //where offsetTimeAttr <> "11:11:11+04:00"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetTimeAttr": {"_neq": "11:11:11+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
//                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
//                ']}}'
//    }

    def "_gt for numbers"() {
        when:
        //where capacity > 50
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"capacity": {"_gt": "50"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"}' +
                ']}}'
    }

    def "_gt for date"() {
        when:
        //where dateAttr > "2020-02-02"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetDateTimeAttr": {"_gt": "2020-02-02T22:22:22+04:00"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    //todo: will work after bug fixing in jmix-core
//    def "_gt for offsetTime"() {
//        when:
//        //where offsetTimeAttr > "11:11:11+04:00"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetTimeAttr": {"_gt": "11:11:11+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
//                ']}}'
//    }

    def "_gte for numbers"() {
        when:
        //where capacity >= 50
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"capacity": {"_gte": "50"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"}' +
                ']}}'
    }

    def "_gte for date"() {
        when:
        //where dateAttr >= "2020-02-02"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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

    //todo uncomment when the bug will be fixed in the jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
//    def "_gte for offsetDateTime"() {
//        when:
//        //where offsetDateTimeAttr >= "2020-02-02T22:22:22"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetDateTimeAttr": {"_gte": "2020-02-02T22:22:22+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
//                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
//                ']}}'
//    }
//
//    def "_gte for offsetTime"() {
//        when:
//        //where offsetTimeAttr >= "11:11:11+04:00"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetTimeAttr": {"_gte": "11:11:11+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
//                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
//                ']}}'
//    }

    def "_lt for numbers"() {
        when:
        //where capacity < 50
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"capacity": {"_lt": "50"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_lt for date"() {
        when:
        //where dateAttr < "2020-02-02"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localTimeAttr": {"_lt": "11:11:11"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    //todo uncomment when the bug will be fixed in the jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
//    def "_lt for offsetDateTime"() {
//        when:
//        //where offsetDateTimeAttr < "2020-02-02T22:22:22"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetDateTimeAttr": {"_lt": "2020-02-02T22:22:22+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
//                ']}}'
//    }
//
//    def "_lt for offsetTime"() {
//        when:
//        //where offsetTimeAttr < "11:11:11+04:00"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetTimeAttr": {"_lt": "11:11:11+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
//                ']}}'
//    }

    def "_lte for numbers"() {
        when:
        //where capacity <= 50
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"capacity": {"_lte": "50"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_lte for date"() {
        when:
        //where dateAttr <= "2020-02-02"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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

    //todo: will work after bug fixing in jmix-core
//    def "_lte for offsetTime"() {
//        when:
//        //where offsetTimeAttr <= "11:11:11+04:00"
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetTimeAttr": {"_lte": "11:11:11+04:00"}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
//                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
//                ']}}'
//    }

    def "_contains for string with strict case"() {
        when:
        //where name like "%Hillwood%"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_contains": "Hillwood"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_contains for string with ignore case"() {
        when:
        //where name like "%hillwood%"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_contains": "hillwood"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_notContains for string with strict case"() {
        when:
        //where name not like "%Hillwood%"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_notContains": "Hillwood"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"}' +
                ']}}'
    }

    def "_notContains for string with ignore case"() {
        when:
        //where name not like "%hillwood%"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_notContains": "hillwood"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"}' +
                ']}}'
    }

    def "_startsWith for string with strict case"() {
        when:
        //where name like "Hil%"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_startsWith": "Hil"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_startsWith for string with ignore case"() {
        when:
        //where name like "hil%"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_startsWith": "hil"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_endsWith for string with strict case"() {
        when:
        //where name like "%ity"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_endsWith": "ity"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_endsWith for string with ignore case"() {
        when:
        //where name like "%ITY"
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_endsWith": "ITY"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_in for numbers"() {
        when:
        //where capacity in (50, 21, 7)
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"capacity": {"_in": ["50", "21", "7"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"}' +
                ']}}'
    }

    def "_in for date"() {
        when:
        //where dateAttr in ("2020-02-02", "2020-03-03")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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

    //todo uncomment when the bug will be fixed in the jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
//    def "_in for offsetDateTime"() {
//        when:
//        //where offsetDateTimeAttr in ("2020-02-02T22:22:22+04:00", "2020-01-01T11:11:11+04:00")
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetDateTimeAttr": {"_in": ["2020-02-02T22:22:22+04:00", "2020-01-01T11:11:11+04:00"]}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
//                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
//                ']}}'
//    }

    def "_in for string with strict case"() {
        when:
        //where name in ("Hillwood City", "Chez Paris")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_in": ["Hillwood City", "Chez Paris"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_in for string with ignore case"() {
        when:
        //where name in ("hillwood city", "chez paris")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_in": ["hillwood city", "chez paris"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[]}}'
    }

    def "_in for UUID"() {
        when:
        //where id in ("bfe41616-f03d-f287-1397-8619f5dde390", "2094170e-5739-43bd-ed5c-783c949c9948")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"id": {"_in": ["bfe41616-f03d-f287-1397-8619f5dde390", "2094170e-5739-43bd-ed5c-783c949c9948"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"}' +
                ']}}'
    }

    def "_notIn for numbers"() {
        when:
        //where capacity not in (50, 21, 7)
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"capacity": {"_notIn": ["50", "21", "7"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_notIn for date"() {
        when:
        //where dateAttr not in ("2020-02-02", "2020-03-03")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"localDateTimeAttr": {"_notIn": ["2020-02-02T22:22:22", "2020-01-01T11:11:11"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    //todo uncomment when the bug will be fixed in the jmix-core module
    // https://github.com/Haulmont/jmix-core/issues/153
//    def "_notIn for offsetDateTime"() {
//        when:
//        //where offsetDateTimeAttr not in ("2020-02-02T22:22:22+04:00", "2020-01-01T11:11:11+04:00")
//        def response = graphQLTestTemplate.perform(
//                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
//                asObjectNode('{"filter": {"AND": [' +
//                        '{"offsetDateTimeAttr": {"_notIn": ["2020-02-02T22:22:22+04:00", "2020-01-01T11:11:11+04:00"]}}' +
//                        ']}}')
//        )
//
//        then:
//        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
//                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
//                ']}}'
//    }

    def "_notIn for string with strict case"() {
        when:
        //where name not in ("Hillwood City", "Chez Paris")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_notIn": ["Hillwood City", "Chez Paris"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"}' +
                ']}}'
    }

    def "_notIn for string with ignore case"() {
        when:
        //where name not in ("hillwood city", "chez paris")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_notIn": ["hillwood city", "chez paris"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"2094170e-5739-43bd-ed5c-783c949c9948"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_notIn for UUID"() {
        when:
        //where id not in ("bfe41616-f03d-f287-1397-8619f5dde390", "2094170e-5739-43bd-ed5c-783c949c9948")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"id": {"_notIn": ["bfe41616-f03d-f287-1397-8619f5dde390", "2094170e-5739-43bd-ed5c-783c949c9948"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"4e0ba898-74e4-8ab7-58fc-044364221044"},' +
                '{"id":"b79e6fc9-f07a-d5cd-e072-8104a5d5101d"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"},' +
                '{"id":"d99d468e-3cc0-01da-295e-595e48fec620"},' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    def "_isNull (true) for numbers"() {
        when:
        //where integerAttr is null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"integerAttr": {"_isNull": "true"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for date"() {
        when:
        //where dateAttr is null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"offsetTimeAttr": {"_isNull": true}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for string"() {
        when:
        //where stringAttr is null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"stringAttr": {"_isNull": "true"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for boolean"() {
        when:
        //where booleanAttr is null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"booleanAttr": {"_isNull": "true"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (true) for UUID"() {
        when:
        //where uuidAttr is null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"uuidAttr": {"_isNull": "true"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (false) for numbers"() {
        when:
        //where integerAttr is not null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"integerAttr": {"_isNull": "false"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (false) for date"() {
        when:
        //where dateAttr is null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
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

    def "_isNull (false) for string"() {
        when:
        //where stringAttr is not null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"stringAttr": {"_isNull": "false"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_isNull (false) for boolean"() {
        when:
        //where booleanAttr is not null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"booleanAttr": {"_isNull": "false"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_isNull (false) for UUID"() {
        when:
        //where uuidAttr is not null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"uuidAttr": {"_isNull": "false"}}' +
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
