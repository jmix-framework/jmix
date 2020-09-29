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
import io.jmix.ui.component.SingleSelectList;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderLine;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UiController
@UiDescriptor("single-select-list-test-screen.xml")
public class SingleSelectListTestScreen extends Screen {

    @Autowired
    protected Metadata metadata;

    @Autowired
    public CollectionContainer<Order> ordersDc;

    @Autowired
    public InstanceContainer<OrderLine> orderLineDc;

    @Autowired
    public SingleSelectList<Order> singleSelectList;

    @Subscribe
    public void onInit(InitEvent event) {
        orderLineDc.setItem(metadata.create(OrderLine.class));

        List<Order> orders = IntStream.range(0, 2)
                .mapToObj(i -> metadata.create(Order.class))
                .collect(Collectors.toList());
        ordersDc.setItems(orders);
    }
}
