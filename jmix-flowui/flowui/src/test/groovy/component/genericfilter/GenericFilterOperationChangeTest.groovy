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
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Status
import test_support.TestRunTimeConfigurations
import test_support.spec.FlowuiTestSpecification

/**
 * The user can change an editable operation of a condition, and the condition then gets a value component
 * of another kind. Verifies that a default value recorded for the original operation is not put into it,
 * and that the listeners of a configuration no longer shown do not act on the current one.
 */
@SpringBootTest
class GenericFilterOperationChangeTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "a design-time default value of #property is not applied after the operation is changed to #operation"() {
        given: "a current design-time configuration whose editable condition has a default value"
        GenericFilter filter = navigateToView(GenericFilterApiTestView).genericFilter
        PropertyFilter<Object> condition = editableFilter(filter, property)
        filter.filterConfigurationBuilder()
                .id("predefined")
                .add(condition, defaultValue)
                .makeCurrent()
                .buildAndRegister()

        when: "the user changes the operation, then selects another configuration"
        condition.setOperation(operation)
        filter.setCurrentConfiguration(filter.emptyConfiguration)

        then: "the condition is cleared instead of getting the default value of the original operation"
        condition.value == null

        where:
        property          | defaultValue | operation
        "number"          | "N1"         | PropertyFilter.Operation.IN_LIST
        "number"          | "N1"         | PropertyFilter.Operation.IS_SET
        "customer.status" | Status.OK    | PropertyFilter.Operation.IS_SET
    }

    def "a design-time default value applies again once the operation is changed back"() {
        given: "a current design-time configuration whose editable condition has a default value"
        GenericFilter filter = navigateToView(GenericFilterApiTestView).genericFilter
        PropertyFilter<Object> numberFilter = editableFilter(filter, "number")
        filter.filterConfigurationBuilder()
                .id("predefined")
                .add(numberFilter, "N1")
                .makeCurrent()
                .buildAndRegister()

        when: "the user changes the operation and changes it back, then selects another configuration"
        numberFilter.setOperation(PropertyFilter.Operation.IN_LIST)
        numberFilter.setOperation(PropertyFilter.Operation.EQUAL)
        filter.setCurrentConfiguration(filter.emptyConfiguration)

        then: "the condition gets its default value"
        numberFilter.value == "N1"
    }

    def "an operation change in a configuration no longer shown does not act on the current one"() {
        given: "a current run-time configuration with an editable condition"
        GenericFilter filter = navigateToView(GenericFilterApiTestView).genericFilter
        PropertyFilter<Object> numberFilter = editableFilter(filter, "number")
        numberFilter.value = "N1"
        filter.setCurrentConfiguration(TestRunTimeConfigurations.register(filter, "own", "Own", numberFilter))

        and: "a design-time configuration without conditions"
        def all = filter.filterConfigurationBuilder()
                .id("all")
                .name("All")
                .buildAndRegister()

        when: "the design-time configuration is selected, and the operation of the other one's condition changes, as when its URL state is restored"
        filter.setCurrentConfiguration(all)
        numberFilter.setOperation(PropertyFilter.Operation.NOT_EQUAL)

        then:
        noExceptionThrown()
    }

    protected static PropertyFilter<Object> editableFilter(GenericFilter filter, String property) {
        return filter.filterComponentBuilder()
                .propertyFilter()
                .property(property)
                .operation(PropertyFilter.Operation.EQUAL)
                .operationEditable(true)
                .build()
    }
}
