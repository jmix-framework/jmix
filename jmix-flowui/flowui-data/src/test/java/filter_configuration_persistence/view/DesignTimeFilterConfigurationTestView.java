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
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

/**
 * A view with a design-time configuration declared in XML. Used to check that a persisted
 * configuration with the same id does not displace the one declared by the view.
 */
@Route("design-time-filter-configuration-test-view")
@ViewController("DesignTimeFilterConfigurationTestView")
@ViewDescriptor("design-time-filter-configuration-test-view.xml")
public class DesignTimeFilterConfigurationTestView extends StandardView {

    public static final String CONFIGURATION_ID = "byName";
    public static final String CONFIGURATION_NAME = "By name";

    @ViewComponent
    public GenericFilter genericFilter;
}
