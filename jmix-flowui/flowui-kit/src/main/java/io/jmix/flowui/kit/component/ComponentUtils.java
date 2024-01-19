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
import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ComponentUtils {

    private static final String SIZE_PATTERN_REGEXP =
            "^(-?\\d*(?:\\.\\d+)?)(%|cm|mm|q|in|pc|pt|px|em|ex|ch|rem|lh|rlh|vw|vh|vmin|vmax|vb|vi|svw|svh|lvw|lvh|dvw|dvh)?$";
    private static final Pattern SIZE_PATTERN = Pattern
            .compile(SIZE_PATTERN_REGEXP, Pattern.CASE_INSENSITIVE);

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
    @Deprecated(since = "2.1", forRemoval = true)
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
    @Deprecated(since = "2.1", forRemoval = true)
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

    public static <T> void setItemsMap(ListBox<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    public static <T> void setItemsMap(MultiSelectListBox<T> component, Map<T, String> items) {
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
        // CSS attribute is removed if null or empty value passed,
        // so default 'auto' size will be used.
        if (Strings.isNullOrEmpty(size)) {
            return true;
        }
        String trimmedSize = size.trim();
        // If size is 'auto' value.
        if ("auto".equalsIgnoreCase(trimmedSize)) {
            return true;
        }
        // Check that size has correct value: numbers and supported unit size.
        // If it does not, CSS will use 'auto' value.
        Matcher matcher = SIZE_PATTERN.matcher(trimmedSize);
        if (!matcher.find()) {
            // Size is incorrect, will be used 'auto' value.
            return true;
        }
        String sizeValue = matcher.group(1);
        String sizeUnit = matcher.group(2);
        // If at least one group is null or empty, size is incorrect
        return Strings.isNullOrEmpty(sizeValue) || Strings.isNullOrEmpty(sizeUnit);
    }

    public static void setVisible(Component component, boolean visible) {
        component.setVisible(visible);

        component.getParent().ifPresent(parent -> {
            if (parent instanceof FormLayout.FormItem) {
                parent.setVisible(visible);
            }
        });
    }

    public static void setEnabled(HasEnabled hasEnabled, boolean enabled) {
        hasEnabled.setEnabled(enabled);

        if (hasEnabled instanceof Component component) {
            component.getParent().ifPresent(parent -> {
                if (parent instanceof FormLayout.FormItem formItem) {
                    formItem.setEnabled(enabled);
                }
            });
        }
    }

    public static boolean isVisible(Object component) {
        Preconditions.checkArgument(component != null, "Passed object is null");

        if (component instanceof Component) {
            return ((Component) component).isVisible();
        } else {
            throw new IllegalArgumentException("Passed object is not a component: " + component.getClass().getName());
        }
    }

    /**
     * Adds a shortcut which 'clicks' the {@link Component} which implements
     * {@link ClickNotifier} interface.
     *
     * @param component           a component to add shortcut
     * @param shortcutCombination an object that stores information about key,
     *                            modifiers and additional settings
     * @return {@link ShortcutRegistration} for configuring the shortcut and removing
     */
    public static ShortcutRegistration addClickShortcut(ClickNotifier<?> component,
                                                        KeyCombination shortcutCombination) {
        ShortcutRegistration shortcutRegistration = component.addClickShortcut(shortcutCombination.getKey(),
                shortcutCombination.getKeyModifiers());
        shortcutRegistration.setResetFocusOnActiveElement(shortcutCombination.isResetFocusOnActiveElement());

        if (shortcutCombination.getListenOnComponents() != null) {
            shortcutRegistration.listenOn(shortcutCombination.getListenOnComponents());
        }

        return shortcutRegistration;
    }
}
