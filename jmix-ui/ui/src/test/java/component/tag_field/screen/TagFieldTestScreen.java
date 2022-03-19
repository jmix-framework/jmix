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

package component.tag_field.screen;

import io.jmix.core.DataManager;
import io.jmix.ui.component.TagField;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderLine;

import javax.annotation.Nullable;

@UiController
@UiDescriptor("tag-field-test-screen.xml")
public class TagFieldTestScreen extends Screen {

    @Autowired
    public TagField<OrderLine> tagFieldOrder;

    @Autowired
    public TagField<OrderLine> tagFieldTagCreation;

    @Autowired
    public InstanceContainer<Order> orderDc;

    @Autowired
    private DataManager dataManager;

    public int valueChangeCount = 0;
    public int valueChangeCountCreation = 0;

    @Subscribe
    public void onInit(InitEvent event) {
        orderDc.setItem(dataManager.create(Order.class));

        tagFieldTagCreation.setEnterPressHandler(new TagField.NewTagProvider<OrderLine>() {
            @Nullable
            @Override
            public OrderLine create(String searchString) {
                OrderLine orderLine = dataManager.create(OrderLine.class);
                orderLine.setDescription(searchString);
                return orderLine;
            }
        });

        tagFieldOrder.addValueChangeListener(changeEvent -> valueChangeCount++);
        tagFieldTagCreation.addValueChangeListener(changeEvent -> valueChangeCountCreation++);
    }
}
