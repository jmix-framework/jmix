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
import io.jmix.security.authentication.RoleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.RestTestUtils

class PermissionsTest extends AbstractGraphQLTest {

    UserDetails lowPermission
    String lowPermissionToken

    def setup() {
        lowPermission = User.builder()
                .username("perm")
                .password("{noop}admin")
                .authorities(RoleGrantedAuthority.ofResourceRole(resourceRoleRepository.getRoleByCode("low-permissions")))
                .build()
        userRepository.addUser(lowPermission)

        lowPermissionToken = RestTestUtils.getAuthToken("perm", "admin", port)
    }

    def "permissions for mechanic"() {
        when:
        def response = query("datafetcher/query-permissions.gql", null, mechanicToken)

        then:
        getBody(response) == '{"data":{"permissions":{' +
                '"entities":[' +
                '{"target":"scr$Car:create","value":1},' +
                '{"target":"scr$Car:read","value":1},' +
                '{"target":"scr$Car:update","value":1},' +
                '{"target":"scr$Car:delete","value":1}' +
                '],' +
                '"entityAttributes":[' +
                '{"target":"scr$Car:manufacturer","value":2},' +
                '{"target":"scr$Car:carType","value":2},' +
                '{"target":"scr$Car:model","value":2},' +
                '{"target":"scr$Car:mileage","value":1}' +
                '],' +
                '"specifics":[' +
                '{"target":"graphql.fileDownload.enabled","value":0},' +
                '{"target":"graphql.fileUpload.enabled","value":0}' +
                ']}}}'
    }

    def "SpecificPermissionInstrumentation is worked"() {
        when:
        def response = query("datafetcher/query-permissions.gql", null, lowPermissionToken)
        def error = getErrors(response)[0].getAsJsonObject()

        then:
        getMessage(error) == "io.jmix.graphql/gqlApiAccessDenied"
    }
}
