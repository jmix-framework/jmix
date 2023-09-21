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
import io.jmix.core.constraint.RowLevelConstraint
import io.jmix.core.entity.EntityValues
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
import test_support.entity.ManyToOneEntity
import test_support.entity.OneToManyEntity
import test_support.entity.TestOrder
import test_support.role.TestDefaultConstraintsRole

import javax.sql.DataSource

class UnconstrainedDataManagerRowLevelConstraintsTest extends SecurityDataSpecification {
    @Autowired
    UnconstrainedDataManager unconstrainedDataManager
    @Autowired
    DataManager dataManager

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

    UUID manyToOneId, oneToManyId

    Authentication systemAuthentication

    public static final String PASSWORD = "123"

    def setup() {
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(
                        roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(TestDefaultConstraintsRole.NAME)
                )
                .build()
        userRepository.addUser(user1)

        orderDenied1 = metadata.create(TestOrder)
        orderDenied1.number = '1'

        orderDenied2 = metadata.create(TestOrder)
        orderDenied2.number = '2'

        orderAllowed = metadata.create(TestOrder)
        orderAllowed.number = 'allowed_3'

        dataManager.unconstrained().save(orderDenied1, orderDenied2, orderAllowed)

        OneToManyEntity oneToManyEntity = metadata.create(OneToManyEntity.class)
        oneToManyEntity.setName("allowed_1")
        dataManager.unconstrained().save(oneToManyEntity)
        oneToManyId = EntityValues.getId(oneToManyEntity) as UUID

        ManyToOneEntity manyToOneEntity = metadata.create(ManyToOneEntity.class)
        manyToOneEntity.setName("1")
        manyToOneEntity.setOneToManyEntity(oneToManyEntity)
        dataManager.unconstrained().save(manyToOneEntity)

        manyToOneEntity = metadata.create(ManyToOneEntity.class)
        manyToOneEntity.setName("allowed_1")
        manyToOneEntity.setOneToManyEntity(oneToManyEntity)
        dataManager.unconstrained().save(manyToOneEntity)
        manyToOneId = EntityValues.getId(manyToOneEntity) as UUID

        systemAuthentication = SecurityContextHelper.getAuthentication()
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)

        userRepository.removeUser(user1)

        new JdbcTemplate(dataSource).execute('delete from TEST_ORDER;' +
                ' delete from TEST_MANY_TO_ONE_ENTITY;' +
                ' delete from TEST_ONE_TO_MANY_ENTITY;')
    }

    def "load root"() {
        setup:

        authenticate('user1')

        when:

        def result = unconstrainedDataManager.load(TestOrder.class)
                .all()
                .accessConstraints(RowLevelConstraint)
                .list()

        then:

        result.size() == 1

        result.contains(orderAllowed)
    }

    def "count root"() {
        setup:

        authenticate('user1')

        when:

        def count = unconstrainedDataManager.getCount(
                new LoadContext<>(metadata.getClass(TestOrder))
                        .setAccessConstraints(accessConstraintsRegistry.getConstraintsOfType(RowLevelConstraint)))

        then:

        count == 1
    }

    def "load collection"() {
        setup:

        authenticate('user1')

        when:

        def oneToManyEntity = unconstrainedDataManager.load(OneToManyEntity.class)
                .id(oneToManyId)
                .accessConstraints(RowLevelConstraint)
                .one()

        then:

        oneToManyEntity.manyToOneEntities.size() == 1
    }

    def "load collection eagerly"() {
        setup:

        authenticate('user1')

        when:

        def oneToManyEntity = unconstrainedDataManager.load(OneToManyEntity.class)
                .id(oneToManyId)
                .accessConstraints(RowLevelConstraint)
                .fetchPlan({ fpb -> fpb.addAll('name', 'manyToOneEntities.name') })
                .one()

        then:

        oneToManyEntity.manyToOneEntities.size() == 1
    }

    protected void authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }
}
