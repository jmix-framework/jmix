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

import facet.url_query_parameters.view.DataGridFilterUrlQueryParamsTestView
import io.jmix.flowui.component.grid.DataGridColumn
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter
import io.jmix.flowui.model.CollectionLoader
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * Pins that {@code DataGridHeaderFilter} is not affected by the loader condition recomposition
 * introduced for delegated filter conditions: its inner {@code PropertyFilter} is neither delegated
 * nor given a recomposition delegate, and {@code DataGridHeaderFilter.apply()} performs exactly one
 * load without replacing the loader condition object.
 */
@SpringBootTest
class DataGridHeaderFilterApplyTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")
    }

    def "a header filter is outside the recomposition wiring and its apply() loads exactly once"() {
        given: "a data grid with a filterable column"
        def view = navigateToView(DataGridFilterUrlQueryParamsTestView)
        def column = view.ownersTable.getColumnByKey("name") as DataGridColumn<?>
        def headerFilter = column.getHeaderComponent() as DataGridHeaderFilter

        expect: "the inner property filter is neither delegated nor wired to an owner"
        !headerFilter.propertyFilter.conditionModificationDelegated
        headerFilter.propertyFilter.loaderConditionRecomposeDelegate == null

        when: "the user applies the header filter"
        def loader = headerFilter.propertyFilter.dataLoader as CollectionLoader
        def conditionBefore = loader.condition
        int loads = 0
        loader.addPostLoadListener { loads++ }
        headerFilter.propertyFilter.setValue("John")
        headerFilter.apply()

        then: "exactly one load, and the loader condition object is not replaced by any recomposition"
        loads == 1
        loader.condition.is(conditionBefore)
    }
}
