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
import io.jmix.core.entity.KeyValueEntity
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.role.RoleGrantedAuthorityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.SecurityDataSpecification
import test_support.entity.TestOrder
import test_support.role.TestDataManagerReadQueryRole

import javax.sql.DataSource

class DataManagerReadQueryConstraintTest extends SecurityDataSpecification {
    @Autowired
    DataManager dataManager

    @Autowired
    UnconstrainedDataManager unsafeDataManager

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    Metadata metadata

    @Autowired
    AccessConstraintsRegistry accessConstraintsRegistry

    @Autowired
    DataSource dataSource

    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils

    UserDetails user1
    TestOrder orderDenied1, orderDenied2, orderAllowed

    Authentication systemAuthentication

    public static final String PASSWORD = "123"

    def setup() {
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(
                        roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(TestDataManagerReadQueryRole.NAME),
                        roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(TestDataManagerReadQueryRole.NAME)
                )
                .build()
        userRepository.addUser(user1)

        orderDenied1 = metadata.create(TestOrder)
        orderDenied1.number = '1'

        orderDenied2 = metadata.create(TestOrder)
        orderDenied2.number = '2'

        orderAllowed = metadata.create(TestOrder)
        orderAllowed.number = 'allowed_3'

        unsafeDataManager.save(orderDenied1, orderDenied2, orderAllowed)

        systemAuthentication = SecurityContextHelper.getAuthentication()
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)

        userRepository.removeUser(user1)

        new JdbcTemplate(dataSource).execute('delete from TEST_ORDER')
    }


    def "load with empty constraints"() {
        setup:

        authenticate('user1')

        when:

        def result = unsafeDataManager.load(TestOrder.class).all().list()

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

        def result = unsafeDataManager.load(TestOrder.class)
                .all()
                .accessConstraints(accessConstraintsRegistry.getConstraints())
                .list()

        then:

        result.size() == 1

        result.contains(orderAllowed)
    }

    def "load values with constraints"() {
        setup:

        authenticate('user1')

        when:
        def valueLoader = unsafeDataManager.loadValues('select e.number from test_Order e')
                .property('number')
                .accessConstraints(accessConstraintsRegistry.getConstraints())

        List<KeyValueEntity> result = ((FluentValuesLoader) valueLoader).list()

        then:
        result.size() == 1
        result.get(0).getValue('number') == 'allowed_3'
    }

    def "load with constraints by ids"() {
        setup:

        authenticate('user1')

        when:

        unsafeDataManager.load(TestOrder.class)
                .ids(orderDenied1.id, orderAllowed.id, orderDenied2.id)
                .accessConstraints(accessConstraintsRegistry.getConstraints())
                .list()

        then:

        thrown EntityAccessException
    }

    def "load with secured"() {
        setup:

        authenticate('user1')

        when:

        def result = dataManager.load(TestOrder.class)
                .all()
                .list()

        then:

        result.size() == 1

        result.contains(orderAllowed)
    }

    def "load with secured by ids"() {
        setup:

        authenticate('user1')

        when:

        dataManager.load(TestOrder.class)
                .ids(orderDenied1.id, orderAllowed.id, orderDenied2.id)
                .list()

        then:

        thrown EntityAccessException
    }

    def "load values with secured"() {
        setup:

        authenticate('user1')

        when:
        List<KeyValueEntity> result = dataManager.loadValues('select e.number from test_Order e')
                .property('number')
                .list()

        then:
        result.size() == 1
        result.get(0).getValue('number') == 'allowed_3'
    }

    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }
}
