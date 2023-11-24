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

import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.menu.provider.MenuConfigMenuItemProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Menu item provider for {@link io.jmix.flowui.component.navigationmenubar.NavigationMenuBar}
 */
@Component("flowui_MenuConfigNavigationMenuBarItemProvider")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MenuConfigNavigationMenuBarItemProvider extends MenuConfigMenuItemProvider<NavigationMenuBar.MenuItem> {

    protected NavigationMenuBarItemConverter itemConverter;

    public MenuConfigNavigationMenuBarItemProvider(MenuConfig menuConfig,
                                                   NavigationMenuBarItemConverter itemConverter) {
        super(menuConfig);
        this.itemConverter = itemConverter;
    }

    @Override
    protected List<NavigationMenuBar.MenuItem> convertToMenuItems(Collection<MenuItem> menuConfigItems) {
        return menuConfigItems.stream()
                .flatMap(item -> itemConverter.createMenuItemWithChildren(item).stream())
                .toList();
    }
}
