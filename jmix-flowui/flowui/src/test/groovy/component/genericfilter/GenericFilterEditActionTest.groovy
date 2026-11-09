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

import com.vaadin.flow.component.Component
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap
import component.genericfilter.view.GenericFilterApiTestView
import io.jmix.core.Metadata
import io.jmix.flowui.OpenedDialogWindows
import io.jmix.flowui.action.genericfilter.GenericFilterEditAction
import io.jmix.flowui.app.filter.condition.FilterConditionDetailView
import io.jmix.flowui.app.filter.condition.GroupFilterConditionDetailView
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.filter.SingleFilterComponentBase
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.jpqlfilter.JpqlFilter
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.component.select.JmixSelect
import io.jmix.flowui.util.OperationResult
import io.jmix.flowui.view.StandardDetailView
import io.jmix.flowui.view.View
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.TestRunTimeConfigurations
import test_support.entity.sales.Customer
import test_support.spec.FlowuiTestSpecification

/**
 * Editing a configuration in the condition editor keeps the values the user has entered in its conditions.
 * Verifies that a value is not put into a condition whose value component the editor has changed to another
 * kind, and that it is kept when the kind stays the same.
 */
@SpringBootTest
class GenericFilterEditActionTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata

    void setup() {
        registerViewBasePackages("component.genericfilter.view", "io.jmix.flowui.app")
    }

    def "a value entered for #fromOperation is not put into the condition edited to #toOperation"() {
        given: "a current run-time configuration whose #property condition has a value"
        GenericFilter filter = navigateToView(GenericFilterApiTestView).genericFilter
        PropertyFilter<Object> condition = propertyFilter(filter, property, fromOperation)
        condition.value = valueOf(metadata)
        filter.setCurrentConfiguration(TestRunTimeConfigurations.register(filter, "own", "Own", condition))

        when: "the user changes the operation of the condition in the editor and saves the configuration"
        def conditionView = openConditionEditor(filter)
        changeFromClient(UiComponentUtils.getComponent(conditionView, "operationField") as JmixSelect, toOperation)
        saveConditionEditorAndConfiguration(conditionView)

        then: "the edited condition is empty"
        PropertyFilter<?> editedCondition = currentCondition(filter) as PropertyFilter
        editedCondition.operation == toOperation
        editedCondition.value == null

        where:
        property   | fromOperation                    | toOperation                      | valueOf
        "customer" | PropertyFilter.Operation.IN_LIST | PropertyFilter.Operation.EQUAL   | { Metadata m -> [m.create(Customer)] }
        "number"   | PropertyFilter.Operation.IN_LIST | PropertyFilter.Operation.IS_SET  | { Metadata m -> ["N1", "N2"] }
    }

    def "a value is kept when the condition is edited to an operation of the same kind"() {
        given: "a current run-time configuration whose condition has a value"
        GenericFilter filter = navigateToView(GenericFilterApiTestView).genericFilter
        PropertyFilter<Object> condition = propertyFilter(filter, "number", PropertyFilter.Operation.EQUAL)
        condition.value = "N1"
        filter.setCurrentConfiguration(TestRunTimeConfigurations.register(filter, "own", "Own", condition))

        when: "the user changes the operation to another single-value one in the editor and saves the configuration"
        def conditionView = openConditionEditor(filter)
        changeFromClient(UiComponentUtils.getComponent(conditionView, "operationField") as JmixSelect,
                PropertyFilter.Operation.NOT_EQUAL)
        saveConditionEditorAndConfiguration(conditionView)

        then: "the edited condition keeps the value"
        PropertyFilter<?> editedCondition = currentCondition(filter) as PropertyFilter
        editedCondition.operation == PropertyFilter.Operation.NOT_EQUAL
        editedCondition.value == "N1"
    }

    def "a list value is not put into a JPQL condition edited to have no 'in' expression"() {
        given: "a current run-time configuration whose JPQL condition has a list of entities"
        GenericFilter filter = navigateToView(GenericFilterApiTestView).genericFilter
        JpqlFilter<Object> condition = filter.filterComponentBuilder()
                .jpqlFilter(Customer)
                .label("Customers")
                .parameterName("customers")
                .where("{E}.customer in ?")
                .hasInExpression(true)
                .build()
        condition.value = [metadata.create(Customer)]
        filter.setCurrentConfiguration(TestRunTimeConfigurations.register(filter, "own", "Own", condition))

        when: "the user clears 'in' expression of the condition in the editor and saves the configuration"
        def conditionView = openConditionEditor(filter)
        conditionView.instanceContainer.item.where = "{E}.customer = ?"
        UiComponentUtils.getComponent(conditionView, "hasInExpressionField").element.node
                .getFeature(ElementPropertyMap)
                .deferredUpdateFromClient("checked", false)
                .run()
        saveConditionEditorAndConfiguration(conditionView)

        then: "the edited condition is empty"
        JpqlFilter<?> editedCondition = currentCondition(filter) as JpqlFilter
        !editedCondition.hasInExpression
        editedCondition.value == null
    }

    protected static PropertyFilter<Object> propertyFilter(GenericFilter filter, String property,
                                                           PropertyFilter.Operation operation) {
        return filter.filterComponentBuilder()
                .propertyFilter()
                .property(property)
                .operation(operation)
                .operationEditable(true)
                .build()
    }

    /**
     * Opens the configuration editor by the 'Edit' action, then the editor of its only condition.
     */
    protected FilterConditionDetailView openConditionEditor(GenericFilter filter) {
        filter.getAction(GenericFilterEditAction.ID).actionPerform(null)
        def groupView = currentDialogView() as GroupFilterConditionDetailView

        def condition = groupView.instanceContainer.item.ownFilterConditions[0]
        groupView.listDataComponent.select(condition)
        groupView.editAction.actionPerform(null)

        return currentDialogView() as FilterConditionDetailView
    }

    /**
     * Clicks 'OK' in the condition editor, then in the configuration editor under it.
     */
    protected void saveConditionEditorAndConfiguration(FilterConditionDetailView conditionView) {
        assert (conditionView as StandardDetailView).closeWithSave().status == OperationResult.Status.SUCCESS
        assert (currentDialogView() as StandardDetailView).closeWithSave().status == OperationResult.Status.SUCCESS
    }

    protected View currentDialogView() {
        return applicationContext.getBean(OpenedDialogWindows).currentDialog.orElseThrow()
    }

    protected static SingleFilterComponentBase<?> currentCondition(GenericFilter filter) {
        return filter.currentConfiguration.rootLogicalFilterComponent.filterComponents[0] as SingleFilterComponentBase
    }

    /**
     * Selects the item as the user does, so the editor reacts to it as to a client change.
     */
    protected static void changeFromClient(JmixSelect<Object> select, Object item) {
        Object previousItem = select.value
        select.value = item
        String key = select.element.getProperty("value")
        select.value = previousItem

        (select as Component).element.node.getFeature(ElementPropertyMap)
                .deferredUpdateFromClient("value", key)
                .run()
    }
}
