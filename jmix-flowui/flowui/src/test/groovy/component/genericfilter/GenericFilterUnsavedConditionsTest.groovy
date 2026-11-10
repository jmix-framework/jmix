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
import io.jmix.flowui.action.genericfilter.GenericFilterAddConditionAction
import io.jmix.flowui.action.genericfilter.GenericFilterResetAction
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.entity.filter.PropertyFilterCondition
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem
import org.springframework.boot.test.context.SpringBootTest
import test_support.TestRunTimeConfigurations
import test_support.spec.FlowuiTestSpecification

/**
 * A condition the user adds to a configuration without saving it is discarded when the filter switches to another
 * configuration, so the configuration shows its saved conditions when it is selected again.
 */
@SpringBootTest
class GenericFilterUnsavedConditionsTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "a condition added without saving is discarded when the user #action"() {
        given: "a saved configuration with a 'number' condition is current"
        GenericFilter filter = navigateToView(GenericFilterApiTestView).genericFilter
        RunTimeConfiguration saved = registerSaved(filter, "saved", "number")
        registerSaved(filter, "other", "date")
        selectItem(filter, "genericFilter_select_saved")

        when: "the user adds an 'amount' condition, does not save the configuration, switches away and selects it again"
        addCondition(filter, "amount")
        selectItem(filter, itemId)
        selectItem(filter, "genericFilter_select_saved")

        then: "the configuration has only its saved condition"
        filter.currentConfiguration.is(saved)
        saved.rootLogicalFilterComponent.ownFilterComponents.collect { (it as PropertyFilter).property } == ["number"]
        !saved.modified

        where:
        action                          | itemId
        "selects another configuration" | "genericFilter_select_other"
        "resets the filter"             | GenericFilterResetAction.ID
    }

    protected static RunTimeConfiguration registerSaved(GenericFilter filter, String id, String property) {
        PropertyFilter<?> condition = filter.filterComponentBuilder()
                .propertyFilter()
                .property(property)
                .operation(PropertyFilter.Operation.EQUAL)
                .build()
        RunTimeConfiguration configuration = TestRunTimeConfigurations.register(filter, id, id, condition)
        configuration.setModified(false)
        return configuration
    }

    /**
     * Adds a property condition the way the user does it in the 'Add condition' dialog.
     */
    protected static void addCondition(GenericFilter filter, String property) {
        GenericFilterAddConditionAction action = filter.addConditionButton.action as GenericFilterAddConditionAction
        PropertyFilterCondition condition = action.builder.buildConditions(filter).find {
            it instanceof PropertyFilterCondition && it.property == property
        } as PropertyFilterCondition
        action.selectHandler.accept([condition])
    }

    protected static void selectItem(GenericFilter filter, String itemId) {
        (filter.applyButton.getItem(itemId) as ActionItem).action.actionPerform(null)
    }
}
