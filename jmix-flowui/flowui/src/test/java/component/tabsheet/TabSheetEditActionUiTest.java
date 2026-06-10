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

package component.tabsheet;

import component.tabsheet.view.TabSheetEditActionListView;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import navigation.view.BackwardNavigationDetailView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;
import test_support.entity.sales.Customer;

@UiTest(viewBasePackages = {"navigation.view", "component.tabsheet.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class TabSheetEditActionUiTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    DataManager dataManager;

    @Test
    @DisplayName("Edit action on a data grid inside a TabSheet opens the detail view")
    public void editActionOnDataGridInsideTabSheetOpensDetailView() {
        // given: a persisted customer
        Customer customer = dataManager.create(Customer.class);
        customer.setName("Joe Doe");
        dataManager.save(customer);

        // and: a list view whose data grid is placed inside a TabSheet tab
        navigationSupport.navigate(TabSheetEditActionListView.class);
        TabSheetEditActionListView listView = UiTestUtils.getCurrentView();

        DataGrid<Customer> dataGrid = UiTestUtils.getComponent(listView, "customersDataGrid");
        Customer loadedCustomer = dataGrid.getItems().getItems().iterator().next();

        // when: selecting the customer and performing the edit action;
        // EditAction resolves the owner view of the grid, which previously failed for a grid
        // inside a TabSheet tab with "A component 'DataGrid' is not attached to a view"
        dataGrid.select(loadedCustomer);
        dataGrid.getAction("edit").actionPerform(null);

        // then: the detail view is opened for the selected entity
        BackwardNavigationDetailView detailView = UiTestUtils.getCurrentView();
        Assertions.assertEquals(loadedCustomer, detailView.getEditedEntity());
    }
}
