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

import component.genericfilter.view.GfBaseConditionAfterActivationTestView
import component.genericfilter.view.GfConfigsNoActivationTestView
import component.genericfilter.view.GfGroupFilterBaseConditionTestView
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.logicalfilter.GroupFilter
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import static component.genericfilter.TestFilterConditions.countPropertyConditions
import static component.genericfilter.TestFilterConditions.hasPropertyConditionOn

/**
 * Applying a filter must load by what the filter shows. When the application replaces the data
 * loader condition after a configuration is active (a new base condition), every "apply" gesture —
 * {@code apply()}, the Apply button — has to combine that base with the current configuration
 * instead of loading by the base alone. The same holds for a standalone {@code GroupFilter}.
 */
@SpringBootTest
class GenericFilterApplyAfterBaseChangeTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "apply() after the base condition was replaced loads once by the base AND the active configuration"() {
        given: "c1 (a 'number' condition) is active and the loader already combines it with the base on 'amount'"
        GenericFilter filter = navigateToView(GfBaseConditionAfterActivationTestView).genericFilter
        int loads = 0
        filter.dataLoader.addPostLoadListener { loads++ }

        expect: "the initial apply at view load composed base AND configuration"
        hasPropertyConditionOn(filter.dataLoader.condition, "amount")
        hasPropertyConditionOn(filter.dataLoader.condition, "number")

        when: "the application replaces the loader condition with a new base, as Scenario 16 of the reporter does"
        filter.dataLoader.setCondition(PropertyCondition.greater("total", 0))

        then: "the loader now holds the new base only — the shown configuration is not applied"
        hasPropertyConditionOn(filter.dataLoader.condition, "total")
        !hasPropertyConditionOn(filter.dataLoader.condition, "number")

        when: "the filter is applied without switching configurations"
        filter.apply()

        then: "the loader condition combines the new base with the configuration the filter shows"
        hasPropertyConditionOn(filter.dataLoader.condition, "total")
        hasPropertyConditionOn(filter.dataLoader.condition, "number")

        and: "the replaced base is gone"
        !hasPropertyConditionOn(filter.dataLoader.condition, "amount")

