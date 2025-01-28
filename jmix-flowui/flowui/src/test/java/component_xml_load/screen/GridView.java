/*
 * Copyright 2022 Haulmont.
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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;

@Route(value = "grid-view")
@ViewController("GridView")
@ViewDescriptor("grid-view.xml")
public class GridView extends StandardView {

    @ViewComponent
    public DataGrid<?> dataGrid;

    @ViewComponent
    public DataGrid<?> anotherDataGrid;

    @ViewComponent
    public DataGrid<?> metaClassDataGrid;

    @ViewComponent
    public DataGrid<?> dataGridWithCustomContextMenu;

    @ViewComponent
    public DataGrid<?> dataGridWithCustomEmptyStateComponent;

    @ViewComponent
    public TreeDataGrid<?> treeDataGrid;

    @ViewComponent
    public TreeDataGrid<?> treeDataGridWithCustomEmptyStateComponent;

    @ViewComponent
    public TreeDataGrid<?> anotherTreeDataGrid;

    @ViewComponent
    public TreeDataGrid<?> metaClassTreeDataGrid;

    @ViewComponent
    public TreeDataGrid<?> treeDataGridWithCustomContextMenu;

    @ViewComponent
    public JmixButton emptyStateButton;

    @ViewComponent
    public JmixButton treeDataGridEmptyStateButton;

    public void loadData() {
        getViewData().loadAll();
    }

    @Subscribe("emptyStateButton")
    public void onEmptyStateButtonClick(ClickEvent<JmixButton> event) {
        loadData();
    }

    @Subscribe("treeDataGridEmptyStateButton")
    public void onTreeDataGridEmptyStateButtonClick(ClickEvent<JmixButton> event) {
        loadData();
    }
}
