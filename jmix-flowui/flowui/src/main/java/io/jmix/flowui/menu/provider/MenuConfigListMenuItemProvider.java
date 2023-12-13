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

package io.jmix.flowui.menu.provider;

import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.menu.ListMenuBuilder;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Menu item provider for {@link io.jmix.flowui.component.main.JmixListMenu}
 */
@Component("flowui_MenuConfigListMenuItemProvider")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MenuConfigListMenuItemProvider extends MenuConfigMenuItemProvider<ListMenu.MenuItem> {

    protected ObjectProvider<ListMenuBuilder> menuBuilderFactory;

    public MenuConfigListMenuItemProvider(MenuConfig menuConfig, ObjectProvider<ListMenuBuilder> menuBuilderFactory) {
        super(menuConfig);
        this.menuBuilderFactory = menuBuilderFactory;
    }

    @Override
    protected List<ListMenu.MenuItem> convertToMenuItems(Collection<MenuItem> menuConfigItems) {
        ListMenuBuilder menuBuilder = menuBuilderFactory.getObject();
        return menuConfigItems.stream()
                .flatMap(menuItem -> menuBuilder.createListMenu(menuItem).stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
