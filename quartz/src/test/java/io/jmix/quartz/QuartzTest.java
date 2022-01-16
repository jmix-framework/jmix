package io.jmix.quartz;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.quartz.model.*;
import io.jmix.quartz.service.QuartzService;
import io.jmix.quartz.util.QuartzJobClassFinder;
import io.jmix.quartz.util.QuartzJobDetailsFinder;
import io.jmix.ui.component.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    @Test
    public void testFindQuartzJobClasses() {
        List<String> classNames = quartzJobClassFinder.getQuartzJobClassNames();
        Assertions.assertEquals(1, classNames.size());
        Assertions.assertEquals("io.jmix.quartz.QuartTestApplication$MyQuartzJob", classNames.get(0));
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

        Assertions.assertThrows(ValidationException.class, () -> quartzService.updateQuartzJob(jobModel, jobDataParameterModels, triggerModels, false));

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

}
