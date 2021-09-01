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

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.model.BaseDateInterval.Type;
import io.jmix.ui.app.propertyfilter.dateinterval.model.DateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.model.DateInterval.TimeUnit;
import io.jmix.ui.app.propertyfilter.dateinterval.model.RelativeDateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.model.predefined.PredefinedDateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.model.predefined.PredefinedDateIntervalRegistry;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Date interval editor.
 */
@UiController("ui_DateIntervalDialog")
@UiDescriptor("date-interval-dialog.xml")
public class DateIntervalDialog extends Screen {

    protected static final List<Class<?>> timeClasses =
            ImmutableList.of(java.sql.Time.class, LocalTime.class, OffsetTime.class);

    @Autowired
    protected Messages messages;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected PredefinedDateIntervalRegistry intervalFactory;

    @Autowired(required = false)
    protected RelativeDateTimeMomentProvider relativeMomentProvider;

    @Autowired
    protected RadioButtonGroup<Type> typeRadioButtonGroup;

    @Autowired
    protected TextField<Integer> numberField;
    @Autowired
    protected ComboBox<TimeUnit> timeUnitComboBox;
    @Autowired
    protected CheckBox includingCurrentCheckBox;

    @Autowired
    protected ComboBox<PredefinedDateInterval> predefinedIntervalsComboBox;

    @Autowired
    protected ComboBox<RelativeDateInterval.Operation> relativeDateTimeOperationComboBox;
    @Autowired
    protected ComboBox<Enum> relativeDateTimeComboBox;

    protected Multimap<Type, Field> componentVisibilityMap = ArrayListMultimap.create();

    protected BaseDateInterval value;
    protected MetaPropertyPath mpp;

    @Subscribe
    protected void onInit(InitEvent event) {
        initTypeRadioButtonGroup();
        initTimeUnitComboBox();
        initPredefinedIntervalsComboBox();
        initRelativeDateTimeOperationComboBox();
        initRelativeDateTimeComboBox();

        componentVisibilityMap.putAll(Type.LAST,
                Arrays.asList(numberField, timeUnitComboBox, includingCurrentCheckBox));
        componentVisibilityMap.putAll(Type.NEXT,
                Arrays.asList(numberField, timeUnitComboBox, includingCurrentCheckBox));
        componentVisibilityMap.putAll(Type.RELATIVE,
                Arrays.asList(relativeDateTimeOperationComboBox, relativeDateTimeComboBox));
        componentVisibilityMap.put(Type.PREDEFINED, predefinedIntervalsComboBox);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        filterOptionsByPropertyType(mpp);

        Type initialValue = Objects.requireNonNull(typeRadioButtonGroup.getOptions())
                .getOptions()
                .collect(Collectors.toList())
                .get(0);
        typeRadioButtonGroup.setValue(initialValue);

        if (value != null) {
            typeRadioButtonGroup.setValue(value.getType());
            if (value.getType() == Type.PREDEFINED) {
                predefinedIntervalsComboBox.setValue((PredefinedDateInterval) value);
            } else if (value.getType() == Type.RELATIVE) {
                RelativeDateInterval dateInterval = (RelativeDateInterval) value;
                Enum relativeDateTime = relativeMomentProvider.getByName(
                        dateInterval.getRelativeDateTimeMomentName());

                relativeDateTimeComboBox.setValue(relativeDateTime);
                relativeDateTimeOperationComboBox.setValue(dateInterval.getOperation());
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

    /**
     * @return meta property path of entity's property for Date Interval
     */
    @Nullable
    public MetaPropertyPath getMetaPropertyPath() {
        return mpp;
    }

    /**
     * Sets meta property path of entity's property for Date Interval.
     *
     * @param metaPropertyPath meta property path
     */
    public void setMetaPropertyPath(@Nullable MetaPropertyPath metaPropertyPath) {
        this.mpp = metaPropertyPath;
    }

    /**
     * Sets meta property path of entity's property for Date Interval.
     *
     * @param metaPropertyPath meta property path
     * @return screen instance
     */
    public DateIntervalDialog withMetaPropertyPath(@Nullable MetaPropertyPath metaPropertyPath) {
        this.mpp = metaPropertyPath;
        return this;
    }

    protected void filterOptionsByPropertyType(@Nullable MetaPropertyPath mpp) {
        // If property is Dynamic Attribute, mpp can be null.
        // DynAttr contains only Date or DateTime attributes.
        if (mpp == null) {
            return;
        }

        Range range = mpp.getRange();
        if (!range.isDatatype()) {
            throw new IllegalStateException("Value is not a simple type");
        }

        Class<?> javaClass = range.asDatatype().getJavaClass();
        if (timeClasses.contains(javaClass)) {
            timeUnitComboBox.setOptionsMap(getLocalizedEnumMap(Arrays.asList(TimeUnit.HOUR, TimeUnit.MINUTE)));

            List<Type> availableTypes = new ArrayList<>(Arrays.asList(Type.LAST, Type.NEXT));

            if (relativeMomentProvider != null) {
                availableTypes.add(Type.RELATIVE);
                relativeDateTimeComboBox.setOptionsMap(
                        getLocalizedEnumMap(relativeMomentProvider.getRelativeTimeMoments()));
            }
            typeRadioButtonGroup.setOptionsMap(getLocalizedEnumMap(availableTypes));
        }
    }

    protected void initTypeRadioButtonGroup() {
        Map<String, Type> map = relativeMomentProvider == null
                ? getLocalizedEnumMap(Arrays.asList(Type.LAST, Type.NEXT, Type.PREDEFINED))
                : getLocalizedEnumMap(Type.class);

        typeRadioButtonGroup.setOptionsMap(map);
    }

    protected void initTimeUnitComboBox() {
        Map<String, TimeUnit> map = getLocalizedEnumMap(TimeUnit.class);
        timeUnitComboBox.setOptionsMap(map);
    }

    protected void initPredefinedIntervalsComboBox() {
        List<PredefinedDateInterval> predefinedDateIntervals = intervalFactory.getAllPredefineIntervals();
        Map<String, PredefinedDateInterval> map = new LinkedHashMap<>(predefinedDateIntervals.size());

        for (PredefinedDateInterval interval : predefinedDateIntervals) {
            map.put(interval.getLocalizedCaption(), interval);
        }

        predefinedIntervalsComboBox.setOptionsMap(map);
    }

    protected void initRelativeDateTimeOperationComboBox() {
        RelativeDateInterval.Operation[] operations = RelativeDateInterval.Operation.values();
        Map<String, RelativeDateInterval.Operation> operationsMap = new LinkedHashMap<>(operations.length);

        for (RelativeDateInterval.Operation operation : operations) {
            operationsMap.put(operation.getValue(), operation);
        }
        relativeDateTimeOperationComboBox.setOptionsMap(operationsMap);
    }

    protected void initRelativeDateTimeComboBox() {
        if (relativeMomentProvider != null) {
            List<Enum> relativeDateTimeMoments = relativeMomentProvider.getRelativeDateTimeMoments();
            relativeDateTimeComboBox.setOptionsMap(getLocalizedEnumMap(relativeDateTimeMoments));
        }
    }

    @SuppressWarnings("rawtypes")
    protected <T extends Enum> Map<String, T> getLocalizedEnumMap(Class<T> enumClass) {
        return getLocalizedEnumMap(Arrays.asList(enumClass.getEnumConstants()));
    }

    @SuppressWarnings("rawtypes")
    protected <T extends Enum> Map<String, T> getLocalizedEnumMap(List<T> values) {
        Map<String, T> map = new LinkedHashMap<>();
        for (T enumConst : values) {
            map.put(messages.getMessage(enumConst), enumConst);
        }
        return map;
    }

    @Install(to = "includingCurrentCheckBox", subject = "contextHelpIconClickHandler")
    protected void textFieldContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent event) {
        dialogs.createMessageDialog()
                .withCaption(
                        messages.getMessage(this.getClass(),
                                "dateIntervalDialog.includingCurrentCheckBox.contextHelp.title"))
                .withContentMode(ContentMode.HTML)
                .withMessage(Strings.nullToEmpty(event.getSource().getContextHelpText()))
                .show();
    }

    @Subscribe("typeRadioButtonGroup")
    protected void onTypeRadioButtonGroupValueChange(HasValue.ValueChangeEvent<Type> event) {
        if (event.getValue() != null) {
            componentVisibilityMap.values().forEach(component -> component.setVisible(false));
            componentVisibilityMap.get(event.getValue()).forEach(component -> component.setVisible(true));

            DialogWindow window = (DialogWindow) getWindow();
            window.center();
        }
    }

    @Subscribe("okBtn")
    protected void onOkBtnClick(Button.ClickEvent event) {
        Type type = typeRadioButtonGroup.getValue();

        ValidationErrors errors = validateFields(componentVisibilityMap.get(type).toArray(new Field[0]));
        if (errors.isEmpty()) {
            if (type == Type.PREDEFINED) {
                value = predefinedIntervalsComboBox.getValue();
            } else if (type == Type.RELATIVE) {
                //noinspection ConstantConditions
                value = new RelativeDateInterval(
                        relativeDateTimeOperationComboBox.getValue(),
                        relativeDateTimeComboBox.getValue().name());
            } else {
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
    protected void onCancelBtnClick(Button.ClickEvent event) {
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

