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
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenInfo;
import io.jmix.flowui.screen.ScreenRegistry;
import io.jmix.flowui.screen.UiController;
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
    protected ScreenRegistry screenRegistry;

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
        screenRegistry = applicationContext.getBean(ScreenRegistry.class);
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
        if (menuItem instanceof ScreenMenuItem) {
            menuItemComponent.setRoute(getControllerClass((ScreenMenuItem) menuItem));
            menuItemComponent.setHighlightCondition(HighlightConditions.sameLocation());
        }
        return menuItemComponent;
    }

    @Override
    protected void addMenuItemClickListener(RouterLink routerLink, MenuItem menuItem) {
        if (!(menuItem instanceof ScreenMenuItem)) {
            super.addMenuItemClickListener(routerLink, menuItem);
        }
    }

    protected Class<? extends Screen<?>> getControllerClass(ScreenMenuItem menuItem) {
        Class<? extends Screen<?>> controllerClass = menuItem.getControllerClass();

        if (controllerClass != null && isSupportedScreen(controllerClass)) {
            return menuItem.getControllerClass();
        }
        ScreenInfo screenInfo = screenRegistry.getScreenInfo(menuItem.getId());
        return screenInfo.getControllerClass();
    }

    protected boolean isSupportedScreen(Class<?> targetView) {
        return Screen.class.isAssignableFrom(targetView)
                && targetView.getAnnotation(UiController.class) != null;
    }

    /**
     * Describes menu item that should navigate to the screen.
     */
    public static class ScreenMenuItem extends MenuItem {

        protected Class<? extends Screen<?>> controllerClass;

        public ScreenMenuItem(String id) {
            super(id);
        }

        /**
         * Creates menu item that should navigate to the screen with provided ID.
         *
         * @param screenId screen ID
         * @return menu item
         */
        public static ScreenMenuItem create(String screenId) {
            return new ScreenMenuItem(screenId);
        }

        public static MenuItem create(Class<? extends Screen<?>> controllerClass) {
            return new ScreenMenuItem(UiDescriptorUtils.getInferredScreenId(controllerClass))
                    .withControllerClass(controllerClass);
        }

        @Override
        public ScreenMenuItem withTitle(@Nullable String title) {
            super.withTitle(title);
            return this;
        }

        @Override
        public ScreenMenuItem withDescription(@Nullable String description) {
            super.withDescription(description);
            return this;
        }

        @Override
        public ScreenMenuItem withIcon(@Nullable VaadinIcon icon) {
            super.withIcon(icon);
            return this;
        }

        @Override
        public ScreenMenuItem withClassNames(List<String> classNames) {
            super.withClassNames(classNames);
            return this;
        }

        /**
         * @return screen class or {@code null} if not set
         */
        @Nullable
        public Class<? extends Screen<?>> getControllerClass() {
            return controllerClass;
        }

        /**
         * Sets screen class that should be shown when the user clicks on the menu item. If not set, {@link #getId()}
         * will be used as screen id to navigate.
         *
         * @param controllerClass screen class to set
         * @return current menu instance
         */
        public ScreenMenuItem withControllerClass(@Nullable Class<? extends Screen<?>> controllerClass) {
            this.controllerClass = controllerClass;
            return this;
        }
    }
}
