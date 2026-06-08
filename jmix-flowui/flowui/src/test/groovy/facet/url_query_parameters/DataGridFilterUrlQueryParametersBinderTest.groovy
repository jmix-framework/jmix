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
import facet.url_query_parameters.view.DataGridFilterUrlQueryParamsTestView
import io.jmix.flowui.component.grid.DataGridColumn
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.UrlQueryParametersFacet
import io.jmix.flowui.facet.urlqueryparameters.DataGridFilterUrlQueryParametersBinder
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DataGridFilterUrlQueryParametersBinderTest extends FlowuiTestSpecification {

    private static final String PARAM_NAME = "ownersFilter"

    @Override
    void setup() {
        // facet.url_query_parameters — наш тестовый view;
        // io.jmix.flowui.app — внутренние flowui-views (например, headerPropertyFilterLayout),
        // которые сбрасываются между тестами в resetViewRegistry() из FlowuiTestSpecification.
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")
    }

    // --- Deserialization ---

    def "applies URL with simple value"() {
        when:
        def filter = applyUrlAndGetFilter("name_name_contains_abc")

        then:
        filter.value == "abc"
        filter.operation == PropertyFilter.Operation.CONTAINS
    }

    def "applies URL with underscore in value (user bug regression)"() {
        when:
        def filter = applyUrlAndGetFilter("name_name_contains_123_123")

        then:
        filter.value == "123_123"
        filter.operation == PropertyFilter.Operation.CONTAINS
    }

    def "applies URL with multiple underscores in value"() {
        when:
        def filter = applyUrlAndGetFilter("name_name_contains_a_b_c")

        then:
        filter.value == "a_b_c"
        filter.operation == PropertyFilter.Operation.CONTAINS
    }

    def "applies URL with leading underscore in value"() {
        when:
        def filter = applyUrlAndGetFilter("name_name_contains__abc")

        then:
        filter.value == "_abc"
        filter.operation == PropertyFilter.Operation.CONTAINS
    }

    def "applies URL with multi-word operation and underscore in value"() {
        when:
        def filter = applyUrlAndGetFilter("name_name_not-equal_a_b")

        then:
        filter.value == "a_b"
        filter.operation == PropertyFilter.Operation.NOT_EQUAL
    }

    // --- Roundtrip ---

    def "roundtrip preserves value with underscores"() {
        given: "a view with a configured filter"
        def screen = navigateToView(DataGridFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)
        def headerFilter = getHeaderFilter(screen, "name")
        def propertyFilter = headerFilter.propertyFilter as PropertyFilter<Object>

        and: "filter value is set to a string containing underscores"
        propertyFilter.operation = PropertyFilter.Operation.CONTAINS
        propertyFilter.value = "123_123"
        // headerFilter.apply() does not fire ApplyEvent on its own (only the button click does);
        // we directly invoke updateQueryParameters so the binder emits its serialized form.

        when: "the binder emits a URL query parameter"
        QueryParameters emitted = null
        binder.addUrlQueryParametersChangeListener({ event -> emitted = event.queryParameters })
        binder.updateQueryParameters()

        then: "an emission happened and contains the underscore value verbatim"
        emitted != null
        emitted.parameters.containsKey(PARAM_NAME)
        emitted.parameters.get(PARAM_NAME).first().endsWith("_123_123")

        when: "we wipe and reapply via updateState"
        propertyFilter.value = null
        binder.updateState(emitted)

        then: "the value is restored exactly"
        propertyFilter.value == "123_123"
        propertyFilter.operation == PropertyFilter.Operation.CONTAINS
    }

    // --- helpers ---

    /**
     * Navigates to the test view, calls binder.updateState with a crafted single
     * URL-parameter value, and returns the PropertyFilter mounted on the "name" column.
     */
    private PropertyFilter<?> applyUrlAndGetFilter(String paramValue) {
        def screen = navigateToView(DataGridFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)
        def headerFilter = getHeaderFilter(screen, "name")

        QueryParameters qp = QueryParameters.simple([(PARAM_NAME): paramValue])
        binder.updateState(qp)

        return headerFilter.propertyFilter
    }

    private DataGridFilterUrlQueryParametersBinder getBinder(DataGridFilterUrlQueryParamsTestView screen) {
        UrlQueryParametersFacet facet = screen.urlQueryParameters
        return facet.binders
                .findAll { it instanceof DataGridFilterUrlQueryParametersBinder }
                .first() as DataGridFilterUrlQueryParametersBinder
    }

    private DataGridHeaderFilter getHeaderFilter(DataGridFilterUrlQueryParamsTestView screen, String columnKey) {
        def column = screen.ownersTable.getColumnByKey(columnKey) as DataGridColumn<?>
        return column.headerComponent as DataGridHeaderFilter
    }
}
