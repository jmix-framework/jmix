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

package user_substitution

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.security.AccessDeniedException
import io.jmix.core.security.CurrentAuthentication
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.core.security.impl.SubstitutedUserAuthenticationToken
import io.jmix.security.UserSubstitutionEventListener
import io.jmix.security.authentication.RoleGrantedAuthority
import io.jmix.security.role.ResourceRoleRepository
import io.jmix.securitydata.entity.UserSubstitution
import io.jmix.securitydata.impl.substitution.UserSubstitutionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.SecurityDataSpecification
import test_support.role.TestDataManagerEntityOperationsRole
import test_support.role.TestDataManagerReadQueryRole

import javax.sql.DataSource

class UserSubstitutionsTest extends SecurityDataSpecification {
    public static final String PASSWORD = "test"
    public static final String USER_DETAILS = "DETAILS"

    @Autowired
    CurrentAuthentication currentAuthentication;
    @Autowired
    InMemoryUserRepository userRepository
    @Autowired
    AuthenticationManager authenticationManager
    @Autowired
    UserSubstitutionManager substitutionManager
    @Autowired
    ResourceRoleRepository roleRepository
    @Autowired
    Metadata metadata
    @Autowired
    DataSource dataSource
    @Autowired
    DataManager dataManager

    @Autowired
    UserSubstitutionEventListener eventListener;

    UserDetails user1, user2, user3
    Authentication systemAuthentication

    def setup() {
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.ofResourceRole(roleRepository.getRoleByCode(TestDataManagerReadQueryRole.NAME)))
                .build()

        userRepository.addUser(user1)

        user2 = User.builder()
                .username("user2")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.ofResourceRole(roleRepository.getRoleByCode(TestDataManagerEntityOperationsRole.NAME)))
                .build()
        userRepository.addUser(user2)

        user3 = User.builder()
                .username("user3")
                .password("{noop}$PASSWORD")
                .authorities(Collections.emptyList())
                .build()
        userRepository.addUser(user3)

        systemAuthentication = SecurityContextHelper.getAuthentication()

        UserSubstitution substitution = dataManager.unconstrained().create(UserSubstitution)
        substitution.userName = user1.username
        substitution.substitutedUserName = user2.username
        dataManager.unconstrained().save(substitution)

    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)

        userRepository.removeUser(user1)
        userRepository.removeUser(user2)
        userRepository.removeUser(user3)

        eventListener.events.clear()

        new JdbcTemplate(dataSource).execute('delete from SEC_USER_SUBSTITUTION')
    }

    def generalSubstitutionTest() {
        when:
        authenticate(user1.username)
        UsernamePasswordAuthenticationToken authToken = currentAuthentication.authentication as UsernamePasswordAuthenticationToken
        authToken.setDetails(USER_DETAILS)
        then:
        ((UserDetails) authToken.principal) == user1
        authToken.authorities.size() == 1
        authToken.authorities[0].authority == TestDataManagerReadQueryRole.NAME

        currentAuthentication.getCurrentOrSubstitutedUser() == user1

        when:
        substitutionManager.substituteUser(user2.username)
        SubstitutedUserAuthenticationToken substitutedToken = currentAuthentication.authentication as SubstitutedUserAuthenticationToken
        then:
        substitutedToken != null
        substitutedToken instanceof SubstitutedUserAuthenticationToken
        ((UserDetails) substitutedToken.principal) == user1
        ((UserDetails) substitutedToken.substitutedPrincipal) == user2
        substitutedToken.authorities.size() == 1
        substitutedToken.authorities[0].authority == TestDataManagerEntityOperationsRole.NAME
        substitutedToken.getDetails() == USER_DETAILS

        currentAuthentication.getCurrentOrSubstitutedUser() == user2

        eventListener.events.size() == 1
        eventListener.events[0].originalUser == user1
        eventListener.events[0].substitutedUser == user2
    }

    def noUserSubstitutionTest() {
        when:
        authenticate(user1.username)
        substitutionManager.substituteUser(user3.username)
        then:
        thrown(AccessDeniedException)
    }


    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }
}
