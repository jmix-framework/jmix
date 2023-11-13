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

package io.jmix.quartzflowui.view.trigger;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.quartz.model.*;
import io.jmix.quartz.service.QuartzService;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ViewController("quartz_TriggerModel.detail")
@ViewDescriptor("trigger-model-detail-view.xml")
@EditedEntityContainer("triggerModelDc")
@DialogMode(width = "50em")
public class TriggerModelDetailView extends StandardDetailView<TriggerModel> {

    protected static long DEFAULT_REPEAT_INTERVAL = 1000L;

    @ViewComponent
    private JmixComboBox<String> triggerGroupField;
    @ViewComponent
    private FormLayout.FormItem cronExpressionFormItem;
    @ViewComponent
    private JmixButton cronExpressionHelpButton;
    @ViewComponent
    private TypedTextField<Integer> repeatCountField;
    @ViewComponent
    private FormLayout.FormItem repeatCountFormItem;
    @ViewComponent
    private JmixButton repeatCountHelpButton;
    @ViewComponent
    private TypedTextField<Long> repeatIntervalField;
    @ViewComponent
    private FormLayout.FormItem repeatIntervalFormItem;
    @ViewComponent
    private Select<ScheduleType> scheduleTypeField;
    @ViewComponent
    private TypedDateTimePicker<Date> startDateTimePicker;
    @ViewComponent
    private TypedDateTimePicker<Date> endDateTimePicker;
    @ViewComponent
    private JmixComboBox<String> misfireInstructionField;
    @ViewComponent
    private JmixButton misfireInstructionHelpButton;
    @ViewComponent
    private JmixRadioButtonGroup<RepeatMode> repeatModeSelector;
    @ViewComponent
    private JmixButton repeatModeSelectorHelpButton;
    @ViewComponent
    private FormLayout.FormItem repeatModeFormItem;

    @Autowired
    private QuartzService quartzService;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private Messages messages;
    private List<String> triggerGroupNames;

    @Subscribe
    public void onInit(InitEvent event) {
        initTriggerGroupNames();
        initCronExpressionHelperButton();
        initRepeatModeHelperButton();
        initRepeatCountHelperButton();
        initMisfireInstructionHelperButton();
        initDateFields();
    }

    @SuppressWarnings("ConstantConditions")
    @Subscribe
    public void onReady(ReadyEvent event) {
        initFieldVisibility();
        initRepeatModeFields();
        if (getEditedEntity().getScheduleType() == null) {
            scheduleTypeField.setValue(ScheduleType.CRON_EXPRESSION);
        }
        String misfireInstructionId = getEditedEntity().getMisfireInstructionId();
        if (misfireInstructionId == null) {
            misfireInstructionField.setValue(getDefaultMisfireInstructionId(scheduleTypeField.getValue()));
        }
    }

    protected String getDefaultMisfireInstructionId(ScheduleType scheduleType) {
        if (ScheduleType.SIMPLE.equals(scheduleType)) {
            return SimpleTriggerMisfireInstruction.SMART_POLICY.getId();
        } else {
            return CronTriggerMisfireInstruction.SMART_POLICY.getId();
        }
    }

    private void initCronExpressionHelperButton() {
        cronExpressionHelpButton.setIcon(VaadinIcon.QUESTION_CIRCLE.create());
        cronExpressionHelpButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        cronExpressionHelpButton.addClickListener(this::onCronHelperButtonClick);
    }

    private void onCronHelperButtonClick(ClickEvent<Button> event) {
        dialogs.createMessageDialog()
                .withContent(new Html(messageBundle.getMessage("cronExpressionHelpText")))
                .withResizable(true)
                .withModal(false)
                .withWidth("60em")
                .open();
    }

    private void initRepeatModeHelperButton() {
        repeatModeSelectorHelpButton.setIcon(VaadinIcon.QUESTION_CIRCLE.create());
        repeatModeSelectorHelpButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        repeatModeSelectorHelpButton.addClickListener(this::onRepeatModeHelperButtonClick);
    }

    private void onRepeatModeHelperButtonClick(ClickEvent<Button> event) {
        dialogs.createMessageDialog()
                .withContent(new Html(messageBundle.getMessage("triggerRepeatModeHelpText")))
                .withResizable(true)
                .withModal(false)
                .withWidth("60em")
                .open();
    }

    private void initRepeatCountHelperButton() {
        repeatCountHelpButton.setIcon(VaadinIcon.QUESTION_CIRCLE.create());
        repeatCountHelpButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        repeatCountHelpButton.addClickListener(this::onRepeatCountHelperButtonClick);
    }

