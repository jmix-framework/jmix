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

package io.jmix.quartz;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.quartz.exception.QuartzJobSaveException;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.ScheduleType;
import io.jmix.quartz.model.TriggerModel;
import io.jmix.quartz.service.JobSaveContext;
import io.jmix.quartz.service.QuartzService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                CoreConfiguration.class,
                DataConfiguration.class,
                EclipselinkConfiguration.class,
                QuartTestApplication.class
        }
)
public class QuartzSaveJobTest {

    static final String JOB_GROUP = "saveJobGroup";
    static final String TRIGGER_GROUP = "saveJobTriggerGroup";

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private QuartzService quartzService;

    @Autowired
    private UnconstrainedDataManager dataManager;

    @AfterEach
    void tearDown() throws SchedulerException {
        for (String jobName : List.of("renameSrcJob", "renameDstJob", "atomicSrcJob", "atomicDstJob",
                "collisionJob", "existingJob")) {
            scheduler.deleteJob(JobKey.jobKey(jobName, JOB_GROUP));
        }
    }

    @Test
    void testSaveNewJob() throws SchedulerException {
        JobModel jobModel = buildJobModel("renameSrcJob");
        jobModel.setTriggers(List.of(buildCronTriggerModel("renameSrcTrigger")));
        quartzService.saveJob(new JobSaveContext(jobModel));

        JobKey jobKey = JobKey.jobKey("renameSrcJob", JOB_GROUP);
        Assertions.assertTrue(scheduler.checkExists(jobKey));
        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobKey).size());
    }

    @Test
    void testSaveNewJobFailsIfJobAlreadyExists() {
        quartzService.saveJob(new JobSaveContext(buildJobModel("existingJob")));

        Assertions.assertThrows(QuartzJobSaveException.class,
                () -> quartzService.saveJob(new JobSaveContext(buildJobModel("existingJob"))));
    }

    @Test
    void testSaveJobWithSameKeyUpdatesInPlace() throws SchedulerException {
        JobModel jobModel = buildJobModel("existingJob");
        quartzService.saveJob(new JobSaveContext(jobModel));

        JobKey jobKey = JobKey.jobKey("existingJob", JOB_GROUP);
        jobModel.setDescription("updated description");
        jobModel.setTriggers(List.of(buildCronTriggerModel("existingJobTrigger")));
        quartzService.saveJob(new JobSaveContext(jobModel).setOriginalJobKey(jobKey));

        Assertions.assertEquals("updated description", scheduler.getJobDetail(jobKey).getDescription());
        Assertions.assertEquals(1, scheduler.getTriggersOfJob(jobKey).size());
    }

    @Test
    void testRenameJobMovesJobToNewKey() throws SchedulerException {
        JobModel jobModel = buildJobModel("renameSrcJob");
        jobModel.setDescription("job to rename");
        jobModel.setTriggers(List.of(buildCronTriggerModel("renameTrigger")));
        quartzService.saveJob(new JobSaveContext(jobModel));

        JobKey srcKey = JobKey.jobKey("renameSrcJob", JOB_GROUP);
        JobKey dstKey = JobKey.jobKey("renameDstJob", JOB_GROUP);

        jobModel.setJobName("renameDstJob");
        quartzService.saveJob(new JobSaveContext(jobModel).setOriginalJobKey(srcKey));

        Assertions.assertFalse(scheduler.checkExists(srcKey));
        Assertions.assertTrue(scheduler.checkExists(dstKey));
        Assertions.assertEquals("job to rename", scheduler.getJobDetail(dstKey).getDescription());
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(dstKey);
        Assertions.assertEquals(1, triggers.size());
        Assertions.assertEquals("renameTrigger", triggers.get(0).getKey().getName());
    }

    @Test
    void testFailedRenameLeavesOriginalJobIntact() throws SchedulerException {
        //an unrelated job whose trigger key will collide with a trigger scheduled during the rename
        JobKey collisionJobKey = JobKey.jobKey("collisionJob", JOB_GROUP);
        JobModel collisionJobModel = buildJobModel("collisionJob");
        collisionJobModel.setTriggers(List.of(buildCronTriggerModel("collisionTrigger")));
        quartzService.saveJob(new JobSaveContext(collisionJobModel));

        JobModel jobModel = buildJobModel("atomicSrcJob");
        jobModel.setTriggers(List.of(buildCronTriggerModel("atomicTrigger")));
        quartzService.saveJob(new JobSaveContext(jobModel));

        JobKey srcKey = JobKey.jobKey("atomicSrcJob", JOB_GROUP);
        JobKey dstKey = JobKey.jobKey("atomicDstJob", JOB_GROUP);

        //scheduling of the second trigger fails in the middle of the rename with ObjectAlreadyExistsException
        jobModel.setJobName("atomicDstJob");
        jobModel.setTriggers(List.of(
                buildCronTriggerModel("atomicTrigger"),
                buildCronTriggerModel("collisionTrigger")));
        Assertions.assertThrows(QuartzJobSaveException.class,
                () -> quartzService.saveJob(new JobSaveContext(jobModel).setOriginalJobKey(srcKey)));

        //the original job with its trigger is intact, no job appears under the new key
        Assertions.assertTrue(scheduler.checkExists(srcKey));
        List<? extends Trigger> srcTriggers = scheduler.getTriggersOfJob(srcKey);
        Assertions.assertEquals(1, srcTriggers.size());
        Assertions.assertEquals("atomicTrigger", srcTriggers.get(0).getKey().getName());
        Assertions.assertFalse(scheduler.checkExists(dstKey));

        //the unrelated job is untouched
        Assertions.assertEquals(1, scheduler.getTriggersOfJob(collisionJobKey).size());
    }

    private JobModel buildJobModel(String jobName) {
        JobModel jobModel = dataManager.create(JobModel.class);
        jobModel.setJobName(jobName);
        jobModel.setJobGroup(JOB_GROUP);
        jobModel.setJobClass(QuartTestApplication.MyQuartzJob.class.getName());
        return jobModel;
    }

    /**
     * Start date is in the future so that the trigger does not fire during the test
     * (see the note about job store locking in the A4 plan item).
     */
    private TriggerModel buildCronTriggerModel(String triggerName) {
        TriggerModel triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName(triggerName);
        triggerModel.setTriggerGroup(TRIGGER_GROUP);
        triggerModel.setScheduleType(ScheduleType.CRON_EXPRESSION);
        triggerModel.setCronExpression("0 0 0 * * ?");
        triggerModel.setStartDate(Date.from(LocalDateTime.now().plus(1, ChronoUnit.HOURS)
                .atZone(ZoneId.systemDefault()).toInstant()));
        return triggerModel;
    }
}
