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
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
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
        implements HasHelper, HasPrefix, HasSuffix, HasValueChangeMode, HasThemeVariant<MenuSearchFieldVariant>,
        HasTitle, HasClearButton, HasAutofocus, HasEnabled, HasLabel, HasSize, HasStyle, HasTooltip,
        InputNotifier, KeyNotifier, HasAriaLabel, Focusable<MenuSearchField>, HasPlaceholder {

    protected static final String SEARCH_FIELD_STYLE_NAME = "jmix-menu-search-field";

    protected MenuItemProvider<?> menuItemProvider;
    protected MenuItemsTransformer<?> itemsTransformer;

    protected SearchMode searchMode = SearchMode.CASE_INSENSITIVE;

    @Override
    protected TextField initContent() {
        TextField field = super.initContent();
        field.addValueChangeListener(this::onValueChange);
        field.addClassName(SEARCH_FIELD_STYLE_NAME);

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
            MenuItemsTransformer<T> itemsTransformer = new MenuItemsTransformer<>(searchMode);
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
    public void setValueChangeTimeout(int valueChangeTimeout) {
        getContent().setValueChangeTimeout(valueChangeTimeout);
    }

    @Override
    public int getValueChangeTimeout() {
        return getContent().getValueChangeTimeout();
    }

    /**
     * Sets new search string value.
     *
     * @param value string to search
     */
    public void setValue(@Nullable String value) {
        getContent().setValue(value);
    }

    /**
     * @return current search string value
     */
    @Nullable
    public String getValue() {
        return getContent().getValue();
    }

    /**
     * Sets read-only mode of the field
     *
     * @param readOnly true if read-only mode should be enabled, false otherwise
     */
    public void setReadOnly(boolean readOnly) {
        getElement().setProperty("readonly", readOnly);
    }

    /**
     * @return true if the field is in read-only mode, false otherwise
     */
    public boolean isReadOnly() {
        return getElement().getProperty("readonly", false);
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

    /**
     * @return field search mode
     * @see SearchMode
     */
    public SearchMode getSearchMode() {
        return searchMode;
    }

    /**
     * Sets search mode.
     *
     * @param searchMode search mode to set
     * @see SearchMode
     */
    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
        if (itemsTransformer != null) {
            itemsTransformer.setSearchMode(searchMode);
        }
    }

    protected static class MenuItemsTransformer<T extends MenuItem> implements Function<List<T>, List<T>> {

        protected String searchString;
        protected SearchMode searchMode;

        public MenuItemsTransformer(SearchMode searchMode) {
            this.searchMode = searchMode;
        }

        public String getSearchString() {
            return searchString;
        }

        public void setSearchString(String searchString) {
            this.searchString = searchString;
        }

        public SearchMode getSearchMode() {
            return searchMode;
        }

        public void setSearchMode(SearchMode searchMode) {
            this.searchMode = searchMode;
        }

        @Override
        public List<T> apply(List<T> menuItems) {
            if (Strings.isNullOrEmpty(searchString)) {
                return menuItems;
            }
            Iterator<T> iterator = menuItems.iterator();
            while (iterator.hasNext()) {
                T rootItem = iterator.next();
                boolean match = filterItemRecursive(rootItem, false);
                if (!match) {
                    iterator.remove();
                }
            }
            return menuItems;
        }

        /**
         * Tests menu item and filters its children recursively by title.
         *
         * @param item                   item to filter
         * @param forceRetainAllChildren flag indicating whether to retain all children of this item or not.
         * @return true/false if the item matches/doesn't match the condition (if title contains search string
         * or any child matches this condition)
         */
        protected boolean filterItemRecursive(MenuItem item, boolean forceRetainAllChildren) {
            //test item itself
            boolean selfMatch = testItemMatch(item);

            boolean match;
            if (item instanceof ParentMenuItem<? extends MenuItem> parentMenuItem) {
                //test item children and filter them by title
                //or retain all children if this item matches the condition or force flag is enabled externally
                boolean anyChildMatch =
                        filterChildren(parentMenuItem, selfMatch || forceRetainAllChildren);

                match = selfMatch || anyChildMatch;
                if (match) {
                    parentMenuItem.setOpened(true);
                }
            } else {
                match = selfMatch;
            }
            return match;
        }

        protected boolean testItemMatch(MenuItem item) {
            String title = item.getTitle();
            return switch (searchMode) {
                case CASE_SENSITIVE -> StringUtils.contains(title, searchString);
                case CASE_INSENSITIVE -> StringUtils.containsIgnoreCase(title, searchString);
            };
        }

        protected <C extends MenuItem> boolean filterChildren(ParentMenuItem<C> parentMenuItem,
                                                              boolean forceRetainAllChildren) {
            boolean anyChildMatch = false;

            List<C> childItems = new ArrayList<>(parentMenuItem.getChildren());

            //update child collection only if force flag is disabled
            if (!forceRetainAllChildren) {
                parentMenuItem.removeAllChildItems();
            }

            for (C childItem : childItems) {
                boolean childMatch = filterItemRecursive(childItem, forceRetainAllChildren);

                if (childMatch) {
                    anyChildMatch = true;
                }

                //update child collection only if force flag is disabled
                if (childMatch && !forceRetainAllChildren) {
                    parentMenuItem.addChildItem(childItem);
                }
            }
            return anyChildMatch;
        }
    }

    /**
     * Search mode variants
     */
    public enum SearchMode {

        /**
         * Search string case will not be ignored
         */
        CASE_SENSITIVE,

        /**
         * Search string case will be ignored
         */
        CASE_INSENSITIVE
    }
}
