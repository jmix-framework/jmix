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
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementConstants;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.loginform.EnhancedLoginForm;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class working with Vaadin UI components.
 *
 * @see com.vaadin.flow.component.ComponentUtil
 */
public final class ComponentUtils {

    private static final String SIZE_PATTERN_REGEXP =
            "^(-?\\d*(?:\\.\\d+)?)(%|cm|mm|q|in|pc|pt|px|em|ex|ch|rem|lh|rlh|vw|vh|vmin|vmax|vb|vi|svw|svh|lvw|lvh|dvw|dvh)?$";
    private static final Pattern SIZE_PATTERN = Pattern
            .compile(SIZE_PATTERN_REGEXP, Pattern.CASE_INSENSITIVE);

    private ComponentUtils() {
    }

    /**
     * Creates a new {@link Icon} instance with the icon determined by the
     * passed string. If a passed string contains ':' delimiter then a new
     * {@link Icon} is created using icon collection and icon name values,
     * otherwise the passed string is considered as {@link VaadinIcon} constant
     * name.
     *
     * @param iconString a string representing an icon
     * @return a new instance of {@link Icon} component
     */
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

    /**
     * Creates a new {@link Icon} instance with the icon determined by the passed {@link VaadinIcon}.
     *
     * @param icon a {@link VaadinIcon} instance
     * @return a new instance of {@link Icon} component
     */
    @Nullable
    public static Icon convertToIcon(@Nullable VaadinIcon icon) {
        return icon != null ? icon.create() : null;
    }


    /**
     * Creates a copy of icon component. For the moment only Icon, SvgIcon and FontIcon types are supported.
     *
     * @param icon icon component to copy
     * @return icon component copy
     */
    public static Component copyIcon(Component icon) {
        Component copy;
        if (icon instanceof Icon iconComponent) {
            copy = copyIconComponent(iconComponent);
        } else if (icon instanceof SvgIcon svgIcon) {
            copy = copySvgIcon(svgIcon);
        } else if (icon instanceof FontIcon fontIcon) {
            copy = copyFontIcon(fontIcon);
        } else {
            throw new IllegalArgumentException(icon.getClass().getSimpleName() + " is not supported");
        }
        return copy;
    }

    /**
     * Creates a copy of icon component.
     *
     * @param icon icon component to copy
     * @return icon component copy
     */
    public static Icon copyIconComponent(Icon icon) {
        String iconAttribute = icon.getElement().getAttribute("icon");
        if (iconAttribute == null) {
            throw new IllegalArgumentException("Icon component doesn't contain 'icon' attribute");
        }
        Icon copy = parseIcon(iconAttribute);

        copyAbstractIconAttributes(icon, copy);
        return copy;
    }

    private static void copyAbstractIconAttributes(AbstractIcon<?> icon, AbstractIcon<?> iconCopy) {
        iconCopy.setColor(icon.getColor());
        iconCopy.setSize(icon.getStyle().get(ElementConstants.STYLE_WIDTH));
        iconCopy.setTooltipText(icon.getTooltip().getText());
        iconCopy.setVisible(icon.isVisible());
        iconCopy.addClassNames(icon.getClassNames().toArray(new String[0]));
    }

    /**
     * Creates a copy of svg icon component.
     *
     * @param svgIcon svg icon component to copy
     * @return svg icon component copy
     */
    public static SvgIcon copySvgIcon(SvgIcon svgIcon) {
        SvgIcon copy = new SvgIcon(svgIcon.getSrc(), svgIcon.getSymbol());
        copyAbstractIconAttributes(svgIcon, copy);
        return copy;
    }

