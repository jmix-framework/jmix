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

import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class SelectLoader extends AbstractComponentLoader<JmixSelect<?>> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected JmixSelect<?> createComponent() {
        return factory.create(JmixSelect.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadItems(resultComponent, element);
        getDataLoaderSupport().loadData(resultComponent, element);

        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadResourceString(element, "placeholder", context.getMessageGroup(), resultComponent::setPlaceholder);
        loadResourceString(element, "emptySelectionCaption", context.getMessageGroup(),
                resultComponent::setEmptySelectionCaption);
        loadBoolean(element, "emptySelectionAllowed", resultComponent::setEmptySelectionAllowed);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
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
