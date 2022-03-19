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

package component.select_list.screen;

import io.jmix.core.Metadata;
import io.jmix.ui.component.MultiSelectList;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.InstancePropertyContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.model_objects.CatalogObject;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderLine;
import test_support.entity.sales.Product;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UiController
@UiDescriptor("multi-select-list-test-screen.xml")
public class MultiSelectListTestScreen extends Screen {

    @Autowired
    private Metadata metadata;

    @Autowired
    public InstanceContainer<Order> orderDc;
    @Autowired
    public CollectionContainer<OrderLine> orderLinesDc;
    @Autowired
    public CollectionContainer<OrderLine> allOrderLinesDc;

    @Autowired
    public InstanceContainer<OrderLine> orderLineDc;
    @Autowired
    public InstancePropertyContainer<Product> productDc;
    @Autowired
    public CollectionContainer<Product> allProductsDc;

    @Autowired
    public InstanceContainer<CatalogObject> catalogDc;

    @Autowired
    public MultiSelectList<OrderLine> selectList;
    @Autowired
    public MultiSelectList<OrderLine> requiredSelectList;
    @Autowired
    public MultiSelectList<Product> setSelectList;

    @Subscribe
    private void onInit(InitEvent event) {
        Order order = metadata.create(Order.class);
        orderDc.setItem(order);

        List<OrderLine> orderLines = IntStream.range(0, 5)
                .mapToObj(i -> metadata.create(OrderLine.class))
                .collect(Collectors.toList());
        allOrderLinesDc.getMutableItems().addAll(orderLines);

        OrderLine orderLine = metadata.create(OrderLine.class);
        orderLineDc.setItem(orderLine);

        List<Product> products = IntStream.range(0, 5)
                .mapToObj(i -> metadata.create(Product.class))
                .collect(Collectors.toList());
        allProductsDc.getMutableItems().addAll(products);

        CatalogObject catalog = metadata.create(CatalogObject.class);
        catalogDc.setItem(catalog);
    }
}
