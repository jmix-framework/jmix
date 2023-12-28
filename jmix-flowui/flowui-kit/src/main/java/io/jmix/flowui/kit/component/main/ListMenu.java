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

package io.jmix.flowui.kit.component.main;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.menu.ParentMenuItem;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ListMenu extends Composite<UnorderedList> implements HasSize, HasStyle, HasThemeVariant<ListMenuVariant> {

    protected static final String TEXT_SMALL_CLASS_NAME = "text-s";
    protected static final String LIST_NONE_CLASS_NAME = "list-none";
    protected static final String MARGIN_NONE_CLASS_NAME = "m-0";
    protected static final String PADDING_NONE_CLASS_NAME = "p-0";
    protected static final String FLEX_CLASS_NAME = "flex";
    protected static final String FONT_MEDIUM_CLASS_NAME = "font-medium";

    protected static final String JMIX_LIST_MENU_CLASS_NAME = "jmix-list-menu";

    protected static final String JMIX_MENUBAR_ITEM_CLASS_NAME = "jmix-menubar-item";
    protected static final String JMIX_MENUBAR_SUMMARY_ICON_CONTAINER_CLASS_NAME = "jmix-menubar-summary-icon-container";
    protected static final String MENUBAR_SUMMARY_CLASS_NAME = "menubar-summary";
    protected static final String MENUBAR_ICON_CLASS_NAME = "menubar-icon";
    protected static final String MENUBAR_LIST_CLASS_NAME = "menubar-list";

    protected static final String JMIX_MENU_ITEM_LINK_CLASS_NAME = "jmix-menu-item-link";
    protected static final String LINK_ICON_CLASS_NAME = "link-icon";
    protected static final String LINK_TEXT_CLASS_NAME = "link-text";
    protected static final String PREFIX_COMPONENT_CLASS_NAME = "prefix-component";
    protected static final String SUFFIX_COMPONENT_CLASS_NAME = "suffix-component";

    protected List<MenuItem> rootMenuItems = new ArrayList<>();

    protected Map<String, Pair<MenuItem, ListItem>> registrations = new HashMap<>();

    protected PropertyChangeListener menuItemPropertyChangeListener = this::onMenuItemPropertyChange;

    @Override
    protected UnorderedList initContent() {
        UnorderedList content = super.initContent();
        content.addClassNames(JMIX_LIST_MENU_CLASS_NAME, LIST_NONE_CLASS_NAME);
        return content;
    }

    /**
     * Gets menu item from the menu by its ID.
     *
     * @param id menu item id
     * @return menu item or {@code null} if not found
     */
    @Nullable
    public MenuItem getMenuItem(String id) {
        if (registrations.containsKey(id)) {
            return registrations.get(id).getKey();
        }
        return null;
    }

    /**
     * @return immutable list of root menu items
     */
    public List<MenuItem> getMenuItems() {
        return Collections.unmodifiableList(rootMenuItems);
    }

    /**
     * Adds menu item and its children to the menu.
     *
     * @param menuItem menu item to add
     */
    public void addMenuItem(MenuItem menuItem) {
        Preconditions.checkNotNull(menuItem, MenuItem.class.getSimpleName() + " cannot be null");

        // create and register
        ListItem item = createMenuRecursively(menuItem);

        rootMenuItems.add(menuItem);
        getContent().add(item);

        attachMenuItemRecursively(menuItem);
    }

    /**
     * Adds menu item and its children to the menu in specified index.
     * <p>
     * If root menu items already contain provided menu item, it will be moved to the provided index.
     *
     * @param menuItem menu item to add
     * @param index    index to add menu item in the root items
     */
    public void addMenuItem(MenuItem menuItem, int index) {
        Preconditions.checkNotNull(menuItem, MenuItem.class.getSimpleName() + " cannot be null");

        MenuItem itemToAdd = menuItem;
        if (rootMenuItems.contains(itemToAdd)) {
            int existingIndex = rootMenuItems.indexOf(itemToAdd);
            if (index > existingIndex) {
                index--;
            }

            //noinspection OptionalGetWithoutIsPresent
            itemToAdd = rootMenuItems.stream()
                    .filter(item -> item.equals(menuItem))
                    .findFirst().get();

            removeMenuItem(itemToAdd);
        }

        // create and register
        ListItem menuItemComponent = createMenuRecursively(itemToAdd);

        rootMenuItems.add(index, itemToAdd);
        getContent().addComponentAtIndex(index, menuItemComponent);

        attachMenuItemRecursively(itemToAdd);
    }

    /**
     * Removes menu item and its children from the menu.
     *
     * @param menuItem menu item to remove
     */
    public void removeMenuItem(MenuItem menuItem) {
        if (!menuItem.isAttachedToMenu()
                || menuItem.getMenuComponent() != this) {
            throw new IllegalArgumentException(MenuItem.class.getSimpleName() + "is not attached to the menu");
        }

        Pair<MenuItem, ListItem> menuItemEntry = registrations.get(menuItem.getId());
        if (menuItemEntry == null) {
            return;
        }
        menuItem = menuItemEntry.getKey();

        // if is root
        rootMenuItems.remove(menuItem);

        // remove component
        menuItemEntry.getValue().getParent()
                .map(parent -> (UnorderedList) parent)
                .ifPresent(parent -> parent.remove(menuItemEntry.getValue()));

        detachMenuItemRecursively(menuItem);
        unregisterMenuItemRecursively(menuItem);
    }

    /**
     * Removes root menu item by index.
     *
     * @param index index to remove from root menu items
     */
    public void removeMenuItem(int index) {
        MenuItem menuItem = rootMenuItems.get(index);
        removeMenuItem(menuItem);
    }

    /**
     * Removes all menu items from the menu.
     */
    public void removeAllMenuItems() {
        for (MenuItem menuItem : new ArrayList<>(rootMenuItems)) {
            removeMenuItem(menuItem);
        }
    }

    protected void addChildren(MenuItem parent, MenuItem childItem) {
        UnorderedList content = getMenuBarContent(parent);

        ListItem menuItemComponent = createMenuRecursively(childItem);

        content.add(menuItemComponent);
    }

    protected void addChildren(MenuItem parent, MenuItem childItem, int index) {
        UnorderedList content = getMenuBarContent(parent);

        ListItem menuItemComponent = createMenuRecursively(childItem);

        content.addComponentAtIndex(index, menuItemComponent);
    }

    protected ListItem createMenuRecursively(MenuItem menuItem) {
        checkItemIdDuplicate(menuItem.getId());

        if (menuItem.isMenu()) {
            MenuBarItem menuBarItem = (MenuBarItem) menuItem;

            Details menuBar = createMenuBarComponent(menuBarItem);
            UnorderedList content = getMenuBarContent(menuBar);

            for (MenuItem childItem : menuBarItem.getChildItems()) {
                ListItem component = createMenuRecursively(childItem);
                content.add(component);
            }

            ListItem menuBarComponent = new ListItem(menuBar);

            registerMenuItem(menuItem, menuBarComponent);

            return menuBarComponent;
        } else if (menuItem.isSeparator()) {
            Component separator = new Hr();
            ListItem menuItemComponent = new ListItem(separator);

            registerMenuItem(menuItem, menuItemComponent);

            return menuItemComponent;
        } else {
            Component link = createMenuItemComponent(menuItem);
            ListItem menuItemComponent = new ListItem(link);

            registerMenuItem(menuItem, menuItemComponent);

            return menuItemComponent;
        }
    }

    protected void attachMenuItemRecursively(MenuItem menuItem) {
        menuItem.setMenuComponent(this);

        if (menuItem.isMenu()) {
            for (MenuItem item : ((MenuBarItem) menuItem).getChildItems()) {
                attachMenuItemRecursively(item);
            }
        }
    }

    protected void detachMenuItemRecursively(MenuItem menuItem) {
        menuItem.setMenuComponent(null);

        if (menuItem.isMenu()) {
            for (MenuItem item : ((MenuBarItem) menuItem).getChildItems()) {
                detachMenuItemRecursively(item);
            }
        }
    }

    protected void unregisterMenuItemRecursively(MenuItem menuItem) {
        registrations.remove(menuItem.getId());
        menuItem.removePropertyChangeListener(menuItemPropertyChangeListener);

        if (menuItem.isMenu()) {
            for (MenuItem item : ((MenuBarItem) menuItem).getChildItems()) {
                unregisterMenuItemRecursively(item);
            }
        }
    }

    protected void registerMenuItem(MenuItem menuItem, ListItem menuItemComponent) {
        registrations.put(menuItem.getId(), Pair.of(menuItem, menuItemComponent));
        menuItem.addPropertyChangeListener(menuItemPropertyChangeListener);
    }

    protected RouterLink createMenuItemComponent(MenuItem menuItem) {
        RouterLink routerLink = new RouterLink();
        routerLink.addClassNames(JMIX_MENU_ITEM_LINK_CLASS_NAME, FLEX_CLASS_NAME);
        routerLink.addClassNames(menuItem.getClassNames().toArray(new String[0]));
        routerLink.setHighlightCondition(HighlightConditions.never());

        Component prefixComponent = menuItem.getPrefixComponent();
        if (prefixComponent != null) {
            setPrefixComponent(routerLink, prefixComponent, null);
        } else if (menuItem.getIcon() != null) {
            Icon icon = new Icon(menuItem.getIcon());
            icon.addClassName(LINK_ICON_CLASS_NAME);
            routerLink.add(icon);
        }

        Span text = new Span(getTitle(menuItem));
        text.addClassNames(LINK_TEXT_CLASS_NAME, FONT_MEDIUM_CLASS_NAME, TEXT_SMALL_CLASS_NAME);
        text.setTitle(Strings.nullToEmpty(menuItem.getDescription()));

        addMenuItemClickListener(routerLink, menuItem);
        addMenuItemClickShortcutCombination(routerLink, menuItem);

        routerLink.add(text);
        setSuffixComponent(routerLink, menuItem.getSuffixComponent(), null);

        return routerLink;
    }

    protected void addMenuItemClickListener(RouterLink routerLink, MenuItem menuItem) {
        routerLink.getElement().addEventListener("click", event -> {
            if (menuItem.getClickHandler() != null) {
                menuItem.getClickHandler().accept(menuItem);
            }
        });
    }

    protected void addMenuItemClickShortcutCombination(RouterLink routerLink, MenuItem menuItem) {
        KeyCombination shortcutCombination = menuItem.getShortcutCombination();

        if (shortcutCombination != null) {
            Key key = shortcutCombination.getKey();
            KeyModifier[] keyModifiers = shortcutCombination.getKeyModifiers();

            Shortcuts.addShortcutListener(
                    routerLink,
                    event -> routerLink.getElement()
                            .executeJs("this.click()"),
                    key,
                    keyModifiers);
        }
    }

    protected Details createMenuBarComponent(MenuBarItem menuBarItem) {
        Details menuItemComponent = new Details();
        menuItemComponent.addClassName(JMIX_MENUBAR_ITEM_CLASS_NAME);
        menuItemComponent.addClassNames(menuBarItem.getClassNames().toArray(new String[0]));
        menuItemComponent.setOpened(menuBarItem.isOpened());

        Div div = new Div();
        div.setWidthFull();
        div.addClassName(JMIX_MENUBAR_SUMMARY_ICON_CONTAINER_CLASS_NAME);
        div.setTitle(Strings.nullToEmpty(menuBarItem.getDescription()));

        Component prefixComponent = menuBarItem.getPrefixComponent();
        if (prefixComponent != null) {
            setPrefixComponent(div, prefixComponent, null);
        } else if (menuBarItem.getIcon() != null) {
            Icon icon = new Icon(menuBarItem.getIcon());
            icon.addClassName(MENUBAR_ICON_CLASS_NAME);
            div.add(icon);
        }

        Span summary = new Span();
        summary.setText(getTitle(menuBarItem));
        summary.addClassNames(MENUBAR_SUMMARY_CLASS_NAME, TEXT_SMALL_CLASS_NAME);
        div.add(summary);

        setSuffixComponent(div, menuBarItem.getSuffixComponent(), null);

        menuItemComponent.setSummary(div);

        UnorderedList menuList = new UnorderedList();
        menuList.addClassNames(MENUBAR_LIST_CLASS_NAME, LIST_NONE_CLASS_NAME, MARGIN_NONE_CLASS_NAME,
                PADDING_NONE_CLASS_NAME);

        menuItemComponent.setContent(menuList);

        return menuItemComponent;
    }

    protected UnorderedList getMenuBarContent(MenuItem menuItem) {
        Details menuBarComponent = getMenuBarComponent(menuItem);

        return getMenuBarContent(menuBarComponent);
    }

    protected UnorderedList getMenuBarContent(Details menuBar) {
        return menuBar.getContent()
                .filter(component -> component instanceof UnorderedList)
                .map(component -> (UnorderedList) component)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Menu bar component does not have content component: "
                                + UnorderedList.class.getName()));
    }

    protected Details getMenuBarComponent(MenuItem menuItem) {
        Pair<MenuItem, ListItem> item = registrations.get(menuItem.getId());

        return item.getValue().getChildren()
                .findFirst()
                .map(details -> (Details) details)
                .orElseThrow(() -> new IllegalStateException(ListItem.class.getSimpleName() + "cannot be empty"));
    }

    protected RouterLink getMenuItemComponent(MenuItem menuItem) {
        Pair<MenuItem, ListItem> item = registrations.get(menuItem.getId());

        return item.getValue().getChildren()
                .findFirst()
                .map(routerLink -> (RouterLink) routerLink)
                .orElseThrow(() -> new IllegalStateException(ListItem.class.getSimpleName() + "cannot be empty"));
    }

    protected String getTitle(MenuItem menuItem) {
        return Strings.isNullOrEmpty(menuItem.getTitle())
                ? menuItem.getId()
                : menuItem.getTitle();
    }

    protected void checkItemIdDuplicate(String id) {
        if (registrations.containsKey(id)) {
            throw new IllegalArgumentException(String.format("Menu item with id \"%s\" already exists", id));
        }
    }

    protected void setPrefixComponent(HasComponents componentContainer,
                                      @Nullable Component prefixComponent,
                                      @Nullable Component oldPrefixComponent) {
        if (oldPrefixComponent != null) {
            oldPrefixComponent.removeClassName(PREFIX_COMPONENT_CLASS_NAME);
            componentContainer.remove(oldPrefixComponent);
        }

        if (prefixComponent != null) {
            prefixComponent.addClassName(PREFIX_COMPONENT_CLASS_NAME);
            componentContainer.addComponentAsFirst(prefixComponent);
        }
    }

    protected void setSuffixComponent(HasComponents componentContainer,
                                      @Nullable Component suffixComponent,
                                      @Nullable Component oldSuffixComponent) {
        if (oldSuffixComponent != null) {
            oldSuffixComponent.removeClassName(SUFFIX_COMPONENT_CLASS_NAME);
            componentContainer.remove(oldSuffixComponent);
        }

        if (suffixComponent != null) {
            suffixComponent.addClassName(SUFFIX_COMPONENT_CLASS_NAME);
            componentContainer.add(suffixComponent);
        }
    }

    protected void onMenuItemPropertyChange(PropertyChangeEvent event) {
        if (MenuBarItem.MENU_ITEM_OPENED.equals(event.getPropertyName())) {
            Details menuBarComponent = getMenuBarComponent((MenuItem) event.getSource());
            menuBarComponent.setOpened((Boolean) event.getNewValue());
        }
        if (MenuItem.MENU_ITEM_CLASS_NAME.equals(event.getPropertyName())) {
            MenuItem menuItem = (MenuItem) event.getSource();
            if (menuItem.isMenu()) {
                Details menuBarComponent = getMenuBarComponent((MenuItem) event.getSource());
                menuBarComponent.setClassName(JMIX_MENUBAR_ITEM_CLASS_NAME);
                menuBarComponent.addClassNames(menuItem.getClassNames().toArray(new String[0]));
            } else if (!menuItem.isSeparator()) {
                RouterLink menuItemComponent = getMenuItemComponent(menuItem);
                menuItemComponent.setClassName(JMIX_MENUBAR_ITEM_CLASS_NAME);
                menuItemComponent.addClassNames(menuItem.getClassNames().toArray(new String[0]));
            }
        }
        if (MenuItem.MENU_ITEM_SUFFIX_COMPONENT.equals(event.getPropertyName())) {
            MenuItem menuItem = (MenuItem) event.getSource();
            Component oldSuffixComponent = (Component) event.getOldValue();
            Component suffixComponent = (Component) event.getNewValue();

            if (menuItem.isMenu()) {
                Details menuBarComponent = getMenuBarComponent(menuItem);
                Div summary = (Div) menuBarComponent.getSummary();
                setSuffixComponent(summary, suffixComponent, oldSuffixComponent);
            } else if (!menuItem.isSeparator()) {
                RouterLink menuItemComponent = getMenuItemComponent(menuItem);
                setSuffixComponent(menuItemComponent, suffixComponent, oldSuffixComponent);
            }
        }
        if (MenuItem.MENU_ITEM_PREFIX_COMPONENT.equals(event.getPropertyName())) {
            MenuItem menuItem = (MenuItem) event.getSource();
            Component oldPrefixComponent = (Component) event.getOldValue();
            Component prefixComponent = (Component) event.getNewValue();

            if (menuItem.isMenu()) {
                Details menuBarComponent = getMenuBarComponent(menuItem);
                Div summary = (Div) menuBarComponent.getSummary();
                setPrefixComponent(summary, prefixComponent, oldPrefixComponent);
            } else if (!menuItem.isSeparator()) {
                RouterLink menuItemComponent = getMenuItemComponent(menuItem);
                setPrefixComponent(menuItemComponent, prefixComponent, oldPrefixComponent);
            }
        }
    }

    /**
     * Describes menu item.
     */
    public static class MenuItem implements io.jmix.flowui.kit.component.menu.MenuItem {
        protected static final String MENU_ITEM_CLASS_NAME = "className";
        protected static final String MENU_ITEM_PREFIX_COMPONENT = "prefixComponent";
        protected static final String MENU_ITEM_SUFFIX_COMPONENT = "suffixComponent";

        protected String id;
        protected String title;
        protected String description;
        protected VaadinIcon icon;
        protected List<String> classNames;
        protected Consumer<MenuItem> clickHandler;
        protected KeyCombination shortcutCombination;
        protected Component prefixComponent;
        protected Component suffixComponent;

        protected ListMenu menuComponent;

        protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public MenuItem(String id) {
            this.id = id;
        }

        /**
         * Creates menu item with provided id.
         *
         * @param id menu item id
         * @return menu item
         */
        public static MenuItem create(String id) {
            return new MenuItem(id);
        }

        /**
         * Creates menu bar that can contains other menu items.
         *
         * @param id menu item id
         * @return menu bar
         */
        public static MenuBarItem createMenuBar(String id) {
            return new MenuBarItem(id);
        }

        public String getId() {
            return id;
        }

        /**
         * @return title or {@code null} if not set
         */
        @Override
        @Nullable
        public String getTitle() {
            return title;
        }

        /**
         * Sets displayed menu item text.
         *
         * @param title displayed text
         * @return current menu instance
         */
        public MenuItem withTitle(@Nullable String title) {
            this.title = title;
            return this;
        }

        /**
         * @return menu item description or {@code null} if not set.
         */
        @Nullable
        public String getDescription() {
            return description;
        }

        /**
         * Sets menu item description that should be shown when the user moves cursor on item.
         *
         * @param description description to set
         * @return current menu instance
         */
        public MenuItem withDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        /**
         * @return icon or {@code null} if not set
         * @deprecated use {@link #getPrefixComponent()}
         */
        @Nullable
        @Deprecated(since="2.2", forRemoval=true)
        public VaadinIcon getIcon() {
            return icon;
        }

        /**
         * Sets icon that should be displayed to the left of the {@link #getTitle()}.
         *
         * @param icon icon to set
         * @return current menu instance
         * @deprecated use {@link #withPrefixComponent(Component)} or {@link #setPrefixComponent(Component)}
         */
        @Deprecated(since="2.2", forRemoval=true)
        public MenuItem withIcon(@Nullable VaadinIcon icon) {
            this.icon = icon;
            return this;
        }

        /**
         * @return class names or empty list
         */
        public List<String> getClassNames() {
            return classNames != null && !classNames.isEmpty()
                    ? Collections.unmodifiableList(classNames)
                    : Collections.emptyList();
        }

        /**
         * Sets class names that should be added to the menu item.
         *
         * @param classNames class names to add
         * @return current menu instance
         */
        public MenuItem withClassNames(List<String> classNames) {
            Preconditions.checkNotNull(classNames, "List of class names cannot be null");

            List<String> oldClassNames = this.classNames == null
                    ? Collections.emptyList()
                    : List.copyOf(this.classNames);

            this.classNames = new ArrayList<>(classNames);

            propertyChangeSupport.firePropertyChange(MENU_ITEM_CLASS_NAME, oldClassNames,
                    Collections.unmodifiableList(this.classNames));

            return this;
        }

        /**
         * Adds class names that should be added to the menu item.
         *
         * @param classNames class names to add
         */
        public void addClassNames(String... classNames) {
            Preconditions.checkNotNull(classNames, "Class names parameter cannot be null");

            if (this.classNames == null) {
                this.classNames = new ArrayList<>();
            }

            List<String> oldClassNames = List.copyOf(this.classNames);

            this.classNames.addAll(Arrays.asList(classNames));

            propertyChangeSupport.firePropertyChange(MENU_ITEM_CLASS_NAME, oldClassNames,
                    Collections.unmodifiableList(this.classNames));
        }

        @Nullable
        public Consumer<MenuItem> getClickHandler() {
            return clickHandler;
        }

        public MenuItem withClickHandler(@Nullable Consumer<MenuItem> clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        @Nullable
        public KeyCombination getShortcutCombination() {
            return shortcutCombination;
        }

        public MenuItem withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
            this.shortcutCombination = shortcutCombination;
            return this;
        }

        /**
         * @return suffix component of the item
         */
        @Nullable
        public Component getSuffixComponent() {
            return suffixComponent;
        }

        /**
         * Sets suffix component of the item.
         *
         * @param suffixComponent component to set
         * @return current menu item instance
         */
        public MenuItem withSuffixComponent(@Nullable Component suffixComponent) {
            setSuffixComponent(suffixComponent);
            return this;
        }

        /**
         * Sets suffix component of the item.
         *
         * @param suffixComponent component to set
         */
        public void setSuffixComponent(@Nullable Component suffixComponent) {
            Component oldSuffixComponent = this.suffixComponent;
            this.suffixComponent = suffixComponent;

            propertyChangeSupport.firePropertyChange(MENU_ITEM_SUFFIX_COMPONENT, oldSuffixComponent,
                    this.suffixComponent);
        }

        /**
         * @return prefix component of the item
         */
        public Component getPrefixComponent() {
            return prefixComponent;
        }

        /**
         * Sets prefix component of the item.
         *
         * @param prefixComponent component to set
         * @return current menu item instance
         */
        public MenuItem withPrefixComponent(Component prefixComponent) {
            setPrefixComponent(prefixComponent);
            return this;
        }

        /**
         * Sets prefix component of the item.
         *
         * @param prefixComponent component to set
         */
        public void setPrefixComponent(Component prefixComponent) {
            Component oldPrefixComponent = this.prefixComponent;
            this.prefixComponent = prefixComponent;

            propertyChangeSupport.firePropertyChange(MENU_ITEM_PREFIX_COMPONENT, oldPrefixComponent,
                    this.prefixComponent);
        }

        /**
         * @return {@code true} if menu item is {@link MenuBarItem} that contains other items,
         * {@code false} otherwise
         */
        public boolean isMenu() {
            return false;
        }

        public boolean isSeparator() {
            return false;
        }

        /**
         * @return menu component that contains given menu item
         */
        @Nullable
        public ListMenu getMenuComponent() {
            return menuComponent;
        }

        protected void setMenuComponent(@Nullable ListMenu menu) {
            this.menuComponent = menu;
        }

        /**
         * @return {@code true} if menu item is attached to the menu component,
         * {@code false} otherwise
         */
        public boolean isAttachedToMenu() {
            return getMenuComponent() != null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            return id.equals(((MenuItem) obj).getId());
        }

        protected void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        protected void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "{\"id\": \"" + getId() + "\"}";
        }
    }

    /**
     * Describes menu item that can contain other menu items.
     */
    public static class MenuBarItem extends MenuItem implements ParentMenuItem<MenuItem> {

        protected static final String MENU_ITEM_OPENED = "isOpened";

        protected List<MenuItem> children;
        protected boolean isOpened;

        public MenuBarItem(String id) {
            super(id);
        }

        @Override
        public MenuBarItem withTitle(@Nullable String title) {
            return (MenuBarItem) super.withTitle(title);
        }

        @Override
        public MenuBarItem withDescription(@Nullable String description) {
            return (MenuBarItem) super.withDescription(description);
        }

        @Override
        @Deprecated(since="2.2", forRemoval=true)
        public MenuBarItem withIcon(@Nullable VaadinIcon icon) {
            return (MenuBarItem) super.withIcon(icon);
        }

        @Override
        public MenuBarItem withClassNames(List<String> classNames) {
            return (MenuBarItem) super.withClassNames(classNames);
        }

        @Override
        public MenuBarItem withSuffixComponent(Component suffixComponent) {
            return (MenuBarItem) super.withSuffixComponent(suffixComponent);
        }

        @Override
        public MenuBarItem withPrefixComponent(Component prefixComponent) {
            return (MenuBarItem) super.withPrefixComponent(prefixComponent);
        }

        /**
         * @return {@code true} if menu bar item initially should open list of sub menu items,
         * {@code false} otherwise
         */
        @Override
        public boolean isOpened() {
            return isOpened;
        }

        /**
         * Sets whether menu bar item should open sub menu when it's added to the menu.
         *
         * @param opened open option
         */
        @Override
        public void setOpened(boolean opened) {
            isOpened = opened;
            propertyChangeSupport.firePropertyChange(
                    new PropertyChangeEvent(this, MENU_ITEM_OPENED, !isOpened, isOpened));
        }

        /**
         * Sets whether menu bar item should open sub menu when it's added to the menu.
         *
         * @param opened open option
         * @return current menu bar item instance
         */
        public MenuBarItem withOpened(boolean opened) {
            setOpened(opened);
            return this;
        }

        /**
         * Adds menu item to the sub menu list.
         *
         * @param menuItem menu item to add
         */
        @Override
        public void addChildItem(MenuItem menuItem) {
            Preconditions.checkNotNull(menuItem, MenuItem.class.getSimpleName() + " cannot be null");

            if (children == null) {
                children = new ArrayList<>();
            }

            if (isAttachedToMenu()) {
                menuComponent.addChildren(this, menuItem);
            }

            children.add(menuItem);
        }

        /**
         * Adds menu item to the sub menu list by index.
         * <p>
         * If child items already contain provided menu item, it will be moved to the provided index.
         *
         * @param menuItem menu item to add
         */
        @Override
        public void addChildItem(MenuItem menuItem, int index) {
            Preconditions.checkNotNull(menuItem, MenuItem.class.getSimpleName() + " cannot be null");

            if (children == null) {
                children = new ArrayList<>();
            }

            if (children.contains(menuItem)) {
                int existingIndex = children.indexOf(menuItem);
                if (index > existingIndex) {
                    index--;
                }

                children.remove(menuItem);
                if (isAttachedToMenu()) {
                    menuComponent.removeMenuItem(menuItem);
                }
            }

            children.add(index, menuItem);
            if (isAttachedToMenu()) {
                menuComponent.addChildren(this, menuItem, index);
            }
        }

        /**
         * Removes menu item from the child items.
         *
         * @param menuItem menu item to remove
         */
        @Override
        public void removeChildItem(MenuItem menuItem) {
            if (!hasChildren()) {
                return;
            }

            children.stream()
                    .filter(item -> item.equals(menuItem))
                    .findFirst()
                    .ifPresent((item) -> {
                        if (isAttachedToMenu()) {
                            menuComponent.removeMenuItem(menuItem);
                        }
                        children.remove(menuItem);
                    });
        }

        /**
         * Removes menu item from the child items by index.
         *
         * @param index index to remove
         */
        public void removeChildItem(int index) {
            if (!hasChildren()) {
                return;
            }

            if (isAttachedToMenu()) {
                menuComponent.removeMenuItem(children.get(index));
            }

            children.remove(index);
        }

        /**
         * Removes all child items.
         */
        @Override
        public void removeAllChildItems() {
            if (!hasChildren()) {
                return;
            }

            if (isAttachedToMenu()) {
                for (MenuItem menuItem : children) {
                    menuComponent.removeMenuItem(menuItem);
                }
            }

            children.clear();
            children = null;
        }

        /**
         * @return immutable list of child items
         * @deprecated use {@link #getChildItems()}
         */
        @Deprecated
        public List<MenuItem> getChildren() {
            return getChildItems();
        }

        @Override
        public List<MenuItem> getChildItems() {
            return hasChildren()
                    ? Collections.unmodifiableList(children)
                    : Collections.emptyList();
        }

        /**
         * @return {@code true} if menu bar item contains menu items,
         * {@code false} otherwise
         */
        public boolean hasChildren() {
            return children != null && !children.isEmpty();
        }

        @Override
        public boolean isMenu() {
            return true;
        }
    }

    public static class MenuSeparatorItem extends MenuItem {

        public MenuSeparatorItem(String id) {
            super(id);
        }

        @Override
        public boolean isSeparator() {
            return true;
        }
    }
}
