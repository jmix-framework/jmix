package io.jmix.quartz.screen.jobs;

import com.google.common.base.Strings;
import io.jmix.quartz.model.*;
import io.jmix.quartz.screen.trigger.TriggerModelEdit;
import io.jmix.quartz.service.QuartzService;
import io.jmix.quartz.util.QuartzJobClassFinder;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.ViewAction;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@UiController("quartz_JobModel.edit")
@UiDescriptor("job-model-edit.xml")
@EditedEntityContainer("jobModelDc")
public class JobModelEdit extends StandardEditor<JobModel> {

    @Autowired
    private QuartzService quartzService;

    @Autowired
    protected QuartzJobClassFinder quartzJobClassFinder;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private CollectionContainer<JobDataParameterModel> jobDataParamsDc;

    @Autowired
    private CollectionContainer<TriggerModel> triggerModelDc;

    @Autowired
    private TextField<String> jobNameField;

    @Autowired
    private ComboBox<String> jobGroupField;

    @Autowired
    private ComboBox<String> jobClassField;

    @Autowired
    private Table<TriggerModel> triggerModelTable;

    @Named("triggerModelTable.edit")
    private EditAction<TriggerModel> triggerModelTableEdit;

    @Named("triggerModelTable.view")
    private ViewAction<TriggerModel> triggerModelTableView;

    private boolean replaceJobIfExists = true;
    private boolean deleteObsoleteJob = false;
    private String obsoleteJobName = null;
    private String obsoleteJobGroup = null;

    @Subscribe
    public void onInitEntity(InitEntityEvent<JobModel> event) {
        JobModel entity = event.getEntity();
        if (entity.getJobSource() == null) {
            entity.setJobSource(JobSource.USER_DEFINED);
            replaceJobIfExists = false;
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        //allow editing only not active and user-defined jobs
        boolean readOnly = JobState.NORMAL.equals(getEditedEntity().getJobState())
                || JobSource.PREDEFINED.equals(getEditedEntity().getJobSource());
        setReadOnly(readOnly);
        jobNameField.setEditable(!readOnly);
        jobGroupField.setEditable(!readOnly);
        jobClassField.setEditable(!readOnly);
        triggerModelTableEdit.setVisible(!readOnly);
        triggerModelTableView.setVisible(readOnly);

        obsoleteJobName = getEditedEntity().getJobName();
        obsoleteJobGroup = getEditedEntity().getJobGroup();

        jobNameField.addValueChangeListener(e -> {
            String currentValue = e.getValue();
            if (!Strings.isNullOrEmpty(obsoleteJobName)
                    && !Strings.isNullOrEmpty(currentValue)
                    && !obsoleteJobName.equals(currentValue)) {
                deleteObsoleteJob = true;
            }
        });

        List<String> jobGroupNames = quartzService.getJobGroupNames();
        jobGroupField.setOptionsList(jobGroupNames);
        jobGroupField.setEnterPressHandler(enterPressEvent -> {
            String newJobGroupName = enterPressEvent.getText();
            if (!Strings.isNullOrEmpty(newJobGroupName)
                    && !jobGroupNames.contains(newJobGroupName)) {
                jobGroupNames.add(newJobGroupName);
                jobGroupField.setOptionsList(jobGroupNames);
            }

            if (!Strings.isNullOrEmpty(obsoleteJobGroup)
                    && !Strings.isNullOrEmpty(newJobGroupName)
                    && !obsoleteJobGroup.equals(newJobGroupName)) {
                deleteObsoleteJob = true;
            }
        });
        jobGroupField.addValueChangeListener(e -> {
            String currentValue = e.getValue();
            if (!Strings.isNullOrEmpty(obsoleteJobGroup)
                    && !Strings.isNullOrEmpty(currentValue)
                    && !obsoleteJobGroup.equals(currentValue)) {
                deleteObsoleteJob = true;
            }
        });

        List<String> existedJobsClassNames = quartzJobClassFinder.getQuartzJobClassNames();
        jobClassField.setOptionsList(existedJobsClassNames);
    }

    @Subscribe("triggerModelTable.view")
    public void onTriggerModelGroupTableView(Action.ActionPerformedEvent event) {
        TriggerModel triggerModel = triggerModelTable.getSingleSelected();
        if (triggerModel == null) {
            return;
        }

        TriggerModelEdit editor = screenBuilders.editor(triggerModelTable)
                .withScreenClass(TriggerModelEdit.class)
                .withParentDataContext(getScreenData().getDataContext())
                .build();
        ((ReadOnlyAwareScreen) editor).setReadOnly(true);
        editor.show();
    }

    @Subscribe
    public void onValidation(ValidationEvent event) {
        ValidationErrors errors = event.getErrors();

        JobModel jobModel = getEditedEntity();
        String currentJobName = jobModel.getJobName();
        String currentJobGroup = jobModel.getJobGroup();

        //if jobKey is changed it is necessary to delete job by it old jobKey and create new one
        //job should be deleted only if it is possible to create new one
        if (deleteObsoleteJob && quartzService.checkJobExists(currentJobName, currentJobGroup)) {
            errors.add(messageBundle.formatMessage("jobAlreadyExistsValidationMessage", currentJobName, Strings.isNullOrEmpty(currentJobGroup) ? "DEFAULT" : currentJobGroup));
        }

        if (!replaceJobIfExists) {
            if (quartzService.checkJobExists(currentJobName, currentJobGroup)) {
                errors.add(messageBundle.formatMessage("jobAlreadyExistsValidationMessage", currentJobName, Strings.isNullOrEmpty(currentJobGroup) ? "DEFAULT" : currentJobGroup));
            }

            getEditedEntity().getTriggers().stream()
                    .filter(triggerModel -> !Strings.isNullOrEmpty(triggerModel.getTriggerName()))
                    .filter(triggerModel -> quartzService.checkTriggerExists(triggerModel.getTriggerName(), triggerModel.getTriggerGroup()))
                    .forEach(triggerModel -> errors.add(
                            messageBundle.formatMessage(
                                    "triggerAlreadyExistsValidationMessage",
                                    triggerModel.getTriggerName(),
                                    Strings.isNullOrEmpty(triggerModel.getTriggerGroup()) ? "DEFAULT" : triggerModel.getTriggerGroup())
                    ));
        }

        //validate if job data param keys are not unique
        boolean jobDataMapOverlapped = jobDataParamsDc.getItems().stream()
                .map(JobDataParameterModel::getKey)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream().anyMatch(entry -> entry.getValue() > 1);
        if (jobDataMapOverlapped) {
            errors.add(messageBundle.getMessage("jobDataParamKeyAlreadyExistsValidationMessage"));
        }
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        if (deleteObsoleteJob) {
            quartzService.deleteJob(obsoleteJobName, obsoleteJobGroup);
        }

        quartzService.updateQuartzJob(getEditedEntity(), jobDataParamsDc.getItems(), triggerModelDc.getItems(), replaceJobIfExists);
    }

}