/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.core.data_manager

import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.model.sales.Customer
import com.haulmont.cuba.core.model.sales.Order
import com.haulmont.cuba.core.entity.contracts.Id
import com.haulmont.cuba.core.testsupport.TestSupport
import io.jmix.core.*
import io.jmix.core.entity.KeyValueEntity
import spec.haulmont.cuba.core.CoreTestSpecification

import org.springframework.beans.factory.annotation.Autowired

class DataManagerCommitTest extends CoreTestSpecification {
    @Autowired
    private DataManager dataManager
    @Autowired
    private EntityStates entityStates
    @Autowired
    private TestSupport testSupport;

    private Customer customer

    void setup() {
        customer = new Customer(name: 'Smith')
    }


    def "committed, returned entities allow you to get entity by its ID"() {

        given:
        Order order = new Order(number: '111', customer: customer)

        and:
        EntitySet committedEntities = dataManager.commit(commitContextFor(customer, order))

        when:
        def committedCustomer = committedEntities.get(Customer, customer.id)

        then:
        committedCustomer == customer

        cleanup:
        testSupport.deleteRecord(order, customer)
    }

    def "committed, returned entities allow you to get commited entity by reference"() {

        given:
        Order order = new Order(number: '111', customer: customer)

        and:
        EntitySet committedEntities = dataManager.commit(commitContextFor(customer, order))

        when:
        def committedCustomer = committedEntities.get(customer)

        then:
        committedCustomer == customer

        cleanup:
        testSupport.deleteRecord(order, customer)
    }

    def "an updated object with a reference through getReference will store the correct reference"() {

        given:
        Order order = new Order(number: '111')

        and:
        Order committedOrder = dataManager.commit(customer, order).get(order)

        and:
        committedOrder.customer = dataManager.getReference(Customer, customer.id)

        when:
        Order recommittedOrder = dataManager.commit(committedOrder)

        then:
        recommittedOrder.customer == customer
        recommittedOrder.customer.version > 0
        recommittedOrder.customer.name == customer.name

        cleanup:
        testSupport.deleteRecord(order, customer)
    }

    def "getReference using Id"() {

        given:
        Order order = new Order(number: '111')

        and:
        Order committedOrder = dataManager.commit(customer, order).get(order)

        and:
        committedOrder.customer = dataManager.getReference(Id.of(customer))

        when:
        Order recommittedOrder = dataManager.commit(committedOrder)

        then:
        recommittedOrder.customer == customer
        recommittedOrder.customer.version > 0
        recommittedOrder.customer.name == customer.name

        cleanup:
        testSupport.deleteRecord(order, customer)
    }

    def "an object can be removed by its reference (getReference)"() {

        given: 'there is a persisted customer'
        dataManager.commit(customer)

        when: 'customer is removed (soft deleted) by its reference'
        dataManager.remove(dataManager.getReference(Customer, customer.id))

        and: 'the customer is reloaded'
        Customer reloadedCustomer = dataManager.load(Customer).id(customer.id).softDeletion(false).one()

        then: 'the reloaded customer is marked as soft deleted'
        reloadedCustomer.isDeleted()

        and: 'the attributes still match with the original customer'
        reloadedCustomer.name == customer.name

        cleanup:
        testSupport.deleteRecord(customer)
    }

    def "KeyValueEntity can be committed to NullStore"() {

        given:
        KeyValueEntity kvEntity = new KeyValueEntity()
        kvEntity.setValue('foo', 'val1')
        kvEntity.setValue('bar', 'val2')

        when: 'a KV entity is commited the NullStore is used'
        KeyValueEntity committedKvEntity = dataManager.commit(kvEntity)

        then:
        committedKvEntity == kvEntity
        committedKvEntity.getValue('foo') == 'val1'
        committedKvEntity.getValue('bar') == 'val2'
    }


    protected CommitContext commitContextFor(Customer customer, Order order) {
        new CommitContext()
                .addInstanceToCommit(customer)
                .addInstanceToCommit(order)
    }

}
