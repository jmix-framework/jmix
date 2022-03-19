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

package io.jmix.graphql

import com.graphql.spring.boot.test.GraphQLResponse

class SecurityTest extends AbstractGraphQLTest {

    def "Admin can use custom operation without policy"() {
        when:
        GraphQLResponse response = query("service/userInfo.graphql")
        then:
        response.get('$.data.userInfo.username') == "admin"
        response.get('$.data.userInfo.locale') == "en"
    }

    def "Mechanic can use custom operation with Policy"(){
        when:
        GraphQLResponse response = query("service/userInfo.graphql", null, mechanicToken)
        then:
        response.get('$.data.userInfo.username') == "mechanic"
        response.get('$.data.userInfo.locale') == "en"
    }

    def "Mechanic cant use custom operation without Policy"(){
        when:
        GraphQLResponse response = query("service/inaccessibleQuery.graphql", null, mechanicToken)
        then:
        getBody(response).contains('ExecutionAborted')
    }
}
