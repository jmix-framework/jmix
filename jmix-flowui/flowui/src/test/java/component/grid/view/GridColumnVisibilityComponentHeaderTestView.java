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

package component.grid.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.gridcolumnvisibility.JmixGridColumnVisibility;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sec.Group;

@Route("GridColumnVisibilityComponentHeaderTestView")
@ViewController
@ViewDescriptor("grid-column-visibility-component-header-test-view.xml")
public class GridColumnVisibilityComponentHeaderTestView extends StandardView {

    @ViewComponent
    public DataGrid<Group> groupsDataGrid;
    @ViewComponent
    public JmixGridColumnVisibility dataGridColumnVisibility;
    @ViewComponent
    public TreeDataGrid<Group> groupsTreeDataGrid;
    @ViewComponent
    public JmixGridColumnVisibility treeDataGridColumnVisibility;

    @Subscribe
    public void onInit(final InitEvent event) {
        groupsDataGrid.getColumnByKey("name").setHeader(createHeader());
        groupsTreeDataGrid.getColumnByKey("name").setHeader(createHeader());
    }

    private HorizontalLayout createHeader() {
        return new HorizontalLayout(VaadinIcon.USER.create(), new Span("Group name"));
    }
}
