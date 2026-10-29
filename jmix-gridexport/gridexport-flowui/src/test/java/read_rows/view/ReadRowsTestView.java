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

package read_rows.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.Product;

/**
 * Shows products in a multi-select data grid with export actions, to read its rows the way the exporters see them.
 */
@Route("read-rows-test-view")
@ViewController("ReadRowsTestView")
@ViewDescriptor("read-rows-test-view.xml")
public class ReadRowsTestView extends StandardView {

    @ViewComponent
    protected DataGrid<Product> productsDataGrid;

    /**
     * Adds a column without a meta property, rendered by a component, as the last column.
     */
    @Subscribe
    public void onInit(InitEvent event) {
        productsDataGrid.addColumn(new ComponentRenderer<>(product -> new Span("Item " + product.getPosition())))
                .setKey("label")
                .setHeader("Label");
    }
}
