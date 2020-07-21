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

package spec.haulmont.cuba.web.components.optionslist.screens;

import com.haulmont.cuba.core.model.sales.Catalog;
import com.haulmont.cuba.core.model.sales.Order;
import com.haulmont.cuba.core.model.sales.OrderLine;
import com.haulmont.cuba.core.model.sales.Product;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.OptionsList;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Metadata;
import io.jmix.ui.screen.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OptionsListTestWindow extends AbstractWindow {

    @Autowired
    private Metadata metadata;

    @Autowired
    public Datasource<Order> orderDs;
    @Autowired
    public CollectionDatasource<OrderLine, UUID> orderLinesDs;
    @Autowired
    public CollectionDatasource<OrderLine, UUID> allOrderLinesDs;

    @Autowired
    public Datasource<OrderLine> orderLineDs;
    @Autowired
    public Datasource<Product> productDs;
    @Autowired
    public CollectionDatasource<Product, UUID> allProductsDs;

    @Autowired
    public Datasource<Catalog> catalogDs;

    @Autowired
    public OptionsList<List<OrderLine>, OrderLine> optionsList;
    @Autowired
    public OptionsList<Product, Product> singleOptionsList;
    @Autowired
    public OptionsList<Set<Product>, Product> setOptionsList;

    @Subscribe
    private void onInit(InitEvent event) {
        Order order = metadata.create(Order.class);
        order.setOrderLines(new ArrayList<>());
        orderDs.setItem(order);

        for (int i = 0; i < 5; i++) {
            allOrderLinesDs.addItem(metadata.create(OrderLine.class));
        }

        OrderLine orderLine = metadata.create(OrderLine.class);
        orderLineDs.setItem(orderLine);

        for (int i = 0; i < 5; i++) {
            allProductsDs.addItem(metadata.create(Product.class));
        }

        Catalog catalog = metadata.create(Catalog.class);
        catalogDs.setItem(catalog);
    }
}
