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

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Table
import io.jmix.ui.component.TextField
import io.jmix.ui.component.data.table.ContainerTableItems
import io.jmix.ui.component.data.value.ContainerValueSource
import io.jmix.ui.model.CollectionContainer
import io.jmix.ui.model.InstanceContainer
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Order

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class ContainerBindingTest extends ScreenSpecification {

    def "fields with one instance container"() {
        InstanceContainer<Order> container = dataComponents.createInstanceContainer(Order)

        TextField field1 = uiComponents.create(TextField)
        field1.setValueSource(new ContainerValueSource(container, 'number'))

        TextField field2 = uiComponents.create(TextField)
        field2.setValueSource(new ContainerValueSource(container, 'number'))

        def order = metadata.create(Order)
        order.number = 'num1'

        when:

        container.setItem(order)

        then:

        field1.value == 'num1'
        field2.value == 'num1'

        when:

        field1.value = 'changed'

        then:

        field2.value == 'changed'
        order.number == 'changed'
    }

    def "field and table with collection container"() {

        CollectionContainer<Order> container = dataComponents.createCollectionContainer(Order)

        Table<Order> table = uiComponents.create(Table)
        table.addColumn(metadata.getClass(Order).getPropertyPath('number'))
        table.setItems(new ContainerTableItems(container))

        TextField textField = uiComponents.create(TextField)
        textField.setValueSource(new ContainerValueSource(container, 'number'))

        Order order1 = metadata.create(Order)
        order1.number = 'num1'
        Order order2 = metadata.create(Order)
        order2.number = 'num2'
        container.items = [order1, order2]

        when:

        table.selected = order1

        then:

        table.singleSelected == order1
        container.item == order1
        textField.value == 'num1'

        when:

        table.selected = order2

        then:

        table.singleSelected == order2
        container.item == order2
        textField.value == 'num2'
    }
}
