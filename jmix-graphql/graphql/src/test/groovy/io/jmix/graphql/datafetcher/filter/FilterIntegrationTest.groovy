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

package io.jmix.graphql.datafetcher.filter

import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import test_support.entity.Car

@TestPropertySource(properties = ["eclipselink.logging.level.sql = FINE"])
class FilterIntegrationTest extends AbstractGraphQLTest {

    def "_eq for numbers"() {
        when:
        //where capacity = 50
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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

    def "_eq for char"() {
        when:
        //where charAttr = 'c'
        def response = query("datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"charAttr": {"_eq": "c"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"}' +
                ']}}'
    }

    def "_eq for string with strict case"() {
        when:
        //where name = "Hillwood City"
        def response = query("datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"id": {"_eq": "bfe41616-f03d-f287-1397-8619f5dde390"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"}' +
                ']}}'
    }

    def "_neq for numbers"() {
        when:
        //where capacity <> 50
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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

    def "_neq for char"() {
        when:
        //where charAttr <> 'c'
        def response = query("datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"charAttr": {"_neq": "c"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_neq for string with strict case"() {
        when:
        //where name <> "Hillwood City"
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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

    def "_gt for numbers"() {
        when:
        //where capacity > 50
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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

    def "_gte for numbers"() {
        when:
        //where capacity >= 50
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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

    def "_lt for numbers"() {
        when:
        //where capacity < 50
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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

    def "_lte for numbers"() {
        when:
        //where capacity <= 50
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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

    def "_contains for string with strict case"() {
        when:
        //where name like "%Hillwood%"
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"name": {"_endsWith": "ITY"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"ff01c573-ebf3-c704-3ad0-fd582f7a2a12"}' +
                ']}}'
    }

    @Ignore //todo https://github.com/Haulmont/jmix-core/issues/136
    def "_in with empty array in condition"() {
        when:
        List<Car> cars = queryCars('{"filter": {"regNumber": {"_in": []}}}}')
        then:
        cars.stream().allMatch(car -> car.regNumber == null)
    }

    def "_in for numbers"() {
        when:
        //where capacity in (50, 21, 7)
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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

    def "_in for char"() {
        when:
        //where charAttr in ('c', 'b')
        def response = query("datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"charAttr": {"_in": ["c", "b"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"}' +
                ']}}'
    }

    def "_in for string with strict case"() {
        when:
        //where name in ("Hillwood City", "Chez Paris")
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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

    def "_notIn for char"() {
        when:
        //where charAttr in ('c', 'b')
        def response = query("datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"charAttr": {"_notIn": ["c", "b"]}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    def "_notIn for string with strict case"() {
        when:
        //where name not in ("Hillwood City", "Chez Paris")
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"integerAttr": {"_isNull": "true"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"00000000-0000-0000-0000-000000000000"}' +
                ']}}'
    }

    def "_isNull (true) for char"() {
        when:
        //where charAttr is null
        def response = query("datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"charAttr": {"_isNull": "true"}}' +
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
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
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

    def "_isNull (false) for char"() {
        when:
        //where charAttr is not null
        def response = query("datafetcher/query-datatypesTestEntity-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"charAttr": {"_isNull": "false"}}' +
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
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
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
        def response = query(
                "datafetcher/query-datatypesTestEntity-with-filter.graphql",
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

    def "garage count query without filter"() {
        when:
        def response = query(
                "datafetcher/query-garage-count.graphql"
        )

        then:
        getBody(response) == '{"data":{"scr_GarageCount":"10"}}'
    }

    def "garage count query filtered by capacity"() {
        when:
        //capacity = 50
        def response = query(
                "datafetcher/query-garage-count-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"capacity": {"_eq": "50"}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageCount":"2"}}'
    }

    def "one to many for UUID"() {
        when:
        //where id <> "bfe41616-f03d-f287-1397-8619f5dde390"
        def response = query(
                "datafetcher/query-garage-with-filter.graphql",
                asObjectNode('{"filter": {"AND": [' +
                        '{"id": {"_neq": "bfe41616-f03d-f287-1397-8619f5dde390"}},' +
                        '{"personnel": {"username": {"_eq": "randomuser"}}}' +
                        ']}}')
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"18a4b0b4-b7b5-da87-e5d8-c8f02e97eda5"},' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"}' +
                ']}}'
    }
}
