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

package datamanager

import io.jmix.core.*
import io.jmix.core.security.AccessDeniedException
import io.jmix.core.security.SecurityContextHelper
import io.jmix.core.security.impl.CoreUser
import io.jmix.core.security.impl.InMemoryUserRepository
import io.jmix.security.role.assignment.InMemoryRoleAssignmentProvider
import io.jmix.security.role.assignment.RoleAssignment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import test_support.SecuritySpecification
import test_support.annotated_role_builder.TestDataManagerEntityOperationsRole
import test_support.annotated_role_builder.TestDataManagerReadQueryRole
import test_support.entity.TestOrder

import javax.sql.DataSource

class DataManagerReadQueryConstraintTest extends SecuritySpecification {
    @Autowired
    DataManager dataManager

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    InMemoryRoleAssignmentProvider roleAssignmentProvider

    @Autowired
    Metadata metadata

    @Autowired
    AccessConstraintsRegistry accessConstraintsRegistry

    @Autowired
    DataSource dataSource

    CoreUser user1
    TestOrder orderDenied1, orderDenied2, orderAllowed

    Authentication systemAuthentication

    public static final String PASSWORD = "123"

    def setup() {
        user1 = new CoreUser("user1", "{noop}$PASSWORD", "user1")
        userRepository.createUser(user1)
        roleAssignmentProvider.addAssignment(new RoleAssignment(user1.key, TestDataManagerReadQueryRole.NAME))

        orderDenied1 = metadata.create(TestOrder)
        orderDenied1.number = '1'

        orderDenied2 = metadata.create(TestOrder)
        orderDenied2.number = '2'

        orderAllowed = metadata.create(TestOrder)
        orderAllowed.number = 'allowed_3'


        dataManager.save(orderDenied1, orderDenied2, orderAllowed)

        systemAuthentication = SecurityContextHelper.getAuthentication()
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)

        userRepository.removeUser(user1)

        roleAssignmentProvider.removeAssignments(user1.key)

        new JdbcTemplate(dataSource).execute('delete from TEST_ORDER')
    }


    def "load with empty constraints"() {
        setup:

        authenticate('user1')

        when:

        def result = dataManager.load(TestOrder.class).list()

        then:

        result.size() == 3

        result.contains(orderDenied1)
        result.contains(orderAllowed)
        result.contains(orderDenied2)

    }

    def "load with constraints"() {
        setup:

        authenticate('user1')

        when:

        def result = dataManager.load(TestOrder.class)
                .accessConstraints(accessConstraintsRegistry.getConstraints())
                .list()

        then:

        result.size() == 1

        result.contains(orderAllowed)
    }

    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }
}
