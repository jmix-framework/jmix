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

import com.vaadin.flow.component.textfield.NumberField;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

//TODO: kremnevda, replace NumberField to JmixNumberField 26.04.2022
public class NumberFieldLoader extends AbstractComponentLoader<NumberField> {

    @Override
    protected NumberField createComponent() {
        return factory.create(NumberField.class);
    }

    @Override
    public void loadComponent() {
        loadDouble(element, "max", resultComponent::setMax);
        loadDouble(element, "min", resultComponent::setMin);
        loadDouble(element, "step", resultComponent::setStep);
        loadDouble(element, "value", resultComponent::setValue);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadBoolean(element, "autoselect", resultComponent::setAutoselect);
        loadResourceString(element, "placeholder", context.getMessageGroup(), resultComponent::setPlaceholder);
        loadBoolean(element, "hasControls", resultComponent::setHasControls);
        loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);
        loadResourceString(element, "title", context.getMessageGroup(), resultComponent::setTitle);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadThemeName(resultComponent, element);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadAutocomplete(resultComponent, element);
        componentLoader().loadAutocapitalize(resultComponent, element);
        componentLoader().loadAutocorrect(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueChangeMode(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
    }
}
