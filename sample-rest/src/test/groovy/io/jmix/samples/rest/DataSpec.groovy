/*
 * Copyright 2019 Haulmont.
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

package io.jmix.samples.rest

import groovy.sql.Sql
import io.jmix.core.Metadata
import io.jmix.core.security.impl.CoreUser
import io.jmix.core.security.impl.InMemoryUserRepository
import io.jmix.samples.rest.api.DataSet
import io.jmix.samples.rest.security.FullAccessRole
import io.jmix.security.role.assignment.InMemoryRoleAssignmentProvider
import io.jmix.security.role.assignment.RoleAssignment
import io.jmix.securitydata.entity.RoleAssignmentEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import spock.lang.Specification

import static io.jmix.samples.rest.DbUtils.getSql
import static io.jmix.samples.rest.RestSpecsUtils.getAuthToken

@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataSpec extends Specification {

    @LocalServerPort
    private int port

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    Metadata metadata

    @Autowired
    InMemoryRoleAssignmentProvider roleAssignmentProvider

    public DataSet dirtyData = new DataSet()
    public Sql sql

    String userPassword = "admin123"
    String userLogin = "admin"
    String userToken
    String baseUrl
    CoreUser admin

    void setup() {
        admin = new CoreUser(userLogin, "{noop}$userPassword", "Admin")
        userRepository.addUser(admin)
        RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class)
        roleAssignmentEntity.setRoleCode("system-full-access")
        roleAssignmentEntity.setUsername(admin.getUsername())
        roleAssignmentProvider.addAssignment(new RoleAssignment(admin.getUsername(), FullAccessRole.NAME))
        baseUrl = "http://localhost:" + port + "/rest"
        userToken = getAuthToken(baseUrl, userLogin, userPassword)

        sql = getSql() as Sql

        prepareDb()
    }

    void cleanup() {
        userRepository.removeUser(admin)
        dirtyData.cleanup(sql.connection)
        if (sql != null) {
            sql.close()
        }
    }

    void prepareDb() {}
}
