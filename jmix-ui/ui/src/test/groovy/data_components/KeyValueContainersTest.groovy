/*
 * Copyright 2020 Haulmont.
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

package data_components

import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.core.entity.KeyValueEntity
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiComponents
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.TextField
import io.jmix.ui.component.data.value.ContainerValueSource
import io.jmix.ui.model.*
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Address
import test_support.entity.sales.Customer
import test_support.entity.sales.Order

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class KeyValueContainersTest extends ScreenSpecification {

    @Autowired
    JdbcTemplate jdbc

    @Autowired
    DataManager dataManager
    @Autowired
    DataComponents factory
    @Autowired
    UiComponents uiComponents

    @Override
    void setup() {
        Customer customer1 = dataManager.save(new Customer(name: 'customer1', address: new Address()))
        dataManager.save(new Order(number: '111', customer: customer1, amount: 100))
    }

    void cleanup() {
        jdbc.update('delete from TEST_ORDER')
        jdbc.update('delete from TEST_CUSTOMER')
    }

    def "load collection"() {
        KeyValueCollectionContainer container = factory.createKeyValueCollectionContainer()
        container.addProperty('custName').addProperty('amount')

        KeyValueCollectionLoader loader = factory.createKeyValueCollectionLoader()
        loader.setContainer(container)
        loader.setQuery('select o.customer.name, sum(o.amount) from test_Order o group by o.customer.name')

        when:

        loader.load()

        then:

        container.items[0].getValue('custName') == 'customer1'
        container.items[0].getValue('amount') == 100

        container.items[0].getMetaClass().getProperty('custName') != null
        container.items[0].getMetaClass().getProperty('amount') != null
    }

    def "load instance"() {
        KeyValueContainer container = factory.createKeyValueContainer()
        container.addProperty('custName').addProperty('amount')

        KeyValueInstanceLoader loader = factory.createKeyValueInstanceLoader()
        loader.setContainer(container)
        loader.setQuery('select o.customer.name, sum(o.amount) from test_Order o group by o.customer.name')

        when:

        loader.load()

        then:

        container.item.getValue('custName') == 'customer1'
        container.item.getValue('amount') == 100

        container.item.getMetaClass().getProperty('custName') != null
        container.item.getMetaClass().getProperty('amount') != null
    }

    def "binding"() {
        KeyValueContainer container = factory.createKeyValueContainer()
        container.addProperty('custName').addProperty('amount')

        TextField field1 = uiComponents.create(TextField)
        field1.setValueSource(new ContainerValueSource(container, 'custName'))

        TextField field2 = uiComponents.create(TextField)
        field2.setValueSource(new ContainerValueSource(container, 'custName'))

        KeyValueEntity entity = new KeyValueEntity()
        entity.setValue('custName', 'customer1')
        entity.setValue('amount', 100)

        when:

        container.setItem(entity)

        then:

        field1.getValue() == 'customer1'
        field2.getValue() == 'customer1'

        when:

        field1.setValue('changed')

        then:

        field2.getValue() == 'changed'
        entity.getValue('custName') == 'changed'
    }

    def "entity has correct MetaClass when set to KeyValueContainer"() {
        KeyValueContainer container = factory.createKeyValueContainer()
        container.addProperty('custName').addProperty('amount').setIdName('custName')

        when:

        KeyValueEntity entity = new KeyValueEntity()
        entity.setValue('custName', 'customer1')
        entity.setValue('amount', 100)

        container.setItem(entity)

        then:

        entity.getMetaClass() != null
        entity.getMetaClass().getProperty('custName') != null
        entity.getMetaClass().getProperty('amount') != null
        entity.getIdName() == 'custName'
    }

    def "entity has correct MetaClass when added to KeyValueCollectionContainer"() {
        KeyValueCollectionContainer container = factory.createKeyValueCollectionContainer()
        container.addProperty('custName').addProperty('amount').setIdName('custName')

        when:

        KeyValueEntity entity = new KeyValueEntity()
        entity.setValue('custName', 'customer1')
        entity.setValue('amount', 100)

        container.setItems([entity])

        then:

        entity.getMetaClass() != null
        entity.getMetaClass().getProperty('custName') != null
        entity.getMetaClass().getProperty('amount') != null
        entity.getIdName() == 'custName'

        when:

        KeyValueEntity entity2 = new KeyValueEntity()
        entity2.setValue('custName', 'customer2')
        entity2.setValue('amount', 200)

        container.getMutableItems().add(entity2)

        then:

        entity2.getMetaClass() != null
        entity2.getMetaClass().getProperty('custName') != null
        entity2.getMetaClass().getProperty('amount') != null
        entity2.getIdName() == 'custName'
    }

    def "new entity has correct MetaClass when created by KeyValueContainer"() {
        KeyValueContainer container = factory.createKeyValueContainer()
        container.addProperty('custName').addProperty('amount').setIdName('custName')

        when:
        def entity = container.createEntity()

        then:
        entity.getInstanceMetaClass().findProperty('custName') != null
        entity.getInstanceMetaClass().findProperty('amount') != null
        entity.getIdName() == 'custName'
    }

    def "new entity has correct MetaClass when created by KeyValueCollectionContainer"() {
        KeyValueCollectionContainer container = factory.createKeyValueCollectionContainer()
        container.addProperty('custName').addProperty('amount').setIdName('custName')

        when:
        def entity = container.createEntity()

        then:
        entity.getInstanceMetaClass().findProperty('custName') != null
        entity.getInstanceMetaClass().findProperty('amount') != null
        entity.getIdName() == 'custName'
    }
}
