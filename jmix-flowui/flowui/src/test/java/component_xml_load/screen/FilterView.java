/*
 * Copyright 2023 Haulmont.
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

package component_xml_load.screen;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.jpqlfilter.JpqlFilter;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "filter-view")
@ViewController("FilterView")
@ViewDescriptor("filter-view.xml")
public class FilterView extends StandardView {

    @ViewComponent
    public PropertyFilter<String> propertyFilterId;

    @ViewComponent
    public PropertyFilter<String> propertyFilterWithCustomValueComponentId;

    @ViewComponent
    public JpqlFilter<Void> jpqlFilterId;

    @ViewComponent
    public JpqlFilter<String> jpqlFilterWithCustomValueComponentId;

    @ViewComponent
    public GroupFilter groupFilterId;
}
