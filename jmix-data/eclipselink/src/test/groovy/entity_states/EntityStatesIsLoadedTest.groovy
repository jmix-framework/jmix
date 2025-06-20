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

package entity_states

import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlan
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.sales.*

class EntityStatesIsLoadedTest extends DataSpec {

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

    def "test load single entity"() {
        when:
        def customer1 = dataManager.load(Customer).id(this.customer.id).one()

        then:
        ['deleteTs', 'updatedBy', 'createdBy', 'name', 'createTs', 'id', 'version', 'updateTs', 'deletedBy', 'status'].each {
            assert entityStates.isLoaded(customer1, it)
        }

        when:
        def customer2 = dataManager.load(Customer).id(this.customer.id).fetchPlanProperties('name').one()

        then:
        ['id', 'version', 'name', 'deleteTs', 'deletedBy'].each {
            assert entityStates.isLoaded(customer2, it)
        }
        ['createTs', 'createdBy', 'updateTs', 'updatedBy', 'status'].each {
            assert !entityStates.isLoaded(customer2, it)
        }
    }

    def "test load graph"() {
        def order

        when:
        order = dataManager.load(Order).id(this.order.id).one()

        then:
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user' /*user is not included in _base fetch plan but marked as loaded because the reference is null*/ ].each {
            assert entityStates.isLoaded(order, it)
        }

        ['customer', 'orderLines'].each {
            assert !entityStates.isLoaded(order, it)
        }

        when:
        order = dataManager.load(Order)
                .id(this.order.id)
                .fetchPlan(fpb ->
                        fpb.addFetchPlan(FetchPlan.BASE).add('customer'))
                .one()

        then:
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user', 'customer'].each {
            assert entityStates.isLoaded(order, it)
        }

        ['id', 'version', 'deleteTs', 'deletedBy'].each {
            assert entityStates.isLoaded(order.customer, it)
        }
        ['name', 'status'].each {
            assert !entityStates.isLoaded(order.customer, it)
        }


        when:
        order = dataManager.load(Order)
                .id(this.order.id)
                .fetchPlan(fpb -> fpb.addFetchPlan(FetchPlan.BASE).add('customer', FetchPlan.BASE))
                .one()

        then:
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'name', 'status'].each {
            assert entityStates.isLoaded(order.customer, it)
        }

        when:
        order = dataManager.load(Order)
                .id(this.order.id)
                .fetchPlan(fpb -> fpb.addFetchPlan(FetchPlan.BASE).add('orderLines', FetchPlan.BASE))
                .one()

        then:
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'quantity'].each {
            assert entityStates.isLoaded(order.orderLines.find({ it instanceof OrderLineA }), it)
        }

        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'quantity'].each {
            assert entityStates.isLoaded(order.orderLines.find({ it instanceof OrderLineB }), it)
        }
    }

    def "test lazy loading"() {
        when:
        def order = dataManager.load(Order).id(this.order.id).one()

        then:
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user' /*user is not included in _base fetch plan but marked as loaded because the reference is null*/ ].each {
            assert entityStates.isLoaded(order, it)
        }

        ['customer', 'orderLines'].each {
            assert !entityStates.isLoaded(order, it)
        }

        when:
        def customer = order.customer

        then:
        customer != null

        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user', 'customer'].each {
            assert entityStates.isLoaded(order, it)
        }

        ['deleteTs', 'updatedBy', 'createdBy', 'name', 'createTs', 'id', 'version', 'updateTs', 'deletedBy', 'status'].each {
            assert entityStates.isLoaded(customer, it)
        }

        when:
        def orderLines = order.orderLines
        orderLines.size() == 2

        then:
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user', 'customer', 'orderLines'].each {
            assert entityStates.isLoaded(order, it)
        }

        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'quantity'].each {
            assert entityStates.isLoaded(orderLines.find({ it instanceof OrderLineA }), it)
        }

        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'quantity'].each {
            assert entityStates.isLoaded(orderLines.find({ it instanceof OrderLineB }), it)
        }
    }

    def "test lazy loading reference replacement"() {
        when:
        def order = dataManager.load(Order).id(this.anotherOrder.id).one()
        def orderLines = order.orderLines
        def firstLine = orderLines[0]
        def product = firstLine.product

        then:
        // these fields are actually loaded because ValueHolder has set them as back- or existing references
        ['order', 'secondOrder'].each {
            assert entityStates.isLoaded(firstLine, it)
        }

        entityStates.isLoaded(firstLine, 'product')
        // while it has equal value, it is not loaded because it's not a back reference
        !entityStates.isLoaded(firstLine, 'secondProduct')
    }

    def "test fetching in managed state"() {
        expect:

        transaction.executeWithoutResult {
            def order = entityManager.find(Order, this.order.id)

            ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
             'user' /*user is not local but loaded because the reference is null*/ ].each {
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

    def "test persist"() {
        when:
        def order = new Order(number: '1')

        then:
        // new
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user', 'customer', 'orderLines'].each {
            assert entityStates.isLoaded(order, it)
        }

        and:
        transaction.executeWithoutResult {
            entityManager.persist(order)
            // managed
            ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
             'user', 'customer', 'orderLines'].each {
                assert entityStates.isLoaded(order, it)
            }
        }
        // detached
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user', 'customer', 'orderLines'].each {
            assert entityStates.isLoaded(order, it)
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
        order.setCustomer(customer) // lazy loading occurs

        then:
        // detached
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user', 'customer'].each {
            assert entityStates.isLoaded(order, it)
        }
        def mergedDetachedOrder = transaction.execute {
            def mergedOrder = entityManager.merge(order)
            // merged managed
            ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
             'user', 'customer'].each {
                assert entityStates.isLoaded(mergedOrder, it)
            }
            return mergedOrder
        }
        // merged detached
        ['id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
         'user', 'customer'].each {
            assert entityStates.isLoaded(mergedDetachedOrder, it)
        }
        order.customer == customer
    }
}
