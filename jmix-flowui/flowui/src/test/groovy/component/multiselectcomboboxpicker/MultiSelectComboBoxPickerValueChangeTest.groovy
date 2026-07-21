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

package component.multiselectcomboboxpicker

import com.vaadin.flow.component.UI
import component.multiselectcomboboxpicker.view.JmixMultiSelectComboBoxPickerOrderDetailTestView
import component.multiselectcomboboxpicker.view.JmixMultiSelectComboBoxPickerOrderListTestView
import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.flowui.kit.component.button.JmixButton
import io.jmix.flowui.kit.component.multiselectcomboboxpicker.MultiSelectComboBoxPicker
import io.jmix.flowui.testassist.UiTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class MultiSelectComboBoxPickerValueChangeTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component.multiselectcomboboxpicker.view")
        setupData()
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER_LINE")
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "base MultiSelectComboBoxPicker: setValueFromClient must be marked as from client"() {
        given: "A bare MultiSelectComboBoxPicker attached to the UI"
        def field = new MultiSelectComboBoxPicker<String>()
        field.setItems("a", "b", "c")
        UI.current.add(field)

        and: "A listener recording the fromClient flag of every value change event"
        def fromClientFlags = []
        field.addValueChangeListener({ event -> fromClientFlags << event.fromClient })

        when: "User selects a value"
        // clearing to null is not exercised: getValue() on a bare MultiSelectComboBox NPEs on a null Set,
        // a pre-existing limitation of the base Vaadin component unrelated to this fix
        field.setValueFromClient(Set.of("a"))

        then: "An event was fired and every event is client-originated"
        !fromClientFlags.isEmpty()
        fromClientFlags.every { it }
    }

    def "JmixMultiSelectComboBoxPicker: setValueFromClient must be marked as from client"() {
        given: "A JmixMultiSelectComboBoxPicker opened in a detail view"
        navigateToView(JmixMultiSelectComboBoxPickerOrderListTestView)
        def orderListView = UiTestUtils.currentView
        def createButton = UiTestUtils.getComponent(orderListView, "createButton") as JmixButton
        createButton.click()
        def orderDetailView = UiTestUtils.currentView as JmixMultiSelectComboBoxPickerOrderDetailTestView

        def orderLineValue = orderDetailView.getOrderLineByDescription("1")

        and: "A listener recording the fromClient flag of every value change event"
        def fromClientFlags = []
        orderDetailView.orderLinesField.addValueChangeListener({ event -> fromClientFlags << event.fromClient })

        when: "User selects a value and then clears it"
        orderDetailView.orderLinesField.setValueFromClient(List.of(orderLineValue))
        orderDetailView.orderLinesField.setValueFromClient(null)

        then: "An event was fired and every event is client-originated"
        !fromClientFlags.isEmpty()
        fromClientFlags.every { it }
    }

    private void setupData() {
        def orderLines = []
        [1, 2, 3].each { i ->
            def orderLine = dataManager.create(OrderLine)
            orderLine.description = i as String
            orderLines.add(orderLine)
        }

        def order = dataManager.create(Order)
        order.number = "1"
        order.orderLines = [orderLines.get(0)]

        dataManager.save(new SaveContext().saving(order).saving(orderLines.toArray()))
    }
}
