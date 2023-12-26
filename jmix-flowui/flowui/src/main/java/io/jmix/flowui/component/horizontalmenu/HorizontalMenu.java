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

package io.jmix.flowui.component.horizontalmenu;

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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasTooltip;
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
public class HorizontalMenu extends Composite<JmixMenuBar>
        implements HasMenuItemProvider<HorizontalMenu.AbstractMenuItem<?>>, HasSize, HasStyle {

    protected static final String HORIZONTAL_MENU_CLASS_NAME = "jmix-horizontal-menu";
    protected static final String ROOT_MENU_ITEM_CLASS_NAME = "jmix-horizontal-menu-root-item";
    protected static final String ITEM_WRAPPER_CLASS_NAME = "jmix-horizontal-menu-item-wrapper";
    protected static final int ADD_TO_END_INDEX = -1;

    protected MenuItemProvider<AbstractMenuItem<?>> menuItemProvider;
    protected Subscription itemCollectionChangedSubscription;

    protected List<AbstractMenuItem<?>> rootMenuItems = new ArrayList<>();
    protected Map<String, AbstractMenuItem<?>> allMenuItems = new HashMap<>();

    @Override
    protected JmixMenuBar initContent() {
        JmixMenuBar menuBar = super.initContent();
        menuBar.setClassName(HORIZONTAL_MENU_CLASS_NAME);
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
     * Adds menu item and its children to menu root.
     *
     * @param menuItem menu item to add
     */
    public void addMenuItem(AbstractMenuItem<?> menuItem) {
        Preconditions.checkNotNullArgument(menuItem);

        addMenuItemRecursive(menuItem, null, ADD_TO_END_INDEX);

        rootMenuItems.add(menuItem);
    }

    protected void addMenuItemRecursive(AbstractMenuItem<?> childMenuItem,
                                        @Nullable ParentMenuItem parentMenuItem,
                                        int index) {
        checkDuplicateItemId(getMenuItemId(childMenuItem));

        JmixSubMenu parentSubMenu = getParentSubMenu(parentMenuItem);

        if (childMenuItem.isMenu()) {
            addMenuItemToSubMenu(childMenuItem, parentSubMenu, index);

            ParentMenuItem childParentMenuItem = (ParentMenuItem) childMenuItem;
            for (AbstractMenuItem<?> item : childParentMenuItem.getChildItems()) {
                addMenuItemRecursive(item, childParentMenuItem, ADD_TO_END_INDEX);
            }
        } else if (childMenuItem.isSeparator()) {
            if (parentSubMenu != null) {
                addComponentToSubMenu(childMenuItem, parentSubMenu, index);
            } else {
                throw new UnsupportedOperationException("Separators are not supported on the root level");
            }
        } else {
            addMenuItemToSubMenu(childMenuItem, parentSubMenu, index);
        }

        attachMenuItem(childMenuItem);
    }

    protected String getMenuItemId(AbstractMenuItem<?> menuItem) {
        return menuItem.getId().orElseThrow(() -> new IllegalStateException("Menu item id is not defined"));
    }

    protected void checkDuplicateItemId(String id) {
        if (allMenuItems.containsKey(id)) {
            throw new IllegalArgumentException("Menu item with id \"%s\" already exists".formatted(id));
        }
    }

    @Nullable
    protected JmixSubMenu getParentSubMenu(@Nullable ParentMenuItem parentMenuItem) {
        if (parentMenuItem == null) {
            return null;
        }

        JmixMenuItem itemWrapper = parentMenuItem.getMenuItemWrapper();
        if (itemWrapper == null) {
            throw new IllegalArgumentException("Parent menu item is not attached to the menu");
        }
        return itemWrapper.getSubMenu();
    }

    protected void addMenuItemToSubMenu(AbstractMenuItem<?> menuItem,
                                        @Nullable JmixSubMenu subMenu,
                                        int index) {
        HasMenuItemsEnhanced hasMenuItems = subMenu != null ? subMenu : getContent();

        JmixMenuItem wrapper = index == ADD_TO_END_INDEX
                ? hasMenuItems.addItem(menuItem)
                : hasMenuItems.addItemAtIndex(index, menuItem);
        wrapper.addClassName(ITEM_WRAPPER_CLASS_NAME);
        menuItem.setMenuItemWrapper(wrapper);

        if (subMenu == null) {
            menuItem.addClassNames(ROOT_MENU_ITEM_CLASS_NAME);
        }
    }

    protected void addComponentToSubMenu(Component component, JmixSubMenu subMenu, int index) {
        if (index == ADD_TO_END_INDEX) {
            subMenu.add(component);
        } else {
            subMenu.addComponentAtIndex(index, component);
        }
    }

    protected void attachMenuItem(AbstractMenuItem<?> menuItem) {
        allMenuItems.put(getMenuItemId(menuItem), menuItem);

        menuItem.setMenu(this);
    }

    /**
     * Adds menu item and its children to menu root at the specified position.
     *
     * @param menuItem menu item to add
     * @param index    index at which the item will be inserted
     */
    public void addMenuItem(AbstractMenuItem<?> menuItem, int index) {
        Preconditions.checkNotNullArgument(menuItem);

        addMenuItemRecursive(menuItem, null, index);

        rootMenuItems.add(index, menuItem);
    }

    protected void addChildItem(AbstractMenuItem<?> menuItem, ParentMenuItem parentMenuItem, int index) {
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

        if (!allMenuItems.containsKey(getMenuItemId(menuItem))) {
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

        detachMenuItemRecursive(menuItem);
    }

    protected void detachMenuItemRecursive(AbstractMenuItem<?> menuItem) {
        menuItem.setMenu(null);
        menuItem.setMenuItemWrapper(null);

        allMenuItems.remove(getMenuItemId(menuItem));

        if (menuItem.isMenu()) {
            for (AbstractMenuItem<?> item : ((ParentMenuItem) menuItem).getChildItems()) {
                detachMenuItemRecursive(item);
            }
        }
    }

    protected void removeChildItem(AbstractMenuItem<?> menuItem, ParentMenuItem parentMenuItem) {
        JmixMenuItem parentItemWrapper = parentMenuItem.getMenuItemWrapper();
        JmixMenuItem itemWrapper = menuItem.getMenuItemWrapper();
        if (itemWrapper != null && parentItemWrapper != null) {
            parentItemWrapper.getSubMenu().remove(itemWrapper);
        }

        detachMenuItemRecursive(menuItem);
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

    /**
     * Provides base functionality for horizontal menu items
     *
     * @param <T> root component type
     */
    public static abstract class AbstractMenuItem<T extends Component> extends Composite<T>
            implements io.jmix.flowui.kit.component.menu.MenuItem {

        protected HorizontalMenu menu;
        protected JmixMenuItem menuItemWrapper;
        protected ParentMenuItem parentMenuItem;

        public AbstractMenuItem() {
        }

        public AbstractMenuItem(String id) {
            setId(id);
        }

        /**
         * @return menu component that contains this item
         */
        @Nullable
        public HorizontalMenu getMenu() {
            return menu;
        }

        protected void setMenu(@Nullable HorizontalMenu menu) {
            this.menu = menu;
        }

        @Nullable
        protected JmixMenuItem getMenuItemWrapper() {
            return menuItemWrapper;
        }

        protected void setMenuItemWrapper(@Nullable JmixMenuItem menuItemWrapper) {
            this.menuItemWrapper = menuItemWrapper;
        }

        /**
         * @return parent menu item of this item or null if no parent has been set.
         */
        @Nullable
        public ParentMenuItem getParentMenuItem() {
            return parentMenuItem;
        }

        protected void setParentMenuItem(@Nullable ParentMenuItem parentMenuItem) {
            this.parentMenuItem = parentMenuItem;
        }

        /**
         * @return true if the item can contain other items, false otherwise
         * @see ParentMenuItem
         */
        public boolean isMenu() {
            return false;
        }

        /**
         * @return true if the item is a separator, false otherwise
         * @see SeparatorMenuItem
         */
        public boolean isSeparator() {
            return false;
        }

        /**
         * @return true if the item is attached to menu component, false otherwise
         */
        public boolean isAttachedToMenu() {
            return getMenu() != null;
        }
    }

    /**
     * Provides base functionality for items that contain text with suffix and prefix components
     *
     * @param <T> root component type
     */
    public static abstract class AbstractContentMenuItem<T extends Component & HasComponents>
            extends AbstractMenuItem<T> implements HasTooltip, HasPrefix, HasSuffix {

        protected static final String CONTENT_ITEM_CLASS_NAME = "jmix-horizontal-menu-content-item";
        protected static final String PREFIX_COMPONENT_CLASS_NAME = "prefix-component";
        protected static final String TEXT_COMPONENT_CLASS_NAME = "text-component";
        protected static final String SUFFIX_COMPONENT_CLASS_NAME = "suffix-component";
        protected static final String ITEM_WRAPPER_SUFFIXED_ATTRIBUTE_NAME = "suffixed";

        protected Component prefixComponent;
        protected Span textComponent;
        protected Component suffixComponent;
        protected Tooltip tooltip;

        public AbstractContentMenuItem() {
        }

        public AbstractContentMenuItem(String id) {
            super(id);
        }

        @Override
        protected T initContent() {
            T content = super.initContent();
            content.addClassName(CONTENT_ITEM_CLASS_NAME);
            return content;
        }

        /**
         * Sets menu item text.
         *
         * @param title item text
         */
        public void setTitle(@Nullable String title) {
            updateContent(prefixComponent, title, suffixComponent);
        }

        protected void updateContent(@Nullable Component prefixComponent,
                                     @Nullable String title,
                                     @Nullable Component suffixComponent) {
            setPrefixComponentInternal(prefixComponent);
            setTextComponentInternal(title);
            setSuffixComponentInternal(suffixComponent);
        }

        protected void setPrefixComponentInternal(@Nullable Component prefixComponent) {
            if (this.prefixComponent != null) {
                this.prefixComponent.removeClassName(PREFIX_COMPONENT_CLASS_NAME);
                getContent().remove(this.prefixComponent);
            }

            this.prefixComponent = prefixComponent;
            if (prefixComponent != null) {
                prefixComponent.addClassName(PREFIX_COMPONENT_CLASS_NAME);
                getContent().addComponentAsFirst(prefixComponent);
            }
        }

        protected void setTextComponentInternal(@Nullable String title) {
            if (this.textComponent != null) {
                this.textComponent.removeClassName(TEXT_COMPONENT_CLASS_NAME);
                getContent().remove(this.textComponent);
            }

            if (Strings.isNullOrEmpty(title)) {
                this.textComponent = null;
            } else {
                this.textComponent = new Span(title);
                this.textComponent.addClassName(TEXT_COMPONENT_CLASS_NAME);
                getContent().add(textComponent);
            }
        }

        protected void setSuffixComponentInternal(@Nullable Component suffixComponent) {
            if (this.suffixComponent != null) {
                this.suffixComponent.removeClassName(SUFFIX_COMPONENT_CLASS_NAME);
                getContent().remove(this.suffixComponent);

                setWrapperSuffixedAttribute(false);
            }

            this.suffixComponent = suffixComponent;
            if (suffixComponent != null) {
                suffixComponent.addClassName(SUFFIX_COMPONENT_CLASS_NAME);
                getContent().add(suffixComponent);

                setWrapperSuffixedAttribute(true);
            }
        }

        protected void setWrapperSuffixedAttribute(boolean suffixed) {
            JmixMenuItem wrapper = getMenuItemWrapper();
            if (wrapper != null) {
                wrapper.getElement().setAttribute(ITEM_WRAPPER_SUFFIXED_ATTRIBUTE_NAME, suffixed);
            }
        }

        @Override
        public void setPrefixComponent(@Nullable Component component) {
            updateContent(component, getTitle(), suffixComponent);
        }

        @Override
        @Nullable
        public Component getPrefixComponent() {
            return prefixComponent;
        }

        @Override
        public void setSuffixComponent(@Nullable Component component) {
            updateContent(prefixComponent, getTitle(), component);
        }

        @Override
        @Nullable
        public Component getSuffixComponent() {
            return suffixComponent;
        }

        @Nullable
        @Override
        public String getTitle() {
            return textComponent != null ? textComponent.getText() : null;
        }

        @Override
        public Tooltip setTooltipText(String text) {
            Tooltip tooltip = getTooltipInternal();

            tooltip.setText(text);
            return tooltip;
        }

        protected Tooltip getTooltipInternal() {
            if (tooltip == null) {
                tooltip = Tooltip.forComponent(this);
            }
            return tooltip;
        }

        @Override
        public Tooltip getTooltip() {
            return getTooltipInternal();
        }
    }

    /**
     * Represents horizontal menu item that can run some action on click
     */
    public static class MenuItem extends AbstractContentMenuItem<RouterLink> {

        protected static final String MENU_ITEM_CLASS_NAME = "jmix-horizontal-menu-item";

        protected KeyCombination shortcutCombination;

        protected DomListenerRegistration clickHandlerRegistration;
        protected ShortcutRegistration shortcutRegistration;

        public MenuItem() {
        }

        public MenuItem(String id) {
            super(id);
        }

        @Override
        protected RouterLink initContent() {
            RouterLink routerLink = super.initContent();
            routerLink.addClassName(MENU_ITEM_CLASS_NAME);
            routerLink.setHighlightCondition(HighlightConditions.never());
            return routerLink;
        }

        /**
         * Sets click handler of the item.
         *
         * @param clickHandler menu item click handler
         * @return click handler registration
         */
        @Nullable
        public Registration setClickHandler(@Nullable Consumer<MenuItem> clickHandler) {
            if (clickHandlerRegistration != null) {
                clickHandlerRegistration.remove();
                clickHandlerRegistration = null;
            }
            if (clickHandler != null) {
                clickHandlerRegistration = getElement()
                        .addEventListener("click", event -> clickHandler.accept(this));
            }
            return clickHandlerRegistration;
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

        /**
         * @return shortcut key combination of the item
         */
        @Nullable
        public KeyCombination getShortcutCombination() {
            return shortcutCombination;
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
                        Shortcuts.addShortcutListener(getContent(), this::onShortcutEvent, key, keyModifiers);
            }
        }

        protected void onShortcutEvent(ShortcutEvent event) {
            event.getLifecycleOwner().getElement().executeJs("this.click()");
        }
    }

    /**
     * Represents menu item that can contain other menu items.
     */
    public static class ParentMenuItem extends AbstractContentMenuItem<HorizontalLayout>
            implements io.jmix.flowui.kit.component.menu.ParentMenuItem<AbstractMenuItem<?>> {

        protected List<AbstractMenuItem<?>> children;

        public ParentMenuItem() {
        }

        public ParentMenuItem(String id) {
            super(id);
        }

        @Override
        protected HorizontalLayout initContent() {
            HorizontalLayout horizontalLayout = super.initContent();
            horizontalLayout.setWidthFull();
            return horizontalLayout;
        }

        @Override
        public boolean isOpened() {
            throw new UnsupportedOperationException(
                    "Parent item of horizontal menu doesn't support retrieving open state from server");
        }

        @Override
        public void setOpened(boolean opened) {
            throw new UnsupportedOperationException(
                    "Parent item of horizontal menu doesn't support opening from server");
        }

        @Override
        public void addChildItem(AbstractMenuItem<?> menuItem) {
            Preconditions.checkNotNullArgument(menuItem);

            if (children == null) {
                children = new ArrayList<>();
            }

            if (isAttachedToMenu()) {
                menu.addChildItem(menuItem, this, ADD_TO_END_INDEX);
            }

            children.add(menuItem);
            menuItem.setParentMenuItem(this);
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
            menuItem.setParentMenuItem(this);
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
        public List<AbstractMenuItem<?>> getChildItems() {
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
     * Represents a separator
     */
    public static class SeparatorMenuItem extends AbstractMenuItem<Hr> {

        public SeparatorMenuItem() {
        }

        public SeparatorMenuItem(String id) {
            super(id);
        }

        @Override
        public boolean isSeparator() {
            return true;
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

        public ViewMenuItem() {
        }

        public ViewMenuItem(String id, Class<? extends View<?>> viewClass) {
            super(id);

            this.viewClass = viewClass;
            getContent().setRoute(viewClass);
        }

        /**
         * @return query parameters of the view to navigate
         */
        @Nullable
        public QueryParameters getUrlQueryParameters() {
            return getContent().getQueryParameters().orElse(null);
        }

        /**
         * Sets query parameters of the view to navigate.
         *
         * @param queryParameters query parameters
         */
        public void setUrlQueryParameters(List<MenuItemParameter> queryParameters) {
            Map<String, String> parametersMap = queryParameters.stream()
                    .collect(Collectors.toMap(MenuItemParameter::getName, MenuItemParameter::getValue));

            getContent().setQueryParameters(QueryParameters.simple(parametersMap));
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

            if (viewClass != null) {
                getContent().setRoute(viewClass, this.routeParameters);
            }
        }

        /**
         * @return view class or null if not set
         */
        @Nullable
        public Class<? extends View<?>> getViewClass() {
            return viewClass;
        }

        /**
         * Sets view class that should be shown when the user clicks on the menu item.
         *
         * @param viewClass view class to set
         */
        public void setViewClass(Class<? extends View<?>> viewClass) {
            this.viewClass = viewClass;
            if (routeParameters == null) {
                getContent().setRoute(viewClass);
            } else {
                getContent().setRoute(viewClass, routeParameters);
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
