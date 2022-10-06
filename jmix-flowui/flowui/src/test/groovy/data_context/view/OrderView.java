/*
 * Copyright 2022 Haulmont.
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

package data_context.view;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.builder.DetailWindowClassBuilder;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderLine;

import java.util.UUID;

@Route(value = "orders")
@ViewController
@ViewDescriptor("order-view.xml")
public class OrderView extends StandardView implements HasUrlParameter<String> {

    private static final Logger log = LoggerFactory.getLogger(OrderView.class);

    @Autowired
    private DataComponents dataComponents;

    @ViewComponent
    private DataContext dataContext;

    @ViewComponent
    private InstanceContainer<Order> orderDc;

    @ViewComponent
    private CollectionContainer<OrderLine> linesDc;

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private Metadata metadata;

    @ViewComponent
    private DataGrid<OrderLine> itemsDataGrid;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private UrlParamSerializer urlParamSerializer;

    private Order order;

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        Object orderId = urlParamSerializer.deserialize(UUID.class, parameter);
        order = dataManager.load(Order.class).id(orderId).one();
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        log.debug("onInit: dataContext={}", dataContext);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        Order mergedOrder = dataContext.merge(order);
        orderDc.setItem(mergedOrder);
    }

    public LineView buildLineScreenForCreate(boolean explicitParentDc) {
        DetailWindowClassBuilder<OrderLine, LineView> builder = dialogWindows.detail(itemsDataGrid)
                .withViewClass(LineView.class)
                .newEntity();
        if (explicitParentDc) {
            builder.withParentDataContext(getViewData().getDataContext());
        }
        return builder.open().getView();
    }

    public LineView buildLineScreenForEdit(boolean explicitParentDc) {
        itemsDataGrid.select(linesDc.getItems().get(0));
        DetailWindowClassBuilder<OrderLine, LineView> builder = dialogWindows.detail(itemsDataGrid)
                .withViewClass(LineView.class);
        if (explicitParentDc) {
            builder.withParentDataContext(getViewData().getDataContext());
        }
        return builder.open().getView();
    }
}
