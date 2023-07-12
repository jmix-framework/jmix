package io.jmix.quartzui.screen.trigger;

import com.google.common.base.Strings;
import io.jmix.quartz.model.RepeatMode;
import io.jmix.quartz.model.ScheduleType;
import io.jmix.quartz.model.TriggerModel;
import io.jmix.quartz.service.QuartzService;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.RadioButtonGroup;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.*;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("quartz_TriggerModel.edit")
@UiDescriptor("trigger-model-edit.xml")
@EditedEntityContainer("triggerModelDc")
public class TriggerModelEdit extends StandardEditor<TriggerModel> {

    protected static long DEFAULT_REPEAT_INTERVAL = 1000L;

    @Autowired
    private QuartzService quartzService;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private ComboBox<String> triggerGroupField;

    @Autowired
    private TextField<String> cronExpressionField;

    @Autowired
    private TextField<Integer> repeatCountField;

    @Autowired
    private TextField<Long> repeatIntervalField;

    @Autowired
    private ComboBox<ScheduleType> scheduleTypeField;

    @Autowired
    private RadioButtonGroup<RepeatMode> repeatModeSelector;

    @SuppressWarnings("ConstantConditions")
    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initTriggerGroupNames();
        initFieldVisibility();
        initRepeatModeFields();
        scheduleTypeField.addValueChangeListener(e -> {
            initFieldVisibility();
            if(ScheduleType.SIMPLE.equals(e.getValue())) {
                initRepeatModeSelectorValue();
            }
        });
        if (getEditedEntity().getScheduleType() == null) {
            scheduleTypeField.setValue(ScheduleType.CRON_EXPRESSION);
        }
    }

    private void initTriggerGroupNames() {
        List<String> triggerGroupNames = quartzService.getTriggerGroupNames();
        triggerGroupField.setOptionsList(triggerGroupNames);
        triggerGroupField.setEnterPressHandler(enterPressEvent -> {
            String newTriggerGroupName = enterPressEvent.getText();
            if (!Strings.isNullOrEmpty(newTriggerGroupName) && !triggerGroupNames.contains(newTriggerGroupName)) {
                triggerGroupNames.add(newTriggerGroupName);
                triggerGroupField.setOptionsList(triggerGroupNames);
            }
        });
    }

    private void initRepeatModeFields() {
        if(isReadOnly()) {
            // RepeatModeSelector is not switched to RO via View action - switch manually
            repeatModeSelector.setEditable(false);
        }
        repeatModeSelector.addValueChangeListener(e -> {
            RepeatMode repeatMode = e.getValue();
            if(repeatMode == null) {
                return;
            }
            boolean isSimpleTrigger = getEditedEntity().getScheduleType() == ScheduleType.SIMPLE;
            initRepeatFieldsVisibility(isSimpleTrigger, repeatMode);
            initRepeatFieldsValues(repeatMode);
        });
        initRepeatModeSelectorValue();
    }

    private void initRepeatModeSelectorValue() {
        Integer repeatCount = repeatCountField.getValue();
        if(repeatCount == null || repeatCount < 0) {
            repeatModeSelector.setValue(RepeatMode.EXECUTE_FOREVER);
        } else if(repeatCount == 0) {
            repeatModeSelector.setValue(RepeatMode.EXECUTE_ONCE);
        } else {
            repeatModeSelector.setValue(RepeatMode.FINITE_REPEATS);
        }
    }

    private void initDefaultRepeatInterval() {
        Long currentRepeatInterval = repeatIntervalField.getValue();
        if(currentRepeatInterval == null || currentRepeatInterval == 0) {
            repeatIntervalField.setValue(DEFAULT_REPEAT_INTERVAL);
        }
    }

    private void initRepeatFieldsVisibility(boolean isSimpleTrigger, RepeatMode currentRepeatMode) {
        if(!isSimpleTrigger) {
            repeatModeSelector.setVisible(false);
            repeatCountField.setVisible(false);
            repeatIntervalField.setVisible(false);
        }  else {
            repeatModeSelector.setVisible(true);
            if(currentRepeatMode == null) {
                return;
            }
            switch (currentRepeatMode) {
                case EXECUTE_ONCE:
                    repeatCountField.setVisible(false);
                    repeatIntervalField.setVisible(false);
                    break;
                case EXECUTE_FOREVER:
                    repeatCountField.setVisible(false);
                    repeatIntervalField.setVisible(true);
                    break;
                case FINITE_REPEATS:
                    repeatCountField.setVisible(true);
                    repeatIntervalField.setVisible(true);
                    break;
            }
        }
    }

    private void initRepeatFieldsValues(RepeatMode currentRepeatMode) {
        switch (currentRepeatMode) {
            case EXECUTE_ONCE:
                repeatCountField.setValue(0);
                repeatIntervalField.setValue(0L);
                break;
            case EXECUTE_FOREVER:
                repeatCountField.setValue(-1);
                initDefaultRepeatInterval();
                break;
            case FINITE_REPEATS:
                Integer currentRepeatCount = repeatCountField.getValue();
                if(currentRepeatCount == null || currentRepeatCount <= 0) {
                    //Set minimal repeat if it was infinite or not set
                    repeatCountField.setValue(1);
                }
                initDefaultRepeatInterval();
                break;
        }
    }

    private void initFieldVisibility() {
        boolean isSimpleTrigger = getEditedEntity().getScheduleType() == ScheduleType.SIMPLE;
        cronExpressionField.setVisible(!isSimpleTrigger);
        initRepeatFieldsVisibility(isSimpleTrigger, repeatModeSelector.getValue());
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
}
