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
import facet.url_query_parameters.view.PropertyFilterUrlQueryParamsTestView
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.UrlQueryParametersFacet
import io.jmix.flowui.facet.urlqueryparameters.PropertyFilterUrlQueryParametersBinder
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * Sanity check that {@link PropertyFilterUrlQueryParametersBinder} does not suffer from the
 * underscore-in-value issue that we fixed for the data grid header filter. Its serialization
 * format is {@code operation_value} (one structural separator) and the parser uses
 * {@code substring(separatorIndex + 1)} to grab the value as the rest of the string, so
 * underscores inside the value are preserved with no special handling.
 */
@SpringBootTest
class PropertyFilterUrlQueryParametersBinderTest extends FlowuiTestSpecification {

    private static final String PARAM_NAME = "nameFilterParam"

    @Override
    void setup() {
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")
    }

    def "applies URL with underscore in value"() {
        when:
        def filter = applyUrlAndGetFilter("contains_123_123")

        then:
        filter.value == "123_123"
        filter.operation == PropertyFilter.Operation.CONTAINS
    }

    def "applies URL with multiple underscores in value"() {
        when:
        def filter = applyUrlAndGetFilter("contains_a_b_c")

        then:
        filter.value == "a_b_c"
        filter.operation == PropertyFilter.Operation.CONTAINS
    }

    def "applies URL with multi-word operation"() {
        when:
        def filter = applyUrlAndGetFilter("not-equal_a_b")

        then:
        filter.value == "a_b"
        filter.operation == PropertyFilter.Operation.NOT_EQUAL
    }

    def "PropertyFilter escapes LIKE wildcards in the underlying query condition"() {
        given:
        def screen = navigateToView(PropertyFilterUrlQueryParamsTestView)
        def filter = screen.nameFilter

        when:
        filter.operation = operation
        filter.value = userInput

        then:
        filter.value == userInput
        filter.queryCondition.parameterValue == escapedValue

        where:
        operation                                  | userInput | escapedValue
        PropertyFilter.Operation.CONTAINS          | "_32"     | "\\_32"
        PropertyFilter.Operation.CONTAINS          | "%32"     | "\\%32"
        PropertyFilter.Operation.NOT_CONTAINS      | "_32"     | "\\_32"
        PropertyFilter.Operation.STARTS_WITH       | "_321"    | "\\_321"
        PropertyFilter.Operation.ENDS_WITH         | "100%"    | "100\\%"
        // operations that do not use LIKE leave the value alone
        PropertyFilter.Operation.EQUAL             | "_32"     | "_32"
    }

    def "roundtrip preserves value with underscores"() {
        given:
        def screen = navigateToView(PropertyFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)
        def filter = screen.nameFilter

        filter.operation = PropertyFilter.Operation.CONTAINS
        filter.value = "123_123"

        when:
        QueryParameters emitted = null
        def registration = binder.addUrlQueryParametersChangeListener({ event -> emitted = event.queryParameters })
        binder.updateQueryParameters()
        // Stop capturing — the subsequent filter.value = null below would otherwise
        // overwrite `emitted` with the empty change event the binder fires on clear.
        registration.remove()

        then:
        emitted != null
        emitted.parameters.containsKey(PARAM_NAME)
        emitted.parameters.get(PARAM_NAME).first().endsWith("_123_123")

        when:
        filter.value = null
        binder.updateState(emitted)

        then:
        filter.value == "123_123"
        filter.operation == PropertyFilter.Operation.CONTAINS
    }

    // --- helpers ---

    private PropertyFilter<?> applyUrlAndGetFilter(String paramValue) {
        def screen = navigateToView(PropertyFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)
        QueryParameters qp = QueryParameters.simple([(PARAM_NAME): paramValue])
        binder.updateState(qp)
        return screen.nameFilter
    }

    private PropertyFilterUrlQueryParametersBinder getBinder(PropertyFilterUrlQueryParamsTestView screen) {
        UrlQueryParametersFacet facet = screen.urlQueryParameters
        return facet.binders
                .findAll { it instanceof PropertyFilterUrlQueryParametersBinder }
                .first() as PropertyFilterUrlQueryParametersBinder
    }
}
