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

package io.jmix.graphql.limitation

import com.graphql.spring.boot.test.GraphQLResponse
import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

import static io.jmix.graphql.schema.Types.FilterOperation.EQ

@TestPropertySource("/test_support/test-app-limitations.properties")
class OperationRateLimitServiceTest extends AbstractGraphQLTest {

    @Autowired
    private OperationRateLimitService operationRateLimitService

    def "query limit is working with 2 attempt"() {
        when:
        //where capacity = 50
        GraphQLResponse response = null

        for (i in 0..<2) {
            response = graphQLTestTemplate.perform(
                    "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                    getFilterVariables("capacity", EQ, 50)
            )
        }

        then:
        getBody(response) == '{"data":{"scr_GarageList":[' +
                '{"id":"bfe41616-f03d-f287-1397-8619f5dde390"},' +
                '{"id":"ca83fc1c-95e5-d012-35bf-151b7f720264"}' +
                ']}}'
    }

    def "mutation limit is working with 1 attempt"() {
        when:
        def response = graphQLTestTemplate.postForResource(
                "graphql/io/jmix/graphql/datafetcher/upsert-car.graphql"
        )

        then:
        getBody(response) == '{"data":{"upsert_scr_Car":{"_instanceName":"1 - 2"}}}'
    }

    def "query limit is throwing exception with 4 attempt"() {
        when:
        //where capacity = 50
        def response = null

        for (i in 0..<4) {
            response = graphQLTestTemplate.perform(
                    "graphql/io/jmix/graphql/datafetcher/query-garage-with-filter.graphql",
                    getFilterVariables("capacity", EQ, 50)
            )
        }
        def error = getErrors(response)[0].getAsJsonObject()
        def errorMsg = getMessage(error)
        def extensionErrMsg = getExtensions(error).get("classification").getAsString()
        def isBlocked = operationRateLimitService.isBlocked("127.0.0.1")

        then:
        isBlocked
        errorMsg == "Exceeded the number of allowed requests per minute"
        extensionErrMsg == "ExecutionAborted"
    }

    def "mutation limit is throwing exception with 4 attempt"() {
        when:
        def response = null
        for (i in 0..<4) {
            response = graphQLTestTemplate.postForResource(
                    "graphql/io/jmix/graphql/datafetcher/upsert-car.graphql"
            )
        }
        def error = getErrors(response)[0].getAsJsonObject()
        def errorMsg = getMessage(error)
        def extensionErrMsg = getExtensions(error).get("classification").getAsString()
        def isBlocked = operationRateLimitService.isBlocked("127.0.0.1")

        then:
        isBlocked
        errorMsg == "Exceeded the number of allowed requests per minute"
        extensionErrMsg == "ExecutionAborted"
    }
}
