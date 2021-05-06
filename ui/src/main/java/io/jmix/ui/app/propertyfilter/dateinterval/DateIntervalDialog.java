/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.app.propertyfilter.dateinterval;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.jmix.core.Messages;
import io.jmix.ui.Notifications;
import io.jmix.ui.app.propertyfilter.dateinterval.predefined.PredefinedDateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.predefined.PredefinedDateIntervalRegistry;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Date interval editor.
 */
@UiController("ui_DateIntervalDialog")
@UiDescriptor("date-interval-dialog.xml")
public class DateIntervalDialog extends Screen {

    @Autowired
    protected Messages messages;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected PredefinedDateIntervalRegistry intervalFactory;

    @Autowired
    private RadioButtonGroup<BaseDateInterval.Type> typeRadioButtonGroup;
    @Autowired
    private TextField<Integer> numberField;
    @Autowired
    private ComboBox<DateInterval.TimeUnit> timeUnitComboBox;
    @Autowired
    private CheckBox includingCurrentCheckBox;
    @Autowired
    private ComboBox<PredefinedDateInterval> predefinedIntervalsComboBox;

    protected Multimap<BaseDateInterval.Type, Field> componentVisibilityMap = ArrayListMultimap.create();

    protected BaseDateInterval value;

    @Subscribe
    public void onInit(InitEvent event) {
        initTypeRadioButtonGroup();
        initTimeUnitComboBox();
        initPredefinedIntervalsComboBox();

        componentVisibilityMap.putAll(BaseDateInterval.Type.LAST, Arrays.asList(numberField, timeUnitComboBox, includingCurrentCheckBox));
        componentVisibilityMap.putAll(BaseDateInterval.Type.NEXT, Arrays.asList(numberField, timeUnitComboBox, includingCurrentCheckBox));
        componentVisibilityMap.put(BaseDateInterval.Type.PREDEFINED, predefinedIntervalsComboBox);

        typeRadioButtonGroup.setValue(BaseDateInterval.Type.LAST);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (value != null) {
            typeRadioButtonGroup.setValue(value.getType());
            if (value.getType() == BaseDateInterval.Type.PREDEFINED) {
                predefinedIntervalsComboBox.setValue((PredefinedDateInterval) value);
            } else {
                DateInterval dateInterval = (DateInterval) value;
                numberField.setValue(dateInterval.getNumber());
                timeUnitComboBox.setValue(dateInterval.getTimeUnit());
                includingCurrentCheckBox.setValue(dateInterval.getIncludingCurrent());
            }
        }
    }

    /**
     * @return date value or {@code null} if value is not set
     */
    @Nullable
    public BaseDateInterval getValue() {
        return value;
    }

    /**
     * Sets value that will be applied when {@link BeforeShowEvent} is fired.
     *
     * @param value date interval
     */
    public void setValue(@Nullable BaseDateInterval value) {
        this.value = value;
    }

    /**
     * Sets value that will be applied when {@link BeforeShowEvent} is fired.
     *
     * @param value date interval
     * @return screen instance
     */
    public DateIntervalDialog withValue(@Nullable BaseDateInterval value) {
        this.value = value;
        return this;
    }

    protected void initTypeRadioButtonGroup() {
        Map<String, BaseDateInterval.Type> map = getLocalizedEnumMap(BaseDateInterval.Type.class);
        typeRadioButtonGroup.setOptionsMap(map);
    }

    protected void initTimeUnitComboBox() {
        Map<String, DateInterval.TimeUnit> map = getLocalizedEnumMap(DateInterval.TimeUnit.class);
        timeUnitComboBox.setOptionsMap(map);
    }

    private void initPredefinedIntervalsComboBox() {
        List<PredefinedDateInterval> predefinedDateIntervals = intervalFactory.getAllPredefineIntervals();
        Map<String, PredefinedDateInterval> map = new LinkedHashMap<>(predefinedDateIntervals.size());

        for (PredefinedDateInterval interval : predefinedDateIntervals) {
            map.put(interval.getLocalizedCaption(), interval);
        }

        predefinedIntervalsComboBox.setOptionsMap(map);
    }

    @SuppressWarnings("rawtypes")
    protected <T extends Enum> Map<String, T> getLocalizedEnumMap(Class<T> enumClass) {
        Map<String, T> map = new LinkedHashMap<>();
        for (T enumConst : enumClass.getEnumConstants()) {
            map.put(messages.getMessage(enumConst), enumConst);
        }
        return map;
    }

    @Subscribe("typeRadioButtonGroup")
    public void onTypeRadioButtonGroupValueChange(HasValue.ValueChangeEvent<DateInterval.Type> event) {
        if (event.getValue() != null) {
            componentVisibilityMap.values().forEach(component -> component.setVisible(false));
            componentVisibilityMap.get(event.getValue()).forEach(component -> component.setVisible(true));
        }
    }

    @Subscribe("okBtn")
    public void onOkBtnClick(Button.ClickEvent event) {
        DateInterval.Type type = typeRadioButtonGroup.getValue();

        ValidationErrors errors = validateFields(componentVisibilityMap.get(type).toArray(new Field[0]));
        if (errors.isEmpty()) {
            if (type == DateInterval.Type.PREDEFINED) {
                value = predefinedIntervalsComboBox.getValue();
            } else {
                //noinspection ConstantConditions
                value = new DateInterval(type,
                        numberField.getValue(),
                        timeUnitComboBox.getValue(),
                        includingCurrentCheckBox.getValue());
            }
            close(StandardOutcome.COMMIT);
        } else {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage("validationFail.caption"))
                    .withDescription(
                            errors.getAll().stream()
                                    .map(item -> item.description)
                                    .collect(Collectors.joining("\n")))
                    .show();
        }
    }

    @Subscribe("cancelBtn")
    public void onCancelBtnClick(Button.ClickEvent event) {
        close(StandardOutcome.CLOSE);
    }

    protected ValidationErrors validateFields(Field... components) {
        ValidationErrors errors = new ValidationErrors();
        for (Field field : components) {
            try {
                field.validate();
            } catch (ValidationException e) {
                errors.add(field, e.getLocalizedMessage());
            }

        }
        return errors;
    }
}

