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

package data_manager

import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.core.event.EntityChangedEvent
import io.jmix.eclipselink.impl.JpaDataStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import test_support.DataSpec
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.listeners.TestCustomerListener

class DataManagerTxTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    PlatformTransactionManager txManager

    @Autowired
    TestCustomerListener listener

    def "joins current transaction on save"() {
        listener.changedEventConsumer = { event ->
            assert TransactionSynchronizationManager.getCurrentTransactionName() == 'test-save'
        }

        when:
        def txDef = new DefaultTransactionDefinition()
        txDef.setName('test-save')
        def txStatus = txManager.getTransaction(txDef)

        def customer = dataManager.create(Customer)
        customer.name = 'cust1'

        dataManager.save(customer)

        txManager.commit(txStatus)

        then:
        noExceptionThrown()

        cleanup:
        listener.changedEventConsumer = null
    }

    def "can start new transaction on save"() {
        listener.changedEventConsumer = { event ->
            assert TransactionSynchronizationManager.getCurrentTransactionName().startsWith(JpaDataStore.SAVE_TX_PREFIX)
        }

        when:
        def txDef = new DefaultTransactionDefinition()
        txDef.setName('test-save')
        def txStatus = txManager.getTransaction(txDef)

        def customer = dataManager.create(Customer)
        customer.name = 'cust1'

        dataManager.save(new SaveContext().saving(customer).setJoinTransaction(false))

        txManager.commit(txStatus)

        then:
        noExceptionThrown()

        cleanup:
        listener.changedEventConsumer = null
    }

    def "joins current transaction on load"() {
        def customer = dataManager.create(Customer)
        customer.name = 'cust1'
        dataManager.save(customer)

        listener.beforeDetachConsumer = { entity ->
            assert TransactionSynchronizationManager.getCurrentTransactionName() == 'test-load'
        }

        when:
        def txDef = new DefaultTransactionDefinition()
        txDef.setName('test-load')
        def txStatus = txManager.getTransaction(txDef)

        dataManager.load(Customer).all().one()

        txManager.commit(txStatus)

        then:
        noExceptionThrown()

        cleanup:
        listener.beforeDetachConsumer = null
    }

    def "can start new transaction on load"() {
        def customer = dataManager.create(Customer)
        customer.name = 'cust1'
        dataManager.save(customer)

        listener.beforeDetachConsumer = { entity ->
            assert TransactionSynchronizationManager.getCurrentTransactionName().startsWith(JpaDataStore.LOAD_TX_PREFIX)
        }

        when:
        def txDef = new DefaultTransactionDefinition()
        txDef.setName('test-load')
        def txStatus = txManager.getTransaction(txDef)

        dataManager.load(Customer).all().joinTransaction(false).one()

        txManager.commit(txStatus)

        then:
        noExceptionThrown()

        cleanup:
        listener.beforeDetachConsumer = null
    }

    def "needs new transaction if called on AfterCommit stage"() {
        def error = null

        def transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
        TransactionTemplate transactionTemplate = new TransactionTemplate(txManager, transactionDefinition)

        listener.afterCommitEventConsumer = { EntityChangedEvent<Customer> event ->
            transactionTemplate.executeWithoutResult {
                try {
                    def cust1 = dataManager.load(Customer).id(event.entityId).one()
                    def order = dataManager.create(Order)
                    order.customer = cust1
                    dataManager.save(order)
                } catch (Exception e) {
                    error = e.message // assert is not recognized by Spock here
                }
            }
        }

        when:
        def txStatus = txManager.getTransaction(new DefaultTransactionDefinition())

        def customer = dataManager.create(Customer)
        customer.name = 'cust1'

        dataManager.save(customer)

        txManager.commit(txStatus)

        then:
        noExceptionThrown()
        error == null

        cleanup:
        listener.afterCommitEventConsumer = null
    }

}
