package io.jmix.quartzui.screen.jobs;

import com.google.common.base.Strings;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.JobSource;
import io.jmix.quartz.model.JobState;
import io.jmix.quartz.service.QuartzService;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.*;

@UiController("quartz_JobModel.browse")
@UiDescriptor("job-model-browse.xml")
@LookupComponent("jobModelsTable")
public class JobModelBrowse extends StandardLookup<JobModel> {

    @Autowired
    private QuartzService quartzService;

    @Autowired
    private Notifications notifications;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private RemoveOperation removeOperation;

    @Autowired
    private CollectionContainer<JobModel> jobModelsDc;

    @Autowired
    private GroupTable<JobModel> jobModelsTable;

    @Autowired
    private TextField<String> nameField;

    @Autowired
    private TextField<String> classField;

    @Autowired
    private TextField<String> groupField;

    @Autowired
    private ComboBox<JobState> jobStateComboBox;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        jobStateComboBox.setOptionsEnum(JobState.class);
        loadJobsData();
    }

    private void loadJobsData() {
        List<JobModel> sortedJobs = quartzService.getAllJobs().stream()
                .filter(jobModel -> Strings.isNullOrEmpty(nameField.getValue()) || StringUtils.containsIgnoreCase(jobModel.getJobName(), nameField.getValue()))
                .filter(jobModel -> Strings.isNullOrEmpty(classField.getValue()) || StringUtils.containsIgnoreCase(jobModel.getJobName(), classField.getValue()))
                .filter(jobModel -> Strings.isNullOrEmpty(groupField.getValue()) || StringUtils.containsIgnoreCase(jobModel.getJobGroup(), groupField.getValue()))
                .filter(jobModel -> jobStateComboBox.getValue() == null || jobStateComboBox.getValue().equals(jobModel.getJobState()))
                .sorted(comparing(JobModel::getJobState, nullsLast(naturalOrder()))
                        .thenComparing(JobModel::getJobName))
                .collect(Collectors.toList());

        jobModelsDc.setItems(sortedJobs);
    }

    @Install(to = "jobModelsTable.executeNow", subject = "enabledRule")
    private boolean jobModelsTableExecuteNowEnabledRule() {
        return !CollectionUtils.isEmpty(jobModelsTable.getSelected())
                && !isJobActive(jobModelsTable.getSelected().iterator().next());
    }

    @Install(to = "jobModelsTable.activate", subject = "enabledRule")
    private boolean jobModelsTableActivateEnabledRule() {
        if (CollectionUtils.isEmpty(jobModelsTable.getSelected())) {
            return false;
        }

        JobModel selectedJobModel = jobModelsTable.getSelected().iterator().next();
        return !isJobActive(selectedJobModel) && CollectionUtils.isNotEmpty(selectedJobModel.getTriggers());
    }

    @Install(to = "jobModelsTable.deactivate", subject = "enabledRule")
    private boolean jobModelsTableDeactivateEnabledRule() {
        return !CollectionUtils.isEmpty(jobModelsTable.getSelected())
                && isJobActive(jobModelsTable.getSelected().iterator().next());
    }

    @Install(to = "jobModelsTable.remove", subject = "enabledRule")
    private boolean jobModelsTableRemoveEnabledRule() {
        if (CollectionUtils.isEmpty(jobModelsTable.getSelected())) {
            return false;
        }

        JobModel selectedJobModel = jobModelsTable.getSelected().iterator().next();
        return !isJobActive(selectedJobModel) && JobSource.USER_DEFINED.equals(selectedJobModel.getJobSource());
    }

    @Subscribe("jobModelsTable.executeNow")
    public void onJobModelsTableExecuteNow(Action.ActionPerformedEvent event) {
        JobModel selectedJobModel = jobModelsTable.getSelected().iterator().next();
        quartzService.executeNow(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withDescription(messageBundle.formatMessage("jobExecuted", selectedJobModel.getJobName()))
                .show();

        loadJobsData();
    }

    @Subscribe("jobModelsTable.activate")
    public void onJobModelsTableActivate(Action.ActionPerformedEvent event) {
        JobModel selectedJobModel = jobModelsTable.getSelected().iterator().next();
        quartzService.resumeJob(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withDescription(messageBundle.formatMessage("jobResumed", selectedJobModel.getJobName()))
                .show();

        loadJobsData();
    }

    @Subscribe("jobModelsTable.deactivate")
    public void onJobModelsTableDeactivate(Action.ActionPerformedEvent event) {
        JobModel selectedJobModel = jobModelsTable.getSelected().iterator().next();
        quartzService.pauseJob(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withDescription(messageBundle.formatMessage("jobPaused", selectedJobModel.getJobName()))
                .show();

        loadJobsData();
    }

    @Subscribe("jobModelsTable.remove")
    public void onJobModelsTableRemove(Action.ActionPerformedEvent event) {
        removeOperation.builder(jobModelsTable)
                .withConfirmation(true)
                .beforeActionPerformed(e -> {
                    if (CollectionUtils.isNotEmpty(e.getItems())) {
                        JobModel jobToDelete = e.getItems().get(0);
                        quartzService.deleteJob(jobToDelete.getJobName(), jobToDelete.getJobGroup());
                        notifications.create(Notifications.NotificationType.HUMANIZED)
                                .withDescription(messageBundle.formatMessage("jobDeleted", jobToDelete.getJobName()))
                                .show();
                        loadJobsData();
                    }
                })
                .remove();
    }

    @Subscribe("jobModelsTable.refresh")
    public void onJobModelsTableRefresh(Action.ActionPerformedEvent event) {
        loadJobsData();
    }

    @Install(to = "jobModelsTable.create", subject = "afterCommitHandler")
    private void jobModelsTableCreateAfterCommitHandler(JobModel jobModel) {
        loadJobsData();
    }

    @Install(to = "jobModelsTable.edit", subject = "afterCommitHandler")
    private void jobModelsTableEditAfterCommitHandler(JobModel jobModel) {
        loadJobsData();
    }

    private boolean isJobActive(JobModel jobModel) {
        return jobModel != null && jobModel.getJobState() == JobState.NORMAL;
    }

    @Subscribe("applyFilter")
    public void onApplyFilter(Action.ActionPerformedEvent event) {
        loadJobsData();
    }

}
