/*
 * Copyright 2026 Haulmont.
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

package job_model_detail;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.OpenedDialogWindows;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.quartz.exception.QuartzJobSaveException;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.ScheduleType;
import io.jmix.quartz.model.TriggerModel;
import io.jmix.quartzflowui.view.jobs.JobModelDetailView;
import io.jmix.quartzflowui.view.jobs.JobModelListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.QuartzFlowuiJdbcStoreTestConfiguration;
import test_support.TestJob;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Verifies that saving a renamed job is all-or-nothing: the delete under the old key
 * and the creation under the new key run in one transaction of the JDBC job store.
 */
@UiTest(viewBasePackages = {"io.jmix.quartzflowui.view", "test_support.view"})
@SpringBootTest(classes = {QuartzFlowuiJdbcStoreTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class JobModelRenameAtomicityTest {

    static final String JOB_GROUP = "atomicityGroup";
    static final String TRIGGER_GROUP = "atomicityTriggerGroup";

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    Scheduler scheduler;
    @Autowired
    UnconstrainedDataManager dataManager;
    //OpenedDialogWindows is vaadin-session scoped, so it must be resolved when the test session is active
    @Autowired
    ObjectProvider<OpenedDialogWindows> openedDialogWindowsProvider;

    @AfterEach
    void tearDown() throws SchedulerException {
        for (String jobName : List.of("renameJobA", "renameJobB", "collisionJob")) {
            scheduler.deleteJob(JobKey.jobKey(jobName, JOB_GROUP));
        }
    }

    @Test
    void testRenameJobMovesJobAndTriggerToNewKey() throws SchedulerException {
        createDurableJob("renameJobA");
        scheduleTrigger("renameJobA", "triggerA");

        JobModelDetailView detailView = openJobEditor("renameJobA");
        TypedTextField<String> jobNameField = UiTestUtils.getComponent(detailView, "jobNameField");
        jobNameField.setValue("renameJobB");

        detailView.closeWithSave();

        JobKey dstKey = JobKey.jobKey("renameJobB", JOB_GROUP);
        Assertions.assertFalse(scheduler.checkExists(JobKey.jobKey("renameJobA", JOB_GROUP)));
        Assertions.assertTrue(scheduler.checkExists(dstKey));
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(dstKey);
        Assertions.assertEquals(1, triggers.size());
        Assertions.assertEquals("triggerA", triggers.get(0).getKey().getName());
    }

    @Test
    void testFailedRenameLeavesOriginalJobIntact() throws SchedulerException {
        createDurableJob("renameJobA");
        scheduleTrigger("renameJobA", "triggerA");
        //an unrelated job whose trigger key will collide with a trigger scheduled during the rename
        createDurableJob("collisionJob");
        scheduleTrigger("collisionJob", "collisionTrigger");

        JobModelDetailView detailView = openJobEditor("renameJobA");
        TypedTextField<String> jobNameField = UiTestUtils.getComponent(detailView, "jobNameField");
        jobNameField.setValue("renameJobB");

        //a trigger whose key belongs to another job makes scheduling fail in the middle of the save
        CollectionContainer<TriggerModel> triggerModelDc = ViewControllerUtils.getViewData(detailView)
                .getContainer("triggerModelDc");
        triggerModelDc.getMutableItems().add(buildTriggerModel("collisionTrigger"));

        Assertions.assertThrows(QuartzJobSaveException.class, detailView::closeWithSave);

        //the save is rolled back entirely: the job under the original key keeps its trigger,
        //nothing appears under the new key
        JobKey srcKey = JobKey.jobKey("renameJobA", JOB_GROUP);
        Assertions.assertTrue(scheduler.checkExists(srcKey));
        List<? extends Trigger> srcTriggers = scheduler.getTriggersOfJob(srcKey);
        Assertions.assertEquals(1, srcTriggers.size());
        Assertions.assertEquals("triggerA", srcTriggers.get(0).getKey().getName());
        Assertions.assertFalse(scheduler.checkExists(JobKey.jobKey("renameJobB", JOB_GROUP)));

        //the unrelated job is untouched
        Assertions.assertEquals(1, scheduler.getTriggersOfJob(JobKey.jobKey("collisionJob", JOB_GROUP)).size());
    }

    void createDurableJob(String jobName) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
                .withIdentity(jobName, JOB_GROUP)
                .storeDurably()
                .build();
        scheduler.addJob(jobDetail, true);
    }

    /**
     * Start date is in the future so that the trigger never becomes eligible for firing during the test.
     */
    void scheduleTrigger(String jobName, String triggerName) throws SchedulerException {
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(JobKey.jobKey(jobName, JOB_GROUP))
                .withIdentity(triggerName, TRIGGER_GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"))
                .startAt(farFutureDate())
                .build();
        scheduler.scheduleJob(trigger);
    }

    TriggerModel buildTriggerModel(String triggerName) {
        TriggerModel triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName(triggerName);
        triggerModel.setTriggerGroup(TRIGGER_GROUP);
        triggerModel.setScheduleType(ScheduleType.CRON_EXPRESSION);
        triggerModel.setCronExpression("0 0 0 * * ?");
        triggerModel.setStartDate(farFutureDate());
        return triggerModel;
    }

    Date farFutureDate() {
        return Date.from(LocalDateTime.now().plus(1, ChronoUnit.HOURS).atZone(ZoneId.systemDefault()).toInstant());
    }

    JobModelDetailView openJobEditor(String jobName) {
        viewNavigationSupport.navigate(JobModelListView.class);
        JobModelListView listView = UiTestUtils.getCurrentView();

        CollectionContainer<JobModel> jobModelsDc = ViewControllerUtils.getViewData(listView)
                .getContainer("jobModelsDc");
        JobModel jobModel = jobModelsDc.getItems().stream()
                .filter(jm -> jobName.equals(jm.getJobName()))
                .findFirst().orElseThrow();

        DataGrid<JobModel> jobModelsTable = UiTestUtils.getComponent(listView, "jobModelsTable");
        jobModelsTable.select(jobModel);
        Objects.requireNonNull(jobModelsTable.getAction("edit")).actionPerform(jobModelsTable);

        JobModelDetailView detailView = (JobModelDetailView) openedDialogWindowsProvider.getObject()
                .getCurrentDialog().orElse(null);
        Assertions.assertNotNull(detailView);
        return detailView;
    }
}
