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

package in_memory_row_level_policy

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.core.security.SystemAuthenticator
import io.jmix.security.authentication.RoleGrantedAuthority
import io.jmix.security.model.ResourceRole
import io.jmix.security.role.ResourceRoleRepository
import io.jmix.security.role.RowLevelRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.SecurityDataSpecification
import test_support.entity.TestOrder
import test_support.role.TestInMemoryRowLevelConstraintsMethodArgsRole
import test_support.role.TestInMemoryRowLevelConstraintsRole
import test_support.role.TestOrderFullAccessRole

import javax.sql.DataSource

class InMemoryRowLevelPolicyTest extends SecurityDataSpecification {

    public static final String PASSWORD = "123"

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    Metadata metadata

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    RowLevelRoleRepository rowLevelRoleRepository

    @Autowired
    ResourceRoleRepository resourceRoleRepository

    @Autowired
    DataSource dataSource

    @Autowired
    DataManager dataManager

    @Autowired
    SystemAuthenticator systemAuthenticator

    UserDetails user1, user2, user3

    TestOrder order1, order2

    def setup() {

        def testOrderFullAccessRole = resourceRoleRepository.getRoleByCode(TestOrderFullAccessRole.NAME)

        //user1 doesn't have any roles
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.ofResourceRole(testOrderFullAccessRole))
                .build()

        userRepository.addUser(user1)

        //user2 has row-level role TestInMemoryRowLevelConstraintsRole
        def testInMemoryRowLevelConstraintsRole = rowLevelRoleRepository.getRoleByCode(TestInMemoryRowLevelConstraintsRole.NAME)
        user2 = User.builder()
                .username("user2")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.ofResourceRole(testOrderFullAccessRole),
                        RoleGrantedAuthority.ofRowLevelRole(testInMemoryRowLevelConstraintsRole))
                .build()
        userRepository.addUser(user2)

        //user3 has row-level role TestInMemoryRowLevelConstraintsMethodArgsRole
        def testInMemoryRowLevelConstraintsMethodArgsRole = rowLevelRoleRepository.getRoleByCode(TestInMemoryRowLevelConstraintsMethodArgsRole.NAME)
        user3 = User.builder()
                .username("user3")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.ofResourceRole(testOrderFullAccessRole),
                        RoleGrantedAuthority.ofRowLevelRole(testInMemoryRowLevelConstraintsMethodArgsRole))
                .build()
        userRepository.addUser(user3)

        order1 = metadata.create(TestOrder)
        order1.number = 'A-1-B'
        dataManager.unconstrained().save(order1)

        order2 = metadata.create(TestOrder)
        order2.number = 'A-1-C'
        dataManager.unconstrained().save(order2)
    }

    def cleanup() {
        userRepository.removeUser(user1)
        userRepository.removeUser(user2)
        userRepository.removeUser(user3)
        new JdbcTemplate(dataSource).execute('delete from TEST_ORDER')
    }

    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }

    def "in-memory row-level policies must be applied to the user with role"() {

        when: "the user has role with row-level policies"

        systemAuthenticator.begin('user2')
        def testOrders = dataManager.load(TestOrder).all().list()
        systemAuthenticator.end()

        then: "only TestOrder that conforms to predicate from the role is returned"

        testOrders.size() == 1
        testOrders[0].number == 'A-1-B'
    }

    def "in-memory row-level policies must NOT be applied to the user without role"() {

        when: "the user has no roles with row-level policies"

        systemAuthenticator.begin('user1')
        def testOrders = dataManager.load(TestOrder).all().list()
        systemAuthenticator.end()


        then: "all TestOrder records are returned"

        testOrders.size() == 2
    }

    def "old-style row-level roles that have method arguments should work"() {

        when: "the user has no roles with row-level policies, policies are received from the role method with arguments"

        systemAuthenticator.begin('user3')
        def testOrders = dataManager.load(TestOrder).all().list()
        systemAuthenticator.end()

        then: "only TestOrder that conforms to predicate from the role is returned"

        testOrders.size() == 1
        testOrders[0].number == 'A-1-B'
    }

}
