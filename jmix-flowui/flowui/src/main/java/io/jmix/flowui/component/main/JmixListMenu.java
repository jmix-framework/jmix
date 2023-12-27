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

package io.jmix.flowui.component.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.menu.ListMenuBuilder;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem.MenuItemParameter;
import io.jmix.flowui.menu.provider.HasMenuItemProvider;
import io.jmix.flowui.menu.provider.MenuItemProvider;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JmixListMenu extends ListMenu implements ApplicationContextAware, InitializingBean,
        HasMenuItemProvider<ListMenu.MenuItem> {

    protected ApplicationContext applicationContext;

    protected UiComponents uiComponents;
    protected ViewRegistry viewRegistry;

    protected MenuItemProvider<ListMenu.MenuItem> itemProvider;
    protected Subscription itemCollectionChangedSubscription;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        viewRegistry = applicationContext.getBean(ViewRegistry.class);
    }

    protected void initComponent() {
        getContent().addClassNames(JMIX_LIST_MENU_CLASS_NAME, LIST_NONE_CLASS_NAME);
    }

    /**
     * Loads menu items from {@link MenuConfig}.
     */
    public void loadMenuConfig() {
        applicationContext.getBean(ListMenuBuilder.class)
                .build(this);
    }

    protected RouterLink createMenuItemComponent(MenuItem menuItem) {
        RouterLink menuItemComponent = super.createMenuItemComponent(menuItem);
        if (menuItem instanceof ViewMenuItem) {
            QueryParameters queryParameters = ((ViewMenuItem) menuItem).getUrlQueryParameters();
            RouteParameters routeParameters = ((ViewMenuItem) menuItem).getRouteParameters();

            if (queryParameters != null) {
                menuItemComponent.setQueryParameters(queryParameters);
            }

            if (routeParameters != null) {
                menuItemComponent.setRoute(getControllerClass((ViewMenuItem) menuItem), routeParameters);
            } else {
                menuItemComponent.setRoute(getControllerClass((ViewMenuItem) menuItem));
            }

            menuItemComponent.setHighlightCondition(HighlightConditions.sameLocation());
        }

        return menuItemComponent;
    }

    @Override
    protected void addMenuItemClickListener(RouterLink routerLink, MenuItem menuItem) {
        if (!(menuItem instanceof ViewMenuItem)) {
            super.addMenuItemClickListener(routerLink, menuItem);
        }
    }

    protected Class<? extends View<?>> getControllerClass(ViewMenuItem menuItem) {
        Class<? extends View<?>> controllerClass = menuItem.getControllerClass();

        if (controllerClass != null && isSupportedView(controllerClass)) {
            return menuItem.getControllerClass();
        }
        ViewInfo viewInfo = viewRegistry.getViewInfo(menuItem.getId());
        return viewInfo.getControllerClass();
    }

    protected boolean isSupportedView(Class<?> targetView) {
        return View.class.isAssignableFrom(targetView)
                && targetView.getAnnotation(ViewController.class) != null;
    }

    @Override
    public void setMenuItemProvider(@Nullable MenuItemProvider<ListMenu.MenuItem> itemProvider) {
        if (Objects.equals(this.itemProvider, itemProvider)) {
            return;
        }
        if (itemCollectionChangedSubscription != null) {
            itemCollectionChangedSubscription.remove();
            itemCollectionChangedSubscription = null;
        }
        this.itemProvider = itemProvider;
        if (itemProvider != null) {
            itemCollectionChangedSubscription =
                    itemProvider.addCollectionChangedListener(this::onMenuItemCollectionChanged);
        }
    }

    protected void onMenuItemCollectionChanged(MenuItemProvider.CollectionChangeEvent<ListMenu.MenuItem> e) {
        removeAllMenuItems();
        e.getItems().forEach(this::addMenuItem);
    }

    @Override
    @Nullable
    public MenuItemProvider<ListMenu.MenuItem> getMenuItemProvider() {
        return this.itemProvider;
    }

    /**
     * Describes menu item that should navigate to the view.
     */
    public static class ViewMenuItem extends MenuItem {

        protected Class<? extends View<?>> controllerClass;
        protected QueryParameters urlQueryParameters;
        protected RouteParameters routeParameters;

        public ViewMenuItem(String id) {
            super(id);
        }

        /**
         * Creates menu item that should navigate to the view with provided ID.
         *
         * @param viewId view ID
         * @return menu item
         */
        public static ViewMenuItem create(String viewId) {
            return new ViewMenuItem(viewId);
        }

        public static MenuItem create(Class<? extends View<?>> controllerClass) {
            return new ViewMenuItem(ViewDescriptorUtils.getInferredViewId(controllerClass))
                    .withControllerClass(controllerClass);
        }

        @Override
        public ViewMenuItem withTitle(@Nullable String title) {
            super.withTitle(title);
            return this;
        }

        @Override
        public ViewMenuItem withDescription(@Nullable String description) {
            super.withDescription(description);
            return this;
        }

        @Override
        @Deprecated(since="2.2", forRemoval=true)
        public ViewMenuItem withIcon(@Nullable VaadinIcon icon) {
            super.withIcon(icon);
            return this;
        }

        @Override
        public ViewMenuItem withClassNames(List<String> classNames) {
            super.withClassNames(classNames);
            return this;
        }

        @Override
        public ViewMenuItem withSuffixComponent(Component suffixComponent) {
            return (ViewMenuItem) super.withSuffixComponent(suffixComponent);
        }

        @Override
        public ViewMenuItem withPrefixComponent(Component prefixComponent) {
            return (ViewMenuItem) super.withPrefixComponent(prefixComponent);
        }

        public ViewMenuItem withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
            return (ViewMenuItem) super.withShortcutCombination(shortcutCombination);
        }

        @Nullable
        public QueryParameters getUrlQueryParameters() {
            return urlQueryParameters;
        }

        public ViewMenuItem withUrlQueryParameters(List<MenuItemParameter> queryParameters) {
            Map<String, String> parametersMap = queryParameters.stream()
                    .collect(Collectors.toMap(MenuItemParameter::getName, MenuItemParameter::getValue));

            this.urlQueryParameters = QueryParameters.simple(parametersMap);
            return this;
        }

        @Nullable
        public RouteParameters getRouteParameters() {
            return routeParameters;
        }

        public ViewMenuItem withRouteParameters(List<MenuItemParameter> routeParameters) {
            Map<String, String> parametersMap = routeParameters.stream()
                    .collect(Collectors.toMap(MenuItemParameter::getName, MenuItemParameter::getValue));

            this.routeParameters = new RouteParameters(parametersMap);
            return this;
        }

        /**
         * @return view class or {@code null} if not set
         */
        @Nullable
        public Class<? extends View<?>> getControllerClass() {
            return controllerClass;
        }

        /**
         * Sets view class that should be shown when the user clicks on the menu item. If not set, {@link #getId()}
         * will be used as view id to navigate.
         *
         * @param controllerClass view class to set
         * @return current menu instance
         */
        public ViewMenuItem withControllerClass(@Nullable Class<? extends View<?>> controllerClass) {
            this.controllerClass = controllerClass;
            return this;
        }
    }

    public static class BeanMenuItem extends MenuItem {

        public BeanMenuItem(String id) {
            super(id);
        }

        public static BeanMenuItem create(String id) {
            return new BeanMenuItem(id);
        }

        @Override
        public BeanMenuItem withTitle(@Nullable String title) {
            super.withTitle(title);
            return this;
        }

        @Override
        public BeanMenuItem withDescription(@Nullable String description) {
            super.withDescription(description);
            return this;
        }

        @Override
        @Deprecated(since="2.2", forRemoval=true)
        public BeanMenuItem withIcon(@Nullable VaadinIcon icon) {
            super.withIcon(icon);
            return this;
        }

        @Override
        public BeanMenuItem withClassNames(List<String> classNames) {
            super.withClassNames(classNames);
            return this;
        }

        @Override
        public BeanMenuItem withSuffixComponent(Component suffixComponent) {
            return (BeanMenuItem) super.withSuffixComponent(suffixComponent);
        }

        @Override
        public BeanMenuItem withPrefixComponent(Component prefixComponent) {
            return (BeanMenuItem) super.withPrefixComponent(prefixComponent);
        }

        public BeanMenuItem withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
            return (BeanMenuItem) super.withShortcutCombination(shortcutCombination);
        }
    }
}
