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
import io.jmix.quartz.model.*;
import io.jmix.quartz.service.QuartzService;
import io.jmix.quartz.util.QuartzJobClassFinder;
import io.jmix.quartz.util.QuartzJobDetailsFinder;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.*;
import org.quartz.listeners.SchedulerListenerSupport;
import org.quartz.spi.OperableTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                CoreConfiguration.class,
                DataConfiguration.class,
                EclipselinkConfiguration.class,
                QuartTestApplication.class
        }
)
public class QuartzTest {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private QuartzService quartzService;

    @Autowired
    private QuartzJobClassFinder quartzJobClassFinder;

    @Autowired
    private QuartzJobDetailsFinder quartzJobDetailsFinder;

    @Autowired
    private UnconstrainedDataManager dataManager;

    private final TestSchedulerListener schedulerListener = new TestSchedulerListener();

    @BeforeEach
    void registerSchedulerListener() throws SchedulerException {
        scheduler.getListenerManager().addSchedulerListener(schedulerListener);
    }

    @AfterEach
    void cleanUp() throws SchedulerException {
        scheduler.getListenerManager().removeSchedulerListener(schedulerListener);
        for (String jobName : List.of("syncUnchangedJob", "syncChangedSimpleJob", "syncChangedCronJob")) {
            scheduler.deleteJob(JobKey.jobKey(jobName, "testJobGroup"));
        }
    }

    @Test
    public void testFindQuartzJobClasses() {
        List<String> classNames = quartzJobClassFinder.getQuartzJobClassNames();
        Assertions.assertEquals(2, classNames.size());
        Assertions.assertTrue(classNames.contains("io.jmix.quartz.QuartTestApplication$MyQuartzJob"));
        Assertions.assertTrue(classNames.contains("io.jmix.quartz.QuartTestApplication$MyAnotherQuartzJob"));
    }

    @Test
    public void testFindQuartzJobDetailKeys() {
        List<JobKey> jobKeys = quartzJobDetailsFinder.getJobDetailBeanKeys();
        Assertions.assertEquals(1, jobKeys.size());
        Assertions.assertEquals("testJob", jobKeys.get(0).getName());
        Assertions.assertEquals("DEFAULT", jobKeys.get(0).getGroup());
    }

    @Test
    public void testGetAllJobs() throws Exception {
        List<JobModel> allJobs = quartzService.getAllJobs();
        Assertions.assertEquals(1, allJobs.size());
        Assertions.assertEquals("testJob", allJobs.get(0).getJobName());

        JobDetail testJob = JobBuilder.newJob()
                .withIdentity("testJobName", "testJobGroup")
                .ofType(QuartTestApplication.MyQuartzJob.class)
                .usingJobData("simpleJobParamKey", "simpleJobParamValue")
                .storeDurably()
                .build();
        scheduler.addJob(testJob, true);

        List<String> jobGroupNames = quartzService.getJobGroupNames();
        Assertions.assertEquals(2, jobGroupNames.size());
        Assertions.assertTrue(jobGroupNames.stream().anyMatch(jobGroupName -> jobGroupName.contains("testJobGroup")));
        Assertions.assertTrue(jobGroupNames.stream().anyMatch(jobGroupName -> jobGroupName.contains("DEFAULT")));

        allJobs = quartzService.getAllJobs();
        Assertions.assertEquals(2, allJobs.size());
        JobModel jobModel = allJobs.stream().filter(jm -> "testJobName".equals(jm.getJobName())).findFirst().orElse(null);
        Assertions.assertNotNull(jobModel);
        Assertions.assertEquals("testJobName", jobModel.getJobName());
        Assertions.assertEquals("testJobGroup", jobModel.getJobGroup());
        Assertions.assertEquals("io.jmix.quartz.QuartTestApplication$MyQuartzJob", jobModel.getJobClass());
        Assertions.assertEquals(0, jobModel.getTriggers().size());
        Assertions.assertEquals(1, jobModel.getJobDataParameters().size());
        Assertions.assertEquals("simpleJobParamKey", jobModel.getJobDataParameters().get(0).getKey());
        Assertions.assertEquals("simpleJobParamValue", jobModel.getJobDataParameters().get(0).getValue());

        SimpleTrigger testTrigger = TriggerBuilder.newTrigger()
                .withIdentity("testTriggerName", "testTriggerGroup")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(10)
                        .withIntervalInSeconds(10))
                .startNow()
                .forJob(testJob)
                .build();
        scheduler.scheduleJob(testTrigger);

