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

import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.xml.layout.inittask.AssignActionInitTask;
import org.dom4j.Element;

public class ComboButtonLoader extends AbstractDropdownButtonLoader<ComboButton> {

    @Override
    protected ComboButton createComponent() {
        return factory.create(ComboButton.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadDropdownIcon(resultComponent, element);
        loadAction(resultComponent, element);
    }

    protected void loadAction(ComboButton component, Element element) {
        loadString(element, "action")
                .ifPresent(actionId -> getComponentContext().addInitTask(
                        new AssignActionInitTask<>(component, actionId, getComponentContext().getView())
                ));
    }

    protected void loadDropdownIcon(ComboButton component, Element element) {
        loaderSupport.loadString(element, "dropdownIcon")
                .map(ComponentUtils::parseIcon)
                .ifPresent(component::setDropdownIcon);
    }
}
