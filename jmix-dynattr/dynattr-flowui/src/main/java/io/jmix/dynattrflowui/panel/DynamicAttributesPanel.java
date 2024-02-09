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

package io.jmix.dynattrflowui.panel;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.dynattr.model.Category;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.model.InstanceContainer;

import javax.annotation.Nullable;
import java.util.List;

public interface DynamicAttributesPanel extends HasSize, HasComponents {

    VerticalLayout getContent();

    VerticalLayout getRootLayout();

    void setVisibility(boolean visible);

    ComboBox<Category> getCategoryField();

    JmixFormLayout getDynAttrForm();

    List<Component> getDynAttrFormFields();

    @Nullable
    Category getDefaultCategory();

    void setInstanceContainer(InstanceContainer<Object> container);

    void setCategoryFieldVisible(boolean visible);

    boolean isValid();
}
