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

import component.genericfilter.view.FilterOrBaseConditionTestView
import io.jmix.core.DataManager
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.JpqlCondition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

import static component.genericfilter.TestFilterConditions.countPropertyConditions
import static component.genericfilter.TestFilterConditions.hasPropertyConditionOn

/**
 * A filter component bound to a data loader whose base condition is an {@code or} group combines its
 * condition with the base by AND instead of adding it to the group. The base is declared in XML:
 * {@code number = 'OB_NEW' or number = 'OB_IN_PROGRESS'}.
 */
@SpringBootTest
class FilterOrBaseConditionTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    List<Order> seeded = []

    void setup() {
        registerViewBasePackages("component.genericfilter.view", "io.jmix.flowui.app")
    }

    void cleanup() {
        seeded.each { dataManager.remove(it) }
    }

    def "genericFilter combines its configuration with an 'or' base condition by AND"() {
        when:
        def view = navigateToView(FilterOrBaseConditionTestView)
        def condition = view.gfOrdersDl.condition

        then: "the loader condition is 'base AND configuration'"
        isAnd(condition)
        baseOrGroups(condition).size() == 1
        baseOrGroups(condition)[0].conditions.size() == 2

        and: "the configuration's condition is outside the base group"
        hasPropertyConditionOn(condition, "number")
    }

    def "genericFilter over an 'or' base never loads rows the base condition excludes"() {
        given: "orders inside and outside the base condition"
        seeded = [
                dataManager.save(new Order(number: "OB_NEW")),
                dataManager.save(new Order(number: "OB_IN_PROGRESS")),
                dataManager.save(new Order(number: "OB_CLOSED")),
        ]
        def view = navigateToView(FilterOrBaseConditionTestView)
        def numberFilter = view.genericFilter.currentConfiguration.rootLogicalFilterComponent
                .filterComponents[0] as PropertyFilter<String>

        when: "the filter looks for an order the base condition excludes"
        numberFilter.setValue("OB_CLOSED")
        view.genericFilter.apply()

        then: "nothing is loaded"
        view.gfOrdersDl.container.items.isEmpty()

        when: "the filter looks for an order inside the base condition"
        numberFilter.setValue("OB_NEW")
        view.genericFilter.apply()

        then: "only that order is loaded"
        view.gfOrdersDl.container.items*.number == ["OB_NEW"]
    }

    def "genericFilter combines its configuration by AND with an 'or' base the application sets later"() {
        given:
        def view = navigateToView(FilterOrBaseConditionTestView)

        when: "the application replaces the base with an 'or' group and the filter is applied"
        view.andGfOrdersDl.setCondition(LogicalCondition.or(
                PropertyCondition.greater("amount", 0),
                PropertyCondition.greater("total", 0)))
        view.andGenericFilter.apply()
        def condition = view.andGfOrdersDl.condition

        then: "the configuration's condition is outside the new base group"
        isAnd(condition)
        def orGroups = condition.conditions.findAll { isOr(it) }
        orGroups.size() == 1
        orGroups[0].conditions*.property == ["amount", "total"]
        hasPropertyConditionOn(condition, "number")
    }

    def "a standalone groupFilter combines its condition with an 'or' base condition by AND"() {
        given:
        def view = navigateToView(FilterOrBaseConditionTestView)

        expect: "the loader condition is 'base AND group'"
        isAnd(view.groupOrdersDl.condition)
        baseOrGroups(view.groupOrdersDl.condition).size() == 1
        baseOrGroups(view.groupOrdersDl.condition)[0].conditions.size() == 2
        hasPropertyConditionOn(view.groupOrdersDl.condition, "number")

        when: "an operation change rebuilds the loader condition"
        view.groupFilter.setOperation(LogicalFilterComponent.Operation.OR)
        def condition = view.groupOrdersDl.condition

        then: "the base group is still combined by AND and holds only its own conditions"
        isAnd(condition)
        baseOrGroups(condition).size() == 1
        baseOrGroups(condition)[0].conditions.size() == 2
        hasPropertyConditionOn(condition, "number")
    }

    def "standalone propertyFilters combine their conditions with an 'or' base condition by AND"() {
        when:
        def view = navigateToView(FilterOrBaseConditionTestView)
        def condition = view.propertyOrdersDl.condition

        then: "the loader condition is 'base AND number AND amount'"
        isAnd(condition)
        condition.conditions.size() == 3
        baseOrGroups(condition).size() == 1
        baseOrGroups(condition)[0].conditions.size() == 2
        condition.conditions.findAll { it instanceof PropertyCondition }*.property == ["number", "amount"]
    }

    def "a standalone jpqlFilter combines its condition with an 'or' base condition by AND"() {
        when:
        def view = navigateToView(FilterOrBaseConditionTestView)
        def condition = view.jpqlOrdersDl.condition

        then: "the loader condition is 'base AND jpql'"
        isAnd(condition)
        condition.conditions.size() == 2
        baseOrGroups(condition).size() == 1
        baseOrGroups(condition)[0].conditions.size() == 2
        condition.conditions.count { it instanceof JpqlCondition } == 1
    }

    def "a data grid header filter combines its condition with an 'or' base condition by AND"() {
        when:
        def view = navigateToView(FilterOrBaseConditionTestView)
        def condition = view.gridOrdersDl.condition

        then: "the loader condition is 'base AND column condition'"
        isAnd(condition)
        condition.conditions.size() == 2
        baseOrGroups(condition).size() == 1
        baseOrGroups(condition)[0].conditions.size() == 2
        condition.conditions.find { it instanceof PropertyCondition }?.property == "number"
    }

    def "a header filter and a genericFilter on one loader both combine with an 'or' base condition by AND"() {
        when:
        def view = navigateToView(FilterOrBaseConditionTestView)
        def condition = view.gridGfOrdersDl.condition

        then: "the loader condition is 'base AND column condition AND configuration'"
        isAnd(condition)
        baseOrGroups(condition).size() == 1
        baseOrGroups(condition)[0].conditions.size() == 2
        hasPropertyConditionOn(condition, "number")
        hasPropertyConditionOn(condition, "amount")
        countPropertyConditions(condition) == 2
    }

    def "genericFilter adds its configuration to an 'and' base condition"() {
        when:
        def view = navigateToView(FilterOrBaseConditionTestView)
        def condition = view.andGfOrdersDl.condition

        then: "the base conditions and the configuration are siblings in one 'and' group"
        isAnd(condition)
        condition.conditions.size() == 3
        condition.conditions.count { it instanceof JpqlCondition } == 2
        condition.conditions.count { isAnd(it) } == 1
        hasPropertyConditionOn(condition, "number")
    }

    def "a standalone propertyFilter appends its condition to an 'and' base condition"() {
        when:
        def view = navigateToView(FilterOrBaseConditionTestView)
        def condition = view.andPropertyOrdersDl.condition

        then: "the base conditions and the filter's condition are siblings in one 'and' group"
        isAnd(condition)
        condition.conditions.size() == 3
        condition.conditions.count { it instanceof JpqlCondition } == 2
        condition.conditions.find { it instanceof PropertyCondition }?.property == "number"
    }

    /**
     * Returns the direct children of the condition that are an {@code or} group of JPQL conditions only,
     * i.e. the base condition declared in the view XML.
     */
    static List<LogicalCondition> baseOrGroups(Condition condition) {
        if (!(condition instanceof LogicalCondition)) {
            return []
        }
        return condition.conditions.findAll {
            isOr(it) && (it as LogicalCondition).conditions.every { c -> c instanceof JpqlCondition }
        } as List<LogicalCondition>
    }

    static boolean isAnd(Condition condition) {
        condition instanceof LogicalCondition && condition.type == LogicalCondition.Type.AND
    }

    static boolean isOr(Condition condition) {
        condition instanceof LogicalCondition && condition.type == LogicalCondition.Type.OR
    }
}
