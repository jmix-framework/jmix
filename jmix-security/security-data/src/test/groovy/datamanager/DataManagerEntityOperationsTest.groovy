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
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.authentication.RoleGrantedAuthority
import io.jmix.security.role.ResourceRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.SecurityDataSpecification
import test_support.entity.OrderInfo
import test_support.entity.TestOrder
import test_support.role.TestDataManagerEntityOperationsCascadeRole
import test_support.role.TestDataManagerEntityOperationsRole

import javax.sql.DataSource

class DataManagerEntityOperationsTest extends SecurityDataSpecification {
    @Autowired
    UnconstrainedDataManager dataManager

    @Autowired
    DataManager secureDataManager

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    ResourceRoleRepository roleRepository

    @Autowired
    Metadata metadata

    @Autowired
    AccessConstraintsRegistry accessConstraintsRegistry

    @Autowired
    DataSource dataSource

    UserDetails user1, user2
    TestOrder order

    Authentication systemAuthentication

    public static final String PASSWORD = "123"

    def setup() {
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.ofResourceRole(roleRepository.getRoleByCode(TestDataManagerEntityOperationsCascadeRole.NAME)))
                .build()

        userRepository.addUser(user1)

        user2 = User.builder()
                .username("user2")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.ofResourceRole(roleRepository.getRoleByCode(TestDataManagerEntityOperationsRole.NAME)))
                .build()
        userRepository.addUser(user2)

        order = metadata.create(TestOrder)
        order.number = '1'
        dataManager.save(order)

        systemAuthentication = SecurityContextHelper.getAuthentication()
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)

        userRepository.removeUser(user1)
        userRepository.removeUser(user2)

        new JdbcTemplate(dataSource).execute('delete from TEST_ORDER_INFO')
        new JdbcTemplate(dataSource).execute('delete from TEST_ORDER')
    }


    def "load with empty constraints"() {
        setup:

        authenticate('user1')

        when:

        def newOrder = dataManager.load(Id.of(order))
                .one()

        then:

        newOrder == order
    }

    def "load is denied on secure DataManager"() {
        setup:

        authenticate('user1')

        when:

        def newOrder = secureDataManager.load(Id.of(order))
                .optional()
                .orElse(null)

        then:

        newOrder == null

    }

    def "load is denied"() {
        setup:

        authenticate('user1')

        when:

        def newOrder = dataManager.load(Id.of(order))
                .accessConstraints(accessConstraintsRegistry.getConstraints())
                .optional()
                .orElse(null)

        then:

        newOrder == null
    }

    def "load is allowed"() {
        setup:

        authenticate('user2')

        when:

        def newOrder = dataManager.load(Id.of(order))
                .one()

        then:

        newOrder == order
    }

    def "save is denied"() {
        setup:

        authenticate('user1')

        when:

        SaveContext saveContext = new SaveContext()
        saveContext.saving(metadata.create(TestOrder))
                .setAccessConstraints(accessConstraintsRegistry.getConstraints())

        dataManager.save(saveContext)

        then:

        thrown(AccessDeniedException)

    }

    def "access constraints checked for cascaded entities"() {
        setup:
        authenticate('user1')

        when:
        def testOrder = metadata.create(TestOrder)

        def orderInfo = metadata.create(OrderInfo)
        orderInfo.setOrder(testOrder)

        SaveContext saveContext = new SaveContext()

        saveContext.saving(orderInfo)
                .setAccessConstraints(accessConstraintsRegistry.getConstraints())

        dataManager.save(saveContext)

        then:
        def e = thrown(AccessDeniedException)
        e.resource == "test_Order"
    }

    def "save is allowed"() {
        setup:

        authenticate('user2')

        when:

        SaveContext saveContext = new SaveContext()
        def newOrder = metadata.create(TestOrder)
        newOrder.number = '2'
        saveContext.saving(metadata.create(TestOrder))
                .setAccessConstraints(accessConstraintsRegistry.getConstraints())

        EntitySet saved = dataManager.save(saveContext)

        def savedOrder = saved.iterator().next()

        then:

        savedOrder != null
    }

    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }
}
