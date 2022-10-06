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

import io.jmix.flowui.component.textfield.JmixBigDecimalField;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

import java.math.BigDecimal;

public class BigDecimalFieldLoader extends AbstractComponentLoader<JmixBigDecimalField> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected JmixBigDecimalField createComponent() {
        return factory.create(JmixBigDecimalField.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        loadDouble(element, "value")
                .ifPresent(aDouble -> resultComponent.setValue(BigDecimal.valueOf(aDouble)));
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
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
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadAutocapitalize(resultComponent, element);
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
