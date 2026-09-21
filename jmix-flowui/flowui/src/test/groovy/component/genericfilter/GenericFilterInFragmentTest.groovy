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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import component.genericfilter.view.GfFragmentHostTestView
import component.genericfilter.view.GfPlainHostTestView
import io.jmix.core.Metadata
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.core.querycondition.PropertyConditionUtils
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.filter.SingleFilterComponentBase
import io.jmix.flowui.component.genericfilter.FilterUtils
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.genericfilter.registration.FilterComponents
import io.jmix.flowui.component.logicalfilter.GroupFilter
import io.jmix.flowui.component.logicalfilter.GroupFilterSupport
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.entity.filter.FilterValueComponent
import io.jmix.flowui.entity.filter.PropertyFilterCondition
import io.jmix.flowui.kit.component.button.JmixButton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * A condition of a filter placed inside a fragment must manage its remove (x) button exactly
 * as a condition of a filter declared directly in a view: one button, added and removed
 * on every configuration refresh.
 */
@SpringBootTest
class GenericFilterInFragmentTest extends FlowuiTestSpecification {

    private static final String REMOVE_BUTTON_SUFFIX = "conditionRemoveButton"

    @Autowired
    GroupFilterSupport groupFilterSupport

    @Autowired
    FilterComponents filterComponents

    @Autowired
    UiComponents uiComponents

    @Autowired
    Metadata metadata

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "a condition of a filter inside a fragment keeps one remove button when more conditions are added"() {
        given: "a filter declared inside a fragment"
        GenericFilter filter = navigateToView(GfFragmentHostTestView).filterFragment.genericFilter

        when: "the user adds the first condition"
        def first = addCondition(filter, "number")

        then: "it has a single remove button"
        countRemoveButtons(first) == 1

        when: "the user adds a second condition"
        def second = addCondition(filter, "description")

        then: "neither condition gains a second remove button"
        countRemoveButtons(first) == 1
        countRemoveButtons(second) == 1

        when: "the user adds a third condition"
        def third = addCondition(filter, "amount")

        then: "every condition still has exactly one remove button"
        countRemoveButtons(first) == 1
        countRemoveButtons(second) == 1
        countRemoveButtons(third) == 1
    }

    def "a condition of a filter declared directly in a view keeps one remove button"() {
        given: "the same filter declared directly in a view"
        GenericFilter filter = navigateToView(GfPlainHostTestView).genericFilter

        when: "the user adds three conditions"
        def first = addCondition(filter, "number")
        def second = addCondition(filter, "description")
        def third = addCondition(filter, "amount")

        then: "every condition has exactly one remove button"
        countRemoveButtons(first) == 1
        countRemoveButtons(second) == 1
        countRemoveButtons(third) == 1
    }

    def "changing the operation of a condition inside a fragment keeps one remove button at the end"() {
        given: "a condition of a filter declared inside a fragment"
        GenericFilter filter = navigateToView(GfFragmentHostTestView).filterFragment.genericFilter
        def condition = addCondition(filter, "number")

        when: "the user changes the operation, which re-creates the value component"
        condition.operation = PropertyFilter.Operation.IS_SET

        then: "the condition still has one remove button and it stays the last component"
        countRemoveButtons(condition) == 1
        isRemoveButtonLast(condition)
    }

    def "clicking the remove button of a condition inside a fragment removes the condition"() {
        given: "a condition of a filter declared inside a fragment that drives the loader condition"
        GenericFilter filter = navigateToView(GfFragmentHostTestView).filterFragment.genericFilter
        def condition = addCondition(filter, "number")
        condition.value = "n1"

        expect: "the condition is in the model and filters the data loader"
        filter.currentConfiguration.rootLogicalFilterComponent.ownFilterComponents.contains(condition)
        hasPropertyConditionOn(filter.dataLoader.condition, "number")

        when: "the user clicks the remove button"
        findRemoveButton(condition).click()

        then: "the condition is gone from the model and no longer filters"
        !filter.currentConfiguration.rootLogicalFilterComponent.ownFilterComponents.contains(condition)
        !hasPropertyConditionOn(filter.dataLoader.condition, "number")
    }

