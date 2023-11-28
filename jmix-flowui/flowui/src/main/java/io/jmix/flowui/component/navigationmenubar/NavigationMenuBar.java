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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutEvent;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;
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
import jakarta.annotation.Nullable;

import java.util.ArrayList;
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
public class NavigationMenuBar extends Composite<JmixMenuBar>
        implements HasMenuItemProvider<NavigationMenuBar.AbstractMenuItem<?>>, HasSize, HasStyle {

    protected static final String NAVIGATION_MENU_BAR_CLASS_NAME = "jmix-navigation-menu-bar";
    protected static final String ROOT_MENU_ITEM_CLASS_NAME = "jmix-navigation-menu-bar-root-item";

    protected MenuItemProvider<AbstractMenuItem<?>> menuItemProvider;
    protected Subscription itemCollectionChangedSubscription;

    protected List<AbstractMenuItem<?>> rootMenuItems = new ArrayList<>();
    protected Map<String, AbstractMenuItem<?>> allMenuItems = new HashMap<>();

    @Override
    protected JmixMenuBar initContent() {
        JmixMenuBar menuBar = super.initContent();
        menuBar.setClassName(NAVIGATION_MENU_BAR_CLASS_NAME);
        return menuBar;
    }

    @Override
    @Nullable
    public MenuItemProvider<AbstractMenuItem<?>> getMenuItemProvider() {
        return menuItemProvider;
    }

    @Override
    public void setMenuItemProvider(@Nullable MenuItemProvider<AbstractMenuItem<?>> menuItemProvider) {
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

    protected void onMenuItemCollectionChanged(MenuItemProvider.CollectionChangeEvent<AbstractMenuItem<?>> e) {
        removeAllMenuItems();
        e.getItems().forEach(this::addMenuItem);
    }

    /**
     * Returns menu item from the menu (including non-root items) by its id.
     *
     * @param id menu item id
     * @return menu item or null if not found
     */
    @Nullable
    public AbstractMenuItem<?> getMenuItem(String id) {
        Preconditions.checkNotNullArgument(id);

        return allMenuItems.get(id);
    }

    /**
     * @return immutable list of root menu items
     */
    public List<AbstractMenuItem<?>> getMenuItems() {
        return Collections.unmodifiableList(rootMenuItems);
    }

    /**
     * Adds menu item and its children to the menu.
     *
     * @param menuItem menu item to add
     */
    public void addMenuItem(AbstractMenuItem<?> menuItem) {
        Preconditions.checkNotNullArgument(menuItem);

        addMenuItemRecursive(menuItem, null, null);

        rootMenuItems.add(menuItem);
    }

    protected void addMenuItemRecursive(AbstractMenuItem<?> childMenuItem,
                                        @Nullable ParentMenuItem menuItem,
                                        @Nullable Integer index) {
        checkItemIdDuplicate(childMenuItem.getId());

        JmixSubMenu parentSubMenu = menuItem != null
                ? getParentSubMenu(menuItem)
                : null;

        Component childMenuItemComponent = childMenuItem.getComponent();

        if (childMenuItem.isMenu()) {
            ParentMenuItem parentMenuItem = (ParentMenuItem) childMenuItem;

            JmixMenuItem itemWrapper = addMenuItemComponent(childMenuItemComponent, parentSubMenu, index);
            childMenuItem.setMenuItemWrapper(itemWrapper);

            for (AbstractMenuItem<?> item : parentMenuItem.getChildren()) {
                addMenuItemRecursive(item, parentMenuItem, null);
            }
        } else if (childMenuItem.isSeparator()) {
            if (parentSubMenu != null) {
                if (index == null) {
                    parentSubMenu.add(childMenuItemComponent);
                } else {
                    parentSubMenu.addComponentAtIndex(index, childMenuItemComponent);
                }
            } else {
                throw new UnsupportedOperationException("Separators are not supported on the root level");
            }
        } else {
            JmixMenuItem itemWrapper = addMenuItemComponent(childMenuItemComponent, parentSubMenu, index);
            childMenuItem.setMenuItemWrapper(itemWrapper);
        }

        registerMenuItem(childMenuItem);
        if (menuItem == null) {
            childMenuItem.addClassNames(ROOT_MENU_ITEM_CLASS_NAME);
        }
        childMenuItem.setMenu(this);
        childMenuItem.setParentMenuItem(menuItem);
    }

    protected void checkItemIdDuplicate(String id) {
        if (allMenuItems.containsKey(id)) {
            throw new IllegalArgumentException("Menu item with id \"%s\" already exists".formatted(id));
        }
    }

    protected JmixSubMenu getParentSubMenu(ParentMenuItem parentMenuItem) {
        JmixMenuItem itemWrapper = parentMenuItem.getMenuItemWrapper();
        if (itemWrapper == null) {
            throw new IllegalArgumentException("JmixMenuItem for parent menu item is not set");
        }
        return itemWrapper.getSubMenu();
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

    protected void registerMenuItem(AbstractMenuItem<?> menuItem) {
        allMenuItems.put(menuItem.getId(), menuItem);
    }

    /**
     * Adds menu item and its children to the menu in specified index.
     *
     * @param menuItem menu item to add
     * @param index    index at which the item will be inserted
     */
    public void addMenuItem(AbstractMenuItem<?> menuItem, int index) {
        Preconditions.checkNotNullArgument(menuItem);

        addMenuItemRecursive(menuItem, null, index);

        rootMenuItems.add(index, menuItem);
    }

    protected void addChildItem(AbstractMenuItem<?> menuItem, ParentMenuItem parentMenuItem, @Nullable Integer index) {
        addMenuItemRecursive(menuItem, parentMenuItem, index);
    }

    /**
     * Removes menu item and its children from the menu.
     *
     * @param menuItem menu item to remove
     */
    public void removeMenuItem(AbstractMenuItem<?> menuItem) {
        if (!menuItem.isAttachedToMenu() || !this.equals(menuItem.getMenu())) {
            throw new IllegalArgumentException("Menu item is not attached to this menu");
        }

        if (!allMenuItems.containsKey(menuItem.getId())) {
            return;
        }

        ParentMenuItem parentMenuItem = menuItem.getParentMenuItem();
        if (parentMenuItem == null) {
            removeRootItem(menuItem);
        } else {
            parentMenuItem.removeChildItem(menuItem);
        }
    }

    protected void removeRootItem(AbstractMenuItem<?> menuItem) {
        rootMenuItems.remove(menuItem);

        JmixMenuItem itemWrapper = menuItem.getMenuItemWrapper();
        if (itemWrapper != null) {
            getContent().remove(itemWrapper);
        }

        detachMenuItemRecursively(menuItem);
        unregisterMenuItemRecursively(menuItem);
    }

    protected void detachMenuItemRecursively(AbstractMenuItem<?> menuItem) {
        menuItem.setMenu(null);
        menuItem.setMenuItemWrapper(null);

        if (menuItem.isMenu()) {
            for (AbstractMenuItem<?> item : ((ParentMenuItem) menuItem).getChildren()) {
                detachMenuItemRecursively(item);
            }
        }
    }

    protected void unregisterMenuItemRecursively(AbstractMenuItem<?> menuItem) {
        allMenuItems.remove(menuItem.getId());

        if (menuItem.isMenu()) {
            for (AbstractMenuItem<?> item : ((ParentMenuItem) menuItem).getChildren()) {
                unregisterMenuItemRecursively(item);
            }
        }
    }

    protected void removeChildItem(AbstractMenuItem<?> menuItem, ParentMenuItem parentMenuItem) {
        JmixMenuItem parentItemWrapper = parentMenuItem.getMenuItemWrapper();
        JmixMenuItem itemWrapper = menuItem.getMenuItemWrapper();
        if (itemWrapper != null && parentItemWrapper != null) {
            parentItemWrapper.getSubMenu().remove(itemWrapper);
        }

        detachMenuItemRecursively(menuItem);
        unregisterMenuItemRecursively(menuItem);
    }

    /**
     * Removes root menu item by index.
     *
     * @param index index of an item to remove
     */
    public void removeMenuItem(int index) {
        AbstractMenuItem<?> menuItem = rootMenuItems.get(index);
        removeMenuItem(menuItem);
    }

    /**
     * Removes all menu items from the menu.
     */
    public void removeAllMenuItems() {
        for (AbstractMenuItem<?> menuItem : new ArrayList<>(rootMenuItems)) {
            removeMenuItem(menuItem);
        }
    }

    public static abstract class AbstractMenuItem<T extends Component>
            implements io.jmix.flowui.kit.component.menu.MenuItem {

        protected String id;
        protected NavigationMenuBar menu;
        protected JmixMenuItem menuItemWrapper;
        protected ParentMenuItem parentMenuItem;

        public AbstractMenuItem(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        /**
         * @return css class names
         */
        public List<String> getClassNames() {
            return getComponent().getClassNames().stream().toList();
        }

        /**
         * Sets css class names that should be added to the menu item.
         *
         * @param classNames css class names to add
         */
        public void setClassNames(List<String> classNames) {
            Preconditions.checkNotNullArgument(classNames);

            getComponent().setClassName(null);
            getComponent().addClassNames(classNames.toArray(new String[0]));
        }

        /**
         * Adds css class names that should be added to the menu item.
         *
         * @param classNames css class names to add
         */
        public void addClassNames(String... classNames) {
            Preconditions.checkNotNullArgument(classNames);

            getComponent().addClassNames(classNames);
        }

        /**
         * @return menu component that contains this item
         */
        @Nullable
        public NavigationMenuBar getMenu() {
            return menu;
        }

        protected void setMenu(@Nullable NavigationMenuBar menu) {
            this.menu = menu;
        }

        public abstract T getComponent();

        @Nullable
        public JmixMenuItem getMenuItemWrapper() {
            return menuItemWrapper;
        }

        public void setMenuItemWrapper(@Nullable JmixMenuItem menuItemWrapper) {
            this.menuItemWrapper = menuItemWrapper;
        }

        @Nullable
        public ParentMenuItem getParentMenuItem() {
            return parentMenuItem;
        }

        public void setParentMenuItem(@Nullable ParentMenuItem parentMenuItem) {
            this.parentMenuItem = parentMenuItem;
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
         * @return true if menu item is attached to the menu component, false otherwise
         */
        public boolean isAttachedToMenu() {
            return getMenu() != null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            return id.equals(((AbstractMenuItem<?>) obj).getId());
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

    public static abstract class AbstractIconTextMenuItem<T extends Component & HasComponents>
            extends AbstractMenuItem<T> {

        protected static final String ITEM_ICON_CLASS_NAME = "jmix-navigation-menu-bar-item-icon";
        protected static final String ITEM_ICON_WITH_TITLE_CLASS_NAME = "jmix-navigation-menu-bar-item-icon-with-title";

        protected Icon iconComponent;
        protected Span textComponent;

        public AbstractIconTextMenuItem(String id) {
            super(id);

            initRootComponent();
        }

        protected abstract void initRootComponent();

        protected void updateContent(@Nullable Icon icon, @Nullable String title) {
            getComponent().removeAll();
            iconComponent = null;
            textComponent = null;

            if (icon != null) {
                iconComponent = icon;
                getComponent().add(iconComponent);
            }
            if (!Strings.isNullOrEmpty(title)) {
                textComponent = new Span(title);
                getComponent().add(textComponent);
            }

            if (iconComponent != null) {
                iconComponent.setClassName(ITEM_ICON_CLASS_NAME);
                if (textComponent != null) {
                    iconComponent.addClassName(ITEM_ICON_WITH_TITLE_CLASS_NAME);
                }
            }
        }

        /**
         * Sets menu item text.
         *
         * @param title item text
         */
        public void setTitle(@Nullable String title) {
            updateContent(iconComponent, title);
        }

        @Nullable
        @Override
        public String getTitle() {
            return textComponent != null ? textComponent.getText() : null;
        }

        /**
         * Sets the icon of menu item.
         *
         * @param icon icon to set
         */
        public void setIcon(@Nullable Icon icon) {
            String title = textComponent != null ? textComponent.getText() : null;
            updateContent(icon, title);
        }

        /**
         * @return menu item icon or null if not set
         */
        @Nullable
        public Icon getIcon() {
            return iconComponent;
        }

        public void setDescription(@Nullable String description) {
            Tooltip.forComponent(getComponent()).setText(description);
        }

        /**
         * @return menu item description or null if not set.
         */
        @Nullable
        public String getDescription() {
            return Tooltip.forComponent(getComponent()).getText();
        }
    }

    /**
     * Represents navigation menu bar item.
     */
    public static class MenuItem extends AbstractIconTextMenuItem<RouterLink> {

        protected static final String MENU_ITEM_LINK_CLASS_NAME = "jmix-navigation-menu-bar-item-link";

        protected RouterLink routerLink;

        protected KeyCombination shortcutCombination;

        protected DomListenerRegistration clickHandlerRegistration;
        protected ShortcutRegistration shortcutRegistration;

        public MenuItem(String id) {
            super(id);
        }

        @Override
        protected void initRootComponent() {
            routerLink = new RouterLink();
            routerLink.setClassName(MENU_ITEM_LINK_CLASS_NAME);
            routerLink.setHighlightCondition(HighlightConditions.never());
        }

        @Override
        public RouterLink getComponent() {
            return routerLink;
        }

        /**
         * Sets click handler of the item.
         *
         * @param clickHandler menu item click handler
         */
        @Nullable
        public Registration setClickHandler(@Nullable Consumer<MenuItem> clickHandler) {
            if (clickHandlerRegistration != null) {
                clickHandlerRegistration.remove();
                clickHandlerRegistration = null;
            }
            if (clickHandler != null) {
                clickHandlerRegistration = routerLink.getElement()
                        .addEventListener("click", event -> clickHandler.accept(this));
            }
            return clickHandlerRegistration;
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
         */
        public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
            this.shortcutCombination = shortcutCombination;
            updateShortcutRegistration();
        }

        protected void updateShortcutRegistration() {
            if (shortcutRegistration != null) {
                shortcutRegistration.remove();
                shortcutRegistration = null;
            }

            if (shortcutCombination != null) {
                Key key = shortcutCombination.getKey();
                KeyModifier[] keyModifiers = shortcutCombination.getKeyModifiers();

                shortcutRegistration =
                        Shortcuts.addShortcutListener(routerLink, this::onShortcutEvent, key, keyModifiers);
            }
        }

        protected void onShortcutEvent(ShortcutEvent event) {
            event.getLifecycleOwner().getElement().executeJs("this.click()");
        }
    }

    /**
     * Describes menu item that can contain other menu items.
     */
    public static class ParentMenuItem extends AbstractIconTextMenuItem<HorizontalLayout>
            implements io.jmix.flowui.kit.component.menu.ParentMenuItem<AbstractMenuItem<?>> {

        protected List<AbstractMenuItem<?>> children;
        protected HorizontalLayout hbox;

        public ParentMenuItem(String id) {
            super(id);
        }

        @Override
        protected void initRootComponent() {
            hbox = new HorizontalLayout();
            hbox.setAlignItems(FlexComponent.Alignment.CENTER);
            hbox.setSpacing(false);
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

        @Override
        public HorizontalLayout getComponent() {
            return hbox;
        }

        /**
         * Adds child menu item.
         *
         * @param menuItem menu item to add
         */
        @Override
        public void addChildItem(AbstractMenuItem<?> menuItem) {
            Preconditions.checkNotNullArgument(menuItem);

            if (children == null) {
                children = new ArrayList<>();
            }

            if (isAttachedToMenu()) {
                menu.addChildItem(menuItem, this, null);
            }

            children.add(menuItem);
        }

        @Override
        public void addChildItem(AbstractMenuItem<?> menuItem, int index) {
            Preconditions.checkNotNullArgument(menuItem);

            if (children == null) {
                children = new ArrayList<>();
            }

            if (isAttachedToMenu()) {
                menu.addChildItem(menuItem, this, index);
            }

            children.add(index, menuItem);
        }

        @Override
        public void removeChildItem(AbstractMenuItem<?> menuItem) {
            Preconditions.checkNotNullArgument(menuItem);

            if (!hasChildren()) {
                return;
            }

            if (!Objects.equals(menuItem.getMenu(), menu)) {
                throw new IllegalArgumentException("The item is not attached to the same menu");
            }

            children.stream()
                    .filter(item -> item.equals(menuItem))
                    .findFirst()
                    .ifPresent((item) -> {
                        if (isAttachedToMenu()) {
                            menu.removeChildItem(menuItem, this);
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
                for (AbstractMenuItem<?> menuItem : children) {
                    menu.removeChildItem(menuItem, this);
                }
            }

            children.clear();
        }

        @Override
        public List<AbstractMenuItem<?>> getChildren() {
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
    public static class MenuSeparatorItem extends AbstractMenuItem<Hr> {

        protected Hr hr;

        public MenuSeparatorItem(String id) {
            super(id);
            hr = new Hr();
        }

        @Override
        public boolean isSeparator() {
            return true;
        }

        @Override
        public Hr getComponent() {
            return hr;
        }

        @Nullable
        @Override
        public String getTitle() {
            return null;
        }
    }

    /**
     * Represents an item which navigates to a view on click
     */
    public static class ViewMenuItem extends MenuItem {

        protected Class<? extends View<?>> viewClass;
        protected RouteParameters routeParameters;

        public ViewMenuItem(String id, Class<? extends View<?>> viewClass) {
            super(id);

            this.viewClass = viewClass;
            routerLink.setRoute(viewClass);
        }

        /**
         * @return query parameters of the view to navigate
         */
        @Nullable
        public QueryParameters getUrlQueryParameters() {
            return routerLink.getQueryParameters().orElse(null);
        }

        /**
         * Sets query parameters of the view to navigate.
         *
         * @param queryParameters query parameters
         */
        public void setUrlQueryParameters(List<MenuItemParameter> queryParameters) {
            Map<String, String> parametersMap = queryParameters.stream()
                    .collect(Collectors.toMap(MenuItemParameter::getName, MenuItemParameter::getValue));

            routerLink.setQueryParameters(QueryParameters.simple(parametersMap));
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
         */
        public void setRouteParameters(List<MenuItemParameter> routeParameters) {
            Map<String, String> parametersMap = routeParameters.stream()
                    .collect(Collectors.toMap(MenuItemParameter::getName, MenuItemParameter::getValue));

            this.routeParameters = new RouteParameters(parametersMap);

            routerLink.setRoute(viewClass, this.routeParameters);
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
         */
        public void setViewClass(Class<? extends View<?>> viewClass) {
            this.viewClass = viewClass;
            if (routeParameters == null) {
                routerLink.setRoute(viewClass);
            } else {
                routerLink.setRoute(viewClass, routeParameters);
            }
        }

        @Override
        public Registration setClickHandler(@Nullable Consumer<MenuItem> clickHandler) {
            //no-op - always navigate to a view
            return () -> {
            };
        }
    }
}
