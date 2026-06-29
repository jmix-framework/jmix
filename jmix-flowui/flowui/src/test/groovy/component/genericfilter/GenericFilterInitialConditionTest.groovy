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

import component.genericfilter.view.GenericFilterInitialConditionTestView
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.component.genericfilter.GenericFilter
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * Regression test: activating a configuration during {@code onInit} must not pollute the data
 * loader's initial condition, so switching to another configuration applies only that
 * configuration's condition (not the previously active one ANDed on top).
 */
@SpringBootTest
class GenericFilterInitialConditionTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "switching configuration applies only its own condition when a configuration was activated in onInit"() {
        when: "the view opens with configuration 'c1' activated in onInit via makeCurrent()"
        GenericFilter filter = navigateToView(GenericFilterInitialConditionTestView).genericFilter

        then: "the data loader condition reflects only 'c1'"
        countPropertyConditions(filter.dataLoader.condition) == 1

        when: "switching to configuration 'c2'"
        filter.setCurrentConfiguration(filter.getConfiguration("c2"))

        then: "the data loader condition reflects only 'c2', not 'c1' AND 'c2'"
        countPropertyConditions(filter.dataLoader.condition) == 1
    }

    protected static int countPropertyConditions(Condition condition) {
        if (condition instanceof PropertyCondition) {
            return 1
        }
        if (condition instanceof LogicalCondition) {
            return condition.conditions.inject(0) { acc, c -> acc + countPropertyConditions(c) }
        }
        return 0
    }
}
