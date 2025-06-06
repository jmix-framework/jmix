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
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.component.grid.editor.EditorCancelEvent;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.AccessManager;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.quartz.model.*;
import io.jmix.quartz.service.QuartzService;
import io.jmix.quartz.util.QuartzJobClassFinder;
import io.jmix.quartz.util.ScheduleDescriptionProvider;
import io.jmix.quartzflowui.accesscontext.UiQuartzAdministrationAccessContext;
import org.apache.commons.lang3.StringUtils;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Route(value = "quartz/jobmodels/:id", layout = DefaultMainViewParent.class)
@ViewController("quartz_JobModel.detail")
@ViewDescriptor("job-model-detail-view.xml")
@EditedEntityContainer("jobModelDc")
@DialogMode(width = "80em", resizable = true)
public class JobModelDetailView extends StandardDetailView<JobModel> {

    @ViewComponent
    protected DataGrid<JobDataParameterModel> jobDataParamsTable;
    @ViewComponent
    protected TextField jobNameField;
    @ViewComponent
    protected ComboBox<String> jobGroupField;
    @ViewComponent
    protected ComboBox<String> jobClassField;
    @ViewComponent
    protected DataGrid<TriggerModel> triggerModelTable;
    @ViewComponent
    protected Button addDataParamButton;
    @ViewComponent
    protected CollectionContainer<JobDataParameterModel> jobDataParamsDc;
    @ViewComponent
    protected CollectionContainer<TriggerModel> triggerModelDc;
    @ViewComponent
    protected MessageBundle messageBundle;

    @Autowired
    protected QuartzService quartzService;
    @Autowired
    protected QuartzJobClassFinder quartzJobClassFinder;
    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected ScheduleDescriptionProvider scheduleDescriptionProvider;
    @Autowired
    protected AccessManager accessManager;

    protected boolean replaceJobIfExists = true;
    protected boolean deleteObsoleteJob = false;
    protected String obsoleteJobName = null;
    protected String obsoleteJobGroup = null;
    protected List<String> jobGroupNames;

    protected boolean administrationPermitted;

    @Subscribe
    protected void onInit(View.InitEvent event) {
        jobGroupNames = quartzService.getJobGroupNames();
        jobGroupField.setItems(jobGroupNames);

        List<String> existedJobsClassNames = quartzJobClassFinder.getQuartzJobClassNames();
        jobClassField.setItems(existedJobsClassNames);

        jobDataParamsTable.getEditor().addCancelListener(this::onJobDataParameterEditorCancel);

        applySecurityConstraints();
    }

    protected void applySecurityConstraints() {
        UiQuartzAdministrationAccessContext accessContext = new UiQuartzAdministrationAccessContext();
        accessManager.applyRegisteredConstraints(accessContext);
        administrationPermitted = accessContext.isPermitted();
    }

    protected void onJobDataParameterEditorCancel(EditorCancelEvent<JobDataParameterModel> event) {
        JobDataParameterModel item = event.getItem();
        if (item != null
                && (Strings.isNullOrEmpty(item.getKey()) || Strings.isNullOrEmpty(item.getValue()))) {
            jobDataParamsDc.getMutableItems().remove(item);
        }
    }

    @Supply(to = "triggerModelTable.triggerDescription", subject = "renderer")
    protected Renderer<TriggerModel> triggerModelTableTriggerDescriptionRenderer() {
        return new TextRenderer<>(scheduleDescriptionProvider::getScheduleDescription);
    }

    @Supply(to = "triggerModelTable.startDate", subject = "renderer")
    protected Renderer<TriggerModel> triggerModelTableStartDateRenderer() {
        return new TextRenderer<>(triggerModel -> getFormattedDate(triggerModel::getStartDate));
    }

    @Supply(to = "triggerModelTable.lastFireDate", subject = "renderer")
    protected Renderer<TriggerModel> triggerModelTableLastFireDateRenderer() {
        return new TextRenderer<>(triggerModel -> getFormattedDate(triggerModel::getLastFireDate));
    }

    @Supply(to = "triggerModelTable.nextFireDate", subject = "renderer")
    protected Renderer<TriggerModel> triggerModelTableNextFireDateRenderer() {
        return new TextRenderer<>(triggerModel -> getFormattedDate(triggerModel::getNextFireDate));
    }

