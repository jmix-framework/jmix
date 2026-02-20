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

package component.side_panel_layout.view;

import com.vaadin.flow.router.Route;
import component.fragment.component.TestAddressFragment;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import test_support.entity.City;

@Route("side-panel-layout-injection-test-view")
@ViewController("SidePanelLayoutInjectionTestView")
@ViewDescriptor("side-panel-layout-injection-test-view.xml")
public class SidePanelLayoutInjectionTestView extends StandardView {

    @ViewComponent
    public DataGrid<City> citiesDataGrid;

    @ViewComponent
    public JmixButton closeButton;

    @ViewComponent
    public TestAddressFragment testAddressFragment;
}
