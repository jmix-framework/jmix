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

package io.jmix.graphql.custom

import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Ignore

@Ignore
@ActiveProfiles("custom")
class CustomLoaderTest extends AbstractGraphQLTest {

    def "Custom cars loader "() {
        when:
        def response = query(
                "datafetcher/query-cars.gql"
        )

        then:
        def body = getBody(response)
        body == '{"data":{"scr_CarList":[' +
                '{"_instanceName":"BMW - M3","price":"10"},' +
                '{"_instanceName":"Lada - Vesta","price":"20"}' +
                ']}}'
    }

    def "Custom car count loader"() {
        when:
        def response = query(
                "datafetcher/query-car-count.gql"
        )
        then:
        getBody(response) == '{"data":{"scr_CarCount":"999"}}'
    }

    def "Custom car loader"() {
        when:
        def response = query(
                "datafetcher/query-car.gql",
                asObjectNode('{"id": "123e4567-e89b-12d3-a456-426655440000"}')
        )
        then:
        def body = getBody(response)
        body == '{"data":{"scr_CarById":' +
                '{"_instanceName":"Lada - Vesta","price":"10"}' +
                '}}'
    }

}
