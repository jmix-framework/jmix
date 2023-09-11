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

import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanRepository
import io.jmix.core.Metadata
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.role.RoleGrantedAuthorityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.SecurityDataSpecification
import test_support.entity.TestOrder
import test_support.repository.FirstRepository
import test_support.repository.SecondRepository
import test_support.repository.ThirdRepository
import test_support.role.TestDataManagerReadQueryRole

import javax.sql.DataSource

class RepositorySecurityTest extends SecurityDataSpecification {

    @Autowired
    Metadata metadata
    @Autowired
    AuthenticationManager authenticationManager
    @Autowired
    InMemoryUserRepository userRepository
    @Autowired
    DataSource dataSource
    @Autowired
    UnconstrainedDataManager unsafeDataManager
    @Autowired
    FetchPlanRepository fetchPlanRepository
    @Autowired
    FirstRepository firstRepository
    @Autowired
    SecondRepository secondRepository
    @Autowired
    ThirdRepository thirdRepository
    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils

    public static final String PASSWORD = "123"
    Authentication systemAuthentication

    private TestOrder orderDenied1, orderDenied2, orderAllowed
    private UserDetails user1

    def setup() {
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(
                        roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(TestDataManagerReadQueryRole.NAME),
                        roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(TestDataManagerReadQueryRole.NAME)
                )
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

        authenticate('user1')
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)
        userRepository.removeUser(user1)
        new JdbcTemplate(dataSource).execute('delete from TEST_ORDER;')
    }


    def "test CRUD method constraints"() {
        setup:
        FetchPlan plan = fetchPlanRepository.findFetchPlan(metadata.getClass(TestOrder), "_local")

        when: "constraints..."
        Page enabledOnInterface = firstRepository.findAll(Pageable.ofSize(10))
        def disabledOnMethod = firstRepository.findAll(plan)
        def enabledOnParentInterface = firstRepository.findAll(Sort.by("id"), plan)
        def disabledOnInterface = secondRepository.findAll(Sort.by("id"), plan)
        def disabledOnParentMethod = thirdRepository.findAll(plan)
        def disabledOnParentInterface = thirdRepository.findAll(Sort.by("id"), plan)
        Page enabledOnMethod = secondRepository.findAll(Pageable.ofSize(10))

        then: "2 of 3 orders filtered out by constraints when them works"
        enabledOnInterface.content.size() == 1
        disabledOnMethod.size() == 3
        enabledOnParentInterface.size() == 1
        disabledOnInterface.size() == 3
        disabledOnParentMethod.size() == 3
        disabledOnParentInterface.size() == 3
        enabledOnMethod.content.size() == 1
    }

    def "test custom query method constraints"() {
        when: "constraints..."
        def disabledOnMethod = firstRepository.findByIdNotNull()
        def disabledOnInterface = secondRepository.getByIdNotNull()
        def disabledOnParentInterface = thirdRepository.findByIdNotNull()
        def disabledOnParentMethod = thirdRepository.findByIdNotNull()

        def enabledOnMethod = secondRepository.searchByIdNotNull()
        def enabledOnParentMethod = thirdRepository.searchByIdNotNull()
        def enabledOnParentInterface = firstRepository.searchByNumberNotNull()

        then: "2 of 3 orders filtered out by constraints when them works"
        disabledOnMethod.size() == 3
        disabledOnInterface.size() == 3
        disabledOnParentInterface.size() == 3
        disabledOnParentMethod.size() == 3

        enabledOnMethod.size() == 1
        enabledOnParentMethod.size() == 1
        enabledOnParentInterface.size() == 1
    }

    def "test @FetchPlan inheritance"() {
        when: "current method annotated with @FetchPlan"
        TestOrder order = firstRepository.searchById(orderAllowed.id)[0]
        order.getNumber()
        then: "fetch plan applied, 'number' attribute is not in '_instance_name' fetch plan"
        thrown(IllegalStateException)

        when: "parent method annotated with @FetchPlan"
        order = secondRepository.searchById(orderAllowed.id)[0]
        order.getNumber()
        then: "fetch plan applied, 'number' attribute is not in '_instance_name' fetch plan"
        thrown(IllegalStateException)

        when: "inherited method also annotaded with @FetchPlan"
        order = thirdRepository.searchById(orderAllowed.id)[0]
        String number = order.getNumber()
        then: "current method annotation overrides parent method annotation"
        number == "allowed_3"
    }

    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }
}
