/*
 * Copyright 2020 Haulmont.
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

package entities


import test_support.RestSpec

import static test_support.DataUtils.createGroup
import static test_support.DataUtils.createUser
import static test_support.RestSpecsUtils.createRequest

class EntitiesControllerTest extends RestSpec {

    void prepareDb() {
        UUID groupId = createGroup(dirtyData, sql, "Company")

        createUser(dirtyData, sql, "admin", groupId)
        createUser(dirtyData, sql, "anonymous", groupId)
        createUser(dirtyData, sql, "login1", "testFirstName", groupId)
        createUser(dirtyData, sql, "login2", "testFirstName", groupId)
    }

    def "GET-request with filter for obtaining the count of entities"() {
        def param = [
                'conditions': [
                        [
                                'property': 'login',
                                'operator': 'notEmpty'
                        ]
                ]
        ]

        when:
        def request = createRequest(userToken).param("filter", param)
        def response = request.with().get(baseUrl + "/entities/sec\$User/search/count")

        then:
        response.statusCode() == 200
        response.body.as(Integer) == 2
    }

    def "POST-request with filter for obtaining the count of entities"() {
        def body = [
                'filter': [
                        'conditions': [
                                [
                                        'property': 'login',
                                        'operator': '=',
                                        'value'   : "admin"
                                ]
                        ]
                ]
        ]

        when:
        def request = createRequest(userToken).body(body)
        def response = request.with().post(baseUrl + "/entities/sec\$User/search/count")

        then:
        response.statusCode() == 200
        response.body.as(Integer) == 1
    }

    def "PUT-request to bulk update (handling case of body containing one object instead of array)"() {
        def body =
                [
                        'firstName': 'Some name'
                ]

        when:
        def request = createRequest(userToken).body(body)
        def response = request.with().put(baseUrl + "/entities/sec\$User")

        then:
        noExceptionThrown()
        response.statusCode == 400
    }

    def "PUT-request to bulk update"() {
        def userRows = sql.rows("select * from SAMPLE_REST_SEC_USER where FIRST_NAME = 'testFirstName'")
        def body =
                [
                        [
                                'id'       : userRows[0].id,
                                'firstName': 'Some name'
                        ],
                        [

                                'id'       : userRows[1].id,
                                'firstName': 'Some name'

                        ]
                ]

        when:
        def request = createRequest(userToken).body(body)
        def response = request.with().put(baseUrl + "/entities/sec\$User")

        then:
        response.statusCode == 200
        sql.rows("select * from SAMPLE_REST_SEC_USER where FIRST_NAME = 'testFirstName'").size() == 0
        sql.rows("select * from SAMPLE_REST_SEC_USER where FIRST_NAME = 'Some name'").size() == 2
    }
}
