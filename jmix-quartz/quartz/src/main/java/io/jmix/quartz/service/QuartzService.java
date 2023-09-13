package io.jmix.quartz.service;

import com.google.common.base.Strings;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.quartz.model.*;
import io.jmix.quartz.util.QuartzJobDetailsFinder;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * Serves as proxy from Jmix to the Quartz engine for fetch information about jobs and triggers and update them
 */
@Service("quartz_QuartzService")
public class QuartzService {

    private static final Logger log = LoggerFactory.getLogger(QuartzService.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private QuartzJobDetailsFinder jobDetailsFinder;

    @Autowired
    private UnconstrainedDataManager dataManager;

    /**
     * Returns information about all configured quartz jobs with related triggers
     */
    public List<JobModel> getAllJobs() {
        List<JobModel> result = new ArrayList<>();
        try {
            List<JobKey> jobDetailsKeys = jobDetailsFinder.getJobDetailBeanKeys();

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                try {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                    JobModel jobModel = dataManager.create(JobModel.class);
                    jobModel.setJobName(jobKey.getName());
                    jobModel.setJobGroup(jobKey.getGroup());
                    jobModel.setJobDataParameters(getDataParamsOfJob(jobKey));

                    jobModel.setJobClass(jobDetail.getJobClass().getName());
                    jobModel.setDescription(jobDetail.getDescription());

                    jobModel.setJobSource(jobDetailsKeys.contains(jobKey) ? JobSource.PREDEFINED : JobSource.USER_DEFINED);

                    List<TriggerModel> triggerModels = new ArrayList<>();
                    List<? extends Trigger> jobTriggers = scheduler.getTriggersOfJob(jobKey);
                    if (!CollectionUtils.isEmpty(jobTriggers)) {
                        boolean isActive = false;
                        for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                            TriggerModel triggerModel = dataManager.create(TriggerModel.class);
                            triggerModel.setTriggerName(trigger.getKey().getName());
                            triggerModel.setTriggerGroup(trigger.getKey().getGroup());
                            triggerModel.setScheduleType(trigger instanceof SimpleTrigger ? ScheduleType.SIMPLE : ScheduleType.CRON_EXPRESSION);
                            triggerModel.setStartDate(trigger.getStartTime());
                            triggerModel.setEndDate(trigger.getEndTime());
                            triggerModel.setLastFireDate(trigger.getPreviousFireTime());
                            triggerModel.setNextFireDate(trigger.getNextFireTime());
                            triggerModel.setMisfireInstructionId(resolveMisfireInstructionId(trigger));

                            if (trigger instanceof CronTrigger) {
                                triggerModel.setCronExpression(((CronTrigger) trigger).getCronExpression());
                            } else if (trigger instanceof SimpleTrigger simpleTrigger) {
                                triggerModel.setRepeatCount(simpleTrigger.getRepeatCount());
                                triggerModel.setRepeatInterval(simpleTrigger.getRepeatInterval());
                            }

                            triggerModels.add(triggerModel);
                            if (scheduler.getTriggerState(trigger.getKey()) == Trigger.TriggerState.NORMAL
                                    && scheduler.isStarted()
                                    && !scheduler.isInStandbyMode()) {
                                isActive = true;
                            }
                        }

                        jobModel.setTriggers(triggerModels);
                        jobModel.setJobState(isActive ? JobState.NORMAL : JobState.PAUSED);
                    }

                    result.add(jobModel);
                } catch (SchedulerException e) {
                    log.error("Unable to fetch information about the job: {}", jobKey, e);
                }
            }
        } catch (SchedulerException e) {
            log.error("Unable to fetch information about active jobs", e);
        }

