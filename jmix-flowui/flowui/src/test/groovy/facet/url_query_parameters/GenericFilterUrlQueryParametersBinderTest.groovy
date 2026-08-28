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

package facet.url_query_parameters

import com.vaadin.flow.router.QueryParameters
import facet.url_query_parameters.view.GenericFilterUrlQueryParamsTestView
import io.jmix.flowui.component.filter.FilterComponent
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import static facet.url_query_parameters.TestGenericFilterUrlBinders.getBinder

/**
 * Checks how {@link GenericFilterUrlQueryParametersBinder} deserializes property conditions from
 * the URL. A property condition serialized by the generic filter uses the shape
 * {@code property:propertyName_operation_value}: the parser uses
 * {@code substring(separatorIndex + 1)} to grab the value (everything past the second underscore),
 * so underscores inside the value are preserved, and a condition that cannot be applied to the
 * loaded entity is skipped instead of failing the navigation.
 */
@SpringBootTest
class GenericFilterUrlQueryParametersBinderTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")
    }

    def "applies condition URL with underscore in value"() {
        when:
        def propertyFilter = applyConditionAndGetPropertyFilter("property:name_contains_123_123")

        then:
        propertyFilter.property == "name"
        propertyFilter.operation == PropertyFilter.Operation.CONTAINS
        propertyFilter.value == "123_123"
    }

    def "applies condition URL with multiple underscores in value"() {
        when:
        def propertyFilter = applyConditionAndGetPropertyFilter("property:name_contains_a_b_c")

        then:
        propertyFilter.property == "name"
        propertyFilter.operation == PropertyFilter.Operation.CONTAINS
        propertyFilter.value == "a_b_c"
    }

    def "applies condition URL with multi-word operation and underscore in value"() {
        when:
        def propertyFilter = applyConditionAndGetPropertyFilter("property:name_not-equal_a_b")

        then:
        propertyFilter.property == "name"
        propertyFilter.operation == PropertyFilter.Operation.NOT_EQUAL
        propertyFilter.value == "a_b"
    }

    def "ignores condition on unknown attribute"() {
        when:
        def components = applyConditionsAndGetFilterComponents("property:doesNotExist_equal_x")

        then:
        noExceptionThrown()
        components.empty
    }

    def "ignores condition on unknown attribute and keeps the valid ones"() {
        when:
        def components = applyConditionsAndGetFilterComponents(
                "property:doesNotExist_equal_x", "property:name_contains_abc")

        then:
        components.size() == 1

        and:
        def propertyFilter = components.first() as PropertyFilter<?>
        propertyFilter.property == "name"
        propertyFilter.operation == PropertyFilter.Operation.CONTAINS
        propertyFilter.value == "abc"
    }

    private PropertyFilter<?> applyConditionAndGetPropertyFilter(String conditionString) {
        def components = applyConditionsAndGetFilterComponents(conditionString)
        assert !components.empty: "expected one filter component, got none"
        return components.first() as PropertyFilter<?>
    }

    private List<FilterComponent> applyConditionsAndGetFilterComponents(String... conditionStrings) {
        def screen = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)
        QueryParameters qp = new QueryParameters([(binder.conditionParam): conditionStrings.toList()])
        binder.updateState(qp)

        return screen.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents
    }
}
