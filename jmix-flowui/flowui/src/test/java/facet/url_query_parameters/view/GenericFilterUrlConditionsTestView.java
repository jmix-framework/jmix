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

package facet.url_query_parameters.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.TestRunTimeConfigurations;
import test_support.entity.petclinic.Owner;

/**
 * A view with several design-time configurations over the {@code Owner} attributes and one runtime
 * configuration, {@code runtimeByName}, whose {@code name} condition has a non-editable operation.
 * No configuration is default, so none is current when the view is opened. Used to check which values
 * URL conditions get and which rows they are matched onto.
 */
@Route("GenericFilterUrlConditionsTestView")
@ViewController
@ViewDescriptor("generic-filter-url-conditions-test-view.xml")
public class GenericFilterUrlConditionsTestView extends StandardView {

    @ViewComponent
    public GenericFilter ownersFilter;

    @ViewComponent
    public CollectionLoader<Owner> ownersDl;

    @ViewComponent("urlQueryParameters")
    public UrlQueryParametersFacet urlQueryParameters;

    @Subscribe
    public void onInit(final InitEvent event) {
        PropertyFilter<String> nameFilter = ownersFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.EQUAL)
                .operationEditable(false)
                .build();
        nameFilter.setValue("John");

        TestRunTimeConfigurations.register(ownersFilter, "runtimeByName", "Runtime by name", nameFilter);
    }
}
