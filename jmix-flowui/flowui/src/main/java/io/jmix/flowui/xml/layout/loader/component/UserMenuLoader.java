/*
 * Copyright 2025 Haulmont.
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

import io.jmix.flowui.component.usermenu.UserMenu;
import io.jmix.flowui.kit.component.usermenu.HasMenuItems;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.component.usermenu.UserMenuItemLoader;
import org.dom4j.Element;

import java.util.Map;

public class UserMenuLoader extends AbstractComponentLoader<UserMenu> {

    @Override
    protected UserMenu createComponent() {
        return factory.create(UserMenu.class);
    }

    @Override
    public void loadComponent() {
        loadBoolean(element, "openOnHover", resultComponent::setOpenOnHover);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);

        loadItems();
    }

    protected void loadItems() {
        Element items = element.element("items");
        if (items != null) {
            loadItems(items, resultComponent);
        }
    }

    protected void loadItems(Element items, HasMenuItems menu) {
        Map<String, UserMenuItemLoader> itemLoaders = applicationContext.getBeansOfType(UserMenuItemLoader.class);
        for (Element itemElement : items.elements()) {
            for (UserMenuItemLoader itemLoader : itemLoaders.values()) {
                if (itemLoader.supports(itemElement.getName())) {
                    itemLoader.loadItem(itemElement, menu, getContext());
                    break;
                }
            }
        }
    }
}
