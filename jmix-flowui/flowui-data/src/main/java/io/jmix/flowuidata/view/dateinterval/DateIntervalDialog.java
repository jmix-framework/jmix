/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowuidata.view.dateinterval;

import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval.Type;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.DateInterval;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.predefined.PredefinedDateInterval;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowuidata.dateinterval.RelativeDateTimeMomentProvider;
import io.jmix.flowuidata.dateinterval.component.RelativeDateIntervalField;
import io.jmix.flowuidata.dateinterval.model.RelativeDateInterval;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ViewController("flowui_DateIntervalDialog")
public class DateIntervalDialog extends io.jmix.flowui.app.propertyfilter.dateinterval.DateIntervalDialog {

    @Autowired
    protected RelativeDateTimeMomentProvider relativeDateTimeMomentProvider;

    protected RelativeDateIntervalField relativeDateIntervalField;

    @Override
    protected void initComponents() {
        initTypeRadioButtonGroup();
        initNextLastIntervalField();
        initRelativeDateIntervalField();
        initPredefinedIntervalsSelect();
        initComponentVisibilityMap();
    }

    @Override
    protected void initTypeRadioButtonGroup() {
        Map<Type, String> localizationMap = getLocalizedEnumMap(Type.class);
        ComponentUtils.setItemsMap(typeRadioButtonGroup, localizationMap);
    }

    protected void initRelativeDateIntervalField() {
        relativeDateIntervalField = uiComponents.create(RelativeDateIntervalField.class);

        Map<Enum<?>, String> localizationMap =
                getLocalizedEnumMap(relativeDateTimeMomentProvider.getRelativeDateTimeMoments());
        relativeDateIntervalField.setDateTimeSelectItemsMap(localizationMap);
        relativeDateIntervalField.setWidthFull();

        contentBox.add(relativeDateIntervalField);
    }

    @Override
    protected void initComponentVisibilityMap() {
        componentVisibilityMap.put(Type.LAST, lastIntervalField);
        componentVisibilityMap.put(Type.NEXT, nextIntervalField);
        componentVisibilityMap.put(Type.RELATIVE, relativeDateIntervalField);
        componentVisibilityMap.put(Type.PREDEFINED, predefinedIntervalsSelect);
    }

    @Override
    protected void filterOptionsByPropertyType() {
        if (isOptionsFilterable()) {
            Map<DateInterval.TimeUnit, String> localizedTimeUnitMap =
                    getLocalizedEnumMap(List.of(DateInterval.TimeUnit.HOUR, DateInterval.TimeUnit.MINUTE));
            lastIntervalField.setTimeUnitItemsMap(localizedTimeUnitMap);
            nextIntervalField.setTimeUnitItemsMap(localizedTimeUnitMap);

            relativeDateIntervalField.setDateTimeSelectItemsMap(
                    getLocalizedEnumMap(relativeDateTimeMomentProvider.getRelativeTimeMoments()));

            List<Type> availableTypes = Arrays.asList(Type.LAST, Type.NEXT, Type.RELATIVE);
            ComponentUtils.setItemsMap(typeRadioButtonGroup, getLocalizedEnumMap(availableTypes));
        }
    }

    @Override
    protected void setupInitialValues() {
        if (isValueEmpty()) {
            return;
        }

        Type initialValue = value.getType();
        typeRadioButtonGroup.setValue(initialValue);

        if (Type.PREDEFINED == value.getType()
                && value instanceof PredefinedDateInterval predefinedDateIntervalValue) {
            predefinedIntervalsSelect.setValue(predefinedDateIntervalValue);

        } else if (Type.RELATIVE == value.getType()
                && value instanceof RelativeDateInterval relativeDateIntervalValue) {
            relativeDateIntervalField.setValue(relativeDateIntervalValue);

        } else if (value instanceof DateInterval dateIntervalValue) {

            if (Type.LAST == value.getType()) {
                lastIntervalField.setValue(dateIntervalValue);
            } else {
                nextIntervalField.setValue(dateIntervalValue);
            }
        }
    }

    @Override
    protected void updateValueByType(Type type) {
        if (Type.PREDEFINED == type) {
            value = predefinedIntervalsSelect.getValue();
        } else if (Type.RELATIVE == type) {
            value = relativeDateIntervalField.getValue();
        } else if (Type.LAST == type) {
            value = lastIntervalField.getValue();
        } else {
            value = nextIntervalField.getValue();
        }
    }
}
