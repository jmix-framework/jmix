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

package component_xml_load.screen;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sales.Order;

@Route(value = "data-grid-header-footer-rows-view")
@ViewController("DataGridHeaderFooterRowsView")
@ViewDescriptor("data-grid-header-footer-rows-view.xml")
public class DataGridHeaderFooterRowsView extends StandardView {

    @ViewComponent
    public DataGrid<Order> headerRowsDataGrid;
    @ViewComponent
    public DataGrid<Order> keyColumnsDataGrid;
    @ViewComponent
    public DataGrid<Order> footerRowsDataGrid;
    @ViewComponent
    public DataGrid<Order> columnFootersDataGrid;
    @ViewComponent
    public DataGrid<Order> nestedGroupsDataGrid;
    @ViewComponent
    public DataGrid<Order> componentCellsDataGrid;
    @ViewComponent
    public DataGrid<Order> filterableDataGrid;
    @ViewComponent
    public DataGrid<Order> topAggregationDataGrid;
    @ViewComponent
    public DataGrid<Order> bottomAggregationDataGrid;
    @ViewComponent
    public TreeDataGrid<Order> headerRowsTreeDataGrid;
    @ViewComponent
    public TypedTextField<String> numberFilterField;
    @ViewComponent
    public JmixButton footerButton;
    @ViewComponent
    public CollectionLoader<Order> ordersDl;

    public String numberFilterValue;
    public boolean footerButtonClicked;

    @Subscribe("numberFilterField")
    public void onNumberFilterFieldValueChange(ComponentValueChangeEvent<TypedTextField<String>, String> event) {
        numberFilterValue = event.getValue();
    }

    @Subscribe("footerButton")
    public void onFooterButtonClick(ClickEvent<JmixButton> event) {
        footerButtonClicked = true;
    }
}
