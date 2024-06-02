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

package actions

import io.jmix.flowui.Actions
import io.jmix.flowui.UiComponents
import io.jmix.flowui.action.entitypicker.EntityClearAction
import io.jmix.flowui.action.entitypicker.EntityLookupAction
import io.jmix.flowui.action.entitypicker.EntityOpenAction
import io.jmix.flowui.action.genericfilter.GenericFilterClearValuesAction
import io.jmix.flowui.action.list.CreateAction
import io.jmix.flowui.action.multivaluepicker.MultiValueSelectAction
import io.jmix.flowui.action.valuepicker.ValueClearAction
import io.jmix.flowui.component.combobox.EntityComboBox
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.component.grid.TreeDataGrid
import io.jmix.flowui.component.valuepicker.EntityPicker
import io.jmix.flowui.kit.component.multiselectcomboboxpicker.MultiSelectComboBoxPicker
import io.jmix.flowui.kit.component.valuepicker.MultiValuePicker
import io.jmix.flowui.kit.component.valuepicker.ValuePicker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ActionTest extends FlowuiTestSpecification {

    @Autowired
    Actions actions

    @Autowired
    UiComponents uiComponents

    def "removeAllActions method correctly removes all actions"() {
        def entityComboBox = uiComponents.create(EntityComboBox)

        when:
        entityComboBox.addAction(actions.create(EntityOpenAction.ID))
        entityComboBox.addAction(actions.create(EntityLookupAction.ID))
        entityComboBox.addAction(actions.create(EntityClearAction.ID))

        then:
        entityComboBox.getActions().size() == 3

        when:
        entityComboBox.removeAllActions()

        then:
        noExceptionThrown()
        entityComboBox.getActions().isEmpty()
    }

    def "Actions with the same id correctly added to ValuePicker"() {
        def picker = uiComponents.create(ValuePicker)

        def action1 = actions.create(ValueClearAction.ID)
        picker.addAction(action1)

        when: "action with the same id is added"
        def action2 = actions.create(ValueClearAction.ID)
        picker.addAction(action2)

        then: "previous one is replaced"
        picker.getAction(ValueClearAction.ID) != action1
        picker.getAction(ValueClearAction.ID) == action2
    }

    def "Actions with the same id correctly added to MultiValuePicker"() {
        def picker = uiComponents.create(MultiValuePicker)

        def action1 = actions.create(MultiValueSelectAction.ID)
        picker.addAction(action1)

        when: "action with the same id is added"
        def action2 = actions.create(MultiValueSelectAction.ID)
        picker.addAction(action2)

        then: "previous one is replaced"
        picker.getAction(MultiValueSelectAction.ID) != action1
        picker.getAction(MultiValueSelectAction.ID) == action2
    }

    def "Actions with the same id correctly added to EntityPicker"() {
        def picker = uiComponents.create(EntityPicker)

        def action1 = actions.create(EntityClearAction.ID)
        picker.addAction(action1)

        when: "action with the same id is added"
        def action2 = actions.create(EntityClearAction.ID)
        picker.addAction(action2)

        then: "previous one is replaced"
        picker.getAction(EntityClearAction.ID) != action1
        picker.getAction(EntityClearAction.ID) == action2
    }

    def "Actions with the same id correctly added to EntityComboBox"() {
        def picker = uiComponents.create(EntityComboBox)

        def action1 = actions.create(EntityClearAction.ID)
        picker.addAction(action1)

        when: "action with the same id is added"
        def action2 = actions.create(EntityClearAction.ID)
        picker.addAction(action2)

        then: "previous one is replaced"
        picker.getAction(EntityClearAction.ID) != action1
        picker.getAction(EntityClearAction.ID) == action2
    }

    def "Actions with the same id correctly added to MultiSelectComboBoxPicker"() {
        def picker = uiComponents.create(MultiSelectComboBoxPicker)

        def action1 = actions.create(EntityClearAction.ID)
        picker.addAction(action1)

        when: "action with the same id is added"
        def action2 = actions.create(EntityClearAction.ID)
        picker.addAction(action2)

        then: "previous one is replaced"
        picker.getAction(EntityClearAction.ID) != action1
        picker.getAction(EntityClearAction.ID) == action2
    }

    def "Actions with the same id correctly added to DataGrid"() {
        def dataGrid = uiComponents.create(DataGrid)

        def action1 = actions.create(CreateAction.ID)
        dataGrid.addAction(action1)

        when: "action with the same id is added"
        def action2 = actions.create(CreateAction.ID)
        dataGrid.addAction(action2)

        then: "previous one is replaced"
        dataGrid.getAction(CreateAction.ID) != action1
        dataGrid.getAction(CreateAction.ID) == action2
    }

    def "Actions with the same id correctly added to TreeDataGrid"() {
        def dataGrid = uiComponents.create(TreeDataGrid)

        def action1 = actions.create(CreateAction.ID)
        dataGrid.addAction(action1)

        when: "action with the same id is added"
        def action2 = actions.create(CreateAction.ID)
        dataGrid.addAction(action2)

        then: "previous one is replaced"
        dataGrid.getAction(CreateAction.ID) != action1
        dataGrid.getAction(CreateAction.ID) == action2
    }

    def "Actions with the same id correctly added to GenericFilter"() {
        def filter = uiComponents.create(GenericFilter)

        def action1 = actions.create(GenericFilterClearValuesAction.ID)
        filter.addAction(action1)

        when: "action with the same id is added"
        def action2 = actions.create(GenericFilterClearValuesAction.ID)
        filter.addAction(action2)

        then: "previous one is replaced"
        filter.getAction(GenericFilterClearValuesAction.ID) != action1
        filter.getAction(GenericFilterClearValuesAction.ID) == action2
    }
}
