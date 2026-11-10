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

import component.genericfilter.view.GenericFilterApiTestView
import io.jmix.flowui.OpenedDialogWindows
import io.jmix.flowui.action.genericfilter.GenericFilterEditAction
import io.jmix.flowui.app.filter.condition.GroupFilterConditionDetailView
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.grid.TreeDataGrid
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.entity.filter.FilterCondition
import io.jmix.flowui.entity.filter.PropertyFilterCondition
import io.jmix.flowui.kit.component.button.JmixButton
import io.jmix.flowui.testassist.vaadin.TestUI
import org.springframework.boot.test.context.SpringBootTest
import test_support.TestRunTimeConfigurations
import test_support.spec.FlowuiTestSpecification

/**
 * Moving a condition up or down in the editor of a group condition changes the order of the group's
 * conditions and the rows the conditions grid shows.
 */
@SpringBootTest
class GroupFilterConditionMoveTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view", "io.jmix.flowui.app")
    }

    def "#buttonId moves the selected condition in the group and in the grid"() {
        given: "the editor of a configuration with the 'number' and 'amount' conditions, its grid shown"
        GroupFilterConditionDetailView view = openConfigurationEditor("number", "amount")
        TreeDataGrid<FilterCondition> grid = view.listDataComponent as TreeDataGrid<FilterCondition>
        shownRows(grid)

        when: "the user selects the '#property' condition and clicks the button"
        grid.select(ownCondition(view, property))
        (UiComponentUtils.getComponent(view, buttonId) as JmixButton).click()

        then: "the group's conditions and the grid rows under the group are in the new order"
        view.instanceContainer.item.ownFilterConditions*.property == ["amount", "number"]
        shownRows(grid).drop(1)*.property == ["amount", "number"]

        where:
        buttonId      | property
        "moveUpBtn"   | "amount"
        "moveDownBtn" | "number"
    }

    GroupFilterConditionDetailView openConfigurationEditor(String... properties) {
        GenericFilter filter = navigateToView(GenericFilterApiTestView).genericFilter
        def conditions = properties.collect {
            filter.filterComponentBuilder()
                    .propertyFilter()
                    .property(it)
                    .operation(PropertyFilter.Operation.EQUAL)
                    .build()
        }
        filter.setCurrentConfiguration(TestRunTimeConfigurations.register(filter, "own", "Own",
                *conditions))

        filter.getAction(GenericFilterEditAction.ID).actionPerform(null)
        return applicationContext.getBean(OpenedDialogWindows).currentDialog.orElseThrow()
                as GroupFilterConditionDetailView
    }

    static PropertyFilterCondition ownCondition(GroupFilterConditionDetailView view, String property) {
        return view.instanceContainer.item.ownFilterConditions
                .find { (it as PropertyFilterCondition).property == property } as PropertyFilterCondition
    }

    /**
     * Returns the rows the grid has sent to the client: the visible range is set as the client sets it,
     * the pending update of the grid is flushed and the response is written. Without a written response
     * every flush would take the client for a new one and resend all rows, hiding stale ones.
     */
    List<FilterCondition> shownRows(TreeDataGrid<FilterCondition> grid) {
        def communicator = grid.dataCommunicator
        communicator.setViewportRange(0, 50)
        (ui as TestUI).runExecutionsBeforeClientResponse()
        ui.internals.stateTree.collectChanges {}

        return communicator.preloadFlatRangeForward(0, 50) as List<FilterCondition>
    }
}
