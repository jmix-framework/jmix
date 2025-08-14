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

package io.jmix.flowui.xml.layout.loader.component.usermenu;

import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.usermenu.HasMenuItems;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.ComponentLoaderSupport;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;

import java.util.Map;


public abstract class AbstractUserMenuItemProvider implements UserMenuItemLoader {

    protected final ApplicationContext applicationContext;
    protected final LoaderSupport loaderSupport;

    protected ComponentLoaderSupport componentLoaderSupport;

    public AbstractUserMenuItemProvider(ApplicationContext applicationContext,
                                        LoaderSupport loaderSupport) {
        this.applicationContext = applicationContext;
        this.loaderSupport = loaderSupport;
    }

    protected String loadItemId(Element element, Class<?> itemClass, ComponentLoader.Context context) {
        return loaderSupport.loadString(element, "id")
                .orElseThrow(() ->
                        new GuiDevelopmentException("No %s 'id' provided"
                                .formatted(itemClass.getSimpleName()), context));
    }

    protected void loadItem(Element element, UserMenuItem item, ComponentLoader.Context context) {
        loaderSupport.loadBoolean(element, "visible", item::setVisible);
        loaderSupport.loadBoolean(element, "enabled", item::setEnabled);
        loaderSupport.loadBoolean(element, "checkable", item::setCheckable);
        loaderSupport.loadBoolean(element, "checked", item::setChecked);

        componentLoader(context).loadThemeNames(item, element);

        if (item instanceof UserMenuItem.HasSubMenu hasSubMenu) {
            Element itemsElement = element.element("items");
            if (itemsElement != null) {
                loadItems(itemsElement, hasSubMenu.getSubMenu(), context);
            }
        }
    }

    protected void loadItems(Element items, HasMenuItems menu, ComponentLoader.Context context) {
        Map<String, UserMenuItemLoader> itemLoaders = applicationContext.getBeansOfType(UserMenuItemLoader.class);
        for (Element itemElement : items.elements()) {
            for (UserMenuItemLoader itemLoader : itemLoaders.values()) {
                if (itemLoader.supports(itemElement.getName())) {
                    itemLoader.loadItem(itemElement, menu, context);
                    break;
                }
            }
        }
    }

    protected ComponentLoaderSupport componentLoader(ComponentLoader.Context context) {
        if (componentLoaderSupport == null) {
            componentLoaderSupport = applicationContext.getBean(ComponentLoaderSupport.class, context);
        }
        return componentLoaderSupport;
    }
}
