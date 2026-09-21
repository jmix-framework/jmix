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

package filter_configuration_persistence.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

/**
 * A view that registers programmatic run-time configurations in {@code onInit}: {@code openProjects}
 * is made current, {@code otherProjects} is only registered. The persisted configurations are loaded
 * after {@code onInit}, so this view shows what happens when a stored configuration has the id of a
 * configuration that is already registered.
 */
@Route("programmatic-filter-configuration-test-view")
@ViewController("ProgrammaticFilterConfigurationTestView")
@ViewDescriptor("programmatic-filter-configuration-test-view.xml")
public class ProgrammaticFilterConfigurationTestView extends StandardView {

    public static final String CURRENT_CONFIGURATION_ID = "openProjects";
    public static final String CURRENT_CONFIGURATION_NAME = "Open Projects";

    public static final String OTHER_CONFIGURATION_ID = "otherProjects";
    public static final String OTHER_CONFIGURATION_NAME = "Other Projects";

    @ViewComponent
    public GenericFilter genericFilter;

    /**
     * The configuration as the application keeps it: the instance returned by the builder.
     */
    public Configuration currentConfigurationReference;

    @Subscribe
    public void onInit(final InitEvent event) {
        PropertyFilter<String> nameFilter = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.CONTAINS)
                .build();

        currentConfigurationReference = genericFilter.runtimeConfigurationBuilder()
                .id(CURRENT_CONFIGURATION_ID)
                .name(CURRENT_CONFIGURATION_NAME)
                .add(nameFilter)
                .makeCurrent()
                .buildAndRegister();

        PropertyFilter<String> otherNameFilter = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();

        genericFilter.runtimeConfigurationBuilder()
                .id(OTHER_CONFIGURATION_ID)
                .name(OTHER_CONFIGURATION_NAME)
                .add(otherNameFilter)
                .buildAndRegister();
    }
}
