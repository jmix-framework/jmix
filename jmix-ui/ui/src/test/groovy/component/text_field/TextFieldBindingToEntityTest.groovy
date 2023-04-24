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

package component.text_field

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.TextField
import io.jmix.ui.component.data.value.ContainerValueSource
import io.jmix.ui.model.InstanceContainer
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.entity.sales.Status

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class TextFieldBindingToEntityTest extends ScreenSpecification {

    private InstanceContainer<Customer> customerDc
    private InstanceContainer<Order> orderDc
    private customer
    private order
    private OrderLine orderLine1, orderLine2

    @Override
    void setup() {
        customer = metadata.create(Customer)
        customer.setName('cust1')
        customer.setStatus(Status.OK)

        order = metadata.create(Order)
        order.setNumber('111')
        order.setCustomer(customer)

        orderLine1 = metadata.create(OrderLine)
        orderLine1.setOrder(order)
        orderLine1.setQuantity(1)

        orderLine2 = metadata.create(OrderLine)
        orderLine2.setOrder(order)
        orderLine2.setQuantity(2)

        order.setOrderLines([orderLine1, orderLine2])

        customerDc = dataComponents.createInstanceContainer(Customer)
        orderDc = dataComponents.createInstanceContainer(Order)
    }

    def "single reference is displayed as its InstanceName"() {

        TextField textField = uiComponents.create(TextField)
        textField.setValueSource(new ContainerValueSource(orderDc, 'customer'))

        when:

        orderDc.setItem(order)

        then:

        textField.unwrap(com.vaadin.ui.TextField).getValue() == 'cust1'
        !textField.isEditable()

        when:

        order.customer = null

        then:

        textField.unwrap(com.vaadin.ui.TextField).getValue() == ''
        !textField.isEditable()
    }

    def "collection reference is displayed as comma-separated list of items InstanceName"() {

        TextField textField = uiComponents.create(TextField)
        textField.setValueSource(new ContainerValueSource(orderDc, 'orderLines'))

        when:

        orderDc.setItem(order)

        then:

        textField.unwrap(com.vaadin.ui.TextField).getValue() == metadataTools.getInstanceName(orderLine1) + ', ' + metadataTools.getInstanceName(orderLine2)
        !textField.isEditable()
    }
}
