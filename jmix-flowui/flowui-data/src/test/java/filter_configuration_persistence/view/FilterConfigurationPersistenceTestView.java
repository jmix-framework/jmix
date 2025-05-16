/*
 * Copyright 2025 Haulmont.
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
import io.jmix.flowui.view.*;

@Route("filter-configuration-persistence-test-view")
@ViewController("FilterConfigurationPersistenceTestView")
@ViewDescriptor("filter-configuration-persistence-test-view.xml")
public class FilterConfigurationPersistenceTestView extends StandardView {

    @ViewComponent
    public GenericFilter genericFilter;
}
