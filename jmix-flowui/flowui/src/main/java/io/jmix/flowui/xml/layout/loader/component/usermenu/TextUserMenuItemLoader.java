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
import io.jmix.flowui.kit.component.usermenu.HasTextMenuItems;
import io.jmix.flowui.kit.component.usermenu.TextUserMenuItem;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.IconLoaderSupport;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("flowui_TextUserMenuItemLoader")
public class TextUserMenuItemLoader extends AbstractUserMenuItemLoader {

    public static final String NAME = "textItem";

    protected IconLoaderSupport iconLoaderSupport;

    public TextUserMenuItemLoader(ApplicationContext applicationContext,
                                  LoaderSupport loaderSupport) {
        super(applicationContext, loaderSupport);
    }

    @Override
    public boolean supports(String itemName) {
        return NAME.equals(itemName);
    }

    @Override
    public void loadItem(Element element, HasMenuItems menu, ComponentLoader.Context context) {
        if (!(menu instanceof HasTextMenuItems hasTextMenuItems)) {
            throw new GuiDevelopmentException("Menu does not support text items", context);
        }

        String id = loadItemId(element, TextUserMenuItem.class, context);

        String text = loaderSupport
                .loadResourceString(element, "text", context.getMessageGroup())
                .orElseThrow(() ->
                        new GuiDevelopmentException("No 'text' provided for %s(%s)"
                                .formatted(TextUserMenuItem.class.getSimpleName(), id), context));

        TextUserMenuItem item = hasTextMenuItems.addTextItem(id, text);

        iconLoaderSupport(context).loadIcon(element, item::setIcon);
        loadItem(element, item, context);
    }

    protected IconLoaderSupport iconLoaderSupport(ComponentLoader.Context context) {
        if (iconLoaderSupport == null) {
            iconLoaderSupport = applicationContext.getBean(IconLoaderSupport.class, context);
        }

        return iconLoaderSupport;
    }
}