        and: "the recomposition is in-memory; the data loader is hit once, as before the fix"
        loads == 1
    }

    def "repeated apply() does not accumulate conditions and does not rewrite an untouched loader condition"() {
        given:
        GenericFilter filter = navigateToView(GfBaseConditionAfterActivationTestView).genericFilter
        def composedAtViewLoad = filter.dataLoader.condition

        when:
        filter.apply()
        filter.apply()
        filter.apply()

        then: "exactly one base and one configuration condition"
        countPropertyConditions(filter.dataLoader.condition) == 2

        and: "the application did not replace the base, so the filter left the loader condition object alone"
        filter.dataLoader.condition.is(composedAtViewLoad)
    }

    def "the Apply button after the base condition was replaced loads by the base AND the active configuration"() {
        given: "c1 is active and the application has replaced the loader condition"
        GenericFilter filter = navigateToView(GfBaseConditionAfterActivationTestView).genericFilter
        filter.dataLoader.setCondition(PropertyCondition.greater("total", 0))
        int loads = 0
        filter.dataLoader.addPostLoadListener { loads++ }

        when: "the user presses the Apply button, which loads directly instead of calling apply()"
        filter.onApplyButtonClick(null)

        then:
        hasPropertyConditionOn(filter.dataLoader.condition, "total")
        hasPropertyConditionOn(filter.dataLoader.condition, "number")
        loads == 1
    }

    def "with autoApply=false apply() recomposes the loader condition but does not load; the Apply button loads"() {
        given: "c1 is active, the filter does not apply automatically, and the base was replaced"
        GenericFilter filter = navigateToView(GfBaseConditionAfterActivationTestView).genericFilter
        filter.setAutoApply(false)
        filter.dataLoader.setCondition(PropertyCondition.greater("total", 0))
        int loads = 0
        filter.dataLoader.addPostLoadListener { loads++ }

        when:
        filter.apply()

        then: "the condition is ready for whoever loads next, but nothing was loaded"
        hasPropertyConditionOn(filter.dataLoader.condition, "total")
        hasPropertyConditionOn(filter.dataLoader.condition, "number")
        loads == 0

        when: "the user presses Apply"
        filter.onApplyButtonClick(null)

        then:
        loads == 1
        countPropertyConditions(filter.dataLoader.condition) == 2
    }

    def "apply() on the empty configuration without a base loads once and leaves the loader condition untouched"() {
        given: "configurations are registered but none is current, and the loader has no condition"
        GenericFilter filter = navigateToView(GfConfigsNoActivationTestView).genericFilter
        int loads = 0
        filter.dataLoader.addPostLoadListener { loads++ }

        expect:
        filter.dataLoader.condition == null

        when:
        filter.apply()

        then: "one load, and the loader condition stays null — the filter has nothing to recompose"
        loads == 1
        filter.dataLoader.condition == null
    }

    def "a standalone GroupFilter recomposes onto a replaced base on apply() and on a child operation change"() {
        given: "a standalone GroupFilter with a 'number' condition and a base on 'amount' set in onInit"
        GroupFilter groupFilter = navigateToView(GfGroupFilterBaseConditionTestView).groupFilter
        PropertyFilter<?> number = groupFilter.ownFilterComponents.find { it instanceof PropertyFilter } as PropertyFilter

        and: "the application replaces the loader condition with a new base"
        groupFilter.dataLoader.setCondition(PropertyCondition.greater("total", 0))

        when:
        groupFilter.apply()

        then: "the loader condition combines the new base with the group's own condition"
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "total")
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "number")
        !hasPropertyConditionOn(groupFilter.dataLoader.condition, "amount")

        when: "the base is replaced again and the user changes the operation of a condition, which applies the group through its listener"
        groupFilter.dataLoader.setCondition(PropertyCondition.greater("amount", 0))
        number.setOperation(PropertyFilter.Operation.CONTAINS)

        then:
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "amount")
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "number")
        !hasPropertyConditionOn(groupFilter.dataLoader.condition, "total")
    }

    def "a standalone GroupFilter with autoApply=false recomposes on apply() without loading"() {
        given:
        GroupFilter groupFilter = navigateToView(GfGroupFilterBaseConditionTestView).groupFilter
        groupFilter.setAutoApply(false)
        groupFilter.dataLoader.setCondition(PropertyCondition.greater("total", 0))
        int loads = 0
        groupFilter.dataLoader.addPostLoadListener { loads++ }

        when:
        groupFilter.apply()

        then: "the condition is ready for the application's own load; nothing was loaded by the group"
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "total")
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "number")
        loads == 0
    }

    def "the delegated root group inside a GenericFilter leaves an untouched loader condition alone"() {
        given: "c1 is active; the loader holds the GenericFilter's composed condition"
        GenericFilter filter = navigateToView(GfBaseConditionAfterActivationTestView).genericFilter
        filter.apply()
        def composedByGenericFilter = filter.dataLoader.condition

        when: "a child operation change applies the delegated root group"
        PropertyFilter<?> number = filter.getConfiguration("c1").rootLogicalFilterComponent.filterComponents
                .find { it instanceof PropertyFilter } as PropertyFilter
        number.setOperation(PropertyFilter.Operation.CONTAINS)

        then: "the application did not replace the base — same loader condition object"
        filter.dataLoader.condition.is(composedByGenericFilter)
    }

    def "a condition value change inside a GenericFilter recomposes onto a replaced base before loading"() {
        given: "c1 is active and the loader combines the base with the configuration"
        GenericFilter filter = navigateToView(GfBaseConditionAfterActivationTestView).genericFilter
        PropertyFilter<?> number = filter.getConfiguration("c1").rootLogicalFilterComponent.filterComponents
                .find { it instanceof PropertyFilter } as PropertyFilter
        int loads = 0
        filter.dataLoader.addPostLoadListener { loads++ }

        and: "the application replaces the loader condition with a new base"
        filter.dataLoader.setCondition(PropertyCondition.greater("total", 0))

        when: "the user commits a condition value, which applies the condition component directly"
        number.apply()

        then: "one load, by the new base AND the shown configuration — not by the base alone"
        loads == 1
        hasPropertyConditionOn(filter.dataLoader.condition, "total")
        hasPropertyConditionOn(filter.dataLoader.condition, "number")
        !hasPropertyConditionOn(filter.dataLoader.condition, "amount")
    }

    def "a condition operation change inside a GenericFilter recomposes onto a replaced base before loading"() {
        given: "c1 is active and the application has replaced the loader condition"
        GenericFilter filter = navigateToView(GfBaseConditionAfterActivationTestView).genericFilter
        PropertyFilter<?> number = filter.getConfiguration("c1").rootLogicalFilterComponent.filterComponents
                .find { it instanceof PropertyFilter } as PropertyFilter
        int loads = 0
        filter.dataLoader.addPostLoadListener { loads++ }
        filter.dataLoader.setCondition(PropertyCondition.greater("total", 0))

        when: "the user changes the condition operation, which applies the delegated root group"
        number.setOperation(PropertyFilter.Operation.CONTAINS)

        then: "one load, by the new base AND the shown configuration"
        loads == 1
        hasPropertyConditionOn(filter.dataLoader.condition, "total")
        hasPropertyConditionOn(filter.dataLoader.condition, "number")
    }

    def "an operation change in a group nested in a standalone GroupFilter recomposes onto a replaced base"() {
        given: "a standalone group with a nested group, and the application has replaced the loader condition"
        GroupFilter groupFilter = navigateToView(GfGroupFilterBaseConditionTestView).groupFilter
        GroupFilter nested = groupFilter.ownFilterComponents.find { it instanceof GroupFilter } as GroupFilter
        PropertyFilter<?> date = nested.ownFilterComponents.find { it instanceof PropertyFilter } as PropertyFilter
        groupFilter.dataLoader.setCondition(PropertyCondition.greater("total", 0))

        when: "the user changes the operation of the nested group's condition, which applies the nested (delegated) group"
        date.setOperation(PropertyFilter.Operation.LESS)

        then: "the loader condition combines the new base with the owning group's output"
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "total")
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "number")
        !hasPropertyConditionOn(groupFilter.dataLoader.condition, "amount")
    }

    def "a value change of a condition delegated to a standalone GroupFilter recomposes onto a replaced base"() {
        given:
        GroupFilter groupFilter = navigateToView(GfGroupFilterBaseConditionTestView).groupFilter
        PropertyFilter<?> number = groupFilter.ownFilterComponents.find { it instanceof PropertyFilter } as PropertyFilter
        groupFilter.dataLoader.setCondition(PropertyCondition.greater("total", 0))
        int loads = 0
        groupFilter.dataLoader.addPostLoadListener { loads++ }

        when: "the user commits a condition value, which applies the condition component directly"
        number.apply()

        then: "one load, by the new base AND the group's conditions"
        loads == 1
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "total")
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "number")
    }
}