    @Supply(to = "triggerModelTable.endDate", subject = "renderer")
    protected Renderer<TriggerModel> triggerModelTableEndDateRenderer() {
        return new TextRenderer<>(triggerModel -> getFormattedDate(triggerModel::getEndDate));
    }

    protected String getFormattedDate(Supplier<Date> dateSupplier) {
        return dateSupplier.get() != null
                ? new SimpleDateFormat(messageBundle.getMessage("dateTimeWithSeconds")).format(dateSupplier.get())
                : StringUtils.EMPTY;
    }

    @Subscribe("jobGroupField")
    protected void onJobGroupFieldValueSet(ComboBoxBase.CustomValueSetEvent<ComboBox<String>> event) {
        String newJobGroupName = event.getDetail();
        if (!Strings.isNullOrEmpty(newJobGroupName)
                && !jobGroupNames.contains(newJobGroupName)) {
            jobGroupNames.add(newJobGroupName);
            jobGroupField.setItems(jobGroupNames);
            jobGroupField.setValue(newJobGroupName);
        }
        if (!Strings.isNullOrEmpty(obsoleteJobGroup)
                && !Strings.isNullOrEmpty(newJobGroupName)
                && !obsoleteJobGroup.equals(newJobGroupName)) {
            deleteObsoleteJob = true;
        }
    }

    @Subscribe("jobGroupField")
    protected void onJobGroupFieldChange(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> event) {
        String currentValue = event.getValue();
        if (!Strings.isNullOrEmpty(obsoleteJobGroup)
                && !Strings.isNullOrEmpty(currentValue)
                && !obsoleteJobGroup.equals(currentValue)) {
            deleteObsoleteJob = true;
        }
    }

