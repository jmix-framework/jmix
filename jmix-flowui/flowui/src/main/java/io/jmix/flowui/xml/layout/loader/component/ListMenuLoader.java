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

import io.jmix.flowui.component.main.JmixListMenu;
import io.jmix.flowui.menu.provider.MenuConfigListMenuItemProvider;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

public class ListMenuLoader extends AbstractComponentLoader<JmixListMenu> {

    @Override
    public void loadComponent() {
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);

        loadMenuConfig(element);
    }

    @Override
    protected JmixListMenu createComponent() {
        return factory.create(JmixListMenu.class);
    }

    protected void loadMenuConfig(Element element) {
        Boolean loadMenuConfig = getLoaderSupport()
                .loadBoolean(element, "loadMenuConfig")
                .orElse(true);

        if (loadMenuConfig) {
            MenuConfigListMenuItemProvider itemProvider =
                    applicationContext.getBean(MenuConfigListMenuItemProvider.class);
            resultComponent.setMenuItemProvider(itemProvider);
            itemProvider.load();
        }
    }
}