    /**
     * Creates a copy of font icon component.
     *
     * @param fontIcon font icon component to copy
     * @return font icon component copy
     */
    public static FontIcon copyFontIcon(FontIcon fontIcon) {
        FontIcon copy = new FontIcon(fontIcon.getIconClassNames());
        copy.setFontFamily(fontIcon.getFontFamily());
        copy.setCharCode(fontIcon.getCharCode());
        copy.setLigature(fontIcon.getLigature());

        copyAbstractIconAttributes(fontIcon, copy);
        return copy;
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

    /**
     * Returns the index of the first occurrence of the action with specified id
     * in the passed actions list, or -1 if this list does not contain the action.
     *
     * @param actions  actions list to search from
     * @param actionId action id to search for
     * @return the index of the first occurrence of the action with specified id
     * in the passed actions list, or -1 if this list does not contain the action
     */
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

    /**
     * Sets key set from passed {@link Map} as component's items and {@link Map}
     * values as visual representation of corresponding item, effectively defining
     * {@link ItemLabelGenerator}.
     *
     * @param component the component to set items and {@link ItemLabelGenerator}
     * @param items     a map to be used as a source of items and their visual representation
     * @param <T>       the component items type
     */
    public static <T> void setItemsMap(CheckboxGroup<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    /**
     * Sets key set from passed {@link Map} as component's items and {@link Map}
     * values as visual representation of corresponding item, effectively defining
     * {@link ItemLabelGenerator}.
     *
     * @param component the component to set items and {@link ItemLabelGenerator}
     * @param items     a map to be used as a source of items and their visual representation
     * @param <T>       the component items type
     */
    public static <T> void setItemsMap(RadioButtonGroup<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    /**
     * Sets key set from passed {@link Map} as component's items and {@link Map}
     * values as visual representation of corresponding item, effectively defining
     * {@link ItemLabelGenerator}.
     *
     * @param component the component to set items and {@link ItemLabelGenerator}
     * @param items     a map to be used as a source of items and their visual representation
     * @param <T>       the component items type
     */
    public static <T> void setItemsMap(ListBox<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    /**
     * Sets key set from passed {@link Map} as component's items and {@link Map}
     * values as visual representation of corresponding item, effectively defining
     * {@link ItemLabelGenerator}.
     *
     * @param component the component to set items and {@link ItemLabelGenerator}
     * @param items     a map to be used as a source of items and their visual representation
     * @param <T>       the component items type
     */
    public static <T> void setItemsMap(MultiSelectListBox<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    /**
     * Sets key set from passed {@link Map} as component's items and {@link Map}
     * values as visual representation of corresponding item, effectively defining
     * {@link ItemLabelGenerator}.
     *
     * @param component the component to set items and {@link ItemLabelGenerator}
     * @param items     a map to be used as a source of items and their visual representation
     * @param <T>       the component items type
     */
    public static <T> void setItemsMap(ComboBox<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    /**
     * Sets key set from passed {@link Map} as component's items and {@link Map}
     * values as visual representation of corresponding item, effectively defining
     * {@link ItemLabelGenerator}.
     *
     * @param component the component to set items and {@link ItemLabelGenerator}
     * @param items     a map to be used as a source of items and their visual representation
     * @param <T>       the component items type
     */
    public static <T> void setItemsMap(Select<T> component, Map<T, String> items) {
        setItemsMapInternal(component, items);
        component.setItemLabelGenerator(createItemLabelGenerator(items));
    }

    /**
     * Sets key set from passed {@link Map} as component's items and {@link Map}
     * values as visual representation of corresponding item, effectively defining
     * {@link ItemLabelGenerator}.
     *
     * @param component the component to set items and {@link ItemLabelGenerator}
     * @param items     a map to be used as a source of items and their visual representation
     */
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

    /**
     * Checks if a passed string represents 'auto' size. Should be in a format
     * understood by the browser, e.g. "100px" or "2.5em".
     *
     * @param size a
     * @return {@code true} if passed string represents 'auto' size, {@code false} otherwise
     */
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

    /**
     * Sets the component visibility value. If the component is wrapped
     * inside {@link FormLayout.FormItem} then its visibility value is
     * also changed.
     *
     * @param component the component to set visibility value
     * @param visible   the component visibility value
     */
    public static void setVisible(Component component, boolean visible) {
        component.setVisible(visible);

        component.getParent().ifPresent(parent -> {
            if (parent instanceof FormLayout.FormItem) {
                parent.setVisible(visible);
            }
        });
    }

    /**
     * Sets the component enabled state. If the component is wrapped
     * inside {@link FormLayout.FormItem} then its enabled state is
     * also changed.
     *
     * @param hasEnabled the component to set enabled state
     * @param enabled    the component enabled state
     */
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

    /**
     * Gets the component visibility value. Throws an exception
     * if a passed object is not a component.
     *
     * @param component the component to get visibility value
     * @return {@code true} if the component is visible, {@code false} otherwise
     * @throws IllegalArgumentException if a passed object is not a component
     */
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
