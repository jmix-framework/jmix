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

package facet.editor_screen.screen;


import io.jmix.core.Metadata;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.EditorScreenFacet;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;
import test_support.entity.sales.screen.OrderEdit;

import java.math.BigDecimal;

@UiController
@UiDescriptor("editor-screen-facet-test-screen.xml")
public class EditorScreenFacetTestScreen extends Screen {

    @Autowired
    public Metadata metadata;

    @Autowired
    public CollectionContainer<Order> ordersDc;

    @Autowired
    public EntityPicker<Order> orderField;
    @Autowired
    public Table<Order> ordersTable;

    @Autowired
    public Action action;
    @Autowired
    public Button button;

    @Autowired
    public EditorScreenFacet<Order, OrderEdit> editorScreenFacet;
    @Autowired
    public EditorScreenFacet<Order, OrderEdit> tableScreenFacet;
    @Autowired
    public EditorScreenFacet<Order, OrderEdit> fieldScreenFacet;
    @Autowired
    public EditorScreenFacet<Order, OrderEdit> editorEntityProvider;

    @Install(to = "editorScreenFacet", subject = "entityProvider")
    public Order provideEntity() {
        Order order = metadata.create(Order.class);
        order.setNumber("Test order");
        return order;
    }

    @Install(to = "editorEntityProvider", subject = "entityProvider")
    public Order provideOrder() {
        Order order = metadata.create(Order.class);
        order.setNumber("Test order");
        return order;
    }

    @Install(to = "editorScreenFacet", subject = "initializer")
    public void initEntity(Order order) {
        order.setAmount(new BigDecimal(10));
    }
}
