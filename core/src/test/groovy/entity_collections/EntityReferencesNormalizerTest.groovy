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

import io.jmix.core.EntityReferencesNormalizer
import io.jmix.core.JmixCoreConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification
import test_support.AppContextTestExecutionListener
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.sales.Customer
import test_support.app.entity.sales.Order
import test_support.app.entity.sales.OrderLineA
import test_support.app.entity.sales.Product
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [JmixCoreConfiguration, TestBaseConfiguration, TestAddon1Configuration, TestAppConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class EntityReferencesNormalizerTest extends Specification {

    @Autowired
    EntityReferencesNormalizer normalizer

    def "update immediate to-one references"() {
        def customer1 = new Customer(name: 'cust')
        def customer2 = new Customer(id: customer1.id, name: 'cust')

        def order = new Order(number: '1', customer: customer2)

        def collection = [order, customer1]

        when:
        normalizer.updateReferences(collection)

        then:
        order.customer.is(customer1)
    }

    def "update deep to-one references"() {
        def product1 = new Product(name: 'product')
        def product2 = new Product(id: product1.id, name: 'product')

        def order = new Order(number: '1')
        def orderLineA = new OrderLineA(order: order, product: product2)
        order.orderLines = [orderLineA]

        def collection = [order, orderLineA, product1]

        when:
        normalizer.updateReferences(collection)

        then:
        order.orderLines[0].product.is(product1)
    }
}
