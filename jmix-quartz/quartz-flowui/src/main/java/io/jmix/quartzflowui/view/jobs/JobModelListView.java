/*
 * Copyright 2024 Haulmont.
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

package io.jmix.quartzflowui.view.jobs;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.MessageTools;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.JobSource;
import io.jmix.quartz.model.JobState;
import io.jmix.quartz.util.ScheduleDescriptionProvider;
import io.jmix.quartz.service.QuartzService;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.*;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

@Route(value = "quartz/jobmodels", layout = DefaultMainViewParent.class)
@ViewController("quartz_JobModel.list")
@ViewDescriptor("job-model-list-view.xml")
@LookupComponent("jobModelsTable")
@DialogMode(width = "60em")
public class JobModelListView extends StandardListView<JobModel> {

    @ViewComponent
    protected DataGrid<JobModel> jobModelsTable;
    @ViewComponent
    protected TypedTextField<String> nameFilter;
    @ViewComponent
    protected TypedTextField<String> classFilter;
    @ViewComponent
    protected TypedTextField<String> groupFilter;
    @ViewComponent
    protected JmixSelect<JobState> jobStateFilter;

    @ViewComponent
    protected CollectionContainer<JobModel> jobModelsDc;

    @Autowired
    protected RemoveOperation removeOperation;
    @Autowired
    protected QuartzService quartzService;

    @Autowired
    protected ScheduleDescriptionProvider scheduleDescriptionProvider;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected MessageTools messageTools;

    @Subscribe
    protected void onInit(View.InitEvent event) {
        initTable();
    }

    protected void initTable() {
        DataGridColumn<JobModel> triggerDescriptionColumn = jobModelsTable.addColumn(new TextRenderer<>(job -> scheduleDescriptionProvider.getScheduleDescription(job)));
        triggerDescriptionColumn.setHeader(messageBundle.getMessage("column.jobScheduleDescription.header"));
        jobModelsTable.setColumnPosition(triggerDescriptionColumn, 5);
        triggerDescriptionColumn.setResizable(true).setWidth("20%");

        jobModelsTable.addColumn(entity -> entity.getLastFireDate() != null ?
                        new SimpleDateFormat(messageBundle.getMessage("dateTimeWithSeconds"))
                                .format(entity.getLastFireDate()) : "").setResizable(false)
                .setHeader(getHeaderForPropertyColumn("lastFireDate"))
                .setAutoWidth(true);

        jobModelsTable.addColumn(entity -> entity.getNextFireDate() != null ?
                        new SimpleDateFormat(messageBundle.getMessage("dateTimeWithSeconds"))
                                .format(entity.getNextFireDate()) : "").setResizable(false)
                .setHeader(getHeaderForPropertyColumn("nextFireDate"))
                .setAutoWidth(true);
    }

    private String getHeaderForPropertyColumn(String propertyName) {
        return messageTools.getPropertyCaption(jobModelsDc.getEntityMetaClass(), propertyName);
    }

    @Subscribe
    protected void onBeforeShow(View.BeforeShowEvent event) {
        nameFilter.addTypedValueChangeListener(this::onFilterFieldValueChange);
        classFilter.addTypedValueChangeListener(this::onFilterFieldValueChange);
        groupFilter.addTypedValueChangeListener(this::onFilterFieldValueChange);
        jobStateFilter.addValueChangeListener(this::onFilterFieldValueChange);

        loadJobsData();
    }


    protected List<JobModel> loadJobsData() {
        List<JobModel> jobs = quartzService.getAllJobs().stream()
                .filter(jobModel -> (Strings.isNullOrEmpty(nameFilter.getTypedValue())
                        || containsIgnoreCase(jobModel.getJobName(), nameFilter.getTypedValue()))
                        && (Strings.isNullOrEmpty(classFilter.getTypedValue())
                        || containsIgnoreCase(jobModel.getJobClass(), classFilter.getTypedValue()))
                        && (Strings.isNullOrEmpty(groupFilter.getTypedValue())
                        || containsIgnoreCase(jobModel.getJobGroup(), groupFilter.getTypedValue()))
                        && (jobStateFilter.getValue() == null
                        || jobStateFilter.getValue().equals(jobModel.getJobState())))
                .collect(Collectors.toList());

        jobModelsDc.setItems(jobs);
        return jobs;
    }

    @Install(to = "jobModelsTable.executeNow", subject = "enabledRule")
    protected boolean jobModelsTableExecuteNowEnabledRule() {
        return !CollectionUtils.isEmpty(jobModelsTable.getSelectedItems())
                && !isJobActive(jobModelsTable.getSingleSelectedItem());
    }

    @Install(to = "jobModelsTable.activate", subject = "enabledRule")
    protected boolean jobModelsTableActivateEnabledRule() {
        JobModel selectedJobModel = jobModelsTable.getSingleSelectedItem();
        return selectedJobModel != null
                && !isJobActive(selectedJobModel)
                && CollectionUtils.isNotEmpty(selectedJobModel.getTriggers());
    }

    @Install(to = "jobModelsTable.deactivate", subject = "enabledRule")
    protected boolean jobModelsTableDeactivateEnabledRule() {
        return (jobModelsTable.getSingleSelectedItem() != null)
                && isJobActive(jobModelsTable.getSingleSelectedItem());
    }

    @Install(to = "jobModelsTable.remove", subject = "enabledRule")
    protected boolean jobModelsTableRemoveEnabledRule() {
        JobModel selectedJobModel = jobModelsTable.getSingleSelectedItem();
        return selectedJobModel != null
                && !isJobActive(selectedJobModel)
                && JobSource.USER_DEFINED.equals(selectedJobModel.getJobSource());
    }

    protected void updateDataWithSelection(JobModel selectedJobModel) {
        List<JobModel> newJobs = loadJobsData();
        jobModelsTable.sort(jobModelsTable.getSortOrder());
        JobKey newJobKey = JobKey.jobKey(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        newJobs.stream()
                .filter(j -> JobKey.jobKey(j.getJobName(), j.getJobGroup()).equals(newJobKey))
                .findAny().ifPresent(selectedJob -> jobModelsTable.select(selectedJob));
    }

    @Subscribe("jobModelsTable.executeNow")
    protected void onJobModelsTableExecuteNow(ActionPerformedEvent event) {
        JobModel selectedJobModel = jobModelsTable.getSingleSelectedItem();
        if (selectedJobModel == null) {
            return;
        }

        quartzService.executeNow(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        notifications.create(messageBundle.formatMessage("jobExecuted", selectedJobModel.getJobName()))
                .withType(Notifications.Type.DEFAULT)
                .show();
        updateDataWithSelection(selectedJobModel);
    }

    @Subscribe("jobModelsTable.activate")
    protected void onJobModelsTableActivate(ActionPerformedEvent event) {
        JobModel selectedJobModel = jobModelsTable.getSingleSelectedItem();
        if (selectedJobModel == null) {
            return;
        }

        quartzService.resumeJob(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        notifications.create(messageBundle.formatMessage("jobResumed", selectedJobModel.getJobName()))
                .withType(Notifications.Type.DEFAULT)
                .show();

        updateDataWithSelection(selectedJobModel);
    }

    @Subscribe("jobModelsTable.deactivate")
    protected void onJobModelsTableDeactivate(ActionPerformedEvent event) {
        JobModel selectedJobModel = jobModelsTable.getSingleSelectedItem();
        quartzService.pauseJob(selectedJobModel.getJobName(), selectedJobModel.getJobGroup());
        notifications.create(messageBundle.formatMessage("jobPaused", selectedJobModel.getJobName()))
                .withType(Notifications.Type.DEFAULT)
                .show();

        updateDataWithSelection(selectedJobModel);
    }

    @Subscribe("jobModelsTable.remove")
    protected void onJobModelsTableRemove(ActionPerformedEvent event) {
        removeOperation.builder(jobModelsTable)
                .withConfirmation(true)
                .beforeActionPerformed(e -> {
                    if (CollectionUtils.isNotEmpty(e.getItems())) {
                        JobModel jobToDelete = e.getItems().get(0);
                        quartzService.deleteJob(jobToDelete.getJobName(), jobToDelete.getJobGroup());
                        notifications.create(messageBundle.formatMessage("jobDeleted", jobToDelete.getJobName()))
                                .withType(Notifications.Type.DEFAULT)
                                .show();
                        loadJobsData();
                    }
                })
                .remove();
    }

    @Subscribe("jobModelsTable.refresh")
    protected void onJobModelsTableRefresh(ActionPerformedEvent event) {
        loadJobsData();
    }

    @Install(to = "jobModelsTable.create", subject = "afterSaveHandler")
    protected void jobModelsTableCreateAfterCommitHandler(JobModel jobModel) {
        loadJobsData();
    }

    @Install(to = "jobModelsTable.edit", subject = "afterSaveHandler")
    protected void jobModelsTableEditAfterCommitHandler(JobModel jobModel) {
        loadJobsData();
    }

    protected boolean isJobActive(JobModel jobModel) {
        return jobModel != null && jobModel.getJobState() == JobState.NORMAL;
    }

    protected void onFilterFieldValueChange(ComponentEvent<?> event) {
        loadJobsData();
    }
}
