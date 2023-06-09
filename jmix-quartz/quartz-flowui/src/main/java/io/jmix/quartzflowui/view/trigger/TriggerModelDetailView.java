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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.quartz.model.ScheduleType;
import io.jmix.quartz.model.TriggerModel;
import io.jmix.quartz.service.QuartzService;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@ViewController("quartz_TriggerModel.detail")
@ViewDescriptor("trigger-model-detail-view.xml")
@EditedEntityContainer("triggerModelDc")
@DialogMode(width = "50em")
public class TriggerModelDetailView extends StandardDetailView<TriggerModel> {

    @ViewComponent
    private ComboBox<String> triggerGroupField;
    @ViewComponent
    private TypedTextField<String> cronExpressionField;
    @ViewComponent
    private TypedTextField<Integer> repeatCountField;
    @ViewComponent
    private TypedTextField<Long> repeatIntervalField;
    @ViewComponent
    private Select<ScheduleType> scheduleTypeField;
    @ViewComponent
    private TypedDateTimePicker<Date> startDateTimePicker;
    @ViewComponent
    private TypedDateTimePicker<Date> endDateTimePicker;

    @Autowired
    private QuartzService quartzService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private UiComponents uiComponents;

    private List<String> triggerGroupNames;

    @Subscribe
    public void onInit(InitEvent event) {
        initTriggerGroupNames();
        initCronHelperButton();
        setupDateTimePickerDefaultTimeListener(startDateTimePicker);
        setupDateTimePickerDefaultTimeListener(endDateTimePicker);
    }

    @SuppressWarnings("ConstantConditions")
    @Subscribe
    public void onReady(ReadyEvent event) {
        initFieldVisibility();
        if (getEditedEntity().getScheduleType() == null) {
            scheduleTypeField.setValue(ScheduleType.CRON_EXPRESSION);
        }
    }

    private void initCronHelperButton() {
        JmixButton helperBtn = uiComponents.create(JmixButton.class);
        helperBtn.setIcon(VaadinIcon.QUESTION_CIRCLE_O.create());
        helperBtn.addThemeVariants(
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_CONTRAST);
        helperBtn.addClickListener(this::onHelperButtonClick);
        cronExpressionField.setSuffixComponent(helperBtn);
    }

    private void onHelperButtonClick(ClickEvent<Button> event) {
        notifications.create(new Html(messageBundle.getMessage("cronExpressionHelpText")))
                .withDuration(0)
                .show();
    }

    @Subscribe("scheduleTypeField")
    private void onScheduleTypeFieldChange(
            AbstractField.ComponentValueChangeEvent<Select<ScheduleType>, ScheduleType> event) {
        initFieldVisibility();
    }

    @Subscribe("startDateTimePicker")
    public void onStartDateTimePickerComponentValueChange(final AbstractField.ComponentValueChangeEvent<DateTimePicker, LocalDateTime> event) {
        endDateTimePicker.setMin(event.getValue());
    }

    @Subscribe("endDateTimePicker")
    public void onEndDateTimePickerComponentValueChange(final AbstractField.ComponentValueChangeEvent<DateTimePicker, LocalDateTime> event) {
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

    private void initFieldVisibility() {
        boolean isSimpleTrigger = getEditedEntity().getScheduleType() == ScheduleType.SIMPLE;
        cronExpressionField.setVisible(!isSimpleTrigger);
        repeatCountField.setVisible(isSimpleTrigger);
        repeatIntervalField.setVisible(isSimpleTrigger);
    }

    @Subscribe
    public void onValidation(ValidationEvent event) {
        if (ScheduleType.SIMPLE.equals(getEditedEntity().getScheduleType())
                || CronExpression.isValidExpression(getEditedEntity().getCronExpression())) {
            return;
        }

        String message = messageBundle.getMessage("invalidCronExpressionValidationMessage");
        event.getErrors().add(message);
    }

    protected void setupDateTimePickerDefaultTimeListener(TypedDateTimePicker<?> dateTimePicker) {
        dateTimePicker.getElement().executeJs(
                "this.getElementsByTagName(\"vaadin-date-picker\")[0].addEventListener('change', function(){" +
                        "if (!this.getElementsByTagName(\"vaadin-time-picker\")[0].value) this.getElementsByTagName(\"vaadin-time-picker\")[0].value='00:00';" +
                        "}.bind(this));"
        );
    }
}
