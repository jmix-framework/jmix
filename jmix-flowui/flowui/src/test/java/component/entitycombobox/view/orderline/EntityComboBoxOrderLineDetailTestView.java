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

package component.entitycombobox.view.orderline;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.entitypicker.EntityOpenAction;
import io.jmix.flowui.action.valuepicker.ValueClearAction;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.view.*;
import test_support.entity.sales.OrderLine;
import test_support.entity.sales.Product;

@Route(value = "EntityComboBoxOrderLineDetailTestView/:id")
@ViewController("EntityComboBoxOrderLineDetailTestView")
@ViewDescriptor("entity-combo-box-order-line-detail-test-view.xml")
@EditedEntityContainer("orderLineDc")
public class EntityComboBoxOrderLineDetailTestView extends StandardDetailView<OrderLine> {
    @ViewComponent
    public EntityComboBox<Product> productField;
    @ViewComponent("productField.entityLookup")
    public EntityLookupAction<Product> productFieldEntityLookup;
    @ViewComponent("productField.entityOpen")
    public EntityOpenAction<Product> productFieldEntityOpen;
    @ViewComponent("productField.entityClear")
    public EntityClearAction<Product> productFieldEntityClear;
    @ViewComponent("productField.valueClear")
    public ValueClearAction<Product> productFieldValueClear;

    public int productValueChangeCount = 0;

    @Subscribe("productField")
    public void onProductFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<Product>, Product> event) {
        productValueChangeCount++;
    }
}