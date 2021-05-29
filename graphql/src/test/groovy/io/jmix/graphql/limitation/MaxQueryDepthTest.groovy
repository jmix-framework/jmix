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

import com.graphql.spring.boot.test.GraphQLTestError
import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["jmix.graphql.maxQueryDepth=2"])
class MaxQueryDepthTest extends AbstractGraphQLTest {

    def "query depth 1 woks on limit 2"() {
        when:
        def response = query("limitation/car-depth-1.gql")

        then:
        response.get('$.data.scr_CarById._instanceName') == "VAZ - 2121"
    }

    def "query depth 3 fail on limit 2"() {
        when:
        def response = query("limitation/car-depth-3.gql")

        then:
        def errs = response.getList('$.errors', GraphQLTestError)
        errs[0].message == 'maximum query depth exceeded 3 > 2'
    }
}
