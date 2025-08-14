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
import io.jmix.flowui.kit.component.usermenu.ComponentUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.HasComponentMenuItems;
import io.jmix.flowui.kit.component.usermenu.HasMenuItems;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("flowui_ComponentUserMenuItemProvider")
public class ComponentUserMenuItemProvider extends AbstractUserMenuItemProvider {

    public static final String NAME = "componentItem";

    public ComponentUserMenuItemProvider(ApplicationContext applicationContext,
                                         LoaderSupport loaderSupport) {
        super(applicationContext, loaderSupport);
    }

    @Override
    public boolean supports(String itemName) {
        return NAME.equals(itemName);
    }

    @Override
    public void loadItem(Element element, HasMenuItems menu, ComponentLoader.Context context) {
        if (!(menu instanceof HasComponentMenuItems hasComponentMenuItems)) {
            throw new GuiDevelopmentException("Menu does not support component items", context);
        }

        String id = loadItemId(element, ComponentUserMenuItem.class, context);

        Element subElement = element.elements().stream()
                .findFirst()
                .orElseThrow(() ->
                        new GuiDevelopmentException("No 'content' defined for %s(%s)"
                                .formatted(ComponentUserMenuItem.class.getSimpleName(), id), context)
                );

        LayoutLoader loader = layoutLoader(context);
        ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
        componentLoader.initComponent();
        componentLoader.loadComponent();

        com.vaadin.flow.component.Component content = componentLoader.getResultComponent();

        ComponentUserMenuItem item = hasComponentMenuItems.addComponentItem(id, content);
        loadItem(element, item, context);
    }

    protected LayoutLoader layoutLoader(ComponentLoader.Context context) {
        return applicationContext.getBean(LayoutLoader.class, context);
    }
}
