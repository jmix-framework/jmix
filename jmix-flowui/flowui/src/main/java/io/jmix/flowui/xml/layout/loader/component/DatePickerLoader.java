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

import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import org.dom4j.Element;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class DatePickerLoader extends AbstractComponentLoader<TypedDatePicker<?>> {

    protected static final String DATE_PATTERN = "yyyy-MM-dd";

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected TypedDatePicker<?> createComponent() {
        return factory.create(TypedDatePicker.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);
        componentLoader().loadDatatype(resultComponent, element);

        loadResourceString(element, "name", context.getMessageGroup(), resultComponent::setName);
        loadBoolean(element, "opened", resultComponent::setOpened);
        loadBoolean(element, "autoOpen", resultComponent::setAutoOpen);
        loadResourceString(element, "placeholder", context.getMessageGroup(), resultComponent::setPlaceholder);
        loadBoolean(element, "weekNumbersVisible", resultComponent::setWeekNumbersVisible);
        loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);
        loadLocalDate(element, "max", resultComponent::setMax);
        loadLocalDate(element, "min", resultComponent::setMin);

        componentLoader().loadDateFormat(element, resultComponent::setI18n);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadAllowedCharPattern(resultComponent, element, context);
    }

    protected void loadLocalDate(Element element, String attributeName, Consumer<LocalDate> setter) {
        loadResourceString(element, attributeName, context.getMessageGroup())
                .ifPresent(dateString -> {
                    try {
                        LocalDate localDate = parseDate(dateString);
                        setter.accept(localDate);
                    } catch (ParseException e) {
                        String errorMessage = String.format("Unparseable date for %s with '%s' id",
                                resultComponent.getClass().getSimpleName(),
                                resultComponent.getId().orElse("null"));
                        throw new GuiDevelopmentException(errorMessage, context);
                    }
                });
    }

    protected LocalDate parseDate(String date) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return LocalDate.parse(date, formatter);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
