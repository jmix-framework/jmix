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

import component.genericfilter.view.GfBaseConditionAfterActivationView
import component.genericfilter.view.GfBaseConditionReviseView
import component.genericfilter.view.GfConfigsNoActivationView
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.component.genericfilter.FilterUtils
import io.jmix.flowui.component.genericfilter.GenericFilter
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * A base condition set on the data loader after a configuration is activated in {@code onInit}
 * must still be applied after switching configurations.
 */
@SpringBootTest
class GenericFilterBaseConditionAfterActivationTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "base condition set after activation in onInit survives a configuration switch"() {
        when: "the view opens: c1 activated in onInit, then a base condition on 'amount' set on the loader"
        GenericFilter filter = navigateToView(GfBaseConditionAfterActivationView).genericFilter

        and: "switching to c2"
        filter.setCurrentConfiguration(filter.getConfiguration("c2"))

        then: "the base condition (on 'amount') is still applied alongside c2"
        hasPropertyConditionOn(filter.dataLoader.condition, "amount")
    }

    def "base condition revised after activation in onInit is the one preserved on switch"() {
        when: "the view opens: base on 'amount', activate c1, then base revised to 'total' — all in onInit"
        GenericFilter filter = navigateToView(GfBaseConditionReviseView).genericFilter

        and: "switching to c2"
        filter.setCurrentConfiguration(filter.getConfiguration("c2"))

        then: "the revised base condition (on 'total') is applied, not the pre-activation one"
        hasPropertyConditionOn(filter.dataLoader.condition, "total")
    }

    def "explicitly set initial condition is preserved when a configuration is later activated"() {
        given: "the view opens with a configuration built but not activated (filter has not contributed yet)"
        GenericFilter filter = navigateToView(GfConfigsNoActivationView).genericFilter

        and: "the loader already holds some condition, and a DIFFERENT initial condition is set explicitly"
        filter.dataLoader.setCondition(PropertyCondition.equal("number", "X"))
        FilterUtils.updateDataLoaderInitialCondition(filter, PropertyCondition.greater("amount", 0))

        when: "a configuration is activated (the first filter contribution)"
        filter.setCurrentConfiguration(filter.getConfiguration("c1"))

        then: "the explicitly set initial condition (on 'amount') is used, not the loader's prior condition"
        hasPropertyConditionOn(filter.dataLoader.condition, "amount")
    }

    def "a logical base condition is preserved and combined with the active configuration"() {
        given: "the view opens with a configuration built but not activated"
        GenericFilter filter = navigateToView(GfConfigsNoActivationView).genericFilter

        and: "the loader has a logical base condition with two properties"
        filter.dataLoader.setCondition(LogicalCondition.and(
                PropertyCondition.greater("amount", 0),
                PropertyCondition.greater("total", 0)))

        when: "a configuration is activated"
        filter.setCurrentConfiguration(filter.getConfiguration("c1"))

        then: "both base conditions are still applied alongside the configuration"
        hasPropertyConditionOn(filter.dataLoader.condition, "amount")
        hasPropertyConditionOn(filter.dataLoader.condition, "total")
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
