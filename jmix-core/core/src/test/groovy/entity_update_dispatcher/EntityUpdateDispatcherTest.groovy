/*
 * Copyright 2026 Haulmont.
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

package entity_update_dispatcher

import io.jmix.core.*
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.ResolvableType
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.sales.Customer
import test_support.app.entity.sales.Order
import test_support.app.entity.sales.OrderLine

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class EntityUpdateDispatcherTest extends Specification {

    @Autowired
    Metadata metadata

    ApplicationContext applicationContext = Mock(ApplicationContext)
    DataManager dataManager = Mock(DataManager)
    EntityUpdateDispatcher dispatcher

    def setup() {
        dispatcher = new EntityUpdateDispatcher(applicationContext, metadata)
    }

    def "test save with delegate"() {
        given:
        def customer = metadata.create(Customer)
        def saveContext = new SaveContext().saving(customer)
        
        def saveDelegate = Mock(SaveDelegate)
        def beanProvider = Mock(ObjectProvider)
        
        applicationContext.getBeanProvider(_ as ResolvableType) >> beanProvider
        beanProvider.getIfAvailable() >> saveDelegate

        when:
        def result = dispatcher.save(dataManager, saveContext)

        then:
        1 * saveDelegate.save(customer, saveContext) >> customer
        0 * dataManager.save(_)
        result.contains(customer)
    }

    def "test save without delegate"() {
        given:
        def order = metadata.create(Order)
        def saveContext = new SaveContext().saving(order)
        
        def beanProvider = Mock(ObjectProvider)
        applicationContext.getBeanProvider(_ as ResolvableType) >> beanProvider
        beanProvider.getIfAvailable() >> null

        when:
        def result = dispatcher.save(dataManager, saveContext)

        then:
        1 * dataManager.save(_ as SaveContext) >> { SaveContext ctx ->
            assert ctx.entitiesToSave.contains(order)
            return new EntitySet([order])
        }
        result.contains(order)
    }

    def "test remove with delegate"() {
        given:
        def customer = metadata.create(Customer)
        
        def removeDelegate = Mock(RemoveDelegate)
        def beanProvider = Mock(ObjectProvider)
        
        applicationContext.getBeanProvider(_ as ResolvableType) >> beanProvider
        beanProvider.getIfAvailable() >> removeDelegate

        when:
        dispatcher.remove(dataManager, [customer])

        then:
        1 * removeDelegate.remove(customer)
        0 * dataManager.save(_)
    }

    def "test remove without delegate"() {
        given:
        def order = metadata.create(Order)
        
        def beanProvider = Mock(ObjectProvider)
        applicationContext.getBeanProvider(_ as ResolvableType) >> beanProvider
        beanProvider.getIfAvailable() >> null

        when:
        dispatcher.remove(dataManager, [order])

        then:
        1 * dataManager.save(_ as SaveContext) >> { SaveContext ctx ->
            assert ctx.entitiesToRemove.contains(order)
            return new EntitySet()
        }
    }

    def "test composition exclusion"() {
        given:
        def order = metadata.create(Order)
        def line = metadata.create(OrderLine)
        order.orderLines = [line]
        
        def saveContext = new SaveContext().saving(order, line)
        
        def orderSaveDelegate = Mock(SaveDelegate)
        def orderBeanProvider = Mock(ObjectProvider)
        
        def lineBeanProvider = Mock(ObjectProvider)

        applicationContext.getBeanProvider(_ as ResolvableType) >> { ResolvableType type ->
            if (type.getGeneric(0).resolve() == Order) {
                return orderBeanProvider
            } else {
                return lineBeanProvider
            }
        }
        
        orderBeanProvider.getIfAvailable() >> orderSaveDelegate
        lineBeanProvider.getIfAvailable() >> null

        when:
        def result = dispatcher.save(dataManager, saveContext)

        then:
        1 * orderSaveDelegate.save(order, saveContext) >> order
        
        // DataManager should NOT be called for 'line' because it is part of 'order' composition
        // and 'order' was saved via delegate.
        0 * dataManager.save(_)
        
        result.contains(order)
    }
}
