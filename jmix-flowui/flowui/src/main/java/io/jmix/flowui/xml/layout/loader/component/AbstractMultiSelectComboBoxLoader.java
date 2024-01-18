/*
 * Copyright 2023 Haulmont.
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

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public abstract class AbstractMultiSelectComboBoxLoader<C extends MultiSelectComboBox<?>>
        extends AbstractComponentLoader<C> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    public void loadComponent() {
        loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);

        getDataLoaderSupport().loadItems(resultComponent, element);

        // These properties are loaded after the data provider is loaded,
        // because setting the data provider resets the value of the readOnly attribute to default.
        loadBoolean(element, "opened", resultComponent::setOpened);
        loadBoolean(element, "autoOpen", resultComponent::setAutoOpen);
        loadInteger(element, "pageSize", resultComponent::setPageSize);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadBoolean(element, "allowCustomValue", resultComponent::setAllowCustomValue);

        componentLoader().loadPlaceholder(resultComponent, element);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadOverlayClass(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadAllowedCharPattern(resultComponent, element, context);
        componentLoader().loadAriaLabel(resultComponent, element);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}