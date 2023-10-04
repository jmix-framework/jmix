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
import io.jmix.core.security.CurrentAuthentication
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.core.security.impl.SubstitutedUserAuthenticationToken
import io.jmix.core.usersubstitution.CurrentUserSubstitution
import io.jmix.core.usersubstitution.UserSubstitutionManager
import io.jmix.localusermanagement.entity.UserSubstitutionEntity
import test_support.TestUserSubstitutionEventListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.LocalUserManagementSpecification

import javax.sql.DataSource

class UserSubstitutionsTest extends LocalUserManagementSpecification {

    public static final String PASSWORD = "test"
    public static final String USER_DETAILS = "DETAILS"

    @Autowired
    CurrentAuthentication currentAuthentication
    @Autowired
    CurrentUserSubstitution currentUserSubstitution
    @Autowired
    InMemoryUserRepository userRepository
    @Autowired
    AuthenticationManager authenticationManager
    @Autowired
    UserSubstitutionManager substitutionManager

    @Autowired
    Metadata metadata
    @Autowired
    DataSource dataSource
    @Autowired
    DataManager dataManager

    @Autowired
    TestUserSubstitutionEventListener eventListener;

    private static final String ROLE_1 = "ROLE_1"
    private static final String ROLE_2 = "ROLE_2"


    UserDetails user1, user2, user3
    Authentication systemAuthentication

    def setup() {
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(new SimpleGrantedAuthority(ROLE_1))
                .build()

        userRepository.addUser(user1)

        user2 = User.builder()
                .username("user2")
                .password("{noop}$PASSWORD")
                .authorities(new SimpleGrantedAuthority(ROLE_2))
                .build()
        userRepository.addUser(user2)

        user3 = User.builder()
                .username("user3")
                .password("{noop}$PASSWORD")
                .authorities(Collections.emptyList())
                .build()
        userRepository.addUser(user3)

        systemAuthentication = SecurityContextHelper.getAuthentication()

        UserSubstitutionEntity substitution = dataManager.unconstrained().create(UserSubstitutionEntity)
        substitution.username = user1.username
        substitution.substitutedUsername = user2.username
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
        authToken.authorities[0].authority == ROLE_1

        currentUserSubstitution.getAuthenticatedUser() == user1
        currentUserSubstitution.getSubstitutedUser() == null
        currentUserSubstitution.getEffectiveUser() == user1

        when:
        substitutionManager.substituteUser(user2.username)
        SubstitutedUserAuthenticationToken substitutedToken = currentAuthentication.authentication as SubstitutedUserAuthenticationToken
        then:
        substitutedToken != null
        substitutedToken instanceof SubstitutedUserAuthenticationToken
        ((UserDetails) substitutedToken.principal) == user1
        ((UserDetails) substitutedToken.substitutedPrincipal) == user2
        substitutedToken.authorities.size() == 1
        substitutedToken.authorities[0].authority == ROLE_2
        substitutedToken.getDetails() == USER_DETAILS

        currentUserSubstitution.getAuthenticatedUser() == user1
        currentUserSubstitution.getSubstitutedUser() == user2
        currentUserSubstitution.getEffectiveUser() == user2

        eventListener.events.size() == 1
        eventListener.events[0].authenticatedUser == user1
        eventListener.events[0].substitutedUser == user2
    }

    def noUserSubstitutionTest() {
        when:
        authenticate(user1.username)
        substitutionManager.substituteUser(user3.username)
        then:
        thrown(IllegalArgumentException)
    }


    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }
}
