/*
 * Copyright 2026 Haulmont.
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

package component.fragment.component;

import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.ViewComponent;
import test_support.entity.sales.Product;

@FragmentDescriptor("test-parameters-fragment.xml")
public class TestParametersFragment extends Fragment<FormLayout> {

    @ViewComponent
    private EntityComboBox<Product> entityField;
    @ViewComponent
    private TypedTextField<String> textField;

    public void setContainer(CollectionContainer<Product> container) {
        entityField.setItems(container);
    }

    public void setPlaceholder(String placeholder) {
        textField.setPlaceholder(placeholder);
    }
}