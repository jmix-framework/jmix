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

package loaded_attributes

import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import io.jmix.core.entity.EntitySystemAccess
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.sales.*

class LoadedPropertiesTest extends DataSpec {

    @Autowired
    DataManager dataManager

    private Customer customer
    private Order order
    private OrderLineA line1
    private OrderLineB line2
    private Product product1
    private Product product2

    @Override
    void setup() {
        this.customer = dataManager.create(Customer)
        this.customer.name = 'cust-1'
        this.customer.status = Status.OK
        dataManager.save(this.customer)

        order = dataManager.create(Order)
        order.customer = this.customer
        order.number = '1'
        dataManager.save(order)

        product1 = dataManager.create(Product)
        product1.name = 'p1'
        dataManager.save(product1)

        product2 = dataManager.create(Product)
        product2.name = 'p1'
        dataManager.save(product2)

        line1 = dataManager.create(OrderLineA)
        line1.order = order
        line1.product = product1
        line1.quantity = 1
        line1.param1 = 'value1'
        dataManager.save(line1)

        line2 = dataManager.create(OrderLineB)
        line2.order = order
        line2.product = product2
        line2.quantity = 2
        line2.param2 = 'value2'
        dataManager.save(line2)
    }

    def "test single entity"() {
        when:
        def customer = dataManager.load(Customer).id(this.customer.id).one()

        then:
        EntitySystemAccess.getEntityEntry(customer).loadedProperties == [
                'deleteTs', 'updatedBy', 'createdBy', 'name', 'createTs', 'id', 'version', 'updateTs', 'deletedBy', 'status'
        ] as Set

        when:
        def cust2 = dataManager.load(Customer).id(this.customer.id).fetchPlanProperties('name').one()

        then:
        EntitySystemAccess.getEntityEntry(cust2).loadedProperties == [
                'id', 'version', 'name', 'deleteTs', 'deletedBy'
        ] as Set
    }

    def "test graph"() {
        def order

        when:
        order = dataManager.load(Order).id(this.order.id).one()

        then:
        EntitySystemAccess.getEntityEntry(order).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
                'user' // user is not included in _base fetch plan but marked as loaded because the reference is null
        ] as Set

        when:
        order = dataManager.load(Order)
                .id(this.order.id)
                .fetchPlan(fpb ->
                        fpb.addFetchPlan(FetchPlan.BASE).add('customer'))
                .one()

        then:
        EntitySystemAccess.getEntityEntry(order).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
                'user', 'customer'
        ] as Set

        EntitySystemAccess.getEntityEntry(order.customer).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy'
        ] as Set

        when:
        order = dataManager.load(Order)
                .id(this.order.id)
                .fetchPlan(fpb -> fpb.addFetchPlan(FetchPlan.BASE).add('customer', FetchPlan.BASE))
                .one()

        then:
        EntitySystemAccess.getEntityEntry(order.customer).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'name', 'status'
        ] as Set

        when:
        order = dataManager.load(Order)
                .id(this.order.id)
                .fetchPlan(fpb -> fpb.addFetchPlan(FetchPlan.BASE).add('orderLines', FetchPlan.BASE))
                .one()

        then:
        EntitySystemAccess.getEntityEntry(order.orderLines.find({ it instanceof OrderLineA })).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'quantity'
        ] as Set
        EntitySystemAccess.getEntityEntry(order.orderLines.find({ it instanceof OrderLineB })).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'quantity'
        ] as Set
    }

    def "test lazy loading"() {
        when:
        def order = dataManager.load(Order).id(this.order.id).one()

        then:
        EntitySystemAccess.getEntityEntry(order).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
                'user' // user is not included in _base fetch plan but marked as loaded because the reference is null
        ] as Set

        when:
        def customer = order.customer

        then:
        customer != null

        EntitySystemAccess.getEntityEntry(order).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
                'user', 'customer'
        ] as Set

        EntitySystemAccess.getEntityEntry(customer).loadedProperties == [
                'deleteTs', 'updatedBy', 'createdBy', 'name', 'createTs', 'id', 'version', 'updateTs', 'deletedBy', 'status'
        ] as Set

        when:
        def orderLines = order.orderLines
        orderLines.size() == 2

        then:
        EntitySystemAccess.getEntityEntry(order).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'number', 'date', 'amount',
                'user', 'customer', 'orderLines'
        ] as Set

        EntitySystemAccess.getEntityEntry(orderLines.find({ it instanceof OrderLineA })).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'quantity'
        ] as Set
        EntitySystemAccess.getEntityEntry(orderLines.find({ it instanceof OrderLineB })).loadedProperties == [
                'id', 'version', 'deleteTs', 'deletedBy', 'updateTs', 'updatedBy', 'createTs', 'createdBy', 'quantity'
        ] as Set
    }
}
