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
import jakarta.persistence.EntityManagerFactory
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import test_support.DataSpec
import test_support.entity.dto.TestNullableIdDto
import test_support.entity.dto.TestUuidDto
import test_support.entity.entity_extension.Client
import test_support.entity.petclinic.Owner
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLineA
import test_support.entity.sales.OrderLineB
import test_support.entity.sec.Group
import test_support.entity.sec.Role
import test_support.entity.sec.User
import test_support.entity.sec.UserRole

class EntityStatesTest extends DataSpec {

    @Autowired
    Metadata metadata
    @Autowired
    DataManager dataManager
    @Autowired
    EntityStates entityStates
    @Autowired
    JdbcTemplate jdbcTemplate
    @Autowired
    FetchPlans fetchPlans
    @Autowired
    EntityManagerFactory entityManagerFactory

    void cleanup() {
        jdbcTemplate.update('delete from SALES_ORDER_LINE')
        jdbcTemplate.update('delete from SALES_ORDER')
        jdbcTemplate.update('delete from APP_OWNER')
    }

    def "test isNew for JPA entities"() {
        when:
        def owner = metadata.create(Owner)

        then:
        entityStates.isNew(owner)

        when:
        def wasNewInsideTx = transaction.execute {
            def em = entityManagerFactory.createEntityManager()
            em.persist(owner)
            return entityStates.isNew(owner)
        }

        then:
        wasNewInsideTx
        !entityStates.isNew(owner)
    }

    def "test isNew for DTO entities"() {
        when:
        def uuidDto = metadata.create(TestUuidDto)

        then:
        uuidDto.id != null
        entityStates.isNew(uuidDto)

        when:
        entityStates.setNew(uuidDto, false)

        then:
        !entityStates.isNew(uuidDto)

        when:
        def nullableIdDto = metadata.create(TestNullableIdDto)

        then:
        nullableIdDto.id == null
        entityStates.isNew(nullableIdDto)

        when:
        entityStates.setNew(nullableIdDto, false)

        then:
        !entityStates.isNew(nullableIdDto)
    }

    def "test getCurrentFetchPlan for object graph"() {
        given:

        Group group = dataManager.create(Group)
        group.name = randomName('group')

        group = dataManager.save(group)

        Role role1 = dataManager.create(Role)
        role1.name = randomName('role1')

        role1 = dataManager.save(role1)

        Role role2 = dataManager.create(Role)
        role2.name = randomName('role2')

        role2 = dataManager.save(role2)

        User user = dataManager.create(User)
        user.name = "user"
        user.login = randomName('login')
        user.group = group

        def userRole1 = dataManager.create(UserRole)
        userRole1.role = role1
        userRole1.user = user

        def userRole2 = dataManager.create(UserRole)
        userRole2.role = role2
        userRole2.user = user

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
        def order = dataManager.create(Order)
        order.number = '1'

        def lineA = dataManager.create(OrderLineA)
        lineA.order = order
        lineA.quantity = 1
        lineA.param1 = 'p1'

        def lineB = dataManager.create(OrderLineB)
        lineB.order = order
        lineB.quantity = 1
        lineB.param2 = 'p2'

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

    def "test getCurrentFetchPlan for new entity with reference"() {
        given:

        Group group = dataManager.create(Group)
        group.name = randomName('group')
        group = dataManager.save(group)

        User user = dataManager.create(User)
        user.name = "user"
        user.login = randomName('login')
        user.group = group
        user = dataManager.save(user)

        def order = dataManager.create(Order)
        order.number = '1'
        order.user = user

        when:
        def fetchPlan = entityStates.getCurrentFetchPlan(order)

        then:
        fetchPlan.containsProperty('number')
        fetchPlan.containsProperty('date') // regardless of null

        // customer is a reference and null, so it's not in FP. Will be lazily loaded if needed.
        !fetchPlan.containsProperty('customer')

        // user is set, so it's in FP
        fetchPlan.containsProperty('user')
        fetchPlan.getProperty('user').fetchPlan.containsProperty('name')
        fetchPlan.getProperty('user').fetchPlan.containsProperty('firstName') // regardless of null
        fetchPlan.getProperty('user').fetchPlan.containsProperty('group')
        fetchPlan.getProperty('user').fetchPlan.getProperty('group').fetchPlan.containsProperty('name')

        // orderLines is null, so it's not in FP. Will be lazily loaded if needed.
        !fetchPlan.containsProperty('orderLines')

        cleanup:
        dataManager.remove(Id.of(user))
        dataManager.remove(Id.of(group))
    }

    def "method-based attribute loaded state determined correctly"() {
        setup:
        var client = dataManager.create(Client);
        client.name = "Diego"
        client.address.city = "Khorinis"
        client.address.street = "Square"
        dataManager.save(client)

        when:
        var loadedClient = dataManager.load(Client)
                .id(client.id)
                .fetchPlan { fp ->
                    fp.addAll('name', 'address', "address.street").partial()
                }
                .one();
        then:
        !entityStates.isLoaded(loadedClient, "label1")
        !entityStates.isLoaded(loadedClient, "label2")
        !entityStates.isLoaded(loadedClient, "addressCity")

        entityStates.isLoaded(loadedClient, "htmlName")
        entityStates.isLoaded(loadedClient, "label3")
        entityStates.isLoaded(loadedClient, "addressStreet")

        cleanup:
        dataManager.remove(client)
    }
}
