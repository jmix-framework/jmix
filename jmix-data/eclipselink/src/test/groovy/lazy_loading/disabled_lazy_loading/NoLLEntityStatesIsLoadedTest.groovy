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

package lazy_loading.disabled_lazy_loading

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlan
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import test_support.DataSpec
import test_support.entity.sales.*


@TestPropertySource(properties = ["jmix.eclipselink.disableLazyLoading = true"])
class NoLLEntityStatesIsLoadedTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    EntityStates entityStates
    @PersistenceContext
    EntityManager entityManager

    private Customer customer
    private Order order
    private Order anotherOrder
    private OrderLineA line1
    private OrderLineB line2
    private OrderLineA anotherOrderLine
    private Product product1
    private Product product2

    @Override
    void setup() {
        this.customer = dataManager.create(Customer)
        this.customer.name = 'cust-1'
        this.customer.status = Status.OK
        dataManager.save(this.customer)

        this.order = dataManager.create(Order)
        this.order.customer = this.customer
        this.order.number = '1'
        dataManager.save(this.order)

        product1 = dataManager.create(Product)
        product1.name = 'p1'
        dataManager.save(product1)

        product2 = dataManager.create(Product)
        product2.name = 'p1'
        dataManager.save(product2)

        line1 = dataManager.create(OrderLineA)
        line1.order = this.order
        line1.product = product1
        line1.quantity = 1
        line1.param1 = 'value1'
        dataManager.save(line1)

        line2 = dataManager.create(OrderLineB)
        line2.order = this.order
        line2.product = product2
        line2.quantity = 2
        line2.param2 = 'value2'
        dataManager.save(line2)

        anotherOrder = dataManager.create(Order)
        anotherOrder.customer = this.customer
        anotherOrder.number = '2'
        dataManager.save(anotherOrder)

        anotherOrderLine = dataManager.create(OrderLineA)
        anotherOrderLine.order = anotherOrder
        anotherOrderLine.secondOrder = anotherOrder
        anotherOrderLine.product = product1
        anotherOrderLine.secondProduct = product1
        anotherOrderLine.quantity = 1
        dataManager.save(anotherOrderLine)
    }

    def "test lazy loading"() {
        when:
        def order = dataManager.load(Order).id(this.order.id).one()

        then:
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user' /*user is not included in _base fetch plan but marked as loaded because the reference is null*/].each {
            assert entityStates.isLoaded(order, it)
        }

        ['customer', 'orderLines'].each {
            assert !entityStates.isLoaded(order, it)
        }

        when:
        def customer = order.customer

        then:
        def exception1 = thrown(IllegalStateException)
        exception1.getMessage().contains("[customer]")
        exception1.getMessage().contains(order.class.name)

        when:
        def orderLines = order.orderLines
        orderLines.size() == 2

        then:
        def exception2 = thrown(IllegalStateException)
        exception2.getMessage().contains("[orderLines]")
        exception2.getMessage().contains(order.class.name)
    }

    @Ignore("till back reference setting will be fixed")//todo [jmix-framework/jmix#3936]
    def "test fetching back references"() {
        when:
        def order = dataManager.load(Order).id(this.anotherOrder.id).fetchPlan(b -> {
            b.addAll("orderLines")
        }).one()
        def orderLines = order.orderLines
        def firstLine = orderLines[0]
        //def product = firstLine.product

        then: "back references loaded (during fetching by fetch-plan)"

        //back reference is not set during fetching by fetch plan (with LL too)
        entityStates.isLoaded(firstLine, 'order')

        entityStates.isLoaded(firstLine, 'product')

        // while it has equal value, it is not loaded because it's not a back reference
        !entityStates.isLoaded(firstLine, 'secondProduct')
    }

    def "test fetching in managed state"() {
        expect:

        transaction.executeWithoutResult {
            def order = entityManager.find(Order, this.order.id)

            ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
             'user' /*user is not local but loaded because the reference is null*/].each {
                assert entityStates.isLoaded(order, it)
            }

            ['customer', 'orderLines'].each {
                assert !entityStates.isLoaded(order, it)
            }

            def customer = order.customer

            assert entityStates.isLoaded(order, 'customer')

            ['deleteTs', 'updatedBy', 'createdBy', 'name', 'createTs', 'id', 'version', 'updateTs', 'deletedBy', 'status'].each {
                assert entityStates.isLoaded(customer, it)
            }
        }
    }

    def "test merge"() {
        Order order

        when:
        order = dataManager.load(Order).id(this.order.id).one()

        def customer = dataManager.create(Customer)
        customer.name = 'cust-2'
        dataManager.save(customer)

        order.setDate(new Date())

        order.setCustomer(customer) // setting value requires this value to be loaded

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage().contains("[customer]")
        exception.getMessage().contains(order.class.name)
    }
}
