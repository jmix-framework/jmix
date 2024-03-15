/*
 * Copyright 2024 Haulmont.
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

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlans
import io.jmix.core.Metadata
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.core.repository.JmixDataRepositoryContext
import io.jmix.data.PersistenceHints
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import test_support.DataSpec
import test_support.entity.repository.Address
import test_support.entity.repository.Customer
import test_support.entity.repository.Product
import test_support.entity.repository.SalesOrder
import test_support.repository.OrderWithContextRepository

import java.time.LocalDate
import java.time.ZoneOffset

class JmixDataRepositoryContextTest extends DataSpec {

    @Autowired
    private DataManager dataManager
    @Autowired
    private Metadata metadata
    @Autowired
    private OrderWithContextRepository orderRepository
    @Autowired
    private EntityStates entityStates
    @Autowired
    private FetchPlans fetchPlans


    private Customer customer1, customer2, customer3;
    private SalesOrder order1, order2, order3, order4, order5, order6, removedOrder;
    private Product product1, product2, product3;

    void setup() {

        customer1 = metadata.create(Customer.class)
        customer1.setName("cust1")
        customer1.setAddress(new Address())
        customer1.getAddress().setCity("Samara")

        customer2 = metadata.create(Customer.class)
        customer2.setName("some cust 2")
        customer2.setAddress(new Address())
        customer2.getAddress().setCity("Springfield")

        customer3 = metadata.create(Customer.class)
        customer3.setName("another cust 3")
        customer3.setAddress(new Address())
        customer3.getAddress().setCity("Springfield")

        product1 = metadata.create(Product)
        product1.name = "product1"
        product1.price = 100

        product2 = metadata.create(Product)
        product2.name = "product2"
        product2.price = 1000

        product3 = metadata.create(Product)
        product3.name = "product3"
        product3.price = 10000

        order1 = metadata.create(SalesOrder.class)
        order1.setCustomer(customer1)
        order1.setNumber("111")
        order1.setDate(Date.from(LocalDate.parse("2010-01-01").atStartOfDay().toInstant(ZoneOffset.UTC)))
        order1.count = 1
        order1.product = product1

        order2 = metadata.create(SalesOrder.class)
        order2.setCustomer(customer1)
        order2.setNumber("112")
        order2.setDate(Date.from(LocalDate.parse("2010-03-01").atStartOfDay().toInstant(ZoneOffset.UTC)))
        order2.count = 10
        order2.product = product2

        order3 = metadata.create(SalesOrder.class)
        order3.setCustomer(customer2)
        order3.setNumber("113")
        order3.setDate(Date.from(LocalDate.parse("2004-02-29").atStartOfDay().toInstant(ZoneOffset.UTC)))
        order3.count = 100
        order3.product = product3

        order4 = metadata.create(SalesOrder.class)
        order4.setCustomer(customer2)
        order4.setNumber("114")
        order4.setDate(Date.from(LocalDate.parse("2018-08-19").atStartOfDay().toInstant(ZoneOffset.UTC)))
        order4.count = 1000
        order4.product = product1

        order5 = metadata.create(SalesOrder.class)
        order5.setCustomer(customer3)
        order5.setNumber(null)
        order5.setDate(null)
        order5.count = 10000
        order5.product = product2

        order6 = metadata.create(SalesOrder.class)
        order6.setCustomer(customer3)
        order6.setNumber("115")
        order6.setDate(null)
        order6.count = 10000
        order6.product = product2

        removedOrder = metadata.create(SalesOrder.class)
        removedOrder.setCustomer(customer3)
        removedOrder.setNumber("214")
        removedOrder.setDate(null)
        removedOrder.count = 5000
        removedOrder.product = product3

        dataManager.save(
                customer1,
                customer2,
                customer3,
                product1,
                product2,
                product3,
                order1,
                order2,
                order3,
                order4,
                order5,
                order6,
                removedOrder
        )

        dataManager.remove(removedOrder)
    }


    void "check fetchPlan special parameter priority"() {
        when: "no fetch plan specified in parameters"
        List<SalesOrder> orders = orderRepository.findByCountGreaterThan(9,
                JmixDataRepositoryContext.of(PropertyCondition.lessOrEqual("count", 10)),
                null)
        then: "default fetch plan used"
        orders.size() == 1
        entityStates.isLoaded(orders[0], "customer")
        !entityStates.isLoaded(orders[0], "product")

        when: "fetch plan specified in JmixDataRepositoryContext"
        orders = orderRepository.findByCountGreaterThan(9,
                JmixDataRepositoryContext.condition(PropertyCondition.lessOrEqual("count", 10))
                        .plan(fetchPlans.builder(SalesOrder).add("product").build())
                        .build(),
                null)
        then: "fetch plan from JmixDataRepositoryContext overrides default fetch plan"
        orders.size() == 1
        !entityStates.isLoaded(orders[0], "customer")
        entityStates.isLoaded(orders[0], "product")

        when: "fetch plan specified in FetchPlan parameter"
        orders = orderRepository.findByCountGreaterThan(9,
                JmixDataRepositoryContext.condition(PropertyCondition.lessOrEqual("count", 10))
                        .plan(fetchPlans.builder(SalesOrder).add("product").build())
                        .build(),
                fetchPlans.builder(SalesOrder).addFetchPlan(FetchPlan.LOCAL).build())
        then: "fetch plan from FetchPlan parameter overrides all other fetch plans"
        orders.size() == 1
        !entityStates.isLoaded(orders[0], "customer")
        !entityStates.isLoaded(orders[0], "product")
    }

    void "check fetchPlan special parameter priority for custom query method"() {
        when: "no fetch plan specified in parameters"
        List<SalesOrder> orders = orderRepository.loadByCustomQuery(9,
                JmixDataRepositoryContext.of(PropertyCondition.lessOrEqual("count", 10)),
                null)
        then: "default fetch plan used"
        orders.size() == 1
        entityStates.isLoaded(orders[0], "customer")
        !entityStates.isLoaded(orders[0], "product")

        when: "fetch plan specified in JmixDataRepositoryContext"
        orders = orderRepository.loadByCustomQuery(9,
                JmixDataRepositoryContext.condition(PropertyCondition.lessOrEqual("count", 10))
                        .plan(fetchPlans.builder(SalesOrder).add("product").build())
                        .build(),
                null)
        then: "fetch plan from JmixDataRepositoryContext overrides default fetch plan"
        orders.size() == 1
        !entityStates.isLoaded(orders[0], "customer")
        entityStates.isLoaded(orders[0], "product")

        when: "fetch plan specified in FetchPlan parameter"
        orders = orderRepository.loadByCustomQuery(9,
                JmixDataRepositoryContext.condition(PropertyCondition.lessOrEqual("count", 10))
                        .plan(fetchPlans.builder(SalesOrder).add("product").build())
                        .build(),
                fetchPlans.builder(SalesOrder).addFetchPlan(FetchPlan.LOCAL).build())
        then: "fetch plan from FetchPlan parameter overrides all other fetch plans"
        orders.size() == 1
        !entityStates.isLoaded(orders[0], "customer")
        !entityStates.isLoaded(orders[0], "product")
    }

    void "check hints priority"() {
        when:
        def orders = orderRepository.findByNumberLike("14", null)
        then:
        orders.size() == 2 //114, 214

        when:
        orders = orderRepository.findByNumberLike("14",
                JmixDataRepositoryContext.of(Map.of(PersistenceHints.SOFT_DELETION, "true")))
        then:
        orders.size() == 1 //114 only; 214 is soft deleted
        orders[0].number == "114"

    }

    void "check hints priority for custom query"() {
        when:
        def orders = orderRepository.loadByQueryWithHints("14", null)
        then:
        orders.size() == 2 //114, 214

        when:
        orders = orderRepository.loadByQueryWithHints("14",
                JmixDataRepositoryContext.of(Map.of(PersistenceHints.SOFT_DELETION, "true")))
        then:
        orders.size() == 1 //114 only; 214 is soft deleted
        orders[0].number == "114"
    }

    void "check top works"() {
        when:
        def orders = orderRepository.findTop3ByCountGreaterThanOrderByCount(8)
        then:
        orders.size() == 3
        orders.toList()[0].count == 10
        orders.toList()[1].count == 100
        orders.toList()[2].count == 1000

    }

    void "check paging works with JDRC"() {
        when:
        def customQueryPage = orderRepository.pageByNumberLikeIgnoreCase(
                JmixDataRepositoryContext
                        .condition(PropertyCondition.greater("count", 1))
                        .hints(Map.of(PersistenceHints.SOFT_DELETION, false))
                        .build(),
                "%1%",
                PageRequest.of(1, 2, Sort.by("number")));

        def derivedMethodPage = orderRepository.findByNumberLikeIgnoreCase(
                "%1%",
                JmixDataRepositoryContext
                        .condition(PropertyCondition.greater("count", 1))
                        .hints(Map.of(PersistenceHints.SOFT_DELETION, false))
                        .build(),

                PageRequest.of(1, 2, Sort.by("number")));
        then:
        customQueryPage.getTotalElements() == 5
        customQueryPage.getTotalPages() == 3
        customQueryPage.toList()[0].number == "114"
        customQueryPage.toList()[1].number == "115"

        derivedMethodPage.getTotalElements() == 5
        derivedMethodPage.getTotalPages() == 3
        derivedMethodPage.toList()[0].number == "114"
        derivedMethodPage.toList()[1].number == "115"
    }

    void "check custom query with JDRC"() {
        when:
        def orders = orderRepository.findByQueryAndJDRC(100,
                JmixDataRepositoryContext.condition(PropertyCondition.notEqual("number", null))
                        .hints(Map.of(PersistenceHints.SOFT_DELETION, "false",
                                PersistenceHints.FETCH_PLAN, fetchPlans.builder(SalesOrder).add("customer").build()))
                        .build())
        then:
        orders.size() == 4

        orders[0].number == "214"
        orders[1].number == "115"
        orders[2].number == "114"
        orders[3].number == "113"

        entityStates.isLoaded(orders[0], "customer")
        !entityStates.isLoaded(orders[0], "product")
    }

    void "check remove by JDRC"() {
        when:
        orderRepository.removeByNumberNotNull(JmixDataRepositoryContext.of(PropertyCondition.greater("count", 2)))
        def remaining = orderRepository.findAll()
        then:
        remaining.size() == 2
    }

    void "check hints for remove operation"() {
        when:
        JmixDataRepositoryContext hardDeleteContext = JmixDataRepositoryContext.of(Map.of(PersistenceHints.SOFT_DELETION, false))
        orderRepository.removeByNumberNotNull(hardDeleteContext)
        def remaining = orderRepository.findAll(hardDeleteContext)

        then:
        remaining.size() == 1
        remaining.iterator().next().number == null
    }

    void cleanup() {
        jdbc.update('delete from REPOSITORY_SALES_ORDER')
        jdbc.update('delete from REPOSITORY_SALES_PRODUCT')
        jdbc.update('delete from REPOSITORY_CUSTOMER')
        jdbc.update('delete from REPOSITORY_EMPLOYEE')
    }
}
