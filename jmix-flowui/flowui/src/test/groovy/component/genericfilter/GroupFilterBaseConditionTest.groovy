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

import component.genericfilter.view.GfGroupFilterBaseConditionView
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.component.logicalfilter.GroupFilter
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * A base condition set on the data loader of a standalone {@code GroupFilter} in {@code onInit}
 * must still be applied after the loader condition is rebuilt (e.g. on an operation change).
 */
@SpringBootTest
class GroupFilterBaseConditionTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "base condition set in onInit on a standalone GroupFilter survives a structural rebuild"() {
        when: "the view opens: standalone GroupFilter with a base condition on 'amount' set in onInit"
        GroupFilter groupFilter = navigateToView(GfGroupFilterBaseConditionView).groupFilter

        and: "a structural change (operation switch) forces the loader condition to be rebuilt"
        groupFilter.setOperation(LogicalFilterComponent.Operation.OR)

        then: "the base condition (on 'amount') is still applied"
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "amount")
    }

    def "a logical base condition on a standalone GroupFilter is preserved on a structural rebuild"() {
        given: "the view opens with a standalone GroupFilter"
        GroupFilter groupFilter = navigateToView(GfGroupFilterBaseConditionView).groupFilter

        and: "the loader has a logical base condition with two properties"
        groupFilter.dataLoader.setCondition(LogicalCondition.and(
                PropertyCondition.greater("amount", 0),
                PropertyCondition.greater("total", 0)))

        when: "a structural change forces the loader condition to be rebuilt"
        groupFilter.setOperation(LogicalFilterComponent.Operation.OR)

        then: "both base conditions are still applied"
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "amount")
        hasPropertyConditionOn(groupFilter.dataLoader.condition, "total")
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
