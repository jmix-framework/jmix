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

package data_components

import io.jmix.core.Metadata
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.data.value.BufferedContainerValueSource
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.InstanceContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class BufferedDataUnitTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents
    @Autowired
    UiComponents uiComponents
    @Autowired
    Metadata metadata

    def "field with BufferedContainerValueSource"() {
        Order order = metadata.create(Order)
        order.number = 'num1'

        InstanceContainer<Order> container = dataComponents.createInstanceContainer(Order)

        TypedTextField<String> field = uiComponents.create(TypedTextField)
        BufferedContainerValueSource<Order, String> valueSource =
                new BufferedContainerValueSource<>(container, "number")
        field.setValueSource(valueSource)

        when: "item is set"
        container.setItem(order)

        then: "value source state is set to ACTIVE and field gets value from an entity"
        order.number == 'num1'
        field.typedValue == 'num1'
        valueSource.value == 'num1'
        !valueSource.modified

        when: "component value is changed"
        field.typedValue = 'num2'

        then: "an entity keeps its value, value source is modified"
        order.number == 'num1'
        field.typedValue == 'num2'
        valueSource.value == 'num2'
        valueSource.modified

        when: "changes are discarded"
        valueSource.discard()

        then: "value source value is synced with the entity"
        order.number == 'num1'
        field.typedValue == 'num1'
        valueSource.value == 'num1'
        !valueSource.modified

        when: "component value is changed and value source value is written"
        field.typedValue = 'num2'
        valueSource.write()

        then: "the entity value is synced with value source"
        order.number == 'num2'
        field.typedValue == 'num2'
        valueSource.value == 'num2'
        !valueSource.modified

        when: "entity value is changed"
        order.number = 'num3'

        then: "value source keeps its value and becomes modified"
        order.number == 'num3'
        field.typedValue == 'num2'
        valueSource.value == 'num2'
        valueSource.modified

        when: "changes are discarded"
        valueSource.discard()

        then: "value source value is synced with the entity"
        order.number == 'num3'
        field.typedValue == 'num3'
        valueSource.value == 'num3'
        !valueSource.modified

        when: "entity value is changed and value source value is written"
        order.number = 'num4'
        valueSource.write()

        then: "the entity value is synced with value source"
        order.number == 'num3'
        field.typedValue == 'num3'
        valueSource.value == 'num3'
        !valueSource.modified
    }
}
