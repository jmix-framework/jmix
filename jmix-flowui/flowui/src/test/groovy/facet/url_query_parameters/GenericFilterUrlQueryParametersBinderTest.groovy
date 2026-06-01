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
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.UrlQueryParametersFacet
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * Sanity check that {@link GenericFilterUrlQueryParametersBinder} does not suffer from the
 * underscore-in-value issue. A property condition serialized by the generic filter uses the
 * shape {@code property:propertyName_operation_value}; the parser uses
 * {@code substring(separatorIndex + 1)} to grab the value (everything past the second
 * underscore), so underscores inside the value are preserved.
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

    private PropertyFilter<?> applyConditionAndGetPropertyFilter(String conditionString) {
        def screen = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)
        QueryParameters qp = QueryParameters.simple([(binder.conditionParam): conditionString])
        binder.updateState(qp)

        def root = screen.ownersFilter.currentConfiguration.rootLogicalFilterComponent
        def components = root.filterComponents
        assert !components.empty: "expected one filter component, got none"
        return components.first() as PropertyFilter<?>
    }

    private GenericFilterUrlQueryParametersBinder getBinder(GenericFilterUrlQueryParamsTestView screen) {
        UrlQueryParametersFacet facet = screen.urlQueryParameters
        return facet.binders
                .findAll { it instanceof GenericFilterUrlQueryParametersBinder }
                .first() as GenericFilterUrlQueryParametersBinder
    }
}
