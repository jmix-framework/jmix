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

import component.genericfilter.view.GfNestedGroupRemoveTestView
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.logicalfilter.GroupFilter
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.kit.component.button.JmixButton
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * Reproduction: the remove (x) button on a condition nested inside a group.
 * Establishes empirically, for a run-time configuration, whether such a condition gets a
 * remove button and whether removing it clears the model and the loader condition.
 */
@SpringBootTest
class GenericFilterNestedRemoveTest extends FlowuiTestSpecification {

    private static final String REMOVE_BUTTON_SUFFIX = "conditionRemoveButton"

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "clicking the remove (x) button on a condition nested in a group removes it"() {
        given: "a run-time configuration whose root contains a nested group with one property condition"
        def view = navigateToView(GfNestedGroupRemoveTestView)
        GenericFilter filter = view.genericFilter
        GroupFilter nestedGroup = view.nestedGroup
        PropertyFilter<String> nestedCondition = view.nestedCondition

        expect: "the nested condition is in the model and drives the loader condition"
        nestedGroup.ownFilterComponents.contains(nestedCondition)
        hasPropertyConditionOn(filter.dataLoader.condition, "number")

        and: "the nested condition has a remove (x) button"
        JmixButton removeButton = findRemoveButton(nestedCondition)
        removeButton != null

        when: "clicking the remove (x) button — the same handler a user triggers"
        removeButton.click()

        then: "it is removed from the nested group and no longer filters"
        !nestedGroup.ownFilterComponents.contains(nestedCondition)
        !hasPropertyConditionOn(filter.dataLoader.condition, "number")
    }

    def "clicking the remove (x) button on a condition nested two groups deep removes it"() {
        given: "a run-time configuration: root -> outer group -> inner group -> one property condition"
        GenericFilter filter = navigateToView(GfNestedGroupRemoveTestView).genericFilter

        PropertyFilter<String> deepCondition = filter.filterComponentBuilder()
                .<String> propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build()
        deepCondition.value = "n2"

        GroupFilter innerGroup = filter.filterComponentBuilder().groupFilter().add(deepCondition).build()
        GroupFilter outerGroup = filter.filterComponentBuilder().groupFilter().add(innerGroup).build()
        filter.runtimeConfigurationBuilder()
                .id("c2")
                .name("C2")
                .add(outerGroup)
                .makeCurrent()
                .buildAndRegister()

        expect: "the two-levels-deep condition drives the loader condition"
        innerGroup.ownFilterComponents.contains(deepCondition)
        hasPropertyConditionOn(filter.dataLoader.condition, "number")

        and: "the deep condition has a remove (x) button"
        JmixButton removeButton = findRemoveButton(deepCondition)
        removeButton != null

        when: "clicking the remove (x) button"
        removeButton.click()

        then: "it is removed from its inner group and no longer filters"
        !innerGroup.ownFilterComponents.contains(deepCondition)
        !hasPropertyConditionOn(filter.dataLoader.condition, "number")
    }

    protected static JmixButton findRemoveButton(PropertyFilter<?> condition) {
        String id = condition.innerComponentPrefix + REMOVE_BUTTON_SUFFIX
        return UiComponentUtils.findComponent(condition.root, id).orElse(null) as JmixButton
    }

    protected static boolean hasPropertyConditionOn(Condition condition, String property) {
        if (condition instanceof PropertyCondition) {
            return property == condition.property
        }
        if (condition instanceof LogicalCondition) {
            return condition.conditions.any { hasPropertyConditionOn(it, property) }
        }
        return false
    }
}
