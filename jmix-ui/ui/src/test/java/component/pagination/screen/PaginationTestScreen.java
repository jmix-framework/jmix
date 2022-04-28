/*
 * Copyright 2020 Haulmont.
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

package component.pagination.screen;

import com.vaadin.ui.ComboBox;
import io.jmix.ui.component.Pagination;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.ui.widget.JmixPagination;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Customer;
import test_support.entity.sales.Order;

@UiController
@UiDescriptor("pagination-test-screen.xml")
public class PaginationTestScreen extends Screen {

    @Autowired
    public Pagination pagination;

    @Autowired
    public Pagination paginationWithoutDataSource;

    @Autowired
    public Pagination paginationCustomOptions;

    @Autowired
    public Pagination paginationEntityPageSize;

    @Autowired
    public Pagination paginationItemsPerPageDefaultValue;

    @Autowired
    public Pagination paginationItemsPerPageDefaultValueCB;

    @Autowired
    public Pagination paginationItemsPerPageDefaultValueCBandCO;

    @Autowired
    public Pagination paginationRemoveLastItem;

    @Autowired
    public Pagination paginationEmptyContainer;

    @Autowired
    public CollectionContainer<Customer> customersRemoveLastItemCODc;

    @Autowired
    public CollectionContainer<Order> ordersEmptyContainerDc;

    public ComboBox<Integer> getComboBoxFromDefaultValueCB() {
        return paginationItemsPerPageDefaultValueCB
                .unwrap(JmixPagination.class).getItemsPerPageComboBox();
    }

    public ComboBox<Integer> getComboBoxFromDefaultValueCBandCO() {
        return paginationItemsPerPageDefaultValueCBandCO
                .unwrap(JmixPagination.class).getItemsPerPageComboBox();
    }
}
