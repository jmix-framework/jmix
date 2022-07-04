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

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.menu.ListMenuBuilder;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.UiController;
import io.jmix.flowui.sys.UiDescriptorUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.List;

public class JmixListMenu extends ListMenu implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected UiComponents uiComponents;
    protected ViewRegistry viewRegistry;

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
        getContent().addClassNames(JMIX_LIST_MENU_STYLE_NAME, LIST_NONE_STYLE_NAME);
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
            menuItemComponent.setRoute(getControllerClass((ViewMenuItem) menuItem));
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
                && targetView.getAnnotation(UiController.class) != null;
    }

    /**
     * Describes menu item that should navigate to the view.
     */
    public static class ViewMenuItem extends MenuItem {

        protected Class<? extends View<?>> controllerClass;

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
            return new ViewMenuItem(UiDescriptorUtils.getInferredViewId(controllerClass))
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
        public ViewMenuItem withIcon(@Nullable VaadinIcon icon) {
            super.withIcon(icon);
            return this;
        }

        @Override
        public ViewMenuItem withClassNames(List<String> classNames) {
            super.withClassNames(classNames);
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
}
