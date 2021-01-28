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

package test_support

import groovy.sql.Sql
import io.jmix.core.CoreConfiguration
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.data.DataConfiguration
import io.jmix.rest.RestConfiguration
import io.jmix.samples.rest.SampleRestApplication
import io.jmix.samples.rest.security.FullAccessRole
import io.jmix.security.SecurityConfiguration
import io.jmix.security.authentication.RoleGrantedAuthority
import io.jmix.security.role.ResourceRoleRepository
import io.jmix.security.role.RowLevelRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static DbUtils.getSql
import static test_support.RestSpecsUtils.getAuthToken

@ContextConfiguration(classes = [
        CoreConfiguration.class,
        DataConfiguration.class,
        SecurityConfiguration.class,
        RestConfiguration.class,
        JmixRestTestConfiguration.class])
@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestSpec extends Specification {

    @LocalServerPort
    int port

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    ResourceRoleRepository resourceRoleRepository

    @Autowired
    RowLevelRoleRepository rowLevelRoleRepository

    public DataSet dirtyData = new DataSet()
    public Sql sql

    String userPassword = "admin123"
    String userLogin = "admin"
    String userToken
    String baseUrl
    UserDetails admin

    void setup() {
        admin = User.builder()
                .username(userLogin)
                .password("{noop}$userPassword")
                .authorities(RoleGrantedAuthority.ofResourceRole(resourceRoleRepository.getRoleByCode(FullAccessRole.NAME)))
                .build()

        userRepository.addUser(admin)

        baseUrl = "http://localhost:" + port + "/rest"
        RestSpecsUtils.setBasePort(port)

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
