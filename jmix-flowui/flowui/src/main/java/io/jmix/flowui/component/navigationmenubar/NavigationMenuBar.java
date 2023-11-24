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

package io.jmix.flowui.component.navigationmenubar;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutEvent;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.menubar.HasMenuItemsEnhanced;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import io.jmix.flowui.menu.MenuItem.MenuItemParameter;
import io.jmix.flowui.menu.provider.HasMenuItemProvider;
import io.jmix.flowui.menu.provider.MenuItemProvider;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents horizontal menu that can be used for navigation to a view or for invoking a method of a bean
 */
public class NavigationMenuBar extends Composite<JmixMenuBar> implements ApplicationContextAware, InitializingBean,
        HasMenuItemProvider<NavigationMenuBar.MenuItem>, HasSize, HasStyle {

    protected static final String NAVIGATION_MENU_BAR_CLASS_NAME = "jmix-navigation-menu-bar";
    protected static final String ROOT_MENU_ITEM_CLASS_NAME = "jmix-navigation-menu-bar-root-item";
    protected static final String MENU_ITEM_LINK_CLASS_NAME = "jmix-navigation-menu-bar-item-link";
    protected static final String ICON_WITH_TITLE_CLASS_NAME = "jmix-navigation-menu-bar-icon-with-title";

    protected ApplicationContext applicationContext;
    protected ViewRegistry viewRegistry;

    protected MenuItemProvider<MenuItem> menuItemProvider;
    protected Subscription itemCollectionChangedSubscription;

    protected List<MenuItem> rootMenuItems = new ArrayList<>();
    protected Map<String, Pair<MenuItem, Component>> registrations = new HashMap<>();

    protected PropertyChangeListener menuItemPropertyChangeListener = this::onMenuItemPropertyChange;

    protected void onMenuItemPropertyChange(PropertyChangeEvent event) {
        if (MenuItem.MENU_ITEM_CLASS_NAME.equals(event.getPropertyName())) {
            MenuItem menuItem = (MenuItem) event.getSource();
            Component menuBarComponent = getMenuItemComponent(menuItem);
            menuBarComponent.setClassName(null);
            menuBarComponent.addClassNames(menuItem.getClassNames().toArray(new String[0]));
        }
    }

    protected Component getMenuItemComponent(MenuItem menuItem) {
        Pair<MenuItem, Component> itemPair = registrations.get(menuItem.getId());
        if (itemPair == null) {
            throw new IllegalArgumentException("Failed to find JmixMenuItem component for menu item %s".formatted(menuItem));
        }
        return itemPair.getValue();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
    }

    protected void autowireDependencies() {
        viewRegistry = applicationContext.getBean(ViewRegistry.class);
    }

    @Override
    protected JmixMenuBar initContent() {
        JmixMenuBar menuBar = super.initContent();
        menuBar.setClassName(NAVIGATION_MENU_BAR_CLASS_NAME);
        return menuBar;
    }

    @Override
    @Nullable
    public MenuItemProvider<MenuItem> getMenuItemProvider() {
        return menuItemProvider;
    }

    @Override
    public void setMenuItemProvider(@Nullable MenuItemProvider<MenuItem> menuItemProvider) {
        if (Objects.equals(this.menuItemProvider, menuItemProvider)) {
            return;
        }
        if (itemCollectionChangedSubscription != null) {
            itemCollectionChangedSubscription.remove();
            itemCollectionChangedSubscription = null;
        }
        this.menuItemProvider = menuItemProvider;
        if (menuItemProvider != null) {
            itemCollectionChangedSubscription =
                    menuItemProvider.addCollectionChangedListener(this::onMenuItemCollectionChanged);
        }
    }

    protected void onMenuItemCollectionChanged(MenuItemProvider.CollectionChangeEvent<MenuItem> e) {
        removeAllMenuItems();
        e.getItems().forEach(this::addMenuItem);
    }

    /**
     * Gets menu item from the menu by its id.
     *
     * @param id menu item id
     * @return menu item or null if not found
     */
    @Nullable
    public MenuItem getMenuItem(String id) {
        Preconditions.checkNotNullArgument(id);

        Pair<MenuItem, Component> itemComponentPair = registrations.get(id);
        return itemComponentPair != null ? itemComponentPair.getKey() : null;
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
        Preconditions.checkNotNullArgument(menuItem);

        createItemComponentRecursively(menuItem, null, null);

        rootMenuItems.add(menuItem);
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
        Preconditions.checkNotNullArgument(menuItem);

        MenuItem itemToAdd;
        int existingIndex = rootMenuItems.indexOf(menuItem);
        if (existingIndex != -1) {
            if (index > existingIndex) {
                //decrement because we will remove existing element
                index--;
            }

            itemToAdd = rootMenuItems.get(existingIndex);
            removeMenuItem(itemToAdd);
        } else {
            itemToAdd = menuItem;
        }

        createItemComponentRecursively(itemToAdd, null, index);

        rootMenuItems.add(index, itemToAdd);

        attachMenuItemRecursively(itemToAdd);
    }

    /**
     * Removes menu item and its children from the menu.
     *
     * @param menuItem menu item to remove
     */
    public void removeMenuItem(MenuItem menuItem) {
        if (!menuItem.isAttachedToMenu()
                || !this.equals(menuItem.getMenuComponent())) {
            throw new IllegalArgumentException(MenuItem.class.getSimpleName() + "is not attached to the menu");
        }

        Pair<MenuItem, Component> menuItemEntry = registrations.get(menuItem.getId());
        if (menuItemEntry == null) {
            return;
        }
        menuItem = menuItemEntry.getKey();

        //if is root
        rootMenuItems.remove(menuItem);

        JmixSubMenu parentSubMenu = menuItem.getParentSubMenu();
        Component menuItemComponent = menuItemEntry.getValue();
        if (parentSubMenu != null) {
            parentSubMenu.remove(menuItemComponent);
        } else {
            if (menuItemComponent instanceof JmixMenuItem jmixMenuItem) {
                getContent().remove(jmixMenuItem);
            } else {
                throw new IllegalStateException("Illegal item type on the root level");
            }
        }

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


    protected void addChildItem(MenuItem parentItem, MenuItem childItem, @Nullable Integer index) {
        Pair<MenuItem, Component> parentItemComponentPair = registrations.get(parentItem.getId());

        if (parentItemComponentPair == null) {
            throw new IllegalArgumentException("The menu doesn't contain parent item");
        }

        if (!parentItem.isMenu()) {
            throw new IllegalArgumentException("Parent item is not a menu");
        }

        if (parentItemComponentPair.getValue() instanceof JmixMenuItem jmixMenuItem) {
            JmixSubMenu subMenu = jmixMenuItem.getSubMenu();
            createItemComponentRecursively(childItem, subMenu, index);
            attachMenuItemRecursively(childItem);
        } else {
            throw new IllegalStateException("Parent item component must be an instance of JmixMenuItem");
        }
    }

    protected void createItemComponentRecursively(MenuItem menuItem,
                                                  @Nullable JmixSubMenu parentSubMenu,
                                                  @Nullable Integer index) {
        checkItemIdDuplicate(menuItem.getId());

        Component menuItemComponent;
        if (menuItem.isMenu()) {
            ParentMenuItem parentMenuItem = (ParentMenuItem) menuItem;

            JmixMenuItem parentItemComponent = createParentMenuItemComponent(parentMenuItem, parentSubMenu, index);

            JmixSubMenu subMenu = parentItemComponent.getSubMenu();
            for (MenuItem childItem : parentMenuItem.getChildren()) {
                createItemComponentRecursively(childItem, subMenu, null);
            }
            menuItemComponent = parentItemComponent;
        } else if (menuItem.isSeparator()) {
            if (parentSubMenu != null) {
                menuItemComponent = createSeparatorMenuItemComponent(parentSubMenu, index);
            } else {
                throw new UnsupportedOperationException("Separators are not supported on the root level");
            }
        } else {
            menuItemComponent = createMenuItemComponent(menuItem, parentSubMenu, index);
        }
        registerMenuItem(menuItem, menuItemComponent);
        menuItem.setParentSubMenu(parentSubMenu);
    }

    protected void attachMenuItemRecursively(MenuItem menuItem) {
        menuItem.setMenuComponent(this);

        if (menuItem.isMenu()) {
            for (MenuItem item : ((ParentMenuItem) menuItem).getChildren()) {
                attachMenuItemRecursively(item);
            }
        }
    }

    protected void detachMenuItemRecursively(MenuItem menuItem) {
        menuItem.setMenuComponent(null);

        if (menuItem.isMenu()) {
            for (MenuItem item : ((ParentMenuItem) menuItem).getChildren()) {
                detachMenuItemRecursively(item);
            }
        }
    }

    protected void unregisterMenuItemRecursively(MenuItem menuItem) {
        registrations.remove(menuItem.getId());
        menuItem.removePropertyChangeListener(menuItemPropertyChangeListener);

        if (menuItem.isMenu()) {
            for (MenuItem item : ((ParentMenuItem) menuItem).getChildren()) {
                unregisterMenuItemRecursively(item);
            }
        }
    }

    protected void registerMenuItem(MenuItem menuItem, Component menuItemComponent) {
        registrations.put(menuItem.getId(), Pair.of(menuItem, menuItemComponent));
        menuItem.addPropertyChangeListener(menuItemPropertyChangeListener);
    }

    protected void addMenuItemClickShortcutCombination(Component component, MenuItem menuItem) {
        KeyCombination shortcutCombination = menuItem.getShortcutCombination();

        if (shortcutCombination != null) {
            Key key = shortcutCombination.getKey();
            KeyModifier[] keyModifiers = shortcutCombination.getKeyModifiers();

            Shortcuts.addShortcutListener(component, this::onShortcutEvent, key, keyModifiers);
        }
    }

    protected void onShortcutEvent(ShortcutEvent event) {
        event.getLifecycleOwner().getElement().executeJs("this.click()");
    }

    protected JmixMenuItem createParentMenuItemComponent(ParentMenuItem parentMenuItem,
                                                         @Nullable JmixSubMenu parentSubMenu,
                                                         @Nullable Integer index) {
        VaadinIcon vaadinIcon = parentMenuItem.getIcon();
        Component titleComponent = createTitleComponent(getText(parentMenuItem));
        JmixMenuItem menuItemComponent;
        if (vaadinIcon != null) {
            Icon menuItemIcon = createMenuItemIcon(vaadinIcon);

            menuItemComponent = addMenuItemComponent(menuItemIcon, parentSubMenu, index);
            menuItemComponent.add(titleComponent);
        } else {
            menuItemComponent = addMenuItemComponent(titleComponent, parentSubMenu, index);
        }

        if (parentSubMenu == null) {
            menuItemComponent.setClassName(ROOT_MENU_ITEM_CLASS_NAME);
        }
        menuItemComponent.addClassNames(parentMenuItem.getClassNames().toArray(new String[0]));

        return menuItemComponent;
    }

    protected Icon createMenuItemIcon(VaadinIcon vaadinIcon) {
        Icon icon = new Icon(vaadinIcon);
        icon.setClassName(ICON_WITH_TITLE_CLASS_NAME);
        return icon;
    }

    protected Component createTitleComponent(String title) {
        return new Text(title);
    }

    protected JmixMenuItem addMenuItemComponent(Component component,
                                                @Nullable JmixSubMenu subMenu,
                                                @Nullable Integer index) {
        HasMenuItemsEnhanced hasMenuItems = subMenu != null ? subMenu : getContent();
        if (index == null) {
            return hasMenuItems.addItem(component);
        } else {
            return hasMenuItems.addItemAtIndex(index, component);
        }
    }

    protected Component createSeparatorMenuItemComponent(JmixSubMenu parentSubMenu, @Nullable Integer index) {
        Component separator = new Hr();
        if (index == null) {
            parentSubMenu.add(separator);
        } else {
            parentSubMenu.addComponentAtIndex(index, separator);
        }
        return separator;
    }

    protected String getText(MenuItem menuItem) {
        return Strings.isNullOrEmpty(menuItem.getTitle())
                ? menuItem.getId()
                : menuItem.getTitle();
    }

    protected void checkItemIdDuplicate(String id) {
        if (registrations.containsKey(id)) {
            throw new IllegalArgumentException("Menu item with id \"%s\" already exists".formatted(id));
        }
    }

    protected JmixMenuItem createMenuItemComponent(MenuItem menuItem,
                                                   @Nullable JmixSubMenu parentSubMenu,
                                                   @Nullable Integer index) {
        RouterLink routerLink = new RouterLink();
        routerLink.setClassName(MENU_ITEM_LINK_CLASS_NAME);
        if (parentSubMenu == null) {
            routerLink.addClassName(ROOT_MENU_ITEM_CLASS_NAME);
        }
        routerLink.setHighlightCondition(HighlightConditions.never());

        VaadinIcon vaadinIcon = menuItem.getIcon();
        if (vaadinIcon != null) {
            Icon menuItemIcon = createMenuItemIcon(vaadinIcon);
            routerLink.add(menuItemIcon);
        }

        Span textComponent = new Span(getText(menuItem));
        textComponent.setTitle(Strings.nullToEmpty(menuItem.getDescription()));
        routerLink.add(textComponent);

        addMenuItemClickListener(routerLink, menuItem);
        addMenuItemClickShortcutCombination(routerLink, menuItem);

        if (menuItem instanceof ViewMenuItem viewMenuItem) {
            QueryParameters queryParameters = viewMenuItem.getUrlQueryParameters();
            RouteParameters routeParameters = viewMenuItem.getRouteParameters();
            if (queryParameters != null) {
                routerLink.setQueryParameters(queryParameters);
            }
            Class<? extends View<?>> viewClass = getViewClass(viewMenuItem);
            if (routeParameters != null) {
                routerLink.setRoute(viewClass, routeParameters);
            } else {
                routerLink.setRoute(viewClass);
            }
        }

        JmixMenuItem jmixMenuItem = addMenuItemComponent(routerLink, parentSubMenu, index);
        jmixMenuItem.addClassNames(menuItem.getClassNames().toArray(new String[0]));
        return jmixMenuItem;
    }

    protected void addMenuItemClickListener(RouterLink routerLink, MenuItem menuItem) {
        Consumer<MenuItem> clickHandler = menuItem.getClickHandler();
        if (clickHandler != null && !(menuItem instanceof ViewMenuItem)) {
            routerLink.getElement().addEventListener("click", event -> clickHandler.accept(menuItem));
        }
    }

    protected Class<? extends View<?>> getViewClass(ViewMenuItem menuItem) {
        Class<? extends View<?>> viewClass = menuItem.getViewClass();

        if (viewClass != null && isSupportedView(viewClass)) {
            return menuItem.getViewClass();
        }
        ViewInfo viewInfo = viewRegistry.getViewInfo(menuItem.getId());
        return viewInfo.getControllerClass();
    }

    protected boolean isSupportedView(Class<?> targetView) {
        return View.class.isAssignableFrom(targetView)
                && targetView.getAnnotation(ViewController.class) != null;
    }

    /**
     * Describes navigation menu bar item.
     */
    public static class MenuItem implements io.jmix.flowui.kit.component.menu.MenuItem {

        protected static final String MENU_ITEM_CLASS_NAME = "className";

        protected String id;
        protected String title;
        protected String description;
        protected VaadinIcon icon;
        protected List<String> classNames;
        protected Consumer<MenuItem> clickHandler;
        protected KeyCombination shortcutCombination;
        protected JmixSubMenu parentSubMenu;
        protected NavigationMenuBar menuComponent;

        protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public MenuItem(String id) {
            this.id = id;
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
         * @return current menu item instance
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
         * Sets menu item description that should be shown when the user moves cursor on the item.
         *
         * @param description description to set
         * @return current menu item instance
         */
        public MenuItem withDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        /**
         * @return icon or {@code null} if not set
         */
        @Nullable
        public VaadinIcon getIcon() {
            return icon;
        }

        /**
         * Sets icon that should be displayed to the left of the {@link #getTitle()}.
         *
         * @param icon icon to set
         * @return current menu item instance
         */
        public MenuItem withIcon(@Nullable VaadinIcon icon) {
            this.icon = icon;
            return this;
        }

        /**
         * @return css class names
         */
        public List<String> getClassNames() {
            return classNames != null && !classNames.isEmpty()
                    ? Collections.unmodifiableList(classNames)
                    : Collections.emptyList();
        }

        /**
         * Sets css class names that should be added to the menu item.
         *
         * @param classNames css class names to add
         * @return current menu item instance
         */
        public MenuItem withClassNames(List<String> classNames) {
            Preconditions.checkNotNullArgument(classNames);

            List<String> oldClassNames = this.classNames == null
                    ? Collections.emptyList()
                    : List.copyOf(this.classNames);

            this.classNames = new ArrayList<>(classNames);

            propertyChangeSupport.firePropertyChange(MENU_ITEM_CLASS_NAME, oldClassNames,
                    Collections.unmodifiableList(this.classNames));

            return this;
        }

        /**
         * Adds css class names that should be added to the menu item.
         *
         * @param classNames css class names to add
         */
        public void addClassNames(String... classNames) {
            Preconditions.checkNotNullArgument(classNames);

            if (this.classNames == null) {
                this.classNames = new ArrayList<>();
            }

            List<String> oldClassNames = List.copyOf(this.classNames);

            this.classNames.addAll(Arrays.asList(classNames));

            propertyChangeSupport.firePropertyChange(MENU_ITEM_CLASS_NAME, oldClassNames,
                    Collections.unmodifiableList(this.classNames));
        }

        /**
         * @return click handler of the item
         */
        @Nullable
        public Consumer<MenuItem> getClickHandler() {
            return clickHandler;
        }

        /**
         * Sets click handler of the item.
         *
         * @param clickHandler menu item click handler
         * @return current menu item instance
         */
        public MenuItem withClickHandler(@Nullable Consumer<MenuItem> clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        /**
         * @return shortcut key combination of the item
         */
        @Nullable
        public KeyCombination getShortcutCombination() {
            return shortcutCombination;
        }

        /**
         * Sets shortcut key combination of the item.
         *
         * @param shortcutCombination shortcut key combination
         * @return current menu item instance
         */
        public MenuItem withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
            this.shortcutCombination = shortcutCombination;
            return this;
        }

        /**
         * @return true if the item can contain other items {@link ParentMenuItem}, false otherwise
         */
        public boolean isMenu() {
            return false;
        }

        /**
         * @return true if the item is a separator
         */
        public boolean isSeparator() {
            return false;
        }

        /**
         * @return menu component that contains this item
         */
        @Nullable
        public NavigationMenuBar getMenuComponent() {
            return menuComponent;
        }

        protected void setMenuComponent(@Nullable NavigationMenuBar menu) {
            this.menuComponent = menu;
        }

        /**
         * @return true if menu item is attached to the menu component, false otherwise
         */
        public boolean isAttachedToMenu() {
            return getMenuComponent() != null;
        }

        /**
         * @return submenu component that contains this item
         */
        @Nullable
        public JmixSubMenu getParentSubMenu() {
            return parentSubMenu;
        }

        /**
         * Sets submenu component that contains this item.
         *
         * @param parentSubMenu submenu to set
         */
        public void setParentSubMenu(@Nullable JmixSubMenu parentSubMenu) {
            this.parentSubMenu = parentSubMenu;
        }

        protected void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        protected void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
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
    public static class ParentMenuItem extends MenuItem
            implements io.jmix.flowui.kit.component.menu.ParentMenuItem<MenuItem> {

        protected List<MenuItem> children;

        public ParentMenuItem(String id) {
            super(id);
        }

        @Override
        public ParentMenuItem withTitle(@Nullable String title) {
            return (ParentMenuItem) super.withTitle(title);
        }

        @Override
        public ParentMenuItem withDescription(@Nullable String description) {
            return (ParentMenuItem) super.withDescription(description);
        }

        @Override
        public ParentMenuItem withIcon(@Nullable VaadinIcon icon) {
            return (ParentMenuItem) super.withIcon(icon);
        }

        @Override
        public ParentMenuItem withClassNames(List<String> classNames) {
            return (ParentMenuItem) super.withClassNames(classNames);
        }

        @Override
        public boolean isOpened() {
            throw new UnsupportedOperationException(
                    "Parent item of navigation menu bar doesn't support retrieving open state from server");
        }

        @Override
        public void setOpened(boolean opened) {
            throw new UnsupportedOperationException(
                    "Parent item of navigation menu bar doesn't support opening from server");
        }

        /**
         * Adds child menu item.
         *
         * @param menuItem menu item to add
         */
        @Override
        public void addChildItem(MenuItem menuItem) {
            Preconditions.checkNotNullArgument(menuItem);

            if (children == null) {
                children = new ArrayList<>();
            }

            if (isAttachedToMenu()) {
                menuComponent.addChildItem(this, menuItem, null);
            }

            children.add(menuItem);
        }

        @Override
        public void addChildItem(MenuItem menuItem, int index) {
            Preconditions.checkNotNullArgument(menuItem);

            if (children == null) {
                children = new ArrayList<>();
            }

            int existingIndex = children.indexOf(menuItem);
            if (existingIndex != -1) {
                if (index > existingIndex) {
                    index--;
                }

                removeChildItem(children.get(existingIndex));
            }

            children.add(index, menuItem);
            if (isAttachedToMenu()) {
                menuComponent.addChildItem(this, menuItem, index);
            }
        }

        @Override
        public void removeChildItem(MenuItem menuItem) {
            Preconditions.checkNotNullArgument(menuItem);

            if (!hasChildren()) {
                return;
            }

            if (!menuItem.isAttachedToMenu()
                    || menuItem.getMenuComponent() != menuComponent) {
                throw new IllegalArgumentException(MenuItem.class.getSimpleName() + " is not attached to the menu");
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
         * Removes child menu item by index.
         *
         * @param index index of an item to remove
         */
        public void removeChildItem(int index) {
            if (!hasChildren()) {
                return;
            }

            removeChildItem(children.get(index));
        }

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
        }

        @Override
        public List<MenuItem> getChildren() {
            return hasChildren()
                    ? Collections.unmodifiableList(children)
                    : Collections.emptyList();
        }

        /**
         * @return true if the item contains child items, false otherwise
         */
        public boolean hasChildren() {
            return children != null && !children.isEmpty();
        }

        @Override
        public boolean isMenu() {
            return true;
        }
    }

    /**
     * Represents menu separator
     */
    public static class MenuSeparatorItem extends MenuItem {

        public MenuSeparatorItem(String id) {
            super(id);
        }

        @Override
        public boolean isSeparator() {
            return true;
        }
    }

    /**
     * Represents an item which navigates to a view on click
     */
    public static class ViewMenuItem extends MenuItem {

        protected Class<? extends View<?>> viewClass;
        protected QueryParameters urlQueryParameters;
        protected RouteParameters routeParameters;

        public ViewMenuItem(String id) {
            super(id);
        }

        @Override
        public ViewMenuItem withTitle(@org.springframework.lang.Nullable String title) {
            super.withTitle(title);
            return this;
        }

        @Override
        public ViewMenuItem withDescription(@org.springframework.lang.Nullable String description) {
            super.withDescription(description);
            return this;
        }

        @Override
        public ViewMenuItem withIcon(@org.springframework.lang.Nullable VaadinIcon icon) {
            super.withIcon(icon);
            return this;
        }

        @Override
        public ViewMenuItem withClassNames(List<String> classNames) {
            super.withClassNames(classNames);
            return this;
        }

        @Override
        public ViewMenuItem withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
            return (ViewMenuItem) super.withShortcutCombination(shortcutCombination);
        }

        /**
         * @return query parameters of the view to navigate
         */
        @Nullable
        public QueryParameters getUrlQueryParameters() {
            return urlQueryParameters;
        }

        /**
         * Sets query parameters of the view to navigate.
         *
         * @param queryParameters query parameters
         * @return current menu item instance
         */
        public ViewMenuItem withUrlQueryParameters(List<MenuItemParameter> queryParameters) {
            Map<String, String> parametersMap = queryParameters.stream()
                    .collect(Collectors.toMap(MenuItemParameter::getName, MenuItemParameter::getValue));

            this.urlQueryParameters = QueryParameters.simple(parametersMap);
            return this;
        }

        /**
         * @return route parameters of the view to navigate
         */
        @Nullable
        public RouteParameters getRouteParameters() {
            return routeParameters;
        }

        /**
         * Sets route parameters of the view to navigate.
         *
         * @param routeParameters route parameters
         * @return current menu item instance
         */
        public ViewMenuItem withRouteParameters(List<MenuItemParameter> routeParameters) {
            Map<String, String> parametersMap = routeParameters.stream()
                    .collect(Collectors.toMap(MenuItemParameter::getName, MenuItemParameter::getValue));

            this.routeParameters = new RouteParameters(parametersMap);
            return this;
        }

        /**
         * @return view class or null if not set
         */
        @Nullable
        public Class<? extends View<?>> getViewClass() {
            return viewClass;
        }

        /**
         * Sets view class that should be shown when the user clicks on the menu item. If not set, {@link #getId()}
         * will be used as view id to navigate.
         *
         * @param viewClass view class to set
         * @return current menu item instance
         */
        public ViewMenuItem withViewClass(@Nullable Class<? extends View<?>> viewClass) {
            this.viewClass = viewClass;
            return this;
        }
    }
}
