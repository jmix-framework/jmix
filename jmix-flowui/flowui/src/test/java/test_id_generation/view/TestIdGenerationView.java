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

package test_id_generation.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import test_support.entity.sales.Order;

@Route("TestIdGenerationView")
@ViewController
@ViewDescriptor("test-id-generation-view.xml")
public class TestIdGenerationView extends StandardView {

    @ViewComponent
    public JmixButton button_1;
    @ViewComponent
    public JmixButton button_2;
    @ViewComponent
    public TypedTextField<String> textField_1;
    @ViewComponent
    public TypedTextField<String> textField_2;
    @ViewComponent
    public TypedTextField<String> textField_3;
    @ViewComponent
    public DataGrid<Order> dataGrid;

    @ViewComponent
    public Action actionId;

    @Subscribe
    public void onInit(InitEvent event) {
        // remove id before attaching
        getContent().getComponents()
                .forEach(c -> c.setId(""));
    }
}
