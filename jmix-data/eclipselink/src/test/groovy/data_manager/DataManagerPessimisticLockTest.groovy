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

package data_manager

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import io.jmix.core.DataManager
import io.jmix.core.querycondition.PropertyCondition
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.TestAppender
import test_support.entity.sales.Customer

import javax.persistence.LockModeType

class DataManagerPessimisticLockTest extends DataSpec {

    @Autowired
    DataManager dataManager

    private Logger logger
    private TestAppender appender

    private Customer customer1

    @Override
    void setup() {
        customer1 = dataManager.create(Customer)
        customer1.name = 'cust1'
        dataManager.save(customer1)

        appender = new TestAppender()
        appender.start()
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory()
        logger = context.getLogger("eclipselink.logging.sql")
        logger.addAppender(appender)
    }

    @Override
    void cleanup() {
        logger.detachAppender(appender)
    }

    def "select for update is executed when using LockModeType.PESSIMISTIC_WRITE"() {
        def customer

        when:
        appender.clearMessages()
        customer = dataManager.load(Customer)
                .id(customer1.id)
                .lockMode(LockModeType.PESSIMISTIC_WRITE)
                .one()

        then:
        customer == customer1
        appender.messages.findAll { it.matches('(?s).+> SELECT.+SALES_CUSTOMER.+FOR UPDATE.+') }.size() == 1

        when:
        appender.clearMessages()
        def customerList = dataManager.load(Customer)
                .ids(customer1.id)
                .lockMode(LockModeType.PESSIMISTIC_WRITE)
                .list()

        then:
        customerList[0] == customer1
        appender.messages.findAll { it.matches('(?s).+> SELECT.+SALES_CUSTOMER.+FOR UPDATE.+') }.size() == 1

        when:
        appender.clearMessages()
        customer = dataManager.load(Customer)
                .query('select e from sales_Customer e where e.id = :id')
                .parameter('id', customer1.id)
                .lockMode(LockModeType.PESSIMISTIC_WRITE)
                .one()

        then:
        customer == customer1
        appender.messages.findAll { it.matches('(?s).+> SELECT.+SALES_CUSTOMER.+FOR UPDATE.+') }.size() == 1

        when:
        appender.clearMessages()
        customer = dataManager.load(Customer)
                .condition(PropertyCondition.equal('id', customer1.id))
                .lockMode(LockModeType.PESSIMISTIC_WRITE)
                .one()

        then:
        customer == customer1
        appender.messages.findAll { it.matches('(?s).+> SELECT.+SALES_CUSTOMER.+FOR UPDATE.+') }.size() == 1

        when:
        appender.clearMessages()
        def kvEntity = dataManager.loadValues('select e.name from sales_Customer e where e.id = :id')
                .property('name')
                .parameter('id', customer1.id)
                .lockMode(LockModeType.PESSIMISTIC_WRITE)
                .one()

        then:
        kvEntity.getValue('name') == customer1.name
        appender.messages.findAll { it.matches('(?s).+> SELECT.+SALES_CUSTOMER.+FOR UPDATE.+') }.size() == 1

        when:
        appender.clearMessages()
        def name = dataManager.loadValue('select e.name from sales_Customer e where e.id = :id', String)
                .parameter('id', customer1.id)
                .lockMode(LockModeType.PESSIMISTIC_WRITE)
                .one()

        then:
        name == customer1.name
        appender.messages.findAll { it.matches('(?s).+> SELECT.+SALES_CUSTOMER.+FOR UPDATE.+') }.size() == 1
    }

    def "no select for update when LockMode is not set"() {
        def customer

        when:
        appender.clearMessages()
        customer = dataManager.load(Customer)
                .id(customer1.id)
                .one()

        then:
        customer == customer1
        appender.messages.findAll { it.matches('(?s).+FOR UPDATE.+') }.isEmpty()

    }
}