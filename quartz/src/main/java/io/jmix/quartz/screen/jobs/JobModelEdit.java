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
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.List;

@UiController("JobModel.edit")
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

    private boolean isJobShouldBeReplaced = false;
    private String jobNameToDelete = null;
    private String jobGroupToDelete = null;

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

        jobNameToDelete = getEditedEntity().getJobName();
        jobGroupToDelete = getEditedEntity().getJobGroup();

        jobNameField.addValueChangeListener(e -> {
            String currentValue = e.getValue();
            if (!Strings.isNullOrEmpty(jobNameToDelete)
                    && !Strings.isNullOrEmpty(currentValue)
                    && !jobNameToDelete.equals(currentValue)) {
                isJobShouldBeReplaced = true;
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

            if (!Strings.isNullOrEmpty(jobGroupToDelete)
                    && !Strings.isNullOrEmpty(newJobGroupName)
                    && !jobGroupToDelete.equals(newJobGroupName)) {
                isJobShouldBeReplaced = true;
            }
        });
        jobGroupField.addValueChangeListener(e -> {
            String currentValue = e.getValue();
            if (!Strings.isNullOrEmpty(jobGroupToDelete)
                    && !Strings.isNullOrEmpty(currentValue)
                    && !jobGroupToDelete.equals(currentValue)) {
                isJobShouldBeReplaced = true;
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

    @Subscribe("windowCommitAndClose")
    public void onWindowCommitAndClose(Action.ActionPerformedEvent event) {
        JobModel jobModel = getEditedEntity();
        if (isJobShouldBeReplaced) {
            quartzService.deleteJob(jobNameToDelete, jobGroupToDelete);
        }

        quartzService.updateQuartzJob(jobModel, jobDataParamsDc.getItems(), triggerModelDc.getItems(), isJobShouldBeReplaced);
    }

}