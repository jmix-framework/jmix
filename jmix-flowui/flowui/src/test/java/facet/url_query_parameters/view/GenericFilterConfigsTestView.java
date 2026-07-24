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
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.View.InitEvent;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

/**
 * A view that activates a programmatic {@code RunTimeConfiguration} in {@code onInit}:
 * {@code active} (a {@code name} condition, made current) and {@code other} (an {@code email}
 * condition, registered but not current). Used to verify that such a programmatic baseline survives
 * a same-view re-navigation ({@code RestoreComponentsStateEvent} + a clean
 * {@code QueryParametersChangeEvent}) instead of being wiped.
 */
@Route("GenericFilterConfigsTestView")
@ViewController
@ViewDescriptor("generic-filter-configs-test-view.xml")
public class GenericFilterConfigsTestView extends StandardView {

    @ViewComponent
    public GenericFilter ownersFilter;

    @ViewComponent("urlQueryParameters")
    public UrlQueryParametersFacet urlQueryParameters;

    @Subscribe
    public void onInit(final InitEvent event) {
        PropertyFilter<String> nameFilter = ownersFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.EQUAL)
                .operationEditable(true)
                .build();

        ownersFilter.runtimeConfigurationBuilder()
                .id("active")
                .name("Active")
                .add(nameFilter, "John")
                .makeCurrent()
                .buildAndRegister();

        PropertyFilter<String> emailFilter = ownersFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("email")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();

        ownersFilter.runtimeConfigurationBuilder()
                .id("other")
                .name("Other")
                .add(emailFilter, "someone@example.com")
                .buildAndRegister();
    }
}
