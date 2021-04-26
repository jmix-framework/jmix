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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.jmix.graphql.AbstractGraphQLTest
import io.jmix.graphql.schema.Types

import static io.jmix.graphql.schema.Types.FilterOperation.*

class FilterIntegrationTest extends AbstractGraphQLTest {

    def "_eq for numbers"() {
        when:
        //where capacity = 50
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariables("capacity", EQ, 50)
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
                getFilterVariables("name", EQ, "Hillwood City")
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
                getFilterVariables("name", EQ, "hillwood city")
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[]}}'
    }

    def "_eq for boolean"() {
        when:
        //where vanEntry = false
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariables("vanEntry", EQ, false)
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
                getFilterVariables("id", EQ, "bfe41616-f03d-f287-1397-8619f5dde390")
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"}' +
                ']}}'
    }

    def "_neq for numbers"() {
        when:
        //where capacity <> 50
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariables("capacity", NEQ, 50)
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
                getFilterVariables("name", NEQ, "Hillwood City")
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
                getFilterVariables("name", NEQ, "hillwood city")
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
                getFilterVariables("vanEntry", NEQ, false)
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
                getFilterVariables("id", NEQ, "bfe41616-f03d-f287-1397-8619f5dde390")
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariables("capacity", GT, 50)
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariables("capacity", GTE, 50)
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariables("capacity", LT, 50)
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariables("capacity", LTE, 50)
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
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariables("name", CONTAINS, "Hillwood")
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
                getFilterVariables("name", CONTAINS, "hillwood")
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
                getFilterVariables("name", NOT_CONTAINS, "Hillwood")
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
                getFilterVariables("name", NOT_CONTAINS, "hillwood")
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
                getFilterVariables("name", STARTS_WITH, "Hil")
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
                getFilterVariables("name", STARTS_WITH, "hil")
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
                getFilterVariables("name", ENDS_WITH, "ity")
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
                getFilterVariables("name", ENDS_WITH, "ITY")
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
                getFilterVariablesWithArray("capacity", IN_LIST, [50, 21, 7])
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"1e3cb465-c0d8-1f31-4231-08c34e101fc3"},' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"},' +
                '{"id":"d881e37a-d28a-4e48-cb96-668d4a6fb57d"}' +
                ']}}'
    }

    def "_in for string with strict case"() {
        when:
        //where name in ("Hillwood City", "Chez Paris")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariablesWithArray("name", IN_LIST, ["Hillwood City", "Chez Paris"])
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
                getFilterVariablesWithArray("name", IN_LIST, ["hillwood city", "chez paris"])
        )

        then:
        getBody(response) == '{"data":{"scr_GarageList":[]}}'
    }

    def "_in for UUID"() {
        when:
        //where id in ("bfe41616-f03d-f287-1397-8619f5dde390", "2094170e-5739-43bd-ed5c-783c949c9948")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariablesWithArray(
                        "id",
                        IN_LIST,
                        ["bfe41616-f03d-f287-1397-8619f5dde390", "2094170e-5739-43bd-ed5c-783c949c9948"]
                )
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
                getFilterVariablesWithArray("capacity", NOT_IN_LIST, [50, 21, 7])
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

    def "_notIn for string with strict case"() {
        when:
        //where name not in ("Hillwood City", "Chez Paris")
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                getFilterVariablesWithArray("name", NOT_IN_LIST, ["Hillwood City", "Chez Paris"])
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
                getFilterVariablesWithArray("name", NOT_IN_LIST, ["hillwood city", "chez paris"])
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
                getFilterVariablesWithArray(
                        "id",
                        NOT_IN_LIST,
                        ["bfe41616-f03d-f287-1397-8619f5dde390", "2094170e-5739-43bd-ed5c-783c949c9948"]
                )
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
                getFilterVariables("integerAttr", IS_NULL, true)
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
                getFilterVariables("stringAttr", IS_NULL, true)
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
                getFilterVariables("booleanAttr", IS_NULL, true)
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
                getFilterVariables("uuidAttr", IS_NULL, true)
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
                getFilterVariables("dateAttr", IS_NULL, true)
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
                getFilterVariables("integerAttr", IS_NULL, false)
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
                getFilterVariables("stringAttr", IS_NULL, false)
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
                getFilterVariables("booleanAttr", IS_NULL, false)
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
                getFilterVariables("uuidAttr", IS_NULL, false)
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
        //where dateAttr is not null
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/query-datatypesTestEntity-with-filter.graphql",
                getFilterVariables("dateAttr", IS_NULL, false)
        )

        then:
        getBody(response) == '{"data":{"scr_DatatypesTestEntityList":[' +
                '{"id":"032fd8a5-e042-4828-a802-36cbd2ce12de"},' +
                '{"id":"b1a1f3c9-6076-4725-8c4a-65a4267d15e1"},' +
                '{"id":"db9faa31-dfa3-4b97-943c-ba268888cdc3"}' +
                ']}}'
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    private static ObjectNode getFilterVariables(String fieldName, Types.FilterOperation filterOperation, Object value) {
        def variables = new ObjectMapper().createObjectNode()
        def fieldForCondition = new ObjectMapper().createObjectNode()
        def condition = new ObjectMapper().createObjectNode()

        condition.put(filterOperation.getId(), value)
        fieldForCondition.set(fieldName, condition)
        variables.set("filter", fieldForCondition)

        return variables
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    private static ObjectNode getFilterVariablesWithArray(String fieldName,
                                                          Types.FilterOperation filterOperation,
                                                          Collection<?> value) {
        def variables = new ObjectMapper().createObjectNode()
        def fieldForCondition = new ObjectMapper().createObjectNode()
        def condition = new ObjectMapper().createObjectNode()
        def array = new ObjectMapper().createArrayNode()

        value.forEach({ val -> array.add(val) })

        condition.set(filterOperation.getId(), array)
        fieldForCondition.set(fieldName, condition)
        variables.set("filter", fieldForCondition)

        return variables
    }
}
