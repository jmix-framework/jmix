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

import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import org.dom4j.Element;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class TimePickerLoader extends AbstractComponentLoader<TypedTimePicker<?>> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected TypedTimePicker<?> createComponent() {
        return factory.create(TypedTimePicker.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);
        componentLoader().loadDatatype(resultComponent, element);

        loadBoolean(element, "autoOpen", resultComponent::setAutoOpen);
        loadResourceString(element, "placeholder", context.getMessageGroup(), resultComponent::setPlaceholder);
        loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);
        loadTime(element, "max", resultComponent::setMax);
        loadTime(element, "min", resultComponent::setMin);

        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadAllowedCharPattern(resultComponent, element, context);
        componentLoader().loadStep(element)
                .ifPresent(resultComponent::setStep);
    }

    protected void loadTime(Element element, String attributeName, Consumer<LocalTime> setter) {
        loadString(element, attributeName)
                .ifPresent(timeString -> {
                    try {
                        LocalTime localTime = parseTime(timeString);
                        setter.accept(localTime);
                    } catch (DateTimeParseException e) {
                        String errorMessage = String.format("Unparseable time for %s with '%s' id",
                                resultComponent.getClass().getSimpleName(),
                                resultComponent.getId().orElse("null"));
                        throw new GuiDevelopmentException(errorMessage, context);
                    }
                });
    }

    protected LocalTime parseTime(String time) throws DateTimeParseException {
        return LocalTime.parse(time);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
