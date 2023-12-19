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

package io.jmix.flowui.xml.layout.loader.component;

import io.jmix.flowui.component.navigationmenubar.MenuConfigNavigationMenuBarItemProvider;
import io.jmix.flowui.component.navigationmenubar.NavigationMenuBar;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public class NavigationMenuBarLoader extends AbstractComponentLoader<NavigationMenuBar> {

    @Override
    protected NavigationMenuBar createComponent() {
        return factory.create(NavigationMenuBar.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);

        loadMenuConfig();
    }

    protected void loadMenuConfig() {
        Boolean loadMenuConfig = getLoaderSupport()
                .loadBoolean(element, "loadMenuConfig")
                .orElse(true);

        if (loadMenuConfig) {
            MenuConfigNavigationMenuBarItemProvider itemProvider =
                    applicationContext.getBean(MenuConfigNavigationMenuBarItemProvider.class);
            resultComponent.setMenuItemProvider(itemProvider);
            itemProvider.load();
        }
    }
}
