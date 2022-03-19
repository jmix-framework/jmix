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

import com.haulmont.cuba.gui.xml.DeclarativeAction;
import io.jmix.ui.component.ActionOwner;
import io.jmix.ui.component.Button;
import io.jmix.ui.xml.layout.loader.ButtonLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;

public class CubaButtonLoader extends ButtonLoader {

    @Override
    public void loadComponent() {
        super.loadComponent();
        loadInvoke(resultComponent, element);
        loadFocusable(resultComponent, element);
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

        DeclarativeAction action = new DeclarativeAction(actionBaseId + "_invoke",
                component.getCaption(), component.getDescription(), component.getIcon(),
                resultComponent.isEnabled(), resultComponent.isVisible(),
                methodName,
                component.getFrame()
        );
        component.setAction(action);
    }

    @Override
    protected void loadAction(ActionOwner component, Element element) {
        String actionId = element.attributeValue("action");
        if (!StringUtils.isEmpty(actionId)) {
            ComponentContext componentContext = getComponentContext();
            componentContext.addPostInitTask(
                    new CubaActionOwnerAssignActionPostInitTask(component, actionId, componentContext.getFrame())
            );
        }
    }
}
