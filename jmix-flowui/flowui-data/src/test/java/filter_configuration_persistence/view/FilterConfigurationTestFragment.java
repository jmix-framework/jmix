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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

/**
 * A fragment whose filter gets two configurations from the configuration builder in the fragment's
 * {@code ReadyEvent}. A fragment executes its loader's init tasks before {@code ReadyEvent}, so the
 * persisted configurations are already loaded when the builder registers its configurations. The fragment
 * records the configuration current at that moment.
 */
@FragmentDescriptor("filter-configuration-test-fragment.xml")
public class FilterConfigurationTestFragment extends Fragment<VerticalLayout> {

    public static final String CONFIGURATION_ID = "fragmentProjects";
    public static final String CONFIGURATION_NAME = "Fragment Projects";
    public static final String OTHER_CONFIGURATION_ID = "otherFragmentProjects";
    public static final String OTHER_CONFIGURATION_NAME = "Other Fragment Projects";

    @ViewComponent
    public GenericFilter genericFilter;

    public Configuration currentConfigurationBeforeBuild;
    public DesignTimeConfiguration builtConfiguration;
    public DesignTimeConfiguration otherBuiltConfiguration;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        currentConfigurationBeforeBuild = genericFilter.getCurrentConfiguration();

        PropertyFilter<String> nameFilter = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.CONTAINS)
                .build();

        builtConfiguration = genericFilter.filterConfigurationBuilder()
                .id(CONFIGURATION_ID)
                .name(CONFIGURATION_NAME)
                .add(nameFilter)
                .buildAndRegister();

        PropertyFilter<String> otherNameFilter = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();

        otherBuiltConfiguration = genericFilter.filterConfigurationBuilder()
                .id(OTHER_CONFIGURATION_ID)
                .name(OTHER_CONFIGURATION_NAME)
                .add(otherNameFilter)
                .buildAndRegister();
    }
}
