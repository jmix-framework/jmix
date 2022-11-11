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
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.data.grid.ContainerDataGridItems
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.InstanceContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ContainerBindingTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents
    @Autowired
    UiComponents uiComponents
    @Autowired
    Metadata metadata

    def "fields with one instance container"() {
        InstanceContainer<Order> container = dataComponents.createInstanceContainer(Order)

        TypedTextField field1 = uiComponents.create(TypedTextField)
        field1.setValueSource(new ContainerValueSource(container, 'number'))

        TypedTextField field2 = uiComponents.create(TypedTextField)
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

        DataGrid<Order> dataGrid = uiComponents.create(DataGrid)
        dataGrid.addColumn(metadata.getClass(Order).getPropertyPath('number'))
        dataGrid.setItems(new ContainerDataGridItems<Order>(container))

        TypedTextField textField = uiComponents.create(TypedTextField)
        textField.setValueSource(new ContainerValueSource(container, 'number'))

        Order order1 = metadata.create(Order)
        order1.number = 'num1'
        Order order2 = metadata.create(Order)
        order2.number = 'num2'
        container.items = [order1, order2]

        when:

        dataGrid.select(order1)

        then:

        dataGrid.singleSelectedItem == order1
        container.item == order1
        textField.value == 'num1'

        when:

        dataGrid.select(order2)

        then:

        dataGrid.singleSelectedItem == order2
        container.item == order2
        textField.value == 'num2'
    }
}
