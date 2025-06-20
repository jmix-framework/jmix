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
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.rest.RestConfiguration
import io.jmix.samples.rest.SampleRestApplication
import io.jmix.samples.rest.security.FullAccessRole
import io.jmix.security.SecurityConfiguration
import io.jmix.security.role.ResourceRoleRepository
import io.jmix.security.role.RoleGrantedAuthorityUtils
import io.jmix.security.role.RowLevelRoleRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static DbUtils.getSql
import static test_support.RestSpecsUtils.getAuthToken

@ContextConfiguration(classes = [
        CoreConfiguration.class,
        DataConfiguration.class,
        EclipselinkConfiguration.class,
        SecurityConfiguration.class,
        RestConfiguration.class,
        JmixRestTestConfiguration.class])
@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestSpec extends Specification {

    private static final Logger log = LoggerFactory.getLogger(RestSpec);

    @LocalServerPort
    int port

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    ResourceRoleRepository resourceRoleRepository

    @Autowired
    RowLevelRoleRepository rowLevelRoleRepository

    @Autowired
    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @Autowired
    SessionRegistry sessionRegistry

    public DataSet dirtyData = new DataSet()
    public Sql sql

    String userPassword = "admin123"
    String userLogin = "admin"
    String userToken
    String baseUrl
    String oauthUrl
    UserDetails admin

    void setup() {
        admin = User.builder()
                .username(userLogin)
                .password("{noop}$userPassword")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(FullAccessRole.NAME))
                .build()

        userRepository.addUser(admin)

        baseUrl = "http://localhost:" + port + "/rest"
        oauthUrl = "http://localhost:" + port

        RestSpecsUtils.setBasePort(port)

        userToken = getAuthToken(oauthUrl, userLogin, userPassword)

        sql = getSql() as Sql

        prepareDb()
    }

    protected void killSessions(String username) {
        killSessions(username, true)
    }

    protected void killSessions(String username, boolean failIfNoSession) {
        def principals = sessionRegistry.getAllPrincipals().stream()
                .filter({ it instanceof UserDetails })
                .map({ (UserDetails) it })
                .filter({ it.getUsername() == username })
                .findAll()
        if (principals.size() < 1) {
            if (failIfNoSession) throw new RuntimeException("Unable to find principal")
        } else {
            log.trace("{} principal(s) with name '{}' found.", principals.size(), username)
        }

        for (Object principal : principals) {
            log.debug("Expiring sessions for principal {}", principal)
            sessionRegistry.getAllSessions(principal, false)
                    .stream()
                    .forEach({
                        it.expireNow()
                        log.debug("Session '${it.sessionId}' for user '$username' is marked as 'expired'")
                    })
        }
    }


    void cleanup() {
        killSessions(admin.username,false)
        userRepository.removeUser(admin)
        dirtyData.cleanup(sql.connection)
        if (sql != null) {
            sql.close()
        }
    }

    void prepareDb() {}
}