    def "a condition inside a fragment loses its remove button when it is no longer modified"() {
        given: "a condition of a filter declared inside a fragment"
        GenericFilter filter = navigateToView(GfFragmentHostTestView).filterFragment.genericFilter
        def condition = addCondition(filter, "number")

        expect: "it has a remove button"
        countRemoveButtons(condition) == 1

        when: "the condition stops being modified and the layout is refreshed"
        def configuration = filter.currentConfiguration
        configuration.setFilterComponentModified(condition, false)
        FilterUtils.setCurrentConfiguration(filter, configuration, true)

        then: "the remove button is taken away"
        countRemoveButtons(condition) == 0
    }

    def "a group condition of a filter inside a fragment keeps one remove button"() {
        given: "a filter declared inside a fragment"
        GenericFilter filter = navigateToView(GfFragmentHostTestView).filterFragment.genericFilter

        when: "the user adds a group condition and then another condition"
        GroupFilter group = addGroupCondition(filter)
        addCondition(filter, "description")

        then: "the group keeps a single remove button"
        countGroupRemoveButtons(group) == 1

        and: "so does the condition nested in the group"
        countRemoveButtons(group.ownFilterComponents[0] as SingleFilterComponentBase) == 1
    }

    protected PropertyFilter<?> addCondition(GenericFilter filter, String property) {
        return addToCurrentConfiguration(filter, createCondition(filter, property))
    }

    protected GroupFilter addGroupCondition(GenericFilter filter) {
        GroupFilter group = uiComponents.create(GroupFilter)
        group.conditionModificationDelegated = true
        group.autoApply = filter.autoApply
        group.dataLoader = filter.dataLoader
        group.operation = LogicalFilterComponent.Operation.AND
        group.add(createCondition(filter, "number"))

        return addToCurrentConfiguration(filter, group)
    }

    /**
     * Builds a condition the way {@code GenericFilterAddConditionAction} does: through the
     * converter registered for the condition model.
     */
    protected PropertyFilter<?> createCondition(GenericFilter filter, String property) {
        PropertyFilterCondition model = metadata.create(PropertyFilterCondition)
        model.property = property
        model.operation = PropertyFilter.Operation.EQUAL
        model.parameterName = PropertyConditionUtils.generateParameterName(property)
        model.valueComponent = metadata.create(FilterValueComponent)

        def converter = filterComponents.getConverterByModelClass(PropertyFilterCondition, filter)
        return converter.convertToComponent(model) as PropertyFilter<?>
    }

    /**
     * Repeats what {@code GenericFilterAddConditionAction} does when the user picks a condition
     * in the "Add search condition" dialog.
     */
    protected <T> T addToCurrentConfiguration(GenericFilter filter, T condition) {
        def configuration = filter.currentConfiguration
        configuration.setFilterComponentModified(condition, true)
        configuration.rootLogicalFilterComponent.add(condition)

        FilterUtils.setCurrentConfiguration(filter, configuration, true)

        return condition
    }

    protected static JmixButton findRemoveButton(SingleFilterComponentBase<?> condition) {
        return findById(condition.root, removeButtonId(condition)) as JmixButton
    }

    protected static boolean isRemoveButtonLast(SingleFilterComponentBase<?> condition) {
        HorizontalLayout layout = condition.root
        Component button = findById(layout, removeButtonId(condition))

        return button != null && layout.indexOf(button) == layout.componentCount - 1
    }

    protected static long countRemoveButtons(SingleFilterComponentBase<?> condition) {
        return countById(condition.root, removeButtonId(condition))
    }

    protected long countGroupRemoveButtons(GroupFilter group) {
        return countById(groupFilterSupport.getGroupFilterSummaryComponent(group), REMOVE_BUTTON_SUFFIX)
    }

    protected static String removeButtonId(SingleFilterComponentBase<?> condition) {
        return condition.innerComponentPrefix + REMOVE_BUTTON_SUFFIX
    }

    protected static Component findById(Component container, String id) {
        return container.children
                .filter { UiComponentUtils.sameId(it, id) }
                .findFirst()
                .orElse(null)
    }

    protected static long countById(Component container, String id) {
        return container.children
                .filter { UiComponentUtils.sameId(it, id) }
                .count()
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
