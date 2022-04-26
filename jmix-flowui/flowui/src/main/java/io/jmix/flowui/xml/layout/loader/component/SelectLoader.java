/*
 * Copyright (c) 2008-2022 Haulmont.
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

import com.vaadin.flow.component.select.Select;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public class SelectLoader extends AbstractComponentLoader<Select<?>> {

    @Override
    protected Select<?> createComponent() {
        return factory.create(Select.class);
    }

    @Override
    public void loadComponent() {
        loadString(element, "label", resultComponent::setLabel);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadString(element, "placeHolder", resultComponent::setPlaceholder);
        loadString(element, "errorMessage", resultComponent::setErrorMessage);
        loadString(element, "emptySelectionCaption", resultComponent::setEmptySelectionCaption);
        loadBoolean(element, "emptySelectionAllowed", resultComponent::setEmptySelectionAllowed);

        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
    }
}
