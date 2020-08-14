/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.action.Action;
import io.jmix.ui.Actions;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.ListComponent;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public abstract class ActionsHolderLoader<T extends ActionsHolder> extends AbstractComponentLoader<T> {

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        String id = element.attributeValue("id");
        if (StringUtils.isEmpty(id)) {
            throw new GuiDevelopmentException("No action id provided", context,
                    "ActionsHolder ID", actionsHolder.getId());
        }

        String actionTypeId = element.attributeValue("type");
        if (StringUtils.isNotEmpty(actionTypeId)) {
            Actions actions = (Actions) applicationContext.getBean(Actions.NAME);
            Action instance = actions.create(actionTypeId, id);

            if (instance instanceof ListAction) {
                ((ListAction) instance).setTarget((ListComponent) actionsHolder);
            }

            loadStandardActionProperties(instance, element);

            loadActionConstraint(instance, element);

            loadShortcut(instance, element);

            loadCustomProperties(instance, element);

            return instance;
        }

        return super.loadDeclarativeAction(actionsHolder, element);
    }

    protected void loadShortcut(Action instance, Element element) {
        String shortcut = StringUtils.trimToNull(element.attributeValue("shortcut"));
        if (StringUtils.isNotEmpty(shortcut)) {
            instance.setShortcut(loadShortcut(shortcut));
        }
    }

    protected void loadStandardActionProperties(Action instance, Element element) {
        String enable = element.attributeValue("enable");
        if (StringUtils.isNotEmpty(enable)) {
            instance.setEnabled(Boolean.parseBoolean(enable));
        }

        String visible = element.attributeValue("visible");
        if (StringUtils.isNotEmpty(visible)) {
            instance.setVisible(Boolean.parseBoolean(visible));
        }

        String caption = element.attributeValue("caption");
        if (caption != null) {
            instance.setCaption(loadResourceString(caption));
        }

        String description = element.attributeValue("description");
        if (StringUtils.isNotEmpty(description)) {
            instance.setDescription(loadResourceString(description));
        }

        String icon = element.attributeValue("icon");
        if (StringUtils.isNotEmpty(icon)) {
            instance.setIcon(getIconPath(icon));
        }
    }

    protected void loadCustomProperties(Action instance, Element element) {
        Element propertiesEl = element.element("properties");
        if (propertiesEl != null) {
            ActionCustomPropertyLoader propertyLoader = applicationContext.getBean(ActionCustomPropertyLoader.class);
            for (Element propertyEl : propertiesEl.elements("property")) {
                propertyLoader.load(instance,
                        propertyEl.attributeValue("name"), propertyEl.attributeValue("value"));
            }
        }
    }
}
