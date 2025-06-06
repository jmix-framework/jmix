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

package io.jmix.quartzflowui.view.jobs;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.AccessManager;
import io.jmix.core.LoadContext;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.AbstractUrlQueryParametersBinder;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.JobSource;
import io.jmix.quartz.model.JobState;
import io.jmix.quartz.service.QuartzService;
import io.jmix.quartz.util.ScheduleDescriptionProvider;
import io.jmix.quartzflowui.accesscontext.UiQuartzAdministrationAccessContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

    public static final String JOB_NAME_URL_PARAM = "jobName";
    public static final String JOB_GROUP_URL_PARAM = "jobGroup";
    public static final String JOB_CLASS_URL_PARAM = "jobClass";
    public static final String JOB_STATE_URL_PARAM = "jobState";

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
    protected MessageBundle messageBundle;
    @ViewComponent
    protected UrlQueryParametersFacet urlQueryParameters;
    @ViewComponent
    protected CollectionContainer<JobModel> jobModelsDc;
    @ViewComponent
    protected CollectionLoader<JobModel> jobModelsDl;

    @Autowired
    protected RemoveOperation removeOperation;
    @Autowired
    protected QuartzService quartzService;
    @Autowired
    protected ScheduleDescriptionProvider scheduleDescriptionProvider;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AccessManager accessManager;

    protected boolean administrationPermitted;

    @Subscribe
    protected void onInit(InitEvent event) {
        initUrlParameters();
        applySecurityConstraints();
    }

    protected void applySecurityConstraints() {
        UiQuartzAdministrationAccessContext accessContext = new UiQuartzAdministrationAccessContext();
        accessManager.applyRegisteredConstraints(accessContext);
        administrationPermitted = accessContext.isPermitted();
    }

    @Supply(to = "jobModelsTable.nextFireDate", subject = "renderer")
    protected Renderer<JobModel> jobModelNextFireDateRenderer() {
        return new TextRenderer<>(jobModel -> getFormattedDate(jobModel::getNextFireDate));
    }

    @Supply(to = "jobModelsTable.lastFireDate", subject = "renderer")
    protected Renderer<JobModel> jobModelTableLastFireDateRenderer() {
        return new TextRenderer<>(jobModel -> getFormattedDate(jobModel::getLastFireDate));
    }

    @Supply(to = "jobModelsTable.jobScheduleDescription", subject = "renderer")
    protected Renderer<JobModel> jobModelTableSceduleDescriptionRenderer() {
        return new TextRenderer<>(scheduleDescriptionProvider::getScheduleDescription);
    }

    protected String getFormattedDate(Supplier<Date> dateSupplier) {
        return dateSupplier.get() != null
                ? new SimpleDateFormat(messageBundle.getMessage("dateTimeWithSeconds")).format(dateSupplier.get())
                : StringUtils.EMPTY;
    }

    @Install(to = "jobModelsDl", target = Target.DATA_LOADER)
    private List<JobModel> jobModelsDlLoadDelegate(final LoadContext<JobModel> loadContext) {
        return loadJobsData();
    }

    @Install(to = "jobModelsTable.jobState", subject = "partNameGenerator")
    protected String jobStatePartNameGenerator(final JobModel entity) {
        if (entity != null && JobState.INVALID.equals(entity.getJobState())) {
            return "quartz-job-invalid";
        }
        return null;
    }

    protected void initUrlParameters() {
        urlQueryParameters.registerBinder(new JobUrlQueryParametersBinder());
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        nameFilter.addTypedValueChangeListener(this::onFilterFieldValueChange);
        classFilter.addTypedValueChangeListener(this::onFilterFieldValueChange);
        groupFilter.addTypedValueChangeListener(this::onFilterFieldValueChange);
        jobStateFilter.addValueChangeListener(this::onFilterFieldValueChange);
    }

    protected List<JobModel> loadJobsData() {
        List<GridSortOrder<JobModel>> sorting = jobModelsTable.getSortOrder();

        Comparator<JobModel> jobModelComparator = createJobModelComparator(sorting);
        List<JobModel> jobs = quartzService.getAllJobs().stream()
                .filter(jobModel -> (Strings.isNullOrEmpty(nameFilter.getTypedValue())
                        || containsIgnoreCase(jobModel.getJobName(), nameFilter.getTypedValue()))
                        && (Strings.isNullOrEmpty(classFilter.getTypedValue())
                        || containsIgnoreCase(jobModel.getJobClass(), classFilter.getTypedValue()))
                        && (Strings.isNullOrEmpty(groupFilter.getTypedValue())
                        || containsIgnoreCase(jobModel.getJobGroup(), groupFilter.getTypedValue()))
                        && (jobStateFilter.getValue() == null
                        || jobStateFilter.getValue().equals(jobModel.getJobState())))
                .sorted(jobModelComparator)
                .collect(Collectors.toList());
        jobModelsDc.setItems(jobs);
        return jobs;
    }

    protected Comparator<JobModel> createJobModelComparator(List<GridSortOrder<JobModel>> sorting) {
        Comparator<JobModel> jobModelComparator = null;
        if (sorting.isEmpty()) {
            // Default sorting
            jobModelComparator = comparing(JobModel::getJobState, nullsLast(naturalOrder()))
                    .thenComparing(JobModel::getJobName, String.CASE_INSENSITIVE_ORDER);
        } else {
            // Keep user sorting
            MetaClass jobModelMetaClass = metadata.getClass(JobModel.class);
            for (GridSortOrder<JobModel> sortOrder : sorting) {
                Grid.Column<JobModel> column = sortOrder.getSorted();
                SortDirection direction = sortOrder.getDirection();
                String key = column.getKey();

                Comparator<?> comparator = createSortOrderComparator(jobModelMetaClass, key, direction);

                if (jobModelComparator == null) {
                    jobModelComparator = comparing(jobModel -> EntityValues.getValue(jobModel, key), nullsLast(comparator));
                } else {
                    jobModelComparator = jobModelComparator.thenComparing(jobModel -> EntityValues.getValue(jobModel, key), nullsLast(comparator));
                }
            }
        }
        return jobModelComparator;
    }

    protected Comparator<?> createSortOrderComparator(MetaClass jobModelMetaClass, String propertyKey, SortDirection direction) {
        MetaProperty property = jobModelMetaClass.getProperty(propertyKey);
        Range range = property.getRange();
        boolean isDatatype = range.isDatatype();
        boolean isStringValue = false;
        if (isDatatype) {
            Datatype<Object> datatype = range.asDatatype();
            String datatypeId = datatype.getId();
            if ("string".equals(datatypeId)) {
                isStringValue = true;
            }
        }

        Comparator<?> comparator;
        if (direction.equals(SortDirection.ASCENDING)) {
            comparator = isStringValue ? String.CASE_INSENSITIVE_ORDER : naturalOrder();
        } else {
            comparator = isStringValue ? String.CASE_INSENSITIVE_ORDER.reversed() : reverseOrder();
        }

        return comparator;
    }

    @Install(to = "jobModelsTable.executeNow", subject = "enabledRule")
    protected boolean jobModelsTableExecuteNowEnabledRule() {
        if (!administrationPermitted) {
            return false;
        } else {
            JobModel selectedJobModel = jobModelsTable.getSingleSelectedItem();
            return !CollectionUtils.isEmpty(jobModelsTable.getSelectedItems())
                    && !isJobActive(selectedJobModel)
                    && !isJobInvalid(selectedJobModel);
        }
    }

    @Install(to = "jobModelsTable.activate", subject = "enabledRule")
    protected boolean jobModelsTableActivateEnabledRule() {
        if (!administrationPermitted) {
            return false;
        } else {
            JobModel selectedJobModel = jobModelsTable.getSingleSelectedItem();
            return selectedJobModel != null
                    && !isJobActive(selectedJobModel)
                    && CollectionUtils.isNotEmpty(selectedJobModel.getTriggers())
                    && !isJobInvalid(selectedJobModel);
        }
    }

    @Install(to = "jobModelsTable.deactivate", subject = "enabledRule")
    protected boolean jobModelsTableDeactivateEnabledRule() {
        if (!administrationPermitted) {
            return false;
        } else {
            JobModel selectedJobModel = jobModelsTable.getSingleSelectedItem();
            return selectedJobModel != null
                    && isJobActive(selectedJobModel)
                    && !isJobInvalid(selectedJobModel);
        }
    }

    @Install(to = "jobModelsTable.remove", subject = "enabledRule")
    protected boolean jobModelsTableRemoveEnabledRule() {
        if (!administrationPermitted) {
            return false;
        } else {
            JobModel selectedJobModel = jobModelsTable.getSingleSelectedItem();
            return selectedJobModel != null
                    && !isJobActive(selectedJobModel)
                    && JobSource.USER_DEFINED.equals(selectedJobModel.getJobSource());
        }
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
        if (selectedJobModel == null) {
            return;
        }
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
                        jobModelsDl.load();
                    }
                })
                .remove();
    }

    @Subscribe("jobModelsTable.refresh")
    protected void onJobModelsTableRefresh(ActionPerformedEvent event) {
        jobModelsDl.load();
    }

    @Install(to = "jobModelsTable.create", subject = "afterSaveHandler")
    protected void jobModelsTableCreateAfterCommitHandler(JobModel jobModel) {
        jobModelsDl.load();
    }

    @Install(to = "jobModelsTable.edit", subject = "afterSaveHandler")
    protected void jobModelsTableEditAfterCommitHandler(JobModel jobModel) {
        jobModelsDl.load();
    }

    /**
     * Checks if job is actually active - waiting for next execution time or running at this moment
     */
    protected boolean isJobActive(JobModel jobModel) {
        return isJobWaitingForExecution(jobModel) || isJobRunning(jobModel);
    }

    /**
     * Check if job is actually running via checking running job cache
     */
    protected boolean isJobRunning(JobModel jobModel) {
        if (jobModel == null) {
            return false;
        }
        JobKey jobKey = JobKey.jobKey(jobModel.getJobName(), jobModel.getJobGroup());
        return quartzService.isJobRunning(jobKey);
    }

    /**
     * Checks if job is actually waiting for next execution
     */
    protected boolean isJobWaitingForExecution(JobModel jobModel) {
        return !isJobRunning(jobModel) && hasActiveState(jobModel);
    }

    protected boolean isJobInvalid(JobModel jobModel) {
        return jobModel != null && jobModel.getJobState() == JobState.INVALID;
    }

    /**
     * Checks job model for active (normal\waiting) state without taking into account running job cache
     */
    protected boolean hasActiveState(JobModel jobModel) {
        JobState jobState = jobModel.getJobState();
        return JobState.NORMAL.equals(jobState)
                || JobState.RUNNING.equals(jobState);
    }

    protected void onFilterFieldValueChange(ComponentEvent<?> event) {
        jobModelsDl.load();
    }

    private class JobUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder {
        public JobUrlQueryParametersBinder() {
            nameFilter.addValueChangeListener(event -> {
                String text = event.getValue();
                QueryParameters qp = new QueryParameters(ImmutableMap.of(JOB_NAME_URL_PARAM,
                        text != null ? Collections.singletonList(text) : Collections.emptyList()));
                fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
            });
            groupFilter.addValueChangeListener(event -> {
                String text = event.getValue();
                QueryParameters qp = new QueryParameters(ImmutableMap.of(JOB_GROUP_URL_PARAM,
                        text != null ? Collections.singletonList(text) : Collections.emptyList()));
                fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
            });
            classFilter.addValueChangeListener(event -> {
                String text = event.getValue();
                QueryParameters qp = new QueryParameters(ImmutableMap.of(JOB_CLASS_URL_PARAM,
                        text != null ? Collections.singletonList(text) : Collections.emptyList()));
                fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
            });
            jobStateFilter.addValueChangeListener(event -> {
                JobState jobState = event.getValue();
                QueryParameters qp = new QueryParameters(ImmutableMap.of(JOB_STATE_URL_PARAM,
                        jobState != null ? Collections.singletonList(jobState.getId()) : Collections.emptyList()));
                fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
            });
        }

        @Override
        public void updateState(QueryParameters queryParameters) {
            List<String> jobNameStrings = queryParameters.getParameters().get(JOB_NAME_URL_PARAM);
            if (jobNameStrings != null && !jobNameStrings.isEmpty()) {
                nameFilter.setValue(jobNameStrings.get(0));
            }
            List<String> jobGroupStrings = queryParameters.getParameters().get(JOB_GROUP_URL_PARAM);
            if (jobGroupStrings != null && !jobGroupStrings.isEmpty()) {
                groupFilter.setValue(jobGroupStrings.get(0));
            }
            List<String> jobClassStrings = queryParameters.getParameters().get(JOB_CLASS_URL_PARAM);
            if (jobClassStrings != null && !jobClassStrings.isEmpty()) {
                classFilter.setValue(jobClassStrings.get(0));
            }
            List<String> jobStateStrings = queryParameters.getParameters().get(JOB_STATE_URL_PARAM);
            if (jobStateStrings != null && !jobStateStrings.isEmpty()) {
                jobStateFilter.setValue(JobState.fromId(jobStateStrings.get(0)));
            }
        }

        @Override
        public Component getComponent() {
            return null;
        }
    }
}
