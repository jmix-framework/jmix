/*
 * Copyright 2024 Haulmont.
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
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.component.listbox.JmixMultiSelectListBox;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.view.*;

@Route("fragment-renderer-view")
@ViewController
@ViewDescriptor("fragment-renderer-view.xml")
public class FragmentRendererView extends StandardView {

    @ViewComponent
    private DataGrid<?> dataGrid;

    @ViewComponent
    public JmixCheckboxGroup<?> checkboxGroupId;

    @ViewComponent
    public JmixRadioButtonGroup<?> radioButtonGroupId;

    @ViewComponent
    public JmixSelect<?> selectId;

    @ViewComponent
    public JmixListBox<?> listBoxId;

    @ViewComponent
    public JmixMultiSelectListBox<?> multiSelectListBoxId;

    public DataGridColumn<?> dataGridColumnId;

    @Subscribe
    public void onInit(InitEvent event) {
        dataGridColumnId = dataGrid.getColumnByKey("dataGridColumnId");
    }
}
