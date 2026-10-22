/*
 * Copyright 2026 Haulmont.
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

package component.pickerreadonly

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.internal.JacksonUtils
import com.vaadin.flow.internal.nodefeature.ElementListenerMap
import component.pickerreadonly.view.OrderLookupTestView
import component.pickerreadonly.view.OrderPickerReadTestView
import component.pickerreadonly.view.PickerShortcutTestView
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.flowui.Actions
import io.jmix.flowui.UiComponents
import io.jmix.flowui.action.entitypicker.EntityLookupAction
import io.jmix.flowui.action.entitypicker.EntityReadAction
import io.jmix.flowui.component.valuepicker.EntityPicker
import io.jmix.flowui.kit.action.BaseAction
import io.jmix.flowui.kit.component.KeyCombination
import io.jmix.flowui.testassist.UiTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(properties = [
        'jmix.ui.component.picker-lookup-shortcut = Control-Alt-L',
        'jmix.ui.component.picker-read-shortcut = Control-Alt-R'
])
class PickerReadOnlyShortcutTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents
    @Autowired
    Actions actions
    @Autowired
    Metadata metadata
    @Autowired
    DataManager dataManager
    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component.pickerreadonly.view")
    }

    def "the lookup shortcut opens no dialog on a read-only picker"() {
        given: "a read-only picker with the lookup action"
        def picker = createPicker()
        picker.addAction(createLookupAction())
        picker.setReadOnly(true)

        when: "the lookup shortcut is pressed"
        fireShortcut('KeyL')

        then: "no dialog is opened"
        UiTestUtils.getOpenedViewDialogs().isEmpty()
    }

    def "a hidden application action's shortcut is not performed on a read-only picker"() {
        given: "a read-only picker with an application action that has a shortcut"
        def picker = createPicker()
        def counter = new TestCounter()
        picker.addAction(createCountingAction(counter))
        picker.setReadOnly(true)

        when: "the action's shortcut is pressed"
        fireShortcut('KeyX')

        then: "the action is not performed"
        counter.count == 0
    }

    def "shortcuts still work on an editable picker"() {
        given: "an editable picker with the lookup action and an application action that has a shortcut"
        def picker = createPicker()
        def counter = new TestCounter()
        picker.addAction(createLookupAction())
        picker.addAction(createCountingAction(counter))

        when: "the application action's shortcut is pressed"
        fireShortcut('KeyX')

        then: "the action is performed"
        counter.count == 1

        when: "the lookup shortcut is pressed"
        fireShortcut('KeyL')

        then: "the lookup dialog is opened"
        UiTestUtils.getLastOpenedViewDialog() instanceof OrderLookupTestView
    }

    def "the read shortcut still works on a read-only picker"() {
        given: "a saved order"
        def order = dataManager.create(Order)
        order.number = 'order-1'
        dataManager.save(order)

        and: "a read-only picker with the read action and the order as its value"
        def picker = createPicker()
        def readAction = actions.create(EntityReadAction.ID) as EntityReadAction<Order>
        readAction.setViewClass(OrderPickerReadTestView)
        picker.addAction(readAction)
        picker.setValue(order)
        picker.setReadOnly(true)

        when: "the read shortcut is pressed"
        fireShortcut('KeyR')

        then: "the read view is opened"
        UiTestUtils.getLastOpenedViewDialog() instanceof OrderPickerReadTestView

        cleanup:
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    EntityPicker<Order> createPicker() {
        def view = navigateToView(PickerShortcutTestView)

        EntityPicker<Order> picker = uiComponents.create(EntityPicker)
        picker.setMetaClass(metadata.getClass(Order))
        view.pickerBox.add(picker)

        return picker
    }

    EntityLookupAction<Order> createLookupAction() {
        def lookupAction = actions.create(EntityLookupAction.ID) as EntityLookupAction<Order>
        lookupAction.setViewClass(OrderLookupTestView)
        return lookupAction
    }

    static BaseAction createCountingAction(TestCounter counter) {
        return new BaseAction('custom')
                .withIcon(VaadinIcon.COG.create())
                .withShortcutCombination(KeyCombination.create('Control-Alt-X'))
                .withHandler { counter.count++ }
    }

    static void fireShortcut(String code) {
        UI.current.internals.stateTree.runExecutionsBeforeClientResponse()

        def listeners = UI.current.element.node.getFeature(ElementListenerMap)
        def expression = listeners.getExpressions('keydown').find { it.contains("'${code}'") }
        assert expression != null

        listeners.fireEvent(new DomEvent(UI.current.element, 'keydown',
                JacksonUtils.createObjectNode().put(expression, true)))
    }

    static class TestCounter {
        int count
    }
}