    @Subscribe("jobNameField")
    protected void onjobNameFieldChange(AbstractField.ComponentValueChangeEvent<TextField, String> event) {
        String currentValue = event.getValue();
        if (!Strings.isNullOrEmpty(obsoleteJobName)
                && !Strings.isNullOrEmpty(currentValue)
                && !obsoleteJobName.equals(currentValue)) {
            deleteObsoleteJob = true;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Subscribe
    protected void onInitEntity(StandardDetailView.InitEntityEvent<JobModel> event) {
        JobModel entity = event.getEntity();
        if (entity.getJobSource() == null) {
            entity.setJobSource(JobSource.USER_DEFINED);
            replaceJobIfExists = false;
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        if (!administrationPermitted) {
            // Access denied: block any modifications
            super.setReadOnly(true);
            this.triggerModelTable.getAction("edit").setVisible(false);
            this.triggerModelTable.getAction("read").setVisible(true);
            this.addDataParamButton.setEnabled(false);
            this.jobDataParamsTable.setEnabled(false);
        } else {
            // Access granted: set availability based on business logic
            super.setReadOnly(readOnly);
            jobNameField.setReadOnly(readOnly);
            jobGroupField.setReadOnly(readOnly);
            jobClassField.setReadOnly(readOnly);
            triggerModelTable.getAction("edit").setVisible(!readOnly);
            triggerModelTable.getAction("read").setVisible(readOnly);
            addDataParamButton.setEnabled(!readOnly);
            jobDataParamsTable.setEnabled(!readOnly);
        }
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        //allow editing only not active and user-defined jobs
        boolean readOnly = JobState.NORMAL.equals(getEditedEntity().getJobState())
                || JobState.RUNNING.equals(getEditedEntity().getJobState())
                || JobState.INVALID.equals(getEditedEntity().getJobState())
                || JobSource.PREDEFINED.equals(getEditedEntity().getJobSource());
        setReadOnly(readOnly);

        obsoleteJobName = getEditedEntity().getJobName();
        obsoleteJobGroup = getEditedEntity().getJobGroup();
    }

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Subscribe
    protected void onValidation(ValidationEvent event) {
        ValidationErrors errors = event.getErrors();

        JobModel jobModel = getEditedEntity();
        String currentJobName = jobModel.getJobName();
        String currentJobGroup = jobModel.getJobGroup();

        // if jobKey is changed it is necessary to delete job by it old jobKey and create new one
        // job should be deleted only if it is possible to create new one
        if (deleteObsoleteJob && quartzService.checkJobExists(currentJobName, currentJobGroup)) {
            errors.add(messageBundle.formatMessage("jobAlreadyExistsValidationMessage", currentJobName,
                    Strings.isNullOrEmpty(currentJobGroup) ? "DEFAULT" : currentJobGroup));
        }
        // check for local key duplicates
        getEditedEntity().getTriggers().stream()
                .filter(triggerModel -> !Strings.isNullOrEmpty(triggerModel.getTriggerName()))
                .filter(triggerModel -> {
                    TriggerKey key = TriggerKey.triggerKey(triggerModel.getTriggerName(), triggerModel.getTriggerGroup());
                    return getEditedEntity().getTriggers().stream()
                            .filter(t -> !Strings.isNullOrEmpty(t.getTriggerName()))
                            .filter(t -> TriggerKey.triggerKey(t.getTriggerName(), t.getTriggerGroup()).equals(key))
                            .count() > 1;
                })
                .filter(distinctByKey(t -> TriggerKey.triggerKey(t.getTriggerName(), t.getTriggerGroup())))
                .forEach(triggerModel ->
                        errors.add(
                                messageBundle.formatMessage(
                                        "triggerAlreadyExistsValidationMessage",
                                        triggerModel.getTriggerName(),
                                        Strings.isNullOrEmpty(triggerModel.getTriggerGroup()) ? "DEFAULT" :
                                                triggerModel.getTriggerGroup()))
                );

        if (!replaceJobIfExists) { // create new job
            if (quartzService.checkJobExists(currentJobName, currentJobGroup)) {
                errors.add(messageBundle.formatMessage("jobAlreadyExistsValidationMessage", currentJobName,
                        Strings.isNullOrEmpty(currentJobGroup) ? "DEFAULT" : currentJobGroup));
            }
            // check for quartz key duplicates
            getEditedEntity().getTriggers().stream()
                    .filter(triggerModel -> !Strings.isNullOrEmpty(triggerModel.getTriggerName()))
                    .filter(triggerModel -> quartzService.checkTriggerExists(triggerModel.getTriggerName(),
                            triggerModel.getTriggerGroup()))
                    .filter(distinctByKey(t -> TriggerKey.triggerKey(t.getTriggerName(), t.getTriggerGroup())))
                    .forEach(triggerModel -> errors.add(
                            messageBundle.formatMessage(
                                    "triggerAlreadyExistsValidationMessage",
                                    triggerModel.getTriggerName(),
                                    Strings.isNullOrEmpty(triggerModel.getTriggerGroup()) ? "DEFAULT" :
                                            triggerModel.getTriggerGroup())
                    ));
        }

        //noinspection ConstantValue
        if (jobDataParamsDc.getItems().stream()
                .map(JobDataParameterModel::getKey)
                .anyMatch(Objects::isNull)) {
            errors.add(messageBundle.getMessage("jobDataParamKeyIsRequired"));
        }

        boolean jobDataMapOverlapped = jobDataParamsDc.getItems().stream()
                .map(JobDataParameterModel::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream().anyMatch(entry -> entry.getValue() > 1);
        if (jobDataMapOverlapped) {
            errors.add(messageBundle.getMessage("jobDataParamKeyAlreadyExistsValidationMessage"));
        }
    }

    @Subscribe
    protected void onBeforeCommitChanges(BeforeSaveEvent event) {
        if (deleteObsoleteJob) {
            quartzService.deleteJob(obsoleteJobName, obsoleteJobGroup);
        }

        quartzService.updateQuartzJob(getEditedEntity(), jobDataParamsDc.getItems(), triggerModelDc.getItems(), replaceJobIfExists);
    }

    @Subscribe("jobDataParamsTable.addNewDataParam")
    protected void onJobDataParamsTableCreate(ActionPerformedEvent event) {
        JobDataParameterModel itemToAdd = dataManager.create(JobDataParameterModel.class);
        jobDataParamsDc.getMutableItems().add(itemToAdd);

        jobDataParamsTable.getEditor().editItem(itemToAdd);
    }

}