    private void onRepeatCountHelperButtonClick(ClickEvent<Button> event) {
        dialogs.createMessageDialog()
                .withContent(new Html(messageBundle.getMessage("repeatCountHelpText")))
                .withResizable(true)
                .withModal(false)
                .withWidth("60em")
                .open();
    }

    private void initDateFields() {
        LocalDateTime now = LocalDate.now().atStartOfDay();
        startDateTimePicker.setMin(now);
        endDateTimePicker.setMin(now);
        setupDateTimePickerDefaultTimeListener(startDateTimePicker);
        setupDateTimePickerDefaultTimeListener(endDateTimePicker);
    }

    private void initMisfireInstructionHelperButton() {
        misfireInstructionHelpButton.setIcon(VaadinIcon.QUESTION_CIRCLE.create());
        misfireInstructionHelpButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        misfireInstructionHelpButton.addClickListener(this::onMisfireInstructionHelperButtonClick);
    }

    private void onMisfireInstructionHelperButtonClick(ClickEvent<Button> event) {
        dialogs.createMessageDialog()
                .withContent(new Html(messageBundle.getMessage("triggerMisfireInstructionHelpText")))
                .withResizable(true)
                .withModal(false)
                .withWidth("60em")
                .open();
    }

    @Subscribe("scheduleTypeField")
    private void onScheduleTypeFieldChange(
            AbstractField.ComponentValueChangeEvent<Select<ScheduleType>, ScheduleType> event) {
        initFieldVisibility();
        if (ScheduleType.SIMPLE.equals(event.getValue())) {
            initRepeatModeSelectorValue();
        }

        ScheduleType oldScheduleType = event.getOldValue();
        String currentMisfireInstructionId = misfireInstructionField.getValue();
        if (ScheduleType.SIMPLE.equals(event.getValue())) {

            Map<String, String> map = Arrays.stream(SimpleTriggerMisfireInstruction.values())
                    .collect(
                            Collectors.toMap(SimpleTriggerMisfireInstruction::getId, this::getLocalizedEnum, (i1, i2) -> i2, LinkedHashMap::new)
                    );
            ComponentUtils.setItemsMap(misfireInstructionField, map);

            if (oldScheduleType != null) {
                String instruction = map.containsKey(currentMisfireInstructionId)
                        ? currentMisfireInstructionId
                        : SimpleTriggerMisfireInstruction.SMART_POLICY.getId();
                misfireInstructionField.setValue(instruction);
            }
        } else {
            Map<String, String> map = Arrays.stream(CronTriggerMisfireInstruction.values())
                    .collect(
                            Collectors.toMap(CronTriggerMisfireInstruction::getId, this::getLocalizedEnum, (i1, i2) -> i2, LinkedHashMap::new)
                    );
            ComponentUtils.setItemsMap(misfireInstructionField, map);

            if (oldScheduleType != null) {
                String instruction = map.containsKey(currentMisfireInstructionId)
                        ? currentMisfireInstructionId
                        : CronTriggerMisfireInstruction.SMART_POLICY.getId();
                misfireInstructionField.setValue(instruction);
            }
        }
    }

    protected String getLocalizedEnum(Enum<?> enumClass) {
        return messages.getMessage(enumClass);
    }

    @Subscribe("startDateTimePicker")
    public void onStartDateTimePickerComponentValueChange(
            AbstractField.ComponentValueChangeEvent<DateTimePicker, LocalDateTime> event) {
        endDateTimePicker.setMin(event.getValue());
    }

    @Subscribe("endDateTimePicker")
    public void onEndDateTimePickerComponentValueChange(
            AbstractField.ComponentValueChangeEvent<DateTimePicker, LocalDateTime> event) {
        startDateTimePicker.setMax(event.getValue());
    }

    private void initTriggerGroupNames() {
        triggerGroupNames = quartzService.getTriggerGroupNames();
        triggerGroupField.setItems(triggerGroupNames);
    }

    @Subscribe("triggerGroupField")
    private void onJobGroupFieldValueSet(ComboBoxBase.CustomValueSetEvent<ComboBox<String>> event) {
        String newTriggerGroupName = event.getDetail();
        if (!Strings.isNullOrEmpty(newTriggerGroupName) && !triggerGroupNames.contains(newTriggerGroupName)) {
            triggerGroupNames.add(newTriggerGroupName);
            triggerGroupField.setItems(triggerGroupNames);
            triggerGroupField.setValue(newTriggerGroupName);
        }
    }

