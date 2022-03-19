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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.graphql.spring.boot.test.GraphQLResponse
import com.graphql.spring.boot.test.GraphQLTestTemplate
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.security.authentication.RoleGrantedAuthority
import io.jmix.security.role.ResourceRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification
import test_support.App
import test_support.RestTestUtils
import test_support.entity.Car


@SpringBootTest(classes = App, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AbstractGraphQLTest extends Specification {

    @LocalServerPort
    int port

    @Autowired
    GraphQLTestTemplate graphQLTestTemplate
    @Autowired
    ResourceRoleRepository resourceRoleRepository
    @Autowired
    InMemoryUserRepository userRepository

    protected TransactionTemplate transaction
    protected String adminToken
    protected String mechanicToken
    protected UserDetails admin
    protected UserDetails mechanic

    void setup() {
        admin = User.builder()
                .username("admin")
                .password("{noop}admin")
                .authorities(RoleGrantedAuthority.ofResourceRole(resourceRoleRepository.getRoleByCode("system-full-access")))
                .build()
        userRepository.addUser(admin)

        mechanic = User.builder()
                .username("mechanic")
                .password("{noop}1")
                .authorities(RoleGrantedAuthority.ofResourceRole(resourceRoleRepository.getRoleByCode("mechanics")))
                .build()
        userRepository.addUser(mechanic)

        adminToken = RestTestUtils.getAuthToken("admin", "admin", port)
        mechanicToken = RestTestUtils.getAuthToken("mechanic", "1", port)
    }

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager)
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
    }

    protected GraphQLResponse query(String queryFilePath) {
        return graphQLTestTemplate
                .withBearerAuth(adminToken)
                .postForResource("graphql/io/jmix/graphql/" + queryFilePath)
    }

    protected GraphQLResponse query(String queryFilePath, HttpHeaders httpHeaders) {
        return graphQLTestTemplate
                .withHeaders(httpHeaders)
                .withBearerAuth(adminToken)
                .postForResource("graphql/io/jmix/graphql/" + queryFilePath)
    }

    protected GraphQLResponse query(String queryFilePath, ObjectNode variables) {
        return query(queryFilePath, variables, adminToken)
    }

    protected GraphQLResponse query(String queryFilePath, String variables) {
        return query(queryFilePath, asObjectNode(variables), adminToken)
    }

    protected GraphQLResponse query(String queryFilePath, ObjectNode variables, String token) {
        return graphQLTestTemplate
                .withBearerAuth(token)
                .perform("graphql/io/jmix/graphql/" + queryFilePath, variables)
    }

    protected List<Car> queryCars(String variables) {
        return query("datafetcher/query-cars.gql", variables)
                .getList('$.data.scr_CarList', Car)
    }


    static ObjectNode asObjectNode(String str) {
        return new ObjectMapper().readValue(str, ObjectNode.class)
    }

    static String getBody(GraphQLResponse response) {
        return response.rawResponse.body
    }

    static JsonObject getExtensions(JsonObject error) {
        error.getAsJsonObject("extensions").getAsJsonObject()
    }

    static String getMessage(JsonObject jsonObject) {
        jsonObject.get("message").getAsString()
    }

    static String getPath(JsonObject jsonObject) {
        jsonObject.get("path").getAsString()
    }

    static JsonArray getErrors(GraphQLResponse response) {
        JsonParser.parseString(response.rawResponse.body).getAsJsonArray("errors")
    }
}
