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
import io.jmix.data.impl.OrmDataStore
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronizationManager
import test_support.DataSpec
import test_support.TestCustomerListener
import test_support.entity.sales.Customer

import org.springframework.beans.factory.annotation.Autowired

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

        dataManager.save(new Customer(name: "cust1"))

        txManager.commit(txStatus)

        then:
        noExceptionThrown()

        cleanup:
        listener.changedEventConsumer = null
    }

    def "can start new transaction on save"() {
        listener.changedEventConsumer = { event ->
            assert TransactionSynchronizationManager.getCurrentTransactionName().startsWith(OrmDataStore.SAVE_TX_PREFIX)
        }

        when:
        def txDef = new DefaultTransactionDefinition()
        txDef.setName('test-save')
        def txStatus = txManager.getTransaction(txDef)

        dataManager.save(new SaveContext().saving(new Customer(name: "cust1")).setJoinTransaction(false))

        txManager.commit(txStatus)

        then:
        noExceptionThrown()

        cleanup:
        listener.changedEventConsumer = null
    }

    def "joins current transaction on load"() {
        dataManager.save(new Customer(name: "cust1"))
        listener.beforeDetachConsumer = { entity ->
            assert TransactionSynchronizationManager.getCurrentTransactionName() == 'test-load'
        }

        when:
        def txDef = new DefaultTransactionDefinition()
        txDef.setName('test-load')
        def txStatus = txManager.getTransaction(txDef)

        dataManager.load(Customer).one()

        txManager.commit(txStatus)

        then:
        noExceptionThrown()

        cleanup:
        listener.beforeDetachConsumer = null
    }

    def "can start new transaction on load"() {
        dataManager.save(new Customer(name: "cust1"))
        listener.beforeDetachConsumer = { entity ->
            assert TransactionSynchronizationManager.getCurrentTransactionName().startsWith(OrmDataStore.LOAD_TX_PREFIX)
        }

        when:
        def txDef = new DefaultTransactionDefinition()
        txDef.setName('test-load')
        def txStatus = txManager.getTransaction(txDef)

        dataManager.load(Customer).joinTransaction(false).one()

        txManager.commit(txStatus)

        then:
        noExceptionThrown()

        cleanup:
        listener.beforeDetachConsumer = null
    }
}
