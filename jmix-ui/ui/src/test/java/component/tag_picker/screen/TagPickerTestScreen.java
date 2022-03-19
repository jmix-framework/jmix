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

package component.tag_picker.screen;

import io.jmix.core.Metadata;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TagPicker;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderLine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UiController
@UiDescriptor("tag-picker-test-screen.xml")
public class TagPickerTestScreen extends Screen {

    @Autowired
    public InstanceContainer<Order> orderDc;

    @Autowired
    public CollectionContainer<OrderLine> optOrderLinesDc;

    @Autowired
    public TagPicker<OrderLine> entityTagPicker;

    @Autowired
    public TagPicker<String> requiredTagPicker;

    @Autowired
    public TagPicker<String> tagPicker;

    @Autowired
    public Metadata metadata;

    public int entityTagValueChangeCount = 0;
    public int containerPropertyChangeCount = 0;

    public int datatypeTagValueChangeCount = 0;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        orderDc.setItem(metadata.create(Order.class));

        List<String> datatypeOptions = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            optOrderLinesDc.getMutableItems().add(metadata.create(OrderLine.class));
            datatypeOptions.add("Option - " + i);
        }

        tagPicker.setOptionsList(datatypeOptions);
    }

    @Subscribe("entityTagPicker")
    public void onEntityTagPickerValueChange(HasValue.ValueChangeEvent<Collection<OrderLine>> event) {
        entityTagValueChangeCount++;
    }

    @Subscribe(id = "orderDc", target = Target.DATA_CONTAINER)
    public void onOrderDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Order> event) {
        if ("orderLines".equals(event.getProperty())) {
            containerPropertyChangeCount++;
        }
    }

    @Subscribe("tagPicker")
    public void onTagPickerValueChange(HasValue.ValueChangeEvent<String> event) {
        datatypeTagValueChangeCount++;
    }
}
