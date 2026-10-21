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

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.City;
import test_support.entity.sales.Customer;
import test_support.entity.sales.Order;
import test_support.entity.sec.Group;

@Route("grid-editor-component-view")
@ViewController("GridEditorComponentView")
@ViewDescriptor("grid-editor-component-view.xml")
public class GridEditorComponentView extends StandardView {

    @ViewComponent
    public DataGrid<Customer> customersDataGrid;
    @ViewComponent
    public DataGrid<Customer> bufferedCustomersDataGrid;
    @ViewComponent
    public DataGrid<Customer> customersSelectDataGrid;
    @ViewComponent
    public DataGrid<Customer> editableCustomersDataGrid;
    @ViewComponent
    public DataGrid<Customer> includeAllCustomersDataGrid;
    @ViewComponent
    public DataGrid<Order> ordersDataGrid;
    @ViewComponent
    public TreeDataGrid<Group> groupsTreeDataGrid;
    @ViewComponent
    public DataGrid<City> citiesDataGrid;

    @ViewComponent
    public CollectionContainer<Customer> customersDc;
    @ViewComponent
    public CollectionContainer<Order> ordersDc;
    @ViewComponent
    public CollectionContainer<Group> groupsDc;
    @ViewComponent
    public CollectionContainer<City> citiesDc;
}
