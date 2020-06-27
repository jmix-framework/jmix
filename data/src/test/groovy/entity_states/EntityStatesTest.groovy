/*
 * Copyright (c) 2008-2020 Haulmont.
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

package entity_states

import io.jmix.core.*
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import test_support.DataSpec
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLineA
import test_support.entity.sales.OrderLineB
import test_support.entity.sec.Group
import test_support.entity.sec.Role
import test_support.entity.sec.User
import test_support.entity.sec.UserRole

class EntityStatesTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    EntityStates entityStates
    @Autowired
    JdbcTemplate jdbcTemplate
    @Autowired
    FetchPlans fetchPlans

    void cleanup() {
        jdbcTemplate.update('delete from SALES_ORDER_LINE')
        jdbcTemplate.update('delete from SALES_ORDER')
    }

    def "test getCurrentFetchPlan for object graph"() {
        given:
        Group group = dataManager.save(new Group(name: randomName('group')))
        Role role1 = dataManager.save(new Role(name: randomName('role1')))
        Role role2 = dataManager.save(new Role(name: randomName('role2')))

        User user = new User(name: "user", login: randomName('login'), group: group)
        def userRole1 = new UserRole(role: role1, user: user)
        def userRole2 = new UserRole(role: role2, user: user)
        user.userRoles = [userRole1, userRole2]
        dataManager.save(user, userRole1, userRole2)

        def fetchPlan = fetchPlans.builder(User).addFetchPlan(FetchPlan.LOCAL)
                .addAll("group.name", "userRoles.role.name").build()

        User user1 = dataManager.load(User).id(user.id).fetchPlan(fetchPlan).one()

        when:
        FetchPlan currentFetchPlan = entityStates.getCurrentFetchPlan(user1)

        User user2 = dataManager.load(User).id(user.id).fetchPlan(currentFetchPlan).one()

        then:
        entityStates.isLoadedWithFetchPlan(user2, fetchPlan)

        cleanup:
        dataManager.remove(Id.of(userRole1))
        dataManager.remove(Id.of(userRole2))
        dataManager.remove(Id.of(user))
        dataManager.remove(Id.of(role1))
        dataManager.remove(Id.of(role2))
        dataManager.remove(Id.of(group))
    }

    private String randomName(String base) {
        return base + '-' + RandomStringUtils.randomAlphabetic(5)
    }

    def "test getCurrentFetchPlan for object graph with inheritance"() {
        given:
        def order = new Order(number: '1')
        def lineA = new OrderLineA(order: order, quantity: 1, param1: 'p1')
        def lineB = new OrderLineB(order: order, quantity: 1, param2: 'p2')
        order.orderLines = [lineA, lineB]

        def orderFetchPlan = entityStates.getCurrentFetchPlan(order)
        def lineAFetchPlan = entityStates.getCurrentFetchPlan(lineA)
        def lineBFetchPlan = entityStates.getCurrentFetchPlan(lineB)
        def saveContext = new SaveContext()
                .saving(order, orderFetchPlan).saving(lineA, lineAFetchPlan).saving(lineB, lineBFetchPlan)

        when:
        def committedOrder = dataManager.save(saveContext).get(order)

        then:
        def committedLineA = committedOrder.orderLines.find { it == lineA }
        def committedLineB = committedOrder.orderLines.find { it == lineB }

        (committedLineA as OrderLineA).param1 == 'p1'
        (committedLineB as OrderLineB).param2 == 'p2'
        committedLineA.product == null
        committedLineB.product == null
    }
}
