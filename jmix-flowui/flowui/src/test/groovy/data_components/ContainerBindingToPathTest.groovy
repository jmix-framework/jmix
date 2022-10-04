/*
 * Copyright (c) 2020 Haulmont.
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

import io.jmix.core.Metadata
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.InstanceContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ContainerBindingToPathTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents
    @Autowired
    UiComponents uiComponents
    @Autowired
    Metadata metadata

    private Customer customer1
    private Customer customer2
    private Order order1
    private Order order2
    private OrderLine line1
    private OrderLine line2
    private InstanceContainer<Order> orderDc
    private InstanceContainer<OrderLine> lineDc
    private TypedTextField field1
    private TypedTextField field2

    @Override
    void setup() {
        customer1 = metadata.create(Customer)
        customer1.name = 'customer1'

        customer2 = metadata.create(Customer)
        customer2.name = 'customer2'

        order1 = metadata.create(Order)
        order1.number = '111'
        order1.customer = customer1

        order2 = metadata.create(Order)
        order2.number = '222'
        order2.customer = customer2

        line1 = metadata.create(OrderLine)
        line1.order = order1
        line1.quantity = 10

        line2 = metadata.create(OrderLine)
        line2.order = order2
        line2.quantity = 20

        orderDc = dataComponents.createInstanceContainer(Order)
        lineDc = dataComponents.createInstanceContainer(OrderLine)

        field1 = uiComponents.create(TypedTextField)
        field2 = uiComponents.create(TypedTextField)
    }

    def "binding to property path"() {

        field1.setValueSource(new ContainerValueSource(orderDc, 'customer.name'))
        field2.setValueSource(new ContainerValueSource(orderDc, 'customer.name'))

        when:

        orderDc.item = order1

        then:

        field1.typedValue == 'customer1'
        field2.typedValue == 'customer1'

        when:

        field1.typedValue = 'customer11'

        then:

        customer1.name == 'customer11'

        and:

        field2.typedValue == 'customer11'

        when:

        customer1.name = 'customer111'

        then:

        field1.typedValue == 'customer111'
        field2.typedValue == 'customer111'
    }

    def "binding to property path - change root item"() {

        field1.setValueSource(new ContainerValueSource(orderDc, 'customer.name'))
        field2.setValueSource(new ContainerValueSource(orderDc, 'customer.name'))

        when:

        orderDc.item = order1

        then:

        field1.typedValue == 'customer1'
        field2.typedValue == 'customer1'

        when:

        orderDc.item = order2

        then:

        field1.typedValue == 'customer2'
        field2.typedValue == 'customer2'
    }

    def "binding to property path - change leaf item"() {

        field1.setValueSource(new ContainerValueSource(orderDc, 'customer.name'))
        field2.setValueSource(new ContainerValueSource(orderDc, 'customer.name'))

        when:

        orderDc.item = order1

        then:

        field1.typedValue == 'customer1'
        field2.typedValue == 'customer1'

        when:

        order1.customer = customer2

        then:

        field1.typedValue == 'customer2'
        field2.typedValue == 'customer2'
    }

    def "binding to deep property path"() {

        field1.setValueSource(new ContainerValueSource(this.lineDc, 'order.customer.name'))
        field2.setValueSource(new ContainerValueSource(this.lineDc, 'order.customer.name'))

        when:

        lineDc.item = line1

        then:

        field1.typedValue == 'customer1'
        field2.typedValue == 'customer1'

        when:

        field1.typedValue = 'customer11'

        then:

        customer1.name == 'customer11'

        and:

        field2.typedValue == 'customer11'

        when:

        customer1.name = 'customer111'

        then:

        field1.typedValue == 'customer111'
        field2.typedValue == 'customer111'

        when: "change root item"

        lineDc.item = line2

        then:

        field1.typedValue == 'customer2'
        field2.typedValue == 'customer2'

        when: "change intermediate item"

        line2.order = order1

        then:

        field1.typedValue == 'customer111'
        field2.typedValue == 'customer111'

        when: "change leaf item"

        order1.customer = customer2

        then:

        field1.typedValue == 'customer2'
        field2.typedValue == 'customer2'

    }

    def "binding to deep property path - set leaf item null"() {

        field1.setValueSource(new ContainerValueSource(this.lineDc, 'order.customer.name'))
        field2.setValueSource(new ContainerValueSource(this.lineDc, 'order.customer.name'))

        when:

        lineDc.item = line1

        then:

        field1.typedValue == 'customer1'
        field2.typedValue == 'customer1'

        when:

        order1.customer = null

        then:

        field1.typedValue == null
        field2.typedValue == null
    }

    def "binding to deep property path - set intermediate item null"() {

        field1.setValueSource(new ContainerValueSource(this.lineDc, 'order.customer.name'))
        field2.setValueSource(new ContainerValueSource(this.lineDc, 'order.customer.name'))

        when:

        lineDc.item = line1

        then:

        field1.typedValue == 'customer1'
        field2.typedValue == 'customer1'

        when:

        line1.order = null

        then:

        field1.typedValue == null
        field2.typedValue == null
    }

    def "binding to deep property path - set root item null"() {

        field1.setValueSource(new ContainerValueSource(this.lineDc, 'order.customer.name'))
        field2.setValueSource(new ContainerValueSource(this.lineDc, 'order.customer.name'))

        when:

        lineDc.item = line1

        then:

        field1.typedValue == 'customer1'
        field2.typedValue == 'customer1'

        when:

        lineDc.item = null

        then:

        field1.typedValue == null
        field2.typedValue == null
    }
}