    private void initRepeatModeFields() {
        if (isReadOnly()) {
            // RepeatModeSelector is not switched to RO via View action - switch manually
            repeatModeSelector.setEnabled(false);
        }
        repeatModeSelector.addValueChangeListener(e -> {
            RepeatMode repeatMode = e.getValue();
            if (repeatMode == null) {
                return;
            }
            boolean isSimpleTrigger = getEditedEntity().getScheduleType() == ScheduleType.SIMPLE;
            initRepeatFieldsVisibility(isSimpleTrigger, repeatMode);
            initRepeatFieldsValues(repeatMode);
        });
        if (ScheduleType.SIMPLE.equals(scheduleTypeField.getValue())) {
            initRepeatModeSelectorValue();
        }
    }

    private void initRepeatModeSelectorValue() {
        Integer repeatCount = repeatCountField.getTypedValue();
        if (repeatCount == null || repeatCount < 0) {
            repeatModeSelector.setValue(RepeatMode.EXECUTE_FOREVER);
        } else if (repeatCount == 0) {
            repeatModeSelector.setValue(RepeatMode.EXECUTE_ONCE);
        } else {
            repeatModeSelector.setValue(RepeatMode.FINITE_REPEATS);
        }
    }

    private void initDefaultRepeatInterval() {
        Long currentRepeatInterval = repeatIntervalField.getTypedValue();
        if (currentRepeatInterval == null || currentRepeatInterval == 0) {
            repeatIntervalField.setTypedValue(DEFAULT_REPEAT_INTERVAL);
        }
    }

    private void initRepeatFieldsVisibility(boolean isSimpleTrigger, RepeatMode currentRepeatMode) {
        if (!isSimpleTrigger) {
            repeatModeFormItem.setVisible(false);
            repeatCountFormItem.setVisible(false);
            repeatIntervalFormItem.setVisible(false);
        } else {
            repeatModeFormItem.setVisible(true);
            if (currentRepeatMode == null) {
                return;
            }
            switch (currentRepeatMode) {
                case EXECUTE_ONCE -> {
                    repeatCountFormItem.setVisible(false);
                    repeatIntervalFormItem.setVisible(false);
                }
                case EXECUTE_FOREVER -> {
                    repeatCountFormItem.setVisible(false);
                    repeatIntervalFormItem.setVisible(true);
                }
                case FINITE_REPEATS -> {
                    repeatCountFormItem.setVisible(true);
                    repeatIntervalFormItem.setVisible(true);
                }
            }
        }
    }

    private void initRepeatFieldsValues(RepeatMode currentRepeatMode) {
        switch (currentRepeatMode) {
            case EXECUTE_ONCE -> {
                repeatCountField.setTypedValue(0);
                repeatIntervalField.setTypedValue(0L);
            }
            case EXECUTE_FOREVER -> {
                repeatCountField.setTypedValue(-1);
                initDefaultRepeatInterval();
            }
            case FINITE_REPEATS -> {
                Integer currentRepeatCount = repeatCountField.getTypedValue();
                if (currentRepeatCount == null || currentRepeatCount <= 0) {
                    //Set minimal repeat if it was infinite or not set
                    repeatCountField.setTypedValue(1);
                }
                initDefaultRepeatInterval();
            }
        }
    }

    private void initFieldVisibility() {
        boolean isSimpleTrigger = getEditedEntity().getScheduleType() == ScheduleType.SIMPLE;
        cronExpressionFormItem.setVisible(!isSimpleTrigger);
        initRepeatFieldsVisibility(isSimpleTrigger, repeatModeSelector.getValue());
    }

    @Subscribe
    public void onValidation(ValidationEvent event) {
        if (ScheduleType.CRON_EXPRESSION.equals(getEditedEntity().getScheduleType())
                && !CronExpression.isValidExpression(getEditedEntity().getCronExpression())) {
            String message = messageBundle.getMessage("invalidCronExpressionValidationMessage");
            event.getErrors().add(message);
        }

        Date startDateTimeValue = startDateTimePicker.getTypedValue();
        if (startDateTimeValue != null && startDateTimeValue.before(new Date())) {
            String message = messageBundle.getMessage("triggerStartDateInThePastValidationMessage");
            event.getErrors().add(message);
        }
    }

    protected void setupDateTimePickerDefaultTimeListener(TypedDateTimePicker<?> dateTimePicker) {
        dateTimePicker.getElement().executeJs(
                "this.getElementsByTagName(\"vaadin-date-picker\")[0].addEventListener('change', function(){" +
                        "if (!this.getElementsByTagName(\"vaadin-time-picker\")[0].value) this.getElementsByTagName(\"vaadin-time-picker\")[0].value='00:00';" +
                        "}.bind(this));"
        );
    }
}
