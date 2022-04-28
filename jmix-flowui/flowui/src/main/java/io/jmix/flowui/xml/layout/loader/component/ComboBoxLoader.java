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

import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class ComboBoxLoader extends AbstractComponentLoader<JmixComboBox<?>> {

    protected DataLoaderSupport dataLoaderSupport;

    public DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }

    @Override
    protected JmixComboBox<?> createComponent() {
        return factory.create(JmixComboBox.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        loadString(element, "label", resultComponent::setLabel);
        loadBoolean(element, "opened", resultComponent::setOpened);
        loadString(element, "pattern", resultComponent::setPattern);
        loadBoolean(element, "invalid", resultComponent::setInvalid);
        loadInteger(element, "pageSize", resultComponent::setPageSize);
        loadBoolean(element, "autoOpen", resultComponent::setAutoOpen);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadString(element, "placeHolder", resultComponent::setPlaceholder);
        loadBoolean(element, "allowCustomValue", resultComponent::setAllowCustomValue);
        loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);
        loadBoolean(element, "preventInvalidInput", resultComponent::setPreventInvalidInput);
        loadResourceString("errorMessage", context.getMessageGroup(), resultComponent::setErrorMessage);
        loadBoolean(element, "requiredIndicatorVisible", resultComponent::setRequiredIndicatorVisible);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadThemeName(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadRequiredMessage(resultComponent, context);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
    }
}