        allJobs = quartzService.getAllJobs();
        Assertions.assertEquals(2, allJobs.size());
        jobModel = allJobs.stream().filter(jm -> "testJobName".equals(jm.getJobName())).findFirst().orElse(null);
        Assertions.assertNotNull(jobModel);
        Assertions.assertEquals(1, jobModel.getTriggers().size());
        TriggerModel triggerModel = jobModel.getTriggers().get(0);
        Assertions.assertEquals("testTriggerName", triggerModel.getTriggerName());
        Assertions.assertEquals("testTriggerGroup", triggerModel.getTriggerGroup());
        Assertions.assertEquals(ScheduleType.SIMPLE, triggerModel.getScheduleType());

        List<String> triggerGroupNames = quartzService.getTriggerGroupNames();
        Assertions.assertEquals(1, triggerGroupNames.size());
        Assertions.assertEquals("testTriggerGroup", triggerGroupNames.get(0));

        //cleanup
        scheduler.deleteJob(JobKey.jobKey(jobModel.getJobName(), jobModel.getJobGroup()));
    }

    @Test
    public void testJobsLifeCycle() throws Exception {
        JobModel jobModel = dataManager.create(JobModel.class);
        jobModel.setJobName("testJobName");
        jobModel.setJobGroup("testJobGroup");
        jobModel.setJobClass(QuartTestApplication.MyQuartzJob.class.getName());

        List<JobDataParameterModel> jobDataParameterModels = new ArrayList<>();
        JobDataParameterModel jobDataParameterModel = dataManager.create(JobDataParameterModel.class);
        jobDataParameterModel.setKey("simple");
        jobDataParameterModel.setValue("dimple");
        jobDataParameterModels.add(jobDataParameterModel);

        List<TriggerModel> triggerModels = new ArrayList<>();
        TriggerModel triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName("testCronTriggerName");
        triggerModel.setTriggerGroup("testTriggerGroup");
        triggerModel.setScheduleType(ScheduleType.CRON_EXPRESSION);
        triggerModel.setCronExpression("0 0 0 * * ?");
        triggerModel.setStartDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        triggerModels.add(triggerModel);

        triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName("testSimpleTriggerName");
        triggerModel.setTriggerGroup("testTriggerGroup");
        triggerModel.setScheduleType(ScheduleType.SIMPLE);
        triggerModel.setRepeatCount(100);
        triggerModel.setRepeatInterval(10000L);
        triggerModel.setStartDate(Date.from(LocalDateTime.now().plus(1, ChronoUnit.HOURS).atZone(ZoneId.systemDefault()).toInstant()));
        triggerModels.add(triggerModel);

        quartzService.updateQuartzJob(jobModel, jobDataParameterModels, triggerModels, false);

        triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName("testSimpleTriggerName");
        triggerModel.setTriggerGroup("testTriggerGroup");
        triggerModel.setScheduleType(ScheduleType.SIMPLE);
        triggerModel.setRepeatCount(100);
        triggerModel.setRepeatInterval(10000L);
        triggerModel.setStartDate(Date.from(LocalDateTime.now().plus(1, ChronoUnit.HOURS).atZone(ZoneId.systemDefault()).toInstant()));
        triggerModels.add(triggerModel);

        Assertions.assertThrows(QuartzJobSaveException.class, () -> quartzService.updateQuartzJob(jobModel, jobDataParameterModels, triggerModels, false));

        List<JobModel> allJobs = quartzService.getAllJobs();
        Assertions.assertEquals(2, allJobs.size());
        JobModel testJobModel = allJobs.stream()
                .filter(jm -> "testJobName".equals(jm.getJobName()))
                .findFirst().orElse(null);
        Assertions.assertNotNull(testJobModel);
        Assertions.assertEquals(JobState.NORMAL, testJobModel.getJobState());
        Assertions.assertEquals(1, testJobModel.getJobDataParameters().size());
        Assertions.assertEquals(2, testJobModel.getTriggers().size());
        Assertions.assertTrue(testJobModel.getTriggers().stream()
                .anyMatch(tm -> ScheduleType.SIMPLE.equals(tm.getScheduleType())));
        Assertions.assertTrue(testJobModel.getTriggers().stream()
                .anyMatch(tm -> ScheduleType.CRON_EXPRESSION.equals(tm.getScheduleType())));

        quartzService.pauseJob(jobModel.getJobName(), jobModel.getJobGroup());
        testJobModel = quartzService.getAllJobs().stream()
                .filter(jm -> "testJobName".equals(jm.getJobName()))
                .findFirst().orElse(null);
        Assertions.assertNotNull(testJobModel);
        Assertions.assertEquals(JobState.PAUSED, testJobModel.getJobState());

        quartzService.resumeJob(jobModel.getJobName(), jobModel.getJobGroup());
        testJobModel = quartzService.getAllJobs().stream()
                .filter(jm -> "testJobName".equals(jm.getJobName()))
                .findFirst().orElse(null);
        Assertions.assertNotNull(testJobModel);
        Assertions.assertEquals(JobState.NORMAL, testJobModel.getJobState());

        quartzService.pauseJob(jobModel.getJobName(), jobModel.getJobGroup());
        quartzService.deleteJob(jobModel.getJobName(), jobModel.getJobGroup());
        testJobModel = quartzService.getAllJobs().stream()
                .filter(jm -> "testJobName".equals(jm.getJobName()))
                .findFirst().orElse(null);
        Assertions.assertNull(testJobModel);
    }

    @Test
    public void testRemovingAllTriggers() throws Exception {
        JobModel jobModel = dataManager.create(JobModel.class);
        jobModel.setJobName("removeTriggersJobName");
        jobModel.setJobGroup("testJobGroup");
        jobModel.setJobClass(QuartTestApplication.MyQuartzJob.class.getName());

        //start dates are in the future so that triggers do not fire during the test
        List<TriggerModel> triggerModels = new ArrayList<>();
        TriggerModel cronTriggerModel = dataManager.create(TriggerModel.class);
        cronTriggerModel.setTriggerName("removeTriggersCronTriggerName");
        cronTriggerModel.setTriggerGroup("testTriggerGroup");
        cronTriggerModel.setScheduleType(ScheduleType.CRON_EXPRESSION);
        cronTriggerModel.setCronExpression("0 0 0 * * ?");
        cronTriggerModel.setStartDate(Date.from(LocalDateTime.now().plus(1, ChronoUnit.HOURS).atZone(ZoneId.systemDefault()).toInstant()));
        triggerModels.add(cronTriggerModel);

        TriggerModel simpleTriggerModel = dataManager.create(TriggerModel.class);
        simpleTriggerModel.setTriggerName("removeTriggersSimpleTriggerName");
        simpleTriggerModel.setTriggerGroup("testTriggerGroup");
        simpleTriggerModel.setScheduleType(ScheduleType.SIMPLE);
        simpleTriggerModel.setRepeatCount(100);
        simpleTriggerModel.setRepeatInterval(10000L);
        simpleTriggerModel.setStartDate(Date.from(LocalDateTime.now().plus(1, ChronoUnit.HOURS).atZone(ZoneId.systemDefault()).toInstant()));
        triggerModels.add(simpleTriggerModel);

        quartzService.updateQuartzJob(jobModel, new ArrayList<>(), triggerModels, false);

        JobKey jobKey = JobKey.jobKey(jobModel.getJobName(), jobModel.getJobGroup());
        Assertions.assertEquals(2, scheduler.getTriggersOfJob(jobKey).size());

        quartzService.updateQuartzJob(jobModel, new ArrayList<>(), new ArrayList<>(), true);

        Assertions.assertTrue(scheduler.getTriggersOfJob(jobKey).isEmpty());

        //cleanup
        scheduler.deleteJob(jobKey);
    }

    @Test
    public void testUnchangedTriggersAreUntouchedOnJobSave() throws Exception {
        //explicit values and null values that are replaced with defaults on build must both compare as unchanged
        TriggerModel simpleTriggerModel = createSimpleTriggerModel("syncUnchangedSimple", futureDate(), 10000L, 100);
        TriggerModel defaultsTriggerModel = createSimpleTriggerModel("syncUnchangedSimpleDefaults", futureDate(),
                null, null);
        TriggerModel cronTriggerModel = createCronTriggerModel("syncUnchangedCron", futureDate(), "0 0 0 * * ?");
        //without a start date the trigger starts "now", which is a start time in the past on the next save
        TriggerModel pastStartTriggerModel = createCronTriggerModel("syncUnchangedCronPastStart", null,
                "0 0 0 1 1 ? 2099");
        List<TriggerModel> triggerModels = List.of(simpleTriggerModel, defaultsTriggerModel, cronTriggerModel,
                pastStartTriggerModel);
        JobModel jobModel = createPausedJobWithFiredTriggers("syncUnchangedJob", triggerModels);
        Map<TriggerKey, Trigger> triggersBefore = getTriggers(triggerModels);
        schedulerListener.clear();

        jobModel.setDescription("updated description");
        quartzService.updateQuartzJob(jobModel, new ArrayList<>(), triggerModels, true);

        Assertions.assertEquals(List.of(), schedulerListener.unscheduledKeys);
        Assertions.assertEquals(List.of(), schedulerListener.scheduledKeys);
        for (Trigger triggerBefore : triggersBefore.values()) {
            assertTriggerUntouched(triggerBefore);
        }
    }

    @Test
    public void testChangedSimpleTriggerIsRescheduledOnJobSave() throws Exception {
        TriggerModel simpleTriggerModel = createSimpleTriggerModel("syncChangedSimple", futureDate(), 10000L, 100);
        TriggerModel cronTriggerModel = createCronTriggerModel("syncChangedSimpleCron", futureDate(), "0 0 0 * * ?");
        List<TriggerModel> triggerModels = List.of(simpleTriggerModel, cronTriggerModel);
        JobModel jobModel = createPausedJobWithFiredTriggers("syncChangedSimpleJob", triggerModels);
        TriggerKey simpleTriggerKey = triggerKey(simpleTriggerModel);
        Trigger simpleTriggerBefore = scheduler.getTrigger(simpleTriggerKey);
        Trigger cronTriggerBefore = scheduler.getTrigger(triggerKey(cronTriggerModel));
        schedulerListener.clear();

        simpleTriggerModel.setRepeatInterval(20000L);
        quartzService.updateQuartzJob(jobModel, new ArrayList<>(), triggerModels, true);

        //only the changed trigger is replaced
        Assertions.assertEquals(List.of(simpleTriggerKey), schedulerListener.unscheduledKeys);
        Assertions.assertEquals(List.of(simpleTriggerKey), schedulerListener.scheduledKeys);
        SimpleTrigger rescheduledTrigger = (SimpleTrigger) scheduler.getTrigger(simpleTriggerKey);
        Assertions.assertEquals(20000L, rescheduledTrigger.getRepeatInterval());
        //the replacement keeps the last fire time and the pause state of the replaced trigger
        Assertions.assertEquals(simpleTriggerBefore.getPreviousFireTime(), rescheduledTrigger.getPreviousFireTime());
        Assertions.assertEquals(Trigger.TriggerState.PAUSED, scheduler.getTriggerState(simpleTriggerKey));
        assertTriggerUntouched(cronTriggerBefore);
    }

    @Test
    public void testChangedCronTriggerIsRescheduledOnJobSave() throws Exception {
        TriggerModel simpleTriggerModel = createSimpleTriggerModel("syncChangedCronSimple", futureDate(), 10000L, 100);
        TriggerModel cronTriggerModel = createCronTriggerModel("syncChangedCron", futureDate(), "0 0 0 * * ?");
        List<TriggerModel> triggerModels = List.of(simpleTriggerModel, cronTriggerModel);
        JobModel jobModel = createPausedJobWithFiredTriggers("syncChangedCronJob", triggerModels);
        TriggerKey cronTriggerKey = triggerKey(cronTriggerModel);
        Trigger cronTriggerBefore = scheduler.getTrigger(cronTriggerKey);
        Trigger simpleTriggerBefore = scheduler.getTrigger(triggerKey(simpleTriggerModel));
        schedulerListener.clear();

        cronTriggerModel.setCronExpression("0 30 0 * * ?");
        quartzService.updateQuartzJob(jobModel, new ArrayList<>(), triggerModels, true);

        //only the changed trigger is replaced
        Assertions.assertEquals(List.of(cronTriggerKey), schedulerListener.unscheduledKeys);
        Assertions.assertEquals(List.of(cronTriggerKey), schedulerListener.scheduledKeys);
        CronTrigger rescheduledTrigger = (CronTrigger) scheduler.getTrigger(cronTriggerKey);
        Assertions.assertEquals("0 30 0 * * ?", rescheduledTrigger.getCronExpression());
        //the replacement keeps the last fire time and the pause state of the replaced trigger
        Assertions.assertEquals(cronTriggerBefore.getPreviousFireTime(), rescheduledTrigger.getPreviousFireTime());
        Assertions.assertEquals(Trigger.TriggerState.PAUSED, scheduler.getTriggerState(cronTriggerKey));
        assertTriggerUntouched(simpleTriggerBefore);
    }

    @Test
    public void testJobStateOfJobWithoutTriggers() throws Exception {
        JobDetail testJob = JobBuilder.newJob()
                .withIdentity("noTriggersJobName", "testJobGroup")
                .ofType(QuartTestApplication.MyQuartzJob.class)
                .storeDurably()
                .build();
        scheduler.addJob(testJob, true);

        JobModel jobModel = quartzService.getAllJobs().stream()
                .filter(jm -> "noTriggersJobName".equals(jm.getJobName()))
                .findFirst().orElse(null);
        Assertions.assertNotNull(jobModel);
        Assertions.assertEquals(JobState.PAUSED, jobModel.getJobState());

        //cleanup
        scheduler.deleteJob(JobKey.jobKey("noTriggersJobName", "testJobGroup"));
    }

    @Test
    public void testChangingJobClass() throws Exception {
        JobModel jobModel = dataManager.create(JobModel.class);
        jobModel.setJobName("changeClassJobName");
        jobModel.setJobGroup("testJobGroup");
        jobModel.setJobClass(QuartTestApplication.MyQuartzJob.class.getName());

        quartzService.updateQuartzJob(jobModel, new ArrayList<>(), new ArrayList<>(), false);

        JobKey jobKey = JobKey.jobKey(jobModel.getJobName(), jobModel.getJobGroup());
        Assertions.assertEquals(QuartTestApplication.MyQuartzJob.class, scheduler.getJobDetail(jobKey).getJobClass());

        jobModel.setJobClass(QuartTestApplication.MyAnotherQuartzJob.class.getName());
        quartzService.updateQuartzJob(jobModel, new ArrayList<>(), new ArrayList<>(), true);

        Assertions.assertEquals(QuartTestApplication.MyAnotherQuartzJob.class, scheduler.getJobDetail(jobKey).getJobClass());

        //class that is not allowed as a Job class must be rejected and the job must stay unchanged
        jobModel.setJobClass("java.lang.String");
        Assertions.assertThrows(QuartzJobSaveException.class,
                () -> quartzService.updateQuartzJob(jobModel, new ArrayList<>(), new ArrayList<>(), true));
        Assertions.assertEquals(QuartTestApplication.MyAnotherQuartzJob.class, scheduler.getJobDetail(jobKey).getJobClass());

        //cleanup
        scheduler.deleteJob(jobKey);
    }


    private Date futureDate() {
        //start dates are in the future so that triggers do not fire during the test
        return Date.from(LocalDateTime.now().plus(1, ChronoUnit.HOURS).atZone(ZoneId.systemDefault()).toInstant());
    }

    private TriggerModel createSimpleTriggerModel(String triggerName, @Nullable Date startDate,
                                                  @Nullable Long repeatInterval, @Nullable Integer repeatCount) {
        TriggerModel triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName(triggerName);
        triggerModel.setTriggerGroup("testTriggerGroup");
        triggerModel.setScheduleType(ScheduleType.SIMPLE);
        triggerModel.setStartDate(startDate);
        triggerModel.setRepeatInterval(repeatInterval);
        triggerModel.setRepeatCount(repeatCount);
        return triggerModel;
    }

    private TriggerModel createCronTriggerModel(String triggerName, @Nullable Date startDate, String cronExpression) {
        TriggerModel triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName(triggerName);
        triggerModel.setTriggerGroup("testTriggerGroup");
        triggerModel.setScheduleType(ScheduleType.CRON_EXPRESSION);
        triggerModel.setStartDate(startDate);
        triggerModel.setCronExpression(cronExpression);
        return triggerModel;
    }

    private TriggerKey triggerKey(TriggerModel triggerModel) {
        return TriggerKey.triggerKey(triggerModel.getTriggerName(), triggerModel.getTriggerGroup());
    }

    /**
     * Creates the job with the given triggers through the service, marks every trigger as already fired
     * (a distinct last fire time per trigger, without waiting for a real execution) and pauses the job.
     */
    private JobModel createPausedJobWithFiredTriggers(String jobName, List<TriggerModel> triggerModels)
            throws SchedulerException {
        JobModel jobModel = dataManager.create(JobModel.class);
        jobModel.setJobName(jobName);
        jobModel.setJobGroup("testJobGroup");
        jobModel.setJobClass(QuartTestApplication.MyQuartzJob.class.getName());
        quartzService.updateQuartzJob(jobModel, new ArrayList<>(), triggerModels, false);

        long lastFireTime = System.currentTimeMillis() - 3_600_000L;
        for (TriggerModel triggerModel : triggerModels) {
            setPreviousFireTime(triggerKey(triggerModel), new Date(lastFireTime));
            lastFireTime += 60_000L;
        }
        quartzService.pauseJob(jobModel.getJobName(), jobModel.getJobGroup());
        return jobModel;
    }

    private void setPreviousFireTime(TriggerKey triggerKey, Date previousFireTime) throws SchedulerException {
        OperableTrigger trigger = (OperableTrigger) scheduler.getTrigger(triggerKey);
        trigger.setPreviousFireTime(previousFireTime);
        scheduler.rescheduleJob(triggerKey, trigger);
        Assertions.assertEquals(previousFireTime, scheduler.getTrigger(triggerKey).getPreviousFireTime());
    }

    private Map<TriggerKey, Trigger> getTriggers(List<TriggerModel> triggerModels) throws SchedulerException {
        Map<TriggerKey, Trigger> triggers = new LinkedHashMap<>();
        for (TriggerModel triggerModel : triggerModels) {
            TriggerKey triggerKey = triggerKey(triggerModel);
            triggers.put(triggerKey, scheduler.getTrigger(triggerKey));
        }
        return triggers;
    }

    private void assertTriggerUntouched(Trigger triggerBefore) throws SchedulerException {
        TriggerKey triggerKey = triggerBefore.getKey();
        Trigger triggerAfter = scheduler.getTrigger(triggerKey);
        Assertions.assertNotNull(triggerBefore.getPreviousFireTime(), triggerKey.toString());
        Assertions.assertEquals(triggerBefore.getPreviousFireTime(), triggerAfter.getPreviousFireTime(),
                triggerKey.toString());
        Assertions.assertEquals(triggerBefore.getNextFireTime(), triggerAfter.getNextFireTime(), triggerKey.toString());
        Assertions.assertEquals(Trigger.TriggerState.PAUSED, scheduler.getTriggerState(triggerKey),
                triggerKey.toString());
    }

    /**
     * Records which triggers the scheduler was asked to schedule and unschedule; a reschedule reports both.
     */
    static class TestSchedulerListener extends SchedulerListenerSupport {

        final List<TriggerKey> scheduledKeys = new ArrayList<>();
        final List<TriggerKey> unscheduledKeys = new ArrayList<>();

        @Override
        public void jobScheduled(Trigger trigger) {
            scheduledKeys.add(trigger.getKey());
        }

        @Override
        public void jobUnscheduled(TriggerKey triggerKey) {
            unscheduledKeys.add(triggerKey);
        }

        void clear() {
            scheduledKeys.clear();
            unscheduledKeys.clear();
        }
    }
}
