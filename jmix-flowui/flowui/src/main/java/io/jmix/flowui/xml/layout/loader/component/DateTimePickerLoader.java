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

import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import org.dom4j.Element;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

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
        loadDateTime(element, "max", resultComponent::setMax);
        loadDateTime(element, "min", resultComponent::setMin);

        componentLoader().loadDatePickerI18n(element, resultComponent::setDatePickerI18n);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadOverlayClass(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadAriaLabel(resultComponent, element);
        componentLoader().loadDuration(element, "step")
                .ifPresent(resultComponent::setStep);
    }

    protected void loadDateTime(Element element, String attributeName, Consumer<LocalDateTime> setter) {
        loadString(element, attributeName)
                .ifPresent(dateTimeString -> {
                    try {
                        LocalDateTime localDateTime = parseDateTime(dateTimeString);
                        setter.accept(localDateTime);
                    } catch (ParseException e) {
                        String errorMessage = String.format("Unparseable date-time for %s with '%s' id",
                                resultComponent.getClass().getSimpleName(),
                                resultComponent.getId().orElse("null"));
                        throw new GuiDevelopmentException(errorMessage, context);
                    }
                });
    }

    protected LocalDateTime parseDateTime(String date) throws ParseException {
        return LocalDateTime.parse(date);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
