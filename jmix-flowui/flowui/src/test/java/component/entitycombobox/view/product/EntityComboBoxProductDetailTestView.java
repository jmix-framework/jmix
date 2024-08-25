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

package component.entitycombobox.view.product;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import test_support.entity.sales.Product;

@Route(value = "EntityCombBoxProductDetailTestView/:id")
@ViewController("EntityCombBoxProductDetailTestView")
@ViewDescriptor("entity-combo-box-product-detail-test-view.xml")
@EditedEntityContainer("productDc")
public class EntityComboBoxProductDetailTestView extends StandardDetailView<Product> {
    @ViewComponent
    public TypedTextField<String> nameField;
    @ViewComponent
    public JmixButton saveAndCloseButton;
}