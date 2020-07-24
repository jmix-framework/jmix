/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.xml.layout.loaders;

import io.jmix.ui.Actions;
import io.jmix.ui.component.Button;
import io.jmix.ui.xml.DeclarativeAction;
import io.jmix.ui.xml.layout.loader.ButtonLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class CubaButtonLoader extends ButtonLoader {

    @Override
    public void loadComponent() {
        super.loadComponent();
        loadInvoke(resultComponent, element);
    }

    protected void loadInvoke(Button component, Element element) {
        if (!StringUtils.isBlank(element.attributeValue("action"))) {
            return;
        }

        final String methodName = element.attributeValue("invoke");
        if (StringUtils.isBlank(methodName)) {
            return;
        }

        String actionBaseId = component.getId();
        if (StringUtils.isEmpty(actionBaseId)) {
            actionBaseId = methodName;
        }

        Actions actions = beanLocator.get(Actions.NAME);
        DeclarativeAction action = (DeclarativeAction) actions.create(DeclarativeAction.ID, actionBaseId + "_invoke");
        action.setCaption(component.getCaption());
        action.setDescription(component.getDescription());
        action.setIcon(component.getIcon());
        action.setEnabled(resultComponent.isEnabled());
        action.setVisible(resultComponent.isVisible());
        action.setMethodName(methodName);
        action.checkActionsHolder(component.getFrame());

        component.setAction(action);
    }

}
