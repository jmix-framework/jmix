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

import io.jmix.flowui.component.horizontalmenu.MenuConfigHorizontalMenuItemProvider;
import io.jmix.flowui.component.horizontalmenu.HorizontalMenu;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public class HorizontalMenuLoader extends AbstractComponentLoader<HorizontalMenu> {

    @Override
    protected HorizontalMenu createComponent() {
        return factory.create(HorizontalMenu.class);
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
            MenuConfigHorizontalMenuItemProvider itemProvider =
                    applicationContext.getBean(MenuConfigHorizontalMenuItemProvider.class);
            resultComponent.setMenuItemProvider(itemProvider);
            itemProvider.load();
        }
    }
}
