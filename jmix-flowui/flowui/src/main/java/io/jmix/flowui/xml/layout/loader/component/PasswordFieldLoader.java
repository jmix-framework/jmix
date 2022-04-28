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

import com.vaadin.flow.component.textfield.Autocapitalize;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

//TODO: kremnevda, replace PasswordField to JmixPasswordField 25.04.2022
public class PasswordFieldLoader extends AbstractComponentLoader<PasswordField> {

    @Override
    protected PasswordField createComponent() {
        return factory.create(PasswordField.class);
    }

    @Override
    public void loadComponent() {
        loadString(element, "label", resultComponent::setLabel);
        loadString(element, "title", resultComponent::setTitle);
        loadString(element, "value", resultComponent::setValue);
        loadString(element, "pattern", resultComponent::setPattern);
        loadBoolean(element, "invalid", resultComponent::setInvalid);
        loadBoolean(element, "required", resultComponent::setRequired);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadBoolean(element, "autoSelect", resultComponent::setAutoselect);
        loadString(element, "placeholder", resultComponent::setPlaceholder);
        loadBoolean(element, "autoCorrect", resultComponent::setAutocorrect);
        loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);
        loadBoolean(element, "preventInvalidInput", resultComponent::setPreventInvalidInput);
        loadEnum(element, Autocomplete.class, "autoComplete", resultComponent::setAutocomplete);
        loadResourceString("errorMessage", context.getMessageGroup(), resultComponent::setErrorMessage);
        loadEnum(element, Autocapitalize.class, "autoCapitalize", resultComponent::setAutocapitalize);
        loadBoolean(element, "revealButtonVisible", resultComponent::setRevealButtonVisible);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadThemeName(resultComponent, element);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueChangeMode(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
    }
}
