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

package component.entitycombobox.view.product;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import test_support.entity.sales.Product;


@Route(value = "EntityComboBoxProductListTestView")
@ViewController("EntityComboBoxProductListTestView")
@ViewDescriptor("entity-combo-box-product-list-test-view.xml")
@LookupComponent("productsDataGrid")
@DialogMode(width = "64em")
public class EntityComboBoxProductListTestView extends StandardListView<Product> {
    @ViewComponent
    public JmixButton selectButton;
    @ViewComponent
    public CollectionContainer<Product> productsDc;
    @ViewComponent
    public DataGrid<Product> productsDataGrid;
}