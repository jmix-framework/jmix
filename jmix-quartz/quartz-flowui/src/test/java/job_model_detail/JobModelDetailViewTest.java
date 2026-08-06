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

import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.service.QuartzService;
import io.jmix.quartzflowui.view.jobs.JobModelDetailView;
import io.jmix.quartzflowui.view.jobs.JobModelListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.QuartzFlowuiTestConfiguration;
import test_support.TestJob;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UiTest(viewBasePackages = {"io.jmix.quartzflowui.view", "test_support.view"})
@SpringBootTest(classes = {QuartzFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class JobModelDetailViewTest {

    static final String JOB_GROUP = "renameGroup";

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    QuartzService quartzService;
    @Autowired
    Scheduler scheduler;

    @AfterEach
    void tearDown() throws SchedulerException {
        scheduler.deleteJob(JobKey.jobKey("renameJobA", JOB_GROUP));
        scheduler.deleteJob(JobKey.jobKey("renameJobB", JOB_GROUP));
    }

    @Test
    void testRenameJobBackToOriginalName() throws SchedulerException {
        createDurableJob("renameJobA");

        JobModelDetailView detailView = openJobEditor("renameJobA");

        TypedTextField<String> jobNameField = UiTestUtils.getComponent(detailView, "jobNameField");
        jobNameField.setValue("renameJobB");
        jobNameField.setValue("renameJobA");

        assertTrue(UiTestUtils.validateView(detailView).isEmpty());

        detailView.closeWithSave();

        assertNull(UiTestUtils.getLastOpenedViewDialog());
        assertTrue(quartzService.checkJobExists("renameJobA", JOB_GROUP));
        assertFalse(quartzService.checkJobExists("renameJobB", JOB_GROUP));
    }

    @Test
    void testRenameJobRemovesJobWithObsoleteKey() throws SchedulerException {
        createDurableJob("renameJobA");

        JobModelDetailView detailView = openJobEditor("renameJobA");

        TypedTextField<String> jobNameField = UiTestUtils.getComponent(detailView, "jobNameField");
        jobNameField.setValue("renameJobB");

        detailView.closeWithSave();

        assertNull(UiTestUtils.getLastOpenedViewDialog());
        assertTrue(quartzService.checkJobExists("renameJobB", JOB_GROUP));
        assertFalse(quartzService.checkJobExists("renameJobA", JOB_GROUP));
    }

    void createDurableJob(String jobName) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
                .withIdentity(jobName, JOB_GROUP)
                .storeDurably()
                .build();
        scheduler.addJob(jobDetail, true);
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

        JobModelDetailView detailView = UiTestUtils.getLastOpenedViewDialog();
        assertNotNull(detailView);
        return detailView;
    }
}
