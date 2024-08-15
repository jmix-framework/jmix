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

package component.multiselectcomboboxpicker.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderLine;

@Route(value = "orders/:id")
@ViewController("JmixMultiSelectComboBoxPickerOrderDetailTestView")
@ViewDescriptor("multiselectcomboboxpicker-order-detail-test-view.xml")
@EditedEntityContainer("orderDc")
public class JmixMultiSelectComboBoxPickerOrderDetailTestView extends StandardDetailView<Order> {
    @ViewComponent
    public CollectionContainer<OrderLine> orderLinesDc;
    @ViewComponent
    public TypedTextField<String> numberField;
    @ViewComponent
    public JmixMultiSelectComboBoxPicker<OrderLine> orderLinesField;

    public OrderLine getOrderLineByDescription(String description) {
        return orderLinesDc.getItems().stream()
                .filter(ol -> description.equals(ol.getDescription()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find OrderLine by description:" + description));
    }
}