/*
 * Copyright 2021 Haulmont.
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

package events

import io.jmix.core.DataManager
import io.jmix.core.Id
import io.jmix.core.event.EntityChangedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import test_support.DataSpec
import test_support.entity.events.on_delete_stack_overflow.History
import test_support.entity.events.on_delete_stack_overflow.Payment
import test_support.entity.events.on_delete_stack_overflow.Product
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.listeners.TestOrdersListener

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import static org.springframework.transaction.event.TransactionPhase.BEFORE_COMMIT

class EntityChangedEventOnDeleteTest extends DataSpec {

    @Autowired
    private DataManager dataManager
    @Autowired
    private TestOrdersListener ordersListener
    @Autowired
    JdbcTemplate jdbc

    private Order order;
    private OrderLine orderLine;

    boolean hasBeforeCommit = false
    boolean hasAfterCommit = false
    boolean hasOnDeleteBeforeCommit = false
    boolean hasOnDeleteAfterCommit = false

    @Override
    void setup() {
        order = dataManager.create(Order)
        order.number = "123"
        order = dataManager.save(order)

        orderLine = dataManager.create(OrderLine)
        orderLine.quantity = 1
        orderLine.order = order
        orderLine = dataManager.save(orderLine)

        hasBeforeCommit = false
        hasAfterCommit = false
        hasOnDeleteBeforeCommit = false
        hasOnDeleteAfterCommit = false

        ordersListener.setConsumer((e, p) -> {
            if (EntityChangedEvent.Type.DELETED != e.type)
                return

            if (Order.isAssignableFrom(e.getEntityId().entityClass) && p == BEFORE_COMMIT) {
                hasBeforeCommit = true
            }
            if (Order.isAssignableFrom(e.getEntityId().entityClass) && p == AFTER_COMMIT) {
                hasAfterCommit = true
            }
            if (OrderLine.isAssignableFrom(e.getEntityId().entityClass) && p == BEFORE_COMMIT) {
                hasOnDeleteBeforeCommit = true
            }
            if (OrderLine.isAssignableFrom(e.getEntityId().entityClass) && p == AFTER_COMMIT) {
                hasOnDeleteAfterCommit = true
            }
        })

    }

    @Override
    void cleanup() {
        ordersListener.setConsumer(null)

        jdbc.update("delete from SALES_ORDER_LINE")
        jdbc.update("delete from SALES_ORDER")

        jdbc.update("delete from ODSO_HISTORY")
        jdbc.update("delete from ODSO_PAYMENT")
        jdbc.update("delete from ODSO_PRODUCT")

    }

    def "Event published for cascade deleted entity"() {
        when:
        dataManager.remove(order)

        then:
        hasBeforeCommit
        hasAfterCommit
        hasOnDeleteBeforeCommit
        hasOnDeleteAfterCommit
    }

    def "no StackOverflowError thrown during deleting entity with ondelete policy"() {
        setup: "see also 'test_support.listeners.OnDeleteStackOverflowListener'"
        def product = dataManager.create(Product)
        product.name = "P1"

        def payment = dataManager.create(Payment)
        payment.name = "payment_1"
        payment.product = product

        product = dataManager.save(product)
        payment = dataManager.save(payment)

        when: "'Payment' entity deleted by @OnDelete policy and 'History' entity created during EntityChangedEvent"
        dataManager.remove(product)
        Optional productAfterRemove = dataManager.load(Id.of(product)).optional()
        Optional paymentAfterRemove = dataManager.load(Id.of(payment)).optional()

        List<History> productEvents = dataManager.load(History)
                .query("select e from odso_History e where e.objId=:id order by e.createdDate")
                .parameter("id", product.id)
                .list()

        List<History> paymentEvents = dataManager.load(History)
                .query("select e from odso_History e where e.objId=:id order by e.createdDate")
                .parameter("id", payment.id)
                .list()

        then: "'Payment' entity deletion event doesn't thrown multiple times"
        noExceptionThrown()

        productAfterRemove.isEmpty()
        paymentAfterRemove.isEmpty()

        productEvents.size() == 2
        productEvents[0].eventType == "CREATED"
        productEvents[1].eventType == "DELETED"

        paymentEvents.size() == 2
        paymentEvents[0].eventType == "CREATED"
        paymentEvents[1].eventType == "DELETED"
    }
}
