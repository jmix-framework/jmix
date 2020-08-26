/*
 * Copyright (c) 2008-2020 Haulmont.
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

package entity_collections

import io.jmix.core.CoreConfiguration
import io.jmix.core.EntityReferencesNormalizer
import io.jmix.core.impl.StandardSerialization
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.sales.Customer
import test_support.app.entity.sales.Order
import test_support.app.entity.sales.OrderLineA
import test_support.app.entity.sales.Product
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestAddon1Configuration, TestAppConfiguration])
class EntityReferencesNormalizerTest extends Specification {

    @Autowired
    EntityReferencesNormalizer normalizer
    @Autowired
    StandardSerialization standardSerialization

    def "update immediate to-one references"() {
        Customer customer1 = new Customer(name: 'cust')
        Customer customer2 = reserialize(customer1)

        def order = new Order(number: '1', customer: customer2)

        def collection = [order, customer1]

        when:
        normalizer.updateReferences(collection)

        then:
        order.customer.is(customer1)
    }

    def "update deep to-one references"() {
        Product product1 = new Product(name: 'product')
        Product product2 = reserialize(product1)

        def order = new Order(number: '1')
        def orderLineA = new OrderLineA(order: order, product: product2)
        order.orderLines = [orderLineA]

        def collection = [order, orderLineA, product1]

        when:
        normalizer.updateReferences(collection)

        then:
        order.orderLines[0].product.is(product1)
    }

    def "update to-many collection"() {
        def order = new Order(number: '1')
        def orderLine1 = new OrderLineA(order: order)
        def orderLine2 = reserialize(orderLine1)
        order.orderLines = [orderLine2]

        def collection = [order, orderLine1]

        when:
        normalizer.updateReferences(collection)

        then:
        order.orderLines.size() == 1
        order.orderLines[0].is(orderLine1)
    }

    @SuppressWarnings("unchecked")
    def <T> T reserialize(Serializable object) {
        if (object == null) {
            return null
        }

        return (T) standardSerialization.deserialize(standardSerialization.serialize(object))
    }
}
