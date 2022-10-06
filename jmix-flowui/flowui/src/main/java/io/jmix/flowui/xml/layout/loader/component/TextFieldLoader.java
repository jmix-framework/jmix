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

import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class TextFieldLoader extends AbstractComponentLoader<TypedTextField<?>> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected TypedTextField<?> createComponent() {
        return factory.create(TypedTextField.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        componentLoader().loadDatatype(resultComponent, element);
        loadString(element, "value", resultComponent::setValue);

        loadString(element, "pattern", resultComponent::setPattern);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadInteger(element, "maxLength", resultComponent::setMaxLength);
        loadInteger(element, "minLength", resultComponent::setMinLength);
        loadBoolean(element, "autoselect", resultComponent::setAutoselect);
        loadResourceString(element, "placeholder", context.getMessageGroup(), resultComponent::setPlaceholder);
        loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);
        loadResourceString(element, "title", context.getMessageGroup(), resultComponent::setTitle);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadAutocorrect(resultComponent, element);
        componentLoader().loadAutocomplete(resultComponent, element);
        componentLoader().loadAutocapitalize(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueChangeMode(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
