/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.component.menusearchfield;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.textfield.HasAutocapitalize;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.HasAutofocus;
import io.jmix.flowui.kit.component.HasPlaceholder;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.menu.MenuItem;
import io.jmix.flowui.kit.component.menu.ParentMenuItem;
import io.jmix.flowui.menu.provider.MenuItemProvider;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * A component which allows to filter items of a menu (for example, {@link io.jmix.flowui.kit.component.main.ListMenu})
 * by their text labels using entered value
 */
public class MenuSearchField extends Composite<TextField>
        implements HasHelper, HasPrefix, HasSuffix, HasValueChangeMode, HasAutocapitalize,
        HasThemeVariant<MenuSearchFieldVariant>, HasTitle, HasClearButton, HasAutofocus, HasEnabled, HasLabel, HasSize,
        HasStyle, HasTooltip, InputNotifier, KeyNotifier, HasAriaLabel, Focusable<MenuSearchField>, HasPlaceholder,
        HasValueAndElement<ComponentValueChangeEvent<MenuSearchField, String>, String> {

    protected MenuItemProvider<?> menuItemProvider;
    protected MenuItemsTransformer<?> itemsTransformer;

    @Override
    protected TextField initContent() {
        TextField field = super.initContent();
        field.addValueChangeListener(this::onValueChange);

        return field;
    }

    protected void onValueChange(HasValue.ValueChangeEvent<String> event) {
        if (menuItemProvider != null) {
            itemsTransformer.setSearchString(event.getValue());
            menuItemProvider.load();
        }
    }

    /**
     * @return menu item provider used to filter menu items
     */
    @Nullable
    public MenuItemProvider<?> getMenuItemProvider() {
        return menuItemProvider;
    }

    /**
     * Sets menu item provider which will be used to filter menu items
     *
     * @param menuItemProvider menu item provider to set
     * @param <T>              menu item type
     */
    public <T extends MenuItem> void setMenuItemProvider(@Nullable MenuItemProvider<T> menuItemProvider) {
        this.menuItemProvider = menuItemProvider;
        if (menuItemProvider != null) {
            MenuItemsTransformer<T> itemsTransformer = new MenuItemsTransformer<>();
            menuItemProvider.addMenuItemsTransformer(itemsTransformer);
            this.itemsTransformer = itemsTransformer;
        } else {
            this.itemsTransformer = null;
        }
    }

    @Override
    public ValueChangeMode getValueChangeMode() {
        return getContent().getValueChangeMode();
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        getContent().setValueChangeMode(valueChangeMode);
    }

    @Override
    public void setValue(String value) {
        getContent().setValue(value);
    }

    @Override
    public String getValue() {
        return getContent().getValue();
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<? super ComponentValueChangeEvent<MenuSearchField, String>> listener) {
        return getContent().addValueChangeListener(e -> listener.valueChanged(convertValueChangedEvent(e)));
    }

    protected ComponentValueChangeEvent<MenuSearchField, String> convertValueChangedEvent(
            AbstractField.ComponentValueChangeEvent<TextField, String> e) {
        return new ComponentValueChangeEvent<>(this, this, e.getOldValue(), e.isFromClient());
    }

    /**
     * @return true/false if auto select is active/inactive
     */
    public boolean isAutoselect() {
        return getContent().isAutoselect();
    }

    /**
     * Set to true to always have the field value automatically
     * selected when the field gains focus, false otherwise.
     *
     * @param autoselect true/false to set auto select on/off
     */
    public void setAutoselect(boolean autoselect) {
        getContent().setAutoselect(autoselect);
    }

    protected static class MenuItemsTransformer<T extends MenuItem> implements Function<List<T>, List<T>> {

        protected String searchString;

        public String getSearchString() {
            return searchString;
        }

        public void setSearchString(String searchString) {
            this.searchString = searchString;
        }

        @Override
        public List<T> apply(List<T> menuItems) {
            if (Strings.isNullOrEmpty(searchString)) {
                return menuItems;
            }
            Iterator<T> iterator = menuItems.iterator();
            while (iterator.hasNext()) {
                T rootItem = iterator.next();
                boolean match = filterItemRecursive(rootItem);
                if (!match) {
                    iterator.remove();
                }
            }
            return menuItems;
        }

        protected boolean filterItemRecursive(MenuItem item) {
            boolean anyChildMatch = false;

            boolean selfMatch = testItemMatch(item);

            if (item instanceof ParentMenuItem<? extends MenuItem> parentMenuItem) {
                anyChildMatch = filterChildren(parentMenuItem, selfMatch);
            }
            boolean match = selfMatch || anyChildMatch;
            if (match) {
                transformMatchingItem(item);
            }
            return match;
        }

        protected boolean testItemMatch(MenuItem item) {
            return StringUtils.contains(item.getLabel(), searchString);
        }

        protected <C extends MenuItem> boolean filterChildren(ParentMenuItem<C> parentMenuItem,
                                                              boolean forceAddChildren) {
            boolean anyChildMatch = false;

            List<C> childItems = new ArrayList<>(parentMenuItem.getChildren());
            parentMenuItem.removeAllChildItems();
            for (C childItem : childItems) {
                boolean childMatch = filterItemRecursive(childItem);

                if (childMatch) {
                    anyChildMatch = true;
                }

                if (childMatch || forceAddChildren) {
                    parentMenuItem.addChildItem(childItem);
                }
            }
            return anyChildMatch;
        }

        protected void transformMatchingItem(MenuItem item) {
            if (item instanceof ParentMenuItem<?> parentMenuItem) {
                parentMenuItem.setExpanded(true);
            }
        }
    }
}
