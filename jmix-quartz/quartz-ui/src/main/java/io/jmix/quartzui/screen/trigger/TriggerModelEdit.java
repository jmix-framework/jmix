package io.jmix.quartzui.screen.trigger;

import com.google.common.base.Strings;
import io.jmix.quartz.model.ScheduleType;
import io.jmix.quartz.model.TriggerModel;
import io.jmix.quartz.service.QuartzService;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.TextField;
import io.jmix.ui.screen.*;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("quartz_TriggerModel.edit")
@UiDescriptor("trigger-model-edit.xml")
@EditedEntityContainer("triggerModelDc")
public class TriggerModelEdit extends StandardEditor<TriggerModel> {

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

    @SuppressWarnings("ConstantConditions")
    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initTriggerGroupNames();
        initFieldVisibility();
        scheduleTypeField.addValueChangeListener(e -> initFieldVisibility());
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

}
