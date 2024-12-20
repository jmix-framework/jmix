/*
 * Copyright 2024 Haulmont.
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

package copier

import io.jmix.core.Copier
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLineA
import test_support.entity.sales.OrderLineB
import test_support.entity.sales.Product

class SerializingCopierTest extends DataSpec {

    @Autowired
    Copier copier

    def "test equality and identity"() {
        def customer = new Customer(name: 'cust-1')

        def order = new Order(customer: customer)

        def product1 = new Product(name: 'prod-1')
        def product2 = new Product(name: 'prod-2')

        def orderLine1 = new OrderLineA(order: order, product: product1, quantity: 10)
        def orderLine2 = new OrderLineB(order: order, product: product2, quantity: 20)

        order.orderLines = [orderLine1, orderLine2]

        when:
        def orderCopy = copier.copy(order)

        then:
        orderCopy == order
        !orderCopy.is(order)

        orderCopy.customer == customer
        !orderCopy.is(customer)

        orderCopy.orderLines[0] == orderLine1
        !orderCopy.orderLines[0].is(orderLine1)

        orderCopy.orderLines[1] == orderLine2
        !orderCopy.orderLines[1].is(orderLine2)

        orderCopy.orderLines[0].product == product1
        !orderCopy.orderLines[0].product.is(product1)

        orderCopy.orderLines[1].product == product2
        !orderCopy.orderLines[1].product.is(product2)
    }
}
