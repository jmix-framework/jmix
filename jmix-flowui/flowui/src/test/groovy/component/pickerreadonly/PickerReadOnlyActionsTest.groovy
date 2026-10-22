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

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.icon.VaadinIcon
import io.jmix.core.Metadata
import io.jmix.flowui.Actions
import io.jmix.flowui.UiComponents
import io.jmix.flowui.action.AdjustWhenFieldReadOnly
import io.jmix.flowui.action.entitypicker.EntityClearAction
import io.jmix.flowui.action.entitypicker.EntityLookupAction
import io.jmix.flowui.action.entitypicker.EntityReadAction
import io.jmix.flowui.action.multivaluepicker.MultiValueSelectAction
import io.jmix.flowui.component.combobox.EntityComboBox
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker
import io.jmix.flowui.component.valuepicker.EntityPicker
import io.jmix.flowui.component.valuepicker.JmixMultiValuePicker
import io.jmix.flowui.kit.action.Action
import io.jmix.flowui.kit.action.BaseAction
import io.jmix.flowui.kit.component.valuepicker.ValuePickerButton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class PickerReadOnlyActionsTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents
    @Autowired
    Actions actions
    @Autowired
    Metadata metadata

    def "only entity_read stays visible on a read-only picker"() {
        given: "a picker with the lookup, clear and read actions"
        def picker = createEntityPicker()
        addLookupClearRead(picker)

        when: "the picker is made read-only"
        picker.setReadOnly(true)

        then: "only the read button is visible"
        findButton(picker, EntityReadAction.ID).visible
        !findButton(picker, EntityLookupAction.ID).visible
        !findButton(picker, EntityClearAction.ID).visible
    }

    def "every button returns when the picker becomes editable again"() {
        given: "a read-only picker with the lookup, clear and read actions"
        def picker = createEntityPicker()
        addLookupClearRead(picker)
        picker.setReadOnly(true)

        when: "the picker is made editable again"
        picker.setReadOnly(false)

        then: "all buttons are visible"
        findButton(picker, EntityReadAction.ID).visible
        findButton(picker, EntityLookupAction.ID).visible
        findButton(picker, EntityClearAction.ID).visible
    }

    def "actions added to a read-only picker start hidden"() {
        given: "a picker made read-only before it has any action"
        def picker = createEntityPicker()
        picker.setReadOnly(true)

        when: "the lookup, clear and read actions are added"
        addLookupClearRead(picker)

        then: "only the read button is visible"
        findButton(picker, EntityReadAction.ID).visible
        !findButton(picker, EntityLookupAction.ID).visible
        !findButton(picker, EntityClearAction.ID).visible
    }

    def "a mutating action made visible again stays hidden on a read-only picker"() {
        given: "a read-only picker with the lookup, clear and read actions"
        def picker = createEntityPicker()
        def (lookup, clear, read) = addLookupClearRead(picker)
        picker.setReadOnly(true)

        when: "the clear action is hidden and then made visible again"
        clear.visible = false
        clear.visible = true

        then: "its button stays hidden"
        !findButton(picker, EntityClearAction.ID).visible
    }

    def "entity_read follows its own visibility on a read-only picker"() {
        given: "a read-only picker with the lookup, clear and read actions"
        def picker = createEntityPicker()
        def (lookup, clear, read) = addLookupClearRead(picker)
        picker.setReadOnly(true)

        when: "the read action is hidden"
        read.visible = false

        then: "its button is hidden"
        !findButton(picker, EntityReadAction.ID).visible

        when: "the read action is made visible again"
        read.visible = true

        then: "its button is visible"
        findButton(picker, EntityReadAction.ID).visible
    }

    def "an application action opts in by implementing AdjustWhenFieldReadOnly"() {
        given: "a picker with the clear action and two application actions"
        def picker = createEntityPicker()
        picker.addAction(actions.create(EntityClearAction.ID))
        picker.addAction(new TestVisibleWhenReadOnlyAction('optIn'))
        picker.addAction(new TestDefaultFieldReadOnlyAction('noOverride'))

        when: "the picker is made read-only"
        picker.setReadOnly(true)

        then: "only the action that stays visible on a read-only field shows its button"
        findButton(picker, 'optIn').visible
        !findButton(picker, 'noOverride').visible
        !findButton(picker, EntityClearAction.ID).visible
    }

    def "a picker with entity_read is marked as having read-only actions"() {
        given: "a picker with the lookup, clear and read actions"
        def picker = createEntityPicker()
        def (lookup, clear, read) = addLookupClearRead(picker)

        expect: "the editable picker is marked"
        picker.element.hasAttribute('has-readonly-actions')

        when: "the picker is made read-only"
        picker.setReadOnly(true)

        then: "the read-only picker is marked"
        picker.element.hasAttribute('has-readonly-actions')

        when: "the read action is removed"
        picker.removeAction(read)

        then: "the mark is removed"
        !picker.element.hasAttribute('has-readonly-actions')
    }

    def "a read-only picker with only mutating actions keeps today's markup"() {
        given: "a picker with the lookup and clear actions"
        def picker = createEntityPicker()
        picker.addAction(actions.create(EntityLookupAction.ID))
        picker.addAction(actions.create(EntityClearAction.ID))

        when: "the picker is made read-only"
        picker.setReadOnly(true)

        then: "it is not marked, and the actions slot and has-actions are in place"
        !picker.element.hasAttribute('has-readonly-actions')
        hasActionsSlot(picker)
        picker.element.hasAttribute('has-actions')
    }

    def "entity_read stays visible on a read-only EntityComboBox"() {
        given: "an entity combo box with the clear and read actions"
        EntityComboBox<Order> comboBox = uiComponents.create(EntityComboBox)
        comboBox.setMetaClass(metadata.getClass(Order))
        comboBox.addAction(actions.create(EntityClearAction.ID))
        comboBox.addAction(actions.create(EntityReadAction.ID))

        when: "the combo box is made read-only"
        comboBox.setReadOnly(true)

        then: "only the read button is visible"
        findButton(comboBox, EntityReadAction.ID).visible
        !findButton(comboBox, EntityClearAction.ID).visible
    }

    def "an opted-in action stays visible on a read-only JmixMultiSelectComboBoxPicker"() {
        given: "a multi-select combo box picker with the clear action and an opted-in application action"
        JmixMultiSelectComboBoxPicker<Order> picker = uiComponents.create(JmixMultiSelectComboBoxPicker)
        picker.setMetaClass(metadata.getClass(Order))
        picker.addAction(actions.create(EntityClearAction.ID))
        picker.addAction(new TestVisibleWhenReadOnlyAction('optIn'))

        when: "the picker is made read-only"
        picker.setReadOnly(true)

        then: "only the opted-in button is visible"
        findButton(picker, 'optIn').visible
        !findButton(picker, EntityClearAction.ID).visible
    }

    def "multi_value_select is hidden on a read-only JmixMultiValuePicker"() {
        given: "a multi-value picker with the select action"
        JmixMultiValuePicker picker = uiComponents.create(JmixMultiValuePicker)
        picker.addAction(actions.create(MultiValueSelectAction.ID))

        when: "the picker is made read-only"
        picker.setReadOnly(true)

        then: "the select button is hidden and the picker is not marked"
        !findButton(picker, MultiValueSelectAction.ID).visible
        !picker.element.hasAttribute('has-readonly-actions')
    }

    EntityPicker<Order> createEntityPicker() {
        EntityPicker<Order> picker = uiComponents.create(EntityPicker)
        picker.setMetaClass(metadata.getClass(Order))
        return picker
    }

    List<Action> addLookupClearRead(EntityPicker<Order> picker) {
        def lookup = actions.create(EntityLookupAction.ID)
        def clear = actions.create(EntityClearAction.ID)
        def read = actions.create(EntityReadAction.ID)

        picker.addAction(lookup)
        picker.addAction(clear)
        picker.addAction(read)

        return [lookup, clear, read]
    }

    static ValuePickerButton findButton(Component picker, String actionId) {
        def actionsLayout = findActionsLayout(picker)
        assert actionsLayout != null

        return actionsLayout.children
                .map { it.component.orElse(null) }
                .filter { it instanceof ValuePickerButton && it.id.orElse(null) == actionId }
                .findFirst()
                .orElseThrow() as ValuePickerButton
    }

    static boolean hasActionsSlot(Component picker) {
        return findActionsLayout(picker) != null
    }

    static findActionsLayout(Component picker) {
        return picker.element.children
                .filter { it.getAttribute('slot') == 'actions' }
                .findFirst()
                .orElse(null)
    }

    static class TestVisibleWhenReadOnlyAction extends BaseAction implements AdjustWhenFieldReadOnly {

        TestVisibleWhenReadOnlyAction(String id) {
            super(id)
            setIcon(VaadinIcon.EYE.create())
        }

        @Override
        boolean isVisibleWhenFieldReadOnly() {
            return true
        }
    }

    static class TestDefaultFieldReadOnlyAction extends BaseAction implements AdjustWhenFieldReadOnly {

        TestDefaultFieldReadOnlyAction(String id) {
            super(id)
            setIcon(VaadinIcon.EYE.create())
        }
    }
}
