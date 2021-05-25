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

package repository


import io.jmix.core.Metadata
import io.jmix.core.UnsafeDataManager
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.authentication.RoleGrantedAuthority
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
import test_support.repository.ChildSafeRepository
import test_support.repository.ChildUnsafeRepository
import test_support.repository.ParentUnsafeRepository
import test_support.repository.SafeRepository
import test_support.role.TestDataManagerReadQueryRole

import javax.sql.DataSource

class RepositorySecurityTest extends SecurityDataSpecification {

    @Autowired
    SafeRepository safeRepository
    @Autowired
    ParentUnsafeRepository parentUnsafeRepository
    @Autowired
    ChildUnsafeRepository childUnsafeRepository
    @Autowired
    ChildSafeRepository childSafeRepository

    @Autowired
    Metadata metadata
    @Autowired
    AuthenticationManager authenticationManager
    @Autowired
    InMemoryUserRepository userRepository
    @Autowired
    RowLevelRoleRepository rowLevelRoleRepository
    @Autowired
    DataSource dataSource
    @Autowired
    UnsafeDataManager unsafeDataManager
    @Autowired
    ResourceRoleRepository resourceRoleRepository

    public static final String PASSWORD = "123"
    Authentication systemAuthentication

    private TestOrder orderDenied1, orderDenied2, orderAllowed
    private UserDetails user1

    def setup() {
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.
                        withRowLevelRoleProvider({ rowLevelRoleRepository.getRoleByCode(it) })
                        .withResourceRoleProvider({ resourceRoleRepository.getRoleByCode(it) })
                        .withRowLevelRoles(TestDataManagerReadQueryRole.NAME)
                        .withResourceRoles(TestDataManagerReadQueryRole.NAME)
                        .build())
                .build()
        userRepository.addUser(user1)

        orderDenied1 = metadata.create(TestOrder)
        orderDenied1.number = '1'

        orderDenied2 = metadata.create(TestOrder)
        orderDenied2.number = '2'

        orderAllowed = metadata.create(TestOrder)
        orderAllowed.number = 'allowed_3'

        unsafeDataManager.save(orderAllowed, orderDenied1, orderDenied2)

        systemAuthentication = SecurityContextHelper.getAuthentication()
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)
        userRepository.removeUser(user1)
        new JdbcTemplate(dataSource).execute('delete from TEST_ORDER;')
    }


    def "@UnsafeDataRepository works"() {
        setup:
        authenticate('user1')

        when:
        def secure = safeRepository.findAll();
        def secureCustomMethod = safeRepository.findOrdersByNumberNotNull()

        def unsecure = parentUnsafeRepository.findAll()
        def inheritedUnsecure = childUnsafeRepository.findAll()
        def inheritedUnsecureCustomMethod = childUnsafeRepository.findOrdersByNumberNotNull()

        def inheritedSecure = childSafeRepository.findAll()
        def inheritedSecureCustomMethod = childSafeRepository.findOrdersByIdNotNull()
        def inheritedSecureOverridden = childSafeRepository.findOrdersByNumberNotNull()

        then:
        secure.size() == 1
        secure[0] == orderAllowed
        secureCustomMethod.size() == 1

        unsecure.size() == 3
        unsecure.contains(orderDenied1)
        unsecure.contains(orderAllowed)
        unsecure.contains(orderDenied2)
        inheritedUnsecure.size() == 3
        inheritedUnsecureCustomMethod.size() == 3

        inheritedSecure.size() == 1
        inheritedSecureCustomMethod.size() == 1
        inheritedSecureOverridden.size() == 1
    }

    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }
}
