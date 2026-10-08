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
import facet.url_query_parameters.view.GenericFilterEditableOpConfigTestView
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.UrlQueryParametersFacet
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder
import io.jmix.flowui.model.CollectionLoader
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * Restoring the filter state from URL query parameters must not fire a data load of its own: the
 * load belongs to the navigation itself. The binder applies the operation from the URL to the
 * configuration's condition programmatically; the group's operation-change listener applies the
 * filter on a client-driven change only, so no intermediate load happens mid-restore.
 */
@SpringBootTest
class GenericFilterUrlRestoreLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")
    }

    def "restoring a configuration with a changed operation fires no load of its own"() {
        given: "a view with a design-time configuration whose condition operation is editable"
        def view = navigateToView(GenericFilterEditableOpConfigTestView)
        def binder = getBinder(view.urlQueryParameters)
        int loads = 0
        (view.ownersFilter.dataLoader as CollectionLoader).addPostLoadListener { loads++ }

        when: "the URL selects the configuration and carries a different operation for its condition"
        binder.updateState(new QueryParameters([
                (binder.configurationParam): List.of("byName"),
                (binder.conditionParam)    : List.of("property:name_not-equal_Bob")]))

        then: "the operation and the value are applied to the configuration's own condition"
        def component = view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents
                .first() as PropertyFilter<?>
        component.operation == PropertyFilter.Operation.NOT_EQUAL
        component.value == "Bob"

        and: "the restore composed the loader condition without loading"
        loads == 0
    }

    private static GenericFilterUrlQueryParametersBinder getBinder(UrlQueryParametersFacet facet) {
        return facet.binders
                .findAll { it instanceof GenericFilterUrlQueryParametersBinder }
                .first() as GenericFilterUrlQueryParametersBinder
    }
}
