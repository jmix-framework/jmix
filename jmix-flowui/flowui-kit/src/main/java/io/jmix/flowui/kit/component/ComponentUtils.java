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

package io.jmix.flowui.kit.component;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.dom.Element;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.loginform.EnhancedLoginForm;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ComponentUtils {

    private ComponentUtils() {
    }

    public static Icon parseIcon(String iconString) {
        if (iconString.contains(":")) {
            String[] parts = iconString.split(":");
            if (parts.length != 2) {
                throw new IllegalStateException("Unexpected number of icon parts, must be two");
            }

            return new Icon(parts[0], parts[1]);
        } else {
            VaadinIcon vaadinIcon = VaadinIcon.valueOf(iconString);
            return convertToIcon(vaadinIcon);
        }
    }

    @Nullable
    public static Icon convertToIcon(@Nullable VaadinIcon icon) {
        return icon != null ? icon.create() : null;
    }

    /**
     * @param element    the parent component element to add the components to
     * @param slot       the name of the slot inside the parent
     * @param components components to add to the specified slot.
     * @deprecated {@link com.vaadin.flow.component.shared.SlotUtils#addToSlot(HasElement, String, Component...)} instead
     */
    @Deprecated
    public static void addComponentsToSlot(Element element, String slot, Component... components) {
        for (Component component : components) {
            component.getElement().setAttribute("slot", slot);
            element.appendChild(component.getElement());
        }
    }

    /**
     * @param element the component element to get children from
     * @param slot    the name of the slot inside the parent
     * @deprecated use {@link com.vaadin.flow.component.shared.SlotUtils#clearSlot(HasElement, String)} instead
     */
    @Deprecated
    public static void clearSlot(Element element, String slot) {
        element.getChildren()
                .filter(child -> slot.equals(child.getAttribute("slot")))
                .forEach(element::removeChild);
    }

    public static int findActionIndexById(List<Action> actions, String actionId) {
        int index = -1;
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (Objects.equals(a.getId(), actionId)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public static <T> void setItemsMap(CheckboxGroup<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    public static <T> void setItemsMap(RadioButtonGroup<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    public static <T> void setItemsMap(ComboBox<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    public static <T> void setItemsMap(Select<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    public static void setItemsMap(EnhancedLoginForm component, Map<Locale, String> items) {
        component.setLocaleItems(items.keySet());
        component.setLocaleItemLabelGenerator(createItemLabelGenerator(items));
    }

    private static <T> void setItemsMapInternal(HasListDataView<T, ?> component, Map<T, String> items) {
        component.setItems(items.keySet());
    }

    private static <T> ItemLabelGenerator<T> createItemLabelGenerator(Map<T, String> items) {
        return item -> items.getOrDefault(item, item != null ? String.valueOf(item) : "");
    }

    public static boolean isAutoSize(@Nullable String size) {
        // TODO: gg, implement
        return false;
    }

    public static boolean isVisible(Object component) {
        Preconditions.checkArgument(component != null, "Passed object is null");

        if (component instanceof Component) {
            return ((Component) component).isVisible();
        } else {
            throw new IllegalArgumentException("Passed object is not a component: " + component.getClass().getName());
        }
    }
}
