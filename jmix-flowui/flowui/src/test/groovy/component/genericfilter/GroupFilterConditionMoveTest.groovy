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

package component.genericfilter

import component.genericfilter.view.GfPlainHostTestView
import io.jmix.core.Metadata
import io.jmix.core.querycondition.PropertyConditionUtils
import io.jmix.flowui.OpenedDialogWindows
import io.jmix.flowui.action.genericfilter.GenericFilterEditAction
import io.jmix.flowui.app.filter.condition.GroupFilterConditionDetailView
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.genericfilter.FilterUtils
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.genericfilter.registration.FilterComponents
import io.jmix.flowui.component.grid.TreeDataGrid
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.entity.filter.FilterCondition
import io.jmix.flowui.entity.filter.FilterValueComponent
import io.jmix.flowui.entity.filter.PropertyFilterCondition
import io.jmix.flowui.kit.component.button.JmixButton
import io.jmix.flowui.testassist.vaadin.TestUI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * Moving a condition up or down in the editor of a group condition changes the order of the group's
 * conditions and the rows the conditions grid shows.
 */
@SpringBootTest
class GroupFilterConditionMoveTest extends FlowuiTestSpecification {

    @Autowired
    FilterComponents filterComponents
    @Autowired
    Metadata metadata

    void setup() {
        registerViewBasePackages("component.genericfilter.view", "io.jmix.flowui.app")
    }

    def "#buttonId moves the selected condition in the group and in the grid"() {
        given: "the editor of a configuration with the 'number' and 'amount' conditions, its grid shown"
        GroupFilterConditionDetailView view = openConfigurationEditor("number", "amount")
        TreeDataGrid<FilterCondition> grid = view.listDataComponent as TreeDataGrid<FilterCondition>
        shownRows(view, grid)

        when: "the user selects the '#property' condition and clicks the button"
        grid.select(ownCondition(view, property))
        (UiComponentUtils.getComponent(view, buttonId) as JmixButton).click()

        then: "the group's conditions and the grid rows under the group are in the new order"
        view.instanceContainer.item.ownFilterConditions*.property == ["amount", "number"]
        shownRows(view, grid).drop(1)*.property == ["amount", "number"]

        where:
        buttonId      | property
        "moveUpBtn"   | "amount"
        "moveDownBtn" | "number"
    }

    GroupFilterConditionDetailView openConfigurationEditor(String... properties) {
        GenericFilter filter = navigateToView(GfPlainHostTestView).genericFilter
        def configuration = filter.currentConfiguration
        properties.each {
            def condition = filterComponents.getConverterByModelClass(PropertyFilterCondition, filter)
                    .convertToComponent(propertyCondition(it))
            configuration.setFilterComponentModified(condition, true)
            configuration.rootLogicalFilterComponent.add(condition)
        }
        FilterUtils.setCurrentConfiguration(filter, configuration, true)

        filter.getAction(GenericFilterEditAction.ID).actionPerform(null)
        return applicationContext.getBean(OpenedDialogWindows).currentDialog.orElseThrow()
                as GroupFilterConditionDetailView
    }

    PropertyFilterCondition propertyCondition(String property) {
        PropertyFilterCondition model = metadata.create(PropertyFilterCondition)
        model.property = property
        model.operation = PropertyFilter.Operation.EQUAL
        model.parameterName = PropertyConditionUtils.generateParameterName(property)
        model.valueComponent = metadata.create(FilterValueComponent)
        return model
    }

    static PropertyFilterCondition ownCondition(GroupFilterConditionDetailView view, String property) {
        return view.instanceContainer.item.ownFilterConditions
                .find { (it as PropertyFilterCondition).property == property } as PropertyFilterCondition
    }

    /**
     * Returns the rows the grid has sent to the client: the client requests the root rows and the children of the
     * expanded root group, the pending update of the grid is flushed and the response is written. Without a written
     * response every flush would take the client for a new one and resend all rows, hiding stale ones.
     */
    List<FilterCondition> shownRows(GroupFilterConditionDetailView view, TreeDataGrid<FilterCondition> grid) {
        def communicator = grid.dataCommunicator
        FilterCondition root = view.instanceContainer.item
        communicator.setRequestedRange(0, 50)
        communicator.setParentRequestedRange(0, 50, root)
        (ui as TestUI).runExecutionsBeforeClientResponse()
        ui.internals.stateTree.collectChanges {}

        def controller = communicator.@dataControllers[communicator.keyMapper.key(root)]
        return [root] + (controller.@activeKeyOrder as List<String>)
                .collect { communicator.keyMapper.get(it) as FilterCondition }
    }
}
