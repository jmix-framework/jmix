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
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.HasActionMenuItems;
import io.jmix.flowui.kit.component.usermenu.HasMenuItems;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.inittask.AssignUserMenuItemActionInitTask;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("flowui_ActionUserMenuItemProvider")
public class ActionUserMenuItemProvider extends AbstractUserMenuItemProvider {

    public static final String NAME = "actionItem";

    protected ActionLoaderSupport actionLoaderSupport;

    public ActionUserMenuItemProvider(ApplicationContext applicationContext,
                                      LoaderSupport loaderSupport) {
        super(applicationContext, loaderSupport);
    }

    @Override
    public boolean supports(String itemName) {
        return NAME.equals(itemName);
    }

    @Override
    public void loadItem(Element element, HasMenuItems menu, ComponentLoader.Context context) {
        if (!(menu instanceof HasActionMenuItems hasActionMenuItems)) {
            throw new GuiDevelopmentException("Menu does not support action items", context);
        }

        String id = loadItemId(element, ActionUserMenuItem.class, context);

        String ref = element.attributeValue("ref");
        Element actionElement = element.element("action");

        if (actionElement != null) {
            Action action = actionLoader(context).loadDeclarativeActionByType(actionElement)
                    .orElseGet(() -> actionLoader(context).loadDeclarativeAction(actionElement));
            ActionUserMenuItem item = hasActionMenuItems.addActionItem(id, action);
            loadItem(element, item, context);
        } else if (ref != null) {
            int index = element.getParent().elements().indexOf(element);

            AssignUserMenuItemActionInitTask<HasActionMenuItems> initTask = new AssignUserMenuItemActionInitTask<>(
                    hasActionMenuItems,
                    ref,
                    id,
                    index
            );
            initTask.setItemConfigurer(item -> loadItem(element, item, context));

            context.addInitTask(initTask);
        } else {
            throw new GuiDevelopmentException("No 'action' defined for %s(%s)"
                    .formatted(ActionUserMenuItem.class.getSimpleName(), id), context);
        }
    }

    @Override
    protected void loadItems(Element items, HasMenuItems menu, ComponentLoader.Context context) {
        throw new GuiDevelopmentException("%s does not support declarative nested items"
                .formatted(ActionUserMenuItem.class.getSimpleName()), context);
    }

    protected ActionLoaderSupport actionLoader(ComponentLoader.Context context) {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }

        return actionLoaderSupport;
    }
}
