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

import io.jmix.core.ClassManager;
import io.jmix.flowui.component.usermenu.HasViewMenuItems;
import io.jmix.flowui.component.usermenu.ViewUserMenuItem;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.usermenu.HasMenuItems;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("flowui_ViewUserMenuItemLoader")
public class ViewUserMenuItemLoader extends AbstractUserMenuItemLoader {

    public static final String NAME = "viewItem";

    public ViewUserMenuItemLoader(ApplicationContext applicationContext,
                                  LoaderSupport loaderSupport) {
        super(applicationContext, loaderSupport);
    }

    @Override
    public boolean supports(String itemName) {
        return NAME.equals(itemName);
    }

    @Override
    public void loadItem(Element element, HasMenuItems menu, ComponentLoader.Context context) {
        if (!(menu instanceof HasViewMenuItems hasViewMenuItems)) {
            throw new GuiDevelopmentException("Menu does not support view items", context);
        }

        String id = loadItemId(element, ViewUserMenuItem.class, context);

        String text = loaderSupport.loadResourceString(element, "text", context.getMessageGroup())
                .orElseThrow(() ->
                        new GuiDevelopmentException("No 'text' provided for %s(%s)"
                                .formatted(ViewUserMenuItem.class.getSimpleName(), id), context));

        ViewUserMenuItem item = loaderSupport.loadString(element, "viewId")
                .map(viewId -> hasViewMenuItems.addViewItem(id, viewId, text))
                .orElse(null);

        if (item == null) {
            Class<?> viewClass = loaderSupport.loadString(element, "viewClass")
                    .map(aClass -> applicationContext.getBean(ClassManager.class).loadClass(aClass))
                    .orElseThrow(() -> new GuiDevelopmentException("Neither 'viewId' nor 'viewClass' provided for %s(%s)"
                            .formatted(ViewUserMenuItem.class.getSimpleName(), id), context));

            if (!View.class.isAssignableFrom(viewClass)) {
                throw new GuiDevelopmentException("Class '%s' is not a %s"
                        .formatted(viewClass.getSimpleName(), View.class.getSimpleName()), context);
            }

            //noinspection unchecked,rawtypes
            item = hasViewMenuItems.addViewItem(id, (Class) viewClass, text);
        }

        componentLoader(context).loadIcon(element, item::setIcon);
        loaderSupport.loadEnum(element, OpenMode.class, "openMode", item::setOpenMode);
        loadItem(element, item, context);
    }
}
