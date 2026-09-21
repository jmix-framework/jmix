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
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A view that registers a configuration of its own {@link Configuration} implementation, which cannot be
 * modified. Used to check that loading a stored configuration with the same id leaves it alone instead of
 * writing into it.
 */
@Route("immutable-filter-configuration-test-view")
@ViewController("ImmutableFilterConfigurationTestView")
@ViewDescriptor("immutable-filter-configuration-test-view.xml")
public class ImmutableFilterConfigurationTestView extends StandardView {

    public static final String CONFIGURATION_ID = "openProjects";
    public static final String CONFIGURATION_NAME = "Open Projects";

    @ViewComponent
    public GenericFilter genericFilter;

    public Configuration immutableConfiguration;

    @Autowired
    private UiComponents uiComponents;

    @Subscribe
    public void onInit(final InitEvent event) {
        PropertyFilter<String> nameFilter = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.CONTAINS)
                .build();

        GroupFilter rootComponent = uiComponents.create(GroupFilter.class);
        rootComponent.setConditionModificationDelegated(true);
        rootComponent.setOperation(LogicalFilterComponent.Operation.AND);
        rootComponent.setDataLoader(genericFilter.getDataLoader());
        rootComponent.add(nameFilter);

        immutableConfiguration =
                new TestImmutableConfiguration(CONFIGURATION_ID, CONFIGURATION_NAME, rootComponent, genericFilter);
        genericFilter.addConfiguration(immutableConfiguration);
    }
}
