/*
 * Copyright 2019 Haulmont.
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
package spec.haulmont.cuba.core.data_events

import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.TransactionalDataManager
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.model.sales.Order
import com.haulmont.cuba.core.model.sales.OrderLine
import com.haulmont.cuba.core.model.sales.Product
import com.haulmont.cuba.core.model.sales.TestEntityChangedEventListener
import com.haulmont.cuba.core.testsupport.TestSupport
import io.jmix.core.*
import io.jmix.data.event.EntityChangedEvent
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

class EntityChangedEventTest extends CoreTestSpecification {
    private TestEntityChangedEventListener listener
    @Autowired
    private Events events
    @Autowired
    private DataManager dataManager
    @Autowired
    private TransactionalDataManager txDataManager
    @Autowired
    private Metadata metadata
    @Autowired
    private EntityStates entityStates
    @Autowired
    private TestSupport testSupport

    void setup() {
        listener = AppBeans.get(TestEntityChangedEventListener)
        listener.entityChangedEvents.clear()

        listener.clear()
    }

    void cleanup() {
        listener.clear()
    }

    private TestEntityChangedEventListener.Info beforeCommit() {
        return listener.entityChangedEvents[0]
    }

    private TestEntityChangedEventListener.Info afterCommit() {
        return listener.entityChangedEvents[1]
    }

    def "create/update/delete entity"() {

        Order order = metadata.create(Order)
        order.setNumber('111')
        order.setAmount(10)

        when:

        Order order1 = dataManager.commit(order)

        then:

        listener.entityChangedEvents.size() == 2

        !beforeCommit().committedToDb
        afterCommit().committedToDb

        beforeCommit().event.getEntityId().value == order.id
        afterCommit().event.getEntityId().value == order.id

        beforeCommit().event.getType() == EntityChangedEvent.Type.CREATED
        afterCommit().event.getType() == EntityChangedEvent.Type.CREATED

        beforeCommit().event.changes.isChanged('number')
        beforeCommit().event.changes.isChanged('amount')
        !beforeCommit().event.changes.isChanged('date')
        !beforeCommit().event.changes.isChanged('customer')

        beforeCommit().event.changes.getOldValue('amount') == null

        when:

        listener.clear()

        order1.setAmount(20)
        Order order2 = dataManager.commit(order1)

        then:

        listener.entityChangedEvents.size() == 2

        beforeCommit().event.getEntityId().value == order.id
        afterCommit().event.getEntityId().value == order.id

        beforeCommit().event.getType() == EntityChangedEvent.Type.UPDATED
        afterCommit().event.getType() == EntityChangedEvent.Type.UPDATED

        beforeCommit().event.getChanges().attributes.contains('amount')
        beforeCommit().event.getChanges().getOldValue('amount') == 10
        afterCommit().event.getChanges().attributes.contains('amount')
        afterCommit().event.getChanges().getOldValue('amount') == 10

        !beforeCommit().event.changes.isChanged('number')
        beforeCommit().event.changes.isChanged('amount')

        beforeCommit().event.changes.getOldValue('amount') == 10

        when:

        listener.clear()

        Order order3 = dataManager.remove(order2)

        then:

        listener.entityChangedEvents.size() == 2

        beforeCommit().event.getEntityId().value == order.id
        afterCommit().event.getEntityId().value == order.id

        beforeCommit().event.getType() == EntityChangedEvent.Type.DELETED
        afterCommit().event.getType() == EntityChangedEvent.Type.DELETED

        beforeCommit().event.changes.isChanged('number')
        beforeCommit().event.changes.isChanged('amount')
        beforeCommit().event.changes.isChanged('date')
        beforeCommit().event.changes.isChanged('customer')

        beforeCommit().event.changes.getOldValue('number') == '111'
        beforeCommit().event.changes.getOldValue('amount') == 20
        beforeCommit().event.changes.getOldValue('date') == null
        beforeCommit().event.changes.getOldValue('customer') == null

        cleanup:

        testSupport.deleteRecord(order)
    }

    def "old value of collection attribute"() {

        Order order1 = new Order(number: '111', amount: 100)
        Product product1 = new Product(name: 'abc', quantity: 1000)
        Product product2 = new Product(name: 'def', quantity: 1000)
        OrderLine orderLine11 = new OrderLine(order: order1, product: product1, quantity: 10)
        OrderLine orderLine12 = new OrderLine(order: order1, product: product2, quantity: 20)

        EntitySet committed = dataManager.commit(order1, orderLine11, orderLine12, product1, product2)
        Order order2 = committed.get(order1)
        OrderLine orderLine2 = committed.get(orderLine11)

        listener.clear()

        when:

        order2.orderLines.remove(orderLine2)
        Transaction tx = txDataManager.transactions().create()
        try {
            txDataManager.save(order2)
            txDataManager.remove(orderLine2)
            tx.commit()
        } finally {
            tx.end()
        }

        then:

        listener.entityChangedEvents[0].event.getEntityId().value == order2.id

        Collection oldLines = listener.entityChangedEvents[0].event.changes.getOldValue('orderLines')
        oldLines.containsAll([Id.of(orderLine11), Id.of(orderLine12)])

        cleanup:

        testSupport.deleteRecord(orderLine11, orderLine12, product1, product2, order1)
    }
}
