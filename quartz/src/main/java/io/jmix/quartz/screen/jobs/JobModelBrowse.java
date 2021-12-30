package io.jmix.quartz.screen.jobs;

import io.jmix.core.Messages;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.JobState;
import io.jmix.quartz.service.QuartzService;
import io.jmix.quartz.util.QuartzJobClassFinder;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UiController("JobModel.browse")
@UiDescriptor("job-model-browse.xml")
@LookupComponent("jobModelsTable")
public class JobModelBrowse extends StandardLookup<JobModel> {

    @Autowired
    private QuartzService quartzService;

    @Autowired
    private QuartzJobClassFinder quartzJobClassFinder;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Messages messages;

    @Autowired
    private RemoveOperation removeOperation;

    @Autowired
    private CollectionContainer<JobModel> jobModelsDc;

    @Autowired
    private GroupTable<JobModel> jobModelsTable;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadJobsData();
    }

    private void loadJobsData() {
        List<JobModel> sortedJobs = quartzService.getAllJobs().stream()
                .sorted(
                        Comparator.comparing(JobModel::getJobState)
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
        if (isJobActive(selectedJobModel)) {
            return false;
        }

        //it should be disabled to remove internal Jmix jobs
        return quartzJobClassFinder.getQuartzJobClassNames().contains(selectedJobModel.getJobClass());
    }

    @Subscribe("jobModelsTable.executeNow")
    public void onJobModelsTableExecuteNow(Action.ActionPerformedEvent event) {
        JobModel selectedJobModel = jobModelsTable.getSelected().iterator().next();
        quartzService.executeNow(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withDescription(String.format(messages.getMessage(JobModelBrowse.class, "jobExecuted"), selectedJobModel.getJobName()))
                .show();

        loadJobsData();
    }

    @Subscribe("jobModelsTable.activate")
    public void onJobModelsTableActivate(Action.ActionPerformedEvent event) {
        JobModel selectedJobModel = jobModelsTable.getSelected().iterator().next();
        quartzService.resumeJob(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withDescription(String.format(messages.getMessage(JobModelBrowse.class, "jobResumed"), selectedJobModel.getJobName()))
                .show();

        loadJobsData();
    }

    @Subscribe("jobModelsTable.deactivate")
    public void onJobModelsTableDeactivate(Action.ActionPerformedEvent event) {
        JobModel selectedJobModel = jobModelsTable.getSelected().iterator().next();
        quartzService.pauseJob(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withDescription(String.format(messages.getMessage(JobModelBrowse.class, "jobPaused"), selectedJobModel.getJobName()))
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
                                .withDescription(String.format(messages.getMessage(JobModelBrowse.class, "jobDeleted"), jobToDelete.getJobName()))
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

    @Install(to = "jobModelsTable.edit", subject = "afterCloseHandler")
    private void jobModelsTableEditAfterCloseHandler(AfterCloseEvent event) {
        if (event.closedWith(StandardOutcome.COMMIT)) {
            loadJobsData();
        }
    }

    private boolean isJobActive(JobModel jobModel) {
        return jobModel != null && jobModel.getJobState() == JobState.NORMAL;
    }

}