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

import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class MultiSelectComboBoxLoader extends AbstractComponentLoader<JmixMultiSelectComboBox<?>> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected JmixMultiSelectComboBox<?> createComponent() {
        return factory.create(JmixMultiSelectComboBox.class);
    }

    @Override
    public void loadComponent() {
        loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);

        getDataLoaderSupport().loadItems(resultComponent, element);
        getDataLoaderSupport().loadData(resultComponent, element);

        // These properties are loaded after the data provider is loaded,
        // because setting the data provider resets the value of the readOnly attribute to default.
        loadBoolean(element, "opened", resultComponent::setOpened);
        loadBoolean(element, "autoOpen", resultComponent::setAutoOpen);
        loadInteger(element, "pageSize", resultComponent::setPageSize);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadBoolean(element, "allowCustomValue", resultComponent::setAllowCustomValue);
        loadString(element, "allowedCharPattern", resultComponent::setAllowedCharPattern);

        loadResourceString(element, "placeholder",
                context.getMessageGroup(), resultComponent::setPlaceholder);
        loadResourceString(element, "tooltipText",
                context.getMessageGroup(), resultComponent::setTooltipText);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
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
