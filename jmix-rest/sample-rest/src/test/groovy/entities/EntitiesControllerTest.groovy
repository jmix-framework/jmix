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

import static test_support.DataUtils.*
import static test_support.RestSpecsUtils.createRequest

class EntitiesControllerTest extends RestSpec {

    void prepareDb() {
        UUID groupId = createGroup(dirtyData, sql, "Company")

        createUser(dirtyData, sql, "admin", groupId)
        createUser(dirtyData, sql, "anonymous", groupId)
        createUser(dirtyData, sql, "login1", "testFirstName", groupId)
        createUser(dirtyData, sql, "login2", "testFirstName", groupId)
        createValidatedEntity(dirtyData, sql, "name1")
        createValidatedEntity(dirtyData, sql, "name2")
        createUser(dirtyData, sql, "toDeleteByObject1", groupId)
        createUser(dirtyData, sql, "toDeleteByObject2", groupId)
        createUser(dirtyData, sql, "toDeleteById1", groupId)
        createUser(dirtyData, sql, "toDeleteById2", groupId)
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
        response.body.as(Integer) == 8
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


    def "PUT-request to bulk update should save entities in single transaction"() {
        def validatedEntityRows = sql.rows("select id from REST_VALIDATED_ENTITY")
        def namesBeforeUpdate = ['name1', 'name2']
        def body =
                [
                        [
                                'id'  : validatedEntityRows[0].id,
                                'name': 'name3'
                        ],
                        [
                                'id'  : validatedEntityRows[1].id,
                                'name': null
                        ]
                ]

        when:
        def request = createRequest(userToken).body(body)
        def response = request.with().put(baseUrl + "/entities/rest_ValidatedEntity")

        then:
        response.statusCode == 400
        sql.rows("select name from REST_VALIDATED_ENTITY").collect { it.name }.sort() == namesBeforeUpdate
    }

    def "DELETE-request bulk deletion with body containing entities array representation"() {
        def users = sql.rows("select * from SAMPLE_REST_SEC_USER where LOGIN like 'toDeleteByObject_'")
        def body =
                [
                        [
                                'id': users[0].id
                        ],
                        [
                                'id': users[1].id
                        ]
                ]

        when:
        def request = createRequest(userToken).body(body)
        def response = request.with().delete(baseUrl + "/entities/sec\$User")

        then:
        response.statusCode == 204
        sql.rows("select * from SAMPLE_REST_SEC_USER where LOGIN like 'toDeleteByObject_'")
                .every { it.delete_ts != null }
    }

    def "DELETE-request bulk deletion with body containing array of ids"() {
        def users = sql.rows("select * from SAMPLE_REST_SEC_USER where LOGIN like 'toDeleteById_'")
        def body = users.collect { it.id }

        when:
        def request = createRequest(userToken).body(body)
        def response = request.with().delete(baseUrl + "/entities/sec\$User")

        then:
        response.statusCode == 204
        sql.rows("select * from SAMPLE_REST_SEC_USER where LOGIN like 'toDeleteById_'")
                .every { it.delete_ts != null }
    }
}