        return result;
    }

    private String resolveMisfireInstructionId(Trigger trigger) {
        ScheduleType scheduleType = trigger instanceof SimpleTrigger
                ? ScheduleType.SIMPLE
                : ScheduleType.CRON_EXPRESSION;
        int miCode = trigger.getMisfireInstruction();
        String misfireInstructionId;
        if (ScheduleType.SIMPLE.equals(scheduleType)) {
            misfireInstructionId = Optional.ofNullable(SimpleTriggerMisfireInstruction.fromCode(miCode))
                    .orElse(SimpleTriggerMisfireInstruction.SMART_POLICY)
                    .getId();
        } else {
            misfireInstructionId = Optional.ofNullable(CronTriggerMisfireInstruction.fromCode(miCode))
                    .orElse(CronTriggerMisfireInstruction.SMART_POLICY)
                    .getId();
        }
        return misfireInstructionId;
    }

    /**
     * Returns given job's parameters
     *
     * @param jobKey key of job
     * @return parameters of given job
     */
    private List<JobDataParameterModel> getDataParamsOfJob(JobKey jobKey) {
        List<JobDataParameterModel> result = new ArrayList<>();

        try {
            scheduler.getJobDetail(jobKey)
                    .getJobDataMap()
                    .getWrappedMap()
                    .forEach((k, v) -> {
                        JobDataParameterModel dataParameterModel = dataManager.create(JobDataParameterModel.class);
                        dataParameterModel.setKey(k);
                        dataParameterModel.setValue(v == null ? "" : v.toString());
                        result.add(dataParameterModel);
                    });
        } catch (SchedulerException e) {
            log.warn("Unable to fetch information about parameters for job {}", jobKey, e);
        }

        return result;
    }

    /**
     * Returns names of all known JobDetail groups
     */
    public List<String> getJobGroupNames() {
        List<String> result = new ArrayList<>();

        try {
            result = scheduler.getJobGroupNames();
        } catch (SchedulerException e) {
            log.warn("Unable to fetch information about job group names", e);
        }

        return result;
    }

    /**
     * Returns names of all known Trigger groups
     */
    public List<String> getTriggerGroupNames() {
        List<String> result = new ArrayList<>();

        try {
            result = scheduler.getTriggerGroupNames();
        } catch (SchedulerException e) {
            log.warn("Unable to fetch information about trigger group names", e);
        }

        return result;
    }

    /**
     * Updates job in the Quartz engine
     *
     * @param jobModel               job to edit
     * @param jobDataParameterModels parameters for job
     * @param triggerModels          triggers for job
     * @param replaceJobIfExists     replace if job with the same name already exists
     */
    @SuppressWarnings("unchecked")
    public void updateQuartzJob(JobModel jobModel,
                                List<JobDataParameterModel> jobDataParameterModels,
                                List<TriggerModel> triggerModels,
                                boolean replaceJobIfExists) {
        log.debug("updating job with name {} and group {}", jobModel.getJobName(), jobModel.getJobGroup());
        try {
            JobKey jobKey = JobKey.jobKey(jobModel.getJobName(), jobModel.getJobGroup());
            JobDetail jobDetail = buildJobDetail(jobModel, scheduler.getJobDetail(jobKey), jobDataParameterModels);
            scheduler.addJob(jobDetail, replaceJobIfExists);

            if (!CollectionUtils.isEmpty(triggerModels)) {
                //remove obsolete triggers
                for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                    scheduler.unscheduleJob(trigger.getKey());
                }
                //recreate triggers
                for (TriggerModel triggerModel : triggerModels) {
                    Trigger trigger = buildTrigger(jobDetail, triggerModel);
                    scheduler.scheduleJob(trigger);
                }
            }
        } catch (SchedulerException e) {
            log.warn("Unable to update job with name {} and group {}", jobModel.getJobName(), jobModel.getJobGroup(), e);
            throw new IllegalStateException(e.getMessage());
        } catch (ClassNotFoundException e) {
            log.warn("Unable to find job class {}", jobModel.getJobClass());
            throw new IllegalStateException("Job class " + jobModel.getJobClass() + " not found");
        }
    }

    @SuppressWarnings("unchecked")
    private JobDetail buildJobDetail(JobModel jobModel, @Nullable JobDetail existedJobDetail, List<JobDataParameterModel> jobDataParameterModels)
            throws ClassNotFoundException {
        JobBuilder jobBuilder;
        if (existedJobDetail != null) {
            jobBuilder = existedJobDetail.getJobBuilder();
        } else {
            Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(jobModel.getJobClass());
            jobBuilder = JobBuilder.newJob()
                    .withIdentity(jobModel.getJobName(), jobModel.getJobGroup())
                    .ofType(jobClass)
                    .storeDurably();
        }

        jobBuilder.withDescription(jobModel.getDescription());

        jobBuilder.setJobData(new JobDataMap());
        if (CollectionUtils.isNotEmpty(jobDataParameterModels)) {
            jobDataParameterModels.forEach(jobDataParameterModel ->
                    jobBuilder.usingJobData(jobDataParameterModel.getKey(), jobDataParameterModel.getValue()));
        }

        return jobBuilder.build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, TriggerModel triggerModel) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .forJob(jobDetail);

        if (!Strings.isNullOrEmpty(triggerModel.getTriggerName())) {
            triggerBuilder.withIdentity(TriggerKey.triggerKey(triggerModel.getTriggerName(), triggerModel.getTriggerGroup()));
        }

        if (triggerModel.getScheduleType() == ScheduleType.CRON_EXPRESSION) {
            String cronExpression = triggerModel.getCronExpression();
            if (cronExpression == null) {
                throw new IllegalStateException("Cron trigger has null cron expression");
            }
            CronScheduleBuilder cronScheduleBuilder = cronSchedule(cronExpression);
            String misfireInstructionId = triggerModel.getMisfireInstructionId();
            if (misfireInstructionId != null) {
                CronTriggerMisfireInstruction misfireInstruction = CronTriggerMisfireInstruction.fromId(misfireInstructionId);
                if (misfireInstruction == null) {
                    log.warn("No misfire instruction has been found for id '{}'. Default one will be used", misfireInstructionId);
                } else {
                    misfireInstruction.applyInstruction(cronScheduleBuilder);
                }
            }
            triggerBuilder.withSchedule(cronScheduleBuilder);
        } else {
            SimpleScheduleBuilder simpleScheduleBuilder = simpleSchedule()
                    .withIntervalInMilliseconds(triggerModel.getRepeatInterval());
            String misfireInstructionId = triggerModel.getMisfireInstructionId();
            if (misfireInstructionId != null) {
                SimpleTriggerMisfireInstruction misfireInstruction = SimpleTriggerMisfireInstruction.fromId(misfireInstructionId);
                if (misfireInstruction == null) {
                    log.warn("No misfire instruction has been found for id '{}'. Default one will be used", misfireInstructionId);
                } else {
                    misfireInstruction.applyInstruction(simpleScheduleBuilder);
                }
            }

            Integer repeatCount = triggerModel.getRepeatCount();
            if (Objects.isNull(repeatCount)) {
                // Infinite executions
                repeatCount = -1;
            }
            if (repeatCount >= 0) {
                simpleScheduleBuilder.withRepeatCount(repeatCount);
            } else {
                simpleScheduleBuilder.repeatForever();
            }
            triggerBuilder.withSchedule(simpleScheduleBuilder);
        }

        if (triggerModel.getStartDate() != null) {
            triggerBuilder.startAt(triggerModel.getStartDate());
        } else {
            triggerBuilder.startNow();
        }

        if (triggerModel.getEndDate() != null) {
            triggerBuilder.endAt(triggerModel.getEndDate());
        }

        return triggerBuilder.build();
    }

    /**
     * Delegates to the Quartz engine resuming given job. This operation makes sense only for job with paused triggers.
     *
     * @param jobName  name of the job
     * @param jobGroup group of the job
     */
    public void resumeJob(String jobName, String jobGroup) {
        log.debug("resuming job with name {} and group {}", jobName, jobGroup);
        try {
            scheduler.resumeJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            log.warn("Unable to resume job with name {} and group {}", jobName, jobGroup, e);
        }
    }

    /**
     * Delegates to the Quartz engine pausing given job. This operation makes sense only for jobs with active triggers.
     *
     * @param jobName  name of the job
     * @param jobGroup group of the job
     */
    public void pauseJob(String jobName, String jobGroup) {
        log.debug("pausing job with name {} and group {}", jobName, jobGroup);
        try {
            scheduler.pauseJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            log.warn("Unable to pause job with name {} and group {}", jobName, jobGroup, e);
        }
    }

    /**
     * Delegates to the Quartz engine triggering given job (executing now)
     *
     * @param jobName  name of the job
     * @param jobGroup group of the job
     */
    public void executeNow(String jobName, String jobGroup) {
        log.debug("triggering job with name {} and group {}", jobName, jobGroup);
        try {
            scheduler.triggerJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            log.warn("Unable to trigger job with name {} and group {}", jobName, jobGroup, e);
        }
    }

    /**
     * Delegates to the Quartz engine deleting given job
     *
     * @param jobName  name of the job
     * @param jobGroup group of the job
     */
    public void deleteJob(String jobName, String jobGroup) {
        log.debug("deleting job with name {} and group {}", jobName, jobGroup);
        try {
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            log.warn("Unable to delete job with name {} and group {}", jobName, jobGroup, e);
        }

    }

    /**
     * Delegates to the Quartz engine determination if the given job exists
     *
     * @param jobName  name of the job
     * @param jobGroup group of the job
     * @return true if job with provided name and group exists, false otherwise
     */
    public boolean checkJobExists(String jobName, String jobGroup) {
        try {
            return scheduler.checkExists(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            log.warn("Unable to define if job with name {} and group {} exists", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * Delegates to the Quartz engine determination if the given trigger exists
     *
     * @param triggerName  name of the trigger
     * @param triggerGroup group of the trigger
     * @return true if trigger with provided name and group exists, false otherwise
     */
    public boolean checkTriggerExists(String triggerName, String triggerGroup) {
        try {
            return scheduler.checkExists(TriggerKey.triggerKey(triggerName, triggerGroup));
        } catch (SchedulerException e) {
            log.warn("Unable to define if trigger with name {} and group {} exists", triggerName, triggerGroup, e);
            return false;
        }
    }
}
