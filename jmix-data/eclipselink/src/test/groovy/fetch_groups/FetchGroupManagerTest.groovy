/*
 * Copyright 2022 Haulmont.
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

package fetch_groups

import io.jmix.core.*
import io.jmix.eclipselink.impl.FetchGroupManager
import org.eclipse.persistence.config.QueryHints
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.sales.*

class FetchGroupManagerTest extends DataSpec {

    @Autowired
    DataManager dm
    @Autowired
    EntityStates entityStates
    @Autowired
    FetchPlans fetchPlans
    @Autowired
    FetchGroupManager fetchGroupManager

    Customer customer
    Order order
    OrderLine orderLine1
    OrderLine orderLine2
    Product product1
    Product product2

    @Override
    void setup() {
        customer = dm.create(Customer)
        customer.name = 'cust1'
        dm.save(customer)

        product1 = dm.create(Product)
        product1.name = 'prod1'
        product1.quantity = 10
        product2 = dm.create(Product)
        product2.name = 'prod2'
        product2.quantity = 20
        dm.save(product1, product2)

        this.order = dm.create(Order)
        this.order.number = order.id.toString()
        this.order.amount = BigDecimal.ONE
        this.order.customer = customer
        dm.save(this.order)

        orderLine1 = dm.create(OrderLineA)
        orderLine1.order = this.order
        orderLine1.quantity = 1
        orderLine1.product = product1
        orderLine2 = dm.create(OrderLineA)
        orderLine2.order = this.order
        orderLine2.quantity = 2
        orderLine2.product = product2
        dm.save(orderLine1, orderLine2)
    }

    def "single result: order.customer, order.orderLines"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', FetchPlan.BASE)
                .add('orderLines', FetchPlan.BASE)
                .build()

        when:
        def fetchGroupDescription = fetchGroupManager.calculateFetchGroup(
                'select o from sales_Order o', fetchPlan, true, true)

        then:
        fetchGroupDescription.hints.isEmpty()

        when:
        def order = dm.load(Id.of(order)).fetchPlan(fetchPlan).one()

        then:
        order == this.order
        entityStates.isLoaded(order, 'customer')
        entityStates.isLoaded(order, 'orderLines')
        entityStates.isLoaded(order.orderLines[0], 'quantity')
    }

    def "single result: order.customer{JOIN}, order.orderLines{BATCH}"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', { it.addFetchPlan(FetchPlan.BASE) }, FetchMode.JOIN)
                .add('orderLines', { it.addFetchPlan(FetchPlan.BASE) }, FetchMode.BATCH)
                .build()

        when:
        def fetchGroupDescription = fetchGroupManager.calculateFetchGroup(
                'select o from sales_Order o', fetchPlan, true, true)

        then:
        fetchGroupDescription.hints.size() == 2
        fetchGroupDescription.hints['o.customer'] == QueryHints.LEFT_FETCH
        fetchGroupDescription.hints['o.orderLines'] == QueryHints.BATCH

        when:
        def order = dm.load(Id.of(order)).fetchPlan(fetchPlan).one()

        then:
        order == this.order
        entityStates.isLoaded(order, 'customer')
        entityStates.isLoaded(order, 'orderLines')
        entityStates.isLoaded(order.orderLines[0], 'quantity')
    }

    def "single result: order.customer, order.orderLines.product"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', FetchPlan.BASE)
                .add('orderLines', FetchPlan.BASE)
                .add('orderLines.product', FetchPlan.BASE)
                .build()

        when:
        def fetchGroupDescription = fetchGroupManager.calculateFetchGroup(
                'select o from sales_Order o', fetchPlan, true, true)

        then:
        fetchGroupDescription.hints.isEmpty()

        when:
        def order = dm.load(Id.of(order)).fetchPlan(fetchPlan).one()

        then:
        order == this.order
        entityStates.isLoaded(order, 'customer')
        entityStates.isLoaded(order, 'orderLines')
        entityStates.isLoaded(order.orderLines[0], 'quantity')
        entityStates.isLoaded(order.orderLines[0], 'product')
        entityStates.isLoaded(order.orderLines[0].product, 'quantity')
    }

    def "multiple results: order.customer, order.orderLines"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', FetchPlan.BASE)
                .add('orderLines', FetchPlan.BASE)
                .build()

        when:
        def fetchGroupDescription = fetchGroupManager.calculateFetchGroup(
                'select o from sales_Order o', fetchPlan, false, true)

        then:
        fetchGroupDescription.hints['o.customer'] == QueryHints.LEFT_FETCH
        fetchGroupDescription.hints['o.orderLines'] == QueryHints.BATCH

        when:
        def orders = dm.load(Order)
                .query('e.number = ?1', order.id.toString())
                .fetchPlan(fetchPlan)
                .list()

        then:
        orders.size() == 1
        def order = orders[0]
        order == this.order
        entityStates.isLoaded(order, 'customer')
        entityStates.isLoaded(order, 'orderLines')
        entityStates.isLoaded(order.orderLines[0], 'quantity')
    }

    def "multiple results: order.customer, order.orderLines.product"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', FetchPlan.BASE)
                .add('orderLines', FetchPlan.BASE)
                .add('orderLines.product', FetchPlan.BASE)
                .build()

        when:
        def fetchGroupDescription = fetchGroupManager.calculateFetchGroup(
                'select o from sales_Order o', fetchPlan, false, true)

        then:
        fetchGroupDescription.hints['o.customer'] == QueryHints.LEFT_FETCH
        fetchGroupDescription.hints['o.orderLines'] == QueryHints.BATCH
        fetchGroupDescription.hints['o.orderLines.product'] == QueryHints.BATCH

        when:
        def orders = dm.load(Order)
                .query('e.number = ?1', order.id.toString())
                .fetchPlan(fetchPlan)
                .list()

        then:
        orders.size() == 1
        def order = orders[0]
        order == this.order
        entityStates.isLoaded(order, 'customer')
        entityStates.isLoaded(order, 'orderLines')
        entityStates.isLoaded(order.orderLines[0], 'quantity')
        entityStates.isLoaded(order.orderLines[0], 'product')
        entityStates.isLoaded(order.orderLines[0].product, 'quantity')
    }
}
