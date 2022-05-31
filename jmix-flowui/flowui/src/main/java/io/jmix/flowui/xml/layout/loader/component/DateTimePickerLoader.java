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

import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class DateTimePickerLoader extends AbstractComponentLoader<TypedDateTimePicker<?>> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected TypedDateTimePicker<?> createComponent() {
        return factory.create(TypedDateTimePicker.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);
        componentLoader().loadDatatype(resultComponent, element);

        loadBoolean(element, "autoOpen", resultComponent::setAutoOpen);
        loadResourceString(element, "timePlaceholder", context.getMessageGroup(), resultComponent::setTimePlaceholder);
        loadResourceString(element, "datePlaceholder", context.getMessageGroup(), resultComponent::setDatePlaceholder);
        loadBoolean(element, "weekNumbersVisible", resultComponent::setWeekNumbersVisible);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadThemeName(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
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
