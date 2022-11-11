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

package keyvalueentity

import io.jmix.core.CoreConfiguration
import io.jmix.core.Stores
import io.jmix.core.entity.KeyValueEntity
import io.jmix.core.impl.StandardSerialization
import io.jmix.core.impl.keyvalue.KeyValueMetaClass
import io.jmix.core.impl.keyvalue.KeyValueMetaClassFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.sales.Customer
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [TestAppConfiguration, TestAddon1Configuration, TestBaseConfiguration, CoreConfiguration])
class KeyValueEntitySerializationTest extends Specification {

    @Autowired
    StandardSerialization serialization

    @Autowired
    KeyValueMetaClassFactory kvMetaClassFactory

    @Autowired
    Stores stores

    def "plain entity"() {
        def entity = new KeyValueEntity()
        entity.setValue("foo", "abc")
        entity.setValue("bar", 10)

        when:
        def bytes = serialization.serialize(entity)
        KeyValueEntity entity1 = serialization.deserialize(bytes) as KeyValueEntity

        then:
        entity1 == entity
        entity1.getValue('foo') == 'abc'
        entity1.getValue('bar') == 10

    }

    def "entity with MetaClass"() {

        def customer = new Customer()

        def entity = new KeyValueEntity()
        entity.setValue("foo", "abc")
        entity.setValue("bar", 10)
        entity.setValue('customer', customer)

        def metaClass = kvMetaClassFactory.builder()
                .addProperty('foo', String)
                .addProperty('bar', Integer)
                .addProperty('customer', Customer)
                .build()
        entity.setInstanceMetaClass(metaClass)

        when:
        def bytes = serialization.serialize(entity)
        KeyValueEntity entity1 = serialization.deserialize(bytes) as KeyValueEntity

        then:
        entity1 == entity
        entity1.getValue('foo') == 'abc'
        entity1.getValue('bar') == 10
        entity1.getValue('customer') == customer
        entity1.getInstanceMetaClass() != metaClass // different KeyValueMetaClass instances are never equal

        entity1.getInstanceMetaClass().getStore() == stores.get(Stores.NOOP)

        entity1.getInstanceMetaClass().getProperty('customer').getRange().isClass()
    }

    def "entity with reference to another KVE"() {

        def customer = new KeyValueEntity()
        customer.setValue('name', 'abc')

        def customerMetaClass = kvMetaClassFactory.builder()
                .addProperty('name', String)
                .build()
        customer.setInstanceMetaClass(customerMetaClass)

        def order = new KeyValueEntity()
        order.setValue('number', '111')
        order.setValue('customer', customer)


        def orderMetaClass = kvMetaClassFactory.builder()
                .addProperty('number', String)
                .addProperty('customer', customerMetaClass)
                .build()
        order.setInstanceMetaClass(orderMetaClass)

        when:
        def bytes = serialization.serialize(order)
        KeyValueEntity order1 = serialization.deserialize(bytes) as KeyValueEntity

        then:
        order1.getValue('number') == '111'
        order1.getInstanceMetaClass().getProperty('customer').getRange().isClass()
        order1.getInstanceMetaClass().getProperty('customer').getRange().asClass() instanceof KeyValueMetaClass

        order1.getValue('customer') instanceof KeyValueEntity
        KeyValueEntity customer1 = order1.getValue('customer')
        customer1.getValue('name') == 'abc'
        customer1.getInstanceMetaClass().getProperty('name').getRange().isDatatype()

        order1.getInstanceMetaClass() != order.getInstanceMetaClass() // different KeyValueMetaClass instances are never equal
    }

    def "entity with reference to another KVE and cyclic back reference"() {

        def customer = new KeyValueEntity()
        customer.setValue('name', 'abc')

        def customerMetaClass = kvMetaClassFactory.builder()
                .addProperty('name', String)
                .build()
        customer.setInstanceMetaClass(customerMetaClass)

        def order = new KeyValueEntity()
        order.setValue('number', '111')
        order.setValue('customer', customer)

        def orderMetaClass = kvMetaClassFactory.builder()
                .addProperty('number', String)
                .addProperty('customer', customerMetaClass)
                .build()
        order.setInstanceMetaClass(orderMetaClass)

        customer.setValue('order', order)
        kvMetaClassFactory.configurer(customerMetaClass).addProperty('order', orderMetaClass)

        when:
        def bytes = serialization.serialize(order)
        KeyValueEntity order1 = serialization.deserialize(bytes) as KeyValueEntity

        then:
        order1.getValue('number') == '111'
        order1.getInstanceMetaClass().getProperty('customer').getRange().isClass()
        order1.getInstanceMetaClass().getProperty('customer').getRange().asClass() instanceof KeyValueMetaClass

        order1.getValue('customer') instanceof KeyValueEntity
        KeyValueEntity customer1 = order1.getValue('customer')
        customer1.getValue('name') == 'abc'
        customer1.getInstanceMetaClass().getProperty('name').getRange().isDatatype()

        customer1.getValue('order') == order1
        customer1.getInstanceMetaClass().getProperty('order').getRange().isClass()
        customer1.getInstanceMetaClass().getProperty('order').getRange().asClass() instanceof KeyValueMetaClass

        order1.getInstanceMetaClass() != order.getInstanceMetaClass() // different KeyValueMetaClass instances are never equal
    }
}
