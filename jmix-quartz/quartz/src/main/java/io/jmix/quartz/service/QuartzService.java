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

package io.jmix.quartz.service;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.quartz.QuartzProperties;
import io.jmix.quartz.exception.QuartzJobSaveException;
import io.jmix.quartz.job.InvalidJobDetail;
import io.jmix.quartz.model.*;
import io.jmix.quartz.util.QuartzJobClassFinder;
import io.jmix.quartz.util.QuartzJobDetailsFinder;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * Serves as proxy from Jmix to the Quartz engine for fetch information about jobs and triggers and update them
 */
@Service("quartz_QuartzService")
public class QuartzService {

    protected static final long DEFAULT_REPEAT_INTERVAL_MILLIS = 60000L;

    private static final Logger log = LoggerFactory.getLogger(QuartzService.class);

    @Autowired
    protected Scheduler scheduler;

    @Autowired
    protected QuartzJobDetailsFinder jobDetailsFinder;

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @Autowired
    protected Messages messages;

    @Autowired
    protected RunningJobsCache runningJobsCache;

    @Autowired
    protected QuartzProperties quartzProperties;

    @Autowired
    protected QuartzJobClassFinder quartzJobClassFinder;

    /**
     * Returns information about all configured quartz jobs with related triggers
     */
    public List<JobModel> getAllJobs() {
        List<JobModel> result = new ArrayList<>();
        try {
            List<JobKey> jobDetailsKeys = jobDetailsFinder.getJobDetailBeanKeys();
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                JobDetail jobDetail = resolveJobDetail(jobKey);
                if (jobDetail == null) {
                    // Unrecoverable error already logged.
                    continue;
                }
                result.add(createJobModel(jobKey, jobDetail, jobDetailsKeys));
            }
        } catch (SchedulerException e) {
            log.error("Unable to fetch information about active jobs", e);
        }

        return result;
    }

    /**
     * Updates job in the Quartz engine
     *
     * @param jobModel               job to edit
     * @param jobDataParameterModels parameters for job
     * @param triggerModels          triggers for job; existing triggers of the job are synchronized with provided
     *                               ones: unchanged triggers are kept with their execution state, changed ones are
     *                               rescheduled, obsolete ones are removed, empty list removes all triggers
     * @param replaceJobIfExists     replace if job with the same name already exists
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackForClassName = {"Exception"})
    public void updateQuartzJob(JobModel jobModel,
                                List<JobDataParameterModel> jobDataParameterModels,
                                List<TriggerModel> triggerModels,
                                boolean replaceJobIfExists) {
        log.debug("updating job with name {} and group {}", jobModel.getJobName(), jobModel.getJobGroup());
        try {
            JobKey jobKey = JobKey.jobKey(jobModel.getJobName(), jobModel.getJobGroup());
            JobDetail jobDetail = buildJobDetail(jobModel, scheduler.getJobDetail(jobKey), jobDataParameterModels);
            scheduler.addJob(jobDetail, replaceJobIfExists);

            updateTriggers(jobDetail, triggerModels);
        } catch (SchedulerException e) {
            log.warn("Unable to update job with name {} and group {}", jobModel.getJobName(), jobModel.getJobGroup(), e);
            throw new QuartzJobSaveException(e.getMessage());
        } catch (ClassNotFoundException e) {
            log.warn("Unable to find job class {}", jobModel.getJobClass());
            throw new QuartzJobSaveException("Job class " + jobModel.getJobClass() + " not found");
        }
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

    /**
     * Fetches the {@link JobDetail} for the given key. Returns an {@link InvalidJobDetail} when the job class is
     * missing, or {@code null} when the detail cannot be fetched.
     */
    @Nullable
    public JobDetail resolveJobDetail(JobKey jobKey) {
        try {
            return scheduler.getJobDetail(jobKey);
        } catch (JobPersistenceException e) {
            if (e.getCause() instanceof ClassNotFoundException) {
                return new InvalidJobDetail(jobKey, e.getCause().getMessage(),
                        messages.formatMessage(QuartzService.class, "jobClassNotFound", e.getCause().getMessage())
                );
            }
            log.error("Unable to fetch information about the job: {}", jobKey, e);
            return null;
        } catch (SchedulerException e) {
            log.error("Unable to fetch information about the job: {}", jobKey, e);
            return null;
        }
    }

    /**
     * Checks if provided job is running
     */
    public boolean isJobRunning(JobKey jobKey) {
        boolean running = false;
        if (quartzProperties.isRunningJobsCacheUsageEnabled()) {
            // Check if job is running using cache. It's mostly relevant for cluster environment.
            running = runningJobsCache.isJobRunning(jobKey);
        }
        if (!running) {
            // Check if job is running within current Scheduler.
            // Additional check in case of cache has been cleared or disabled.
            // But may not find some running jobs in case of cluster environment.
            running = isJobRunningWithinCurrentScheduler(jobKey);
        }
        return running;
    }

    public String getDisplayedClassName(JobDetail jobDetail) {
        if (jobDetail instanceof InvalidJobDetail) {
            return ((InvalidJobDetail) jobDetail).getOriginClassName();
        } else {
            return jobDetail.getJobClass().getName();
        }
    }

    /**
     * Returns given job's parameters
     *
     * @param jobKey key of job
     * @return parameters of given job
     */
    public List<JobDataParameterModel> getDataParamsOfJob(JobKey jobKey) {
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

    protected JobModel createJobModel(JobKey jobKey, JobDetail jobDetail, List<JobKey> jobDetailsKeys)
            throws SchedulerException {
        JobModel jobModel = dataManager.create(JobModel.class);
        jobModel.setJobName(jobKey.getName());
        jobModel.setJobGroup(jobKey.getGroup());
        jobModel.setJobDataParameters(getDataParamsOfJob(jobKey));
        jobModel.setDescription(jobDetail.getDescription());
        jobModel.setJobClass(getDisplayedClassName(jobDetail));
        jobModel.setJobSource(jobDetailsKeys.contains(jobKey) ? JobSource.PREDEFINED : JobSource.USER_DEFINED);

        List<? extends Trigger> jobTriggers = scheduler.getTriggersOfJob(jobKey);
        boolean active = false;
        boolean hasBlockedTrigger = false;
        if (!CollectionUtils.isEmpty(jobTriggers)) {
            Date now = new Date();
            List<TriggerModel> triggerModels = new ArrayList<>();
            for (Trigger trigger : jobTriggers) {
                triggerModels.add(createTriggerModel(trigger, now));
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                if ((triggerState == Trigger.TriggerState.NORMAL || triggerState == Trigger.TriggerState.BLOCKED)
                        && scheduler.isStarted()
                        && !scheduler.isInStandbyMode()) {
                    active = true;
                    if (triggerState == Trigger.TriggerState.BLOCKED) {
                        hasBlockedTrigger = true;
                    }
                }
            }
            jobModel.setTriggers(triggerModels);
        }
        //resolve the state even for a job without triggers, e.g. to detect the invalid state
        jobModel.setJobState(resolveJobState(jobKey, jobDetail, active, hasBlockedTrigger));

        return jobModel;
    }

    protected TriggerModel createTriggerModel(Trigger trigger, Date now) {
        TriggerModel triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName(trigger.getKey().getName());
        triggerModel.setTriggerGroup(trigger.getKey().getGroup());
        triggerModel.setScheduleType(trigger instanceof SimpleTrigger ? ScheduleType.SIMPLE : ScheduleType.CRON_EXPRESSION);
        /*
        Ignore startTime if it's in the past - during saving empty startTime will be set as 'now'.
        This in combination with validation prevents case when scheduler reproduces all executions
        from the startTime to the current moment after trigger is recreated (all triggers
        a created with startTime not earlier than 'now')
        */
        Date startTime = trigger.getStartTime();
        if (startTime.after(now)) {
            triggerModel.setStartDate(startTime);
        }
        triggerModel.setEndDate(trigger.getEndTime());
        triggerModel.setLastFireDate(trigger.getPreviousFireTime());
        triggerModel.setNextFireDate(trigger.getNextFireTime());
        triggerModel.setMisfireInstructionId(resolveMisfireInstructionId(trigger));

        if (trigger instanceof CronTrigger) {
            triggerModel.setCronExpression(((CronTrigger) trigger).getCronExpression());
            triggerModel.setTimeZoneId(((CronTrigger) trigger).getTimeZone().getID());
        } else if (trigger instanceof SimpleTrigger simpleTrigger) {
            triggerModel.setRepeatCount(simpleTrigger.getRepeatCount());
            triggerModel.setRepeatInterval(simpleTrigger.getRepeatInterval());
        }

        return triggerModel;
    }

    protected JobState resolveJobState(JobKey jobKey, JobDetail jobDetail, boolean active, boolean hasBlockedTrigger) {
        if (jobDetail instanceof InvalidJobDetail) {
            return JobState.INVALID;
        }
        // A blocked trigger means a job class with @DisallowConcurrentExecution is currently running;
        // otherwise consult the Scheduler/Cache to determine if the job is running.
        if (hasBlockedTrigger || isJobRunning(jobKey)) {
            return JobState.RUNNING;
        }
        return active ? JobState.NORMAL : JobState.PAUSED;
    }

    protected boolean isJobRunningWithinCurrentScheduler(JobKey jobKey) {
        try {
            List<JobExecutionContext> currentlyExecutingJobs = scheduler.getCurrentlyExecutingJobs();
            return currentlyExecutingJobs.stream()
                    .map(JobExecutionContext::getJobDetail)
                    .map(JobDetail::getKey)
                    .anyMatch(runningJobKey -> runningJobKey.equals(jobKey));
        } catch (SchedulerException e) {
            throw new RuntimeException("Unable to get currently executing jobs", e);
        }
    }

    protected String resolveMisfireInstructionId(Trigger trigger) {
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

    protected JobDetail buildJobDetail(JobModel jobModel, @Nullable JobDetail existedJobDetail, List<JobDataParameterModel> jobDataParameterModels)
            throws ClassNotFoundException {
        JobBuilder jobBuilder;
        if (existedJobDetail != null) {
            jobBuilder = existedJobDetail.getJobBuilder();
            String jobClassName = jobModel.getJobClass();
            if (jobClassName != null && !jobClassName.equals(existedJobDetail.getJobClass().getName())) {
                jobBuilder.ofType(resolveJobClass(jobClassName));
            }
        } else {
            jobBuilder = JobBuilder.newJob()
                    .withIdentity(jobModel.getJobName(), jobModel.getJobGroup())
                    .ofType(resolveJobClass(jobModel.getJobClass()))
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

    /**
     * Validates that the given class name is allowed as a Job class and loads it. The allow-list check
     * is performed before class loading to prevent execution of static initializers of arbitrary classes.
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends Job> resolveJobClass(String jobClassName) throws ClassNotFoundException {
        List<String> existedJobsClassNames = quartzJobClassFinder.getQuartzJobClassNames();
        if (!existedJobsClassNames.contains(jobClassName)) {
            log.error("Attempt to use non-Job class as for a Job");
            throw new QuartzJobSaveException("Class " + jobClassName + " is not allowed as a Job class");
        }
        return (Class<? extends Job>) Class.forName(jobClassName);
    }

    /**
     * Synchronizes triggers of the job with the provided models: unchanged triggers are left untouched, so they
     * keep their execution state and pause state, changed ones are rescheduled, missing ones are scheduled
     * and obsolete ones are unscheduled.
     */
    protected void updateTriggers(JobDetail jobDetail, List<TriggerModel> triggerModels) throws SchedulerException {
        Map<TriggerKey, Trigger> existingTriggers = new HashMap<>();
        for (Trigger trigger : scheduler.getTriggersOfJob(jobDetail.getKey())) {
            existingTriggers.put(trigger.getKey(), trigger);
        }

        Set<TriggerKey> keysToKeep = new HashSet<>();
        if (!CollectionUtils.isEmpty(triggerModels)) {
            for (TriggerModel triggerModel : triggerModels) {
                Trigger newTrigger = buildTrigger(jobDetail, triggerModel);
                Trigger existingTrigger = existingTriggers.get(newTrigger.getKey());
                if (existingTrigger == null) {
                    scheduler.scheduleJob(newTrigger);
                } else {
                    keysToKeep.add(newTrigger.getKey());
                    if (isTriggerChanged(existingTrigger, triggerModel)) {
                        Trigger.TriggerState oldTriggerState = scheduler.getTriggerState(existingTrigger.getKey());
                        scheduler.rescheduleJob(newTrigger.getKey(), newTrigger);
                        if (oldTriggerState == Trigger.TriggerState.PAUSED) {
                            //replacing a trigger resets its individual pause state - restore it
                            scheduler.pauseTrigger(newTrigger.getKey());
                        }
                    }
                }
            }
        }
        //remove obsolete triggers
        for (TriggerKey existingTriggerKey : existingTriggers.keySet()) {
            if (!keysToKeep.contains(existingTriggerKey)) {
                scheduler.unscheduleJob(existingTriggerKey);
            }
        }
    }

    /**
     * Defines whether the trigger registered in the scheduler differs from the submitted model,
     * which means the trigger must be rescheduled.
     */
    protected boolean isTriggerChanged(Trigger trigger, TriggerModel triggerModel) {
        if (isScheduleChanged(trigger, triggerModel)) {
            return true;
        }
        /*
        Null start date in the model matches a start time in the past because past start times
        are cleared when the trigger is loaded into the model (see createTriggerModel).
        */
        Date modelStartDate = triggerModel.getStartDate();
        if (modelStartDate == null) {
            if (trigger.getStartTime() != null && trigger.getStartTime().after(new Date())) {
                return true;
            }
        } else if (!modelStartDate.equals(trigger.getStartTime())) {
            return true;
        }
        if (!Objects.equals(trigger.getEndTime(), triggerModel.getEndDate())) {
            return true;
        }
        return !resolveMisfireInstructionId(trigger).equals(resolveModelMisfireInstructionId(trigger, triggerModel));
    }

    protected boolean isScheduleChanged(Trigger trigger, TriggerModel triggerModel) {
        if (trigger instanceof CronTrigger cronTrigger) {
            if (triggerModel.getScheduleType() != ScheduleType.CRON_EXPRESSION) {
                return true;
            }
            String modelTimeZoneId = triggerModel.getTimeZoneId() != null
                    ? triggerModel.getTimeZoneId()
                    : TimeZone.getDefault().getID();
            return !Objects.equals(cronTrigger.getCronExpression(), triggerModel.getCronExpression())
                    || !cronTrigger.getTimeZone().getID().equals(modelTimeZoneId);
        }
        if (trigger instanceof SimpleTrigger simpleTrigger) {
            if (triggerModel.getScheduleType() != ScheduleType.SIMPLE) {
                return true;
            }
            long modelRepeatInterval = triggerModel.getRepeatInterval() != null
                    ? triggerModel.getRepeatInterval()
                    : DEFAULT_REPEAT_INTERVAL_MILLIS;
            int modelRepeatCount = triggerModel.getRepeatCount() != null && triggerModel.getRepeatCount() >= 0
                    ? triggerModel.getRepeatCount()
                    : SimpleTrigger.REPEAT_INDEFINITELY;
            return simpleTrigger.getRepeatInterval() != modelRepeatInterval
                    || simpleTrigger.getRepeatCount() != modelRepeatCount;
        }
        //unsupported trigger type - reschedule to be safe
        return true;
    }

    protected String resolveModelMisfireInstructionId(Trigger trigger, TriggerModel triggerModel) {
        String misfireInstructionId = triggerModel.getMisfireInstructionId();
        if (misfireInstructionId != null) {
            return misfireInstructionId;
        }
        return trigger instanceof SimpleTrigger
                ? SimpleTriggerMisfireInstruction.SMART_POLICY.getId()
                : CronTriggerMisfireInstruction.SMART_POLICY.getId();
    }

    protected Trigger buildTrigger(JobDetail jobDetail, TriggerModel triggerModel) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .forJob(jobDetail);

        if (!Strings.isNullOrEmpty(triggerModel.getTriggerName())) {
            triggerBuilder.withIdentity(TriggerKey.triggerKey(triggerModel.getTriggerName(), triggerModel.getTriggerGroup()));
        }

        setupSchedule(triggerBuilder, triggerModel);
        setupTriggerActivityDates(triggerBuilder, triggerModel);

        return triggerBuilder.build();
    }

    protected void setupSchedule(TriggerBuilder<Trigger> triggerBuilder, TriggerModel triggerModel) {
        if (triggerModel.getScheduleType() == ScheduleType.CRON_EXPRESSION) {
            triggerBuilder.withSchedule(buildCronSchedule(triggerModel));
        } else {
            triggerBuilder.withSchedule(buildSimpleSchedule(triggerModel));
        }
    }

    protected void setupTriggerActivityDates(TriggerBuilder<Trigger> triggerBuilder, TriggerModel triggerModel) {
        if (triggerModel.getStartDate() != null) {
            triggerBuilder.startAt(triggerModel.getStartDate());
        } else {
            triggerBuilder.startNow();
        }

        if (triggerModel.getEndDate() != null) {
            triggerBuilder.endAt(triggerModel.getEndDate());
        }
    }

    protected CronScheduleBuilder buildCronSchedule(TriggerModel triggerModel) {
        String cronExpression = triggerModel.getCronExpression();
        if (cronExpression == null) {
            throw new IllegalStateException("Cron trigger has null cron expression");
        }
        CronScheduleBuilder cronScheduleBuilder = cronSchedule(cronExpression);
        if (triggerModel.getTimeZoneId() != null) {
            cronScheduleBuilder.inTimeZone(TimeZone.getTimeZone(triggerModel.getTimeZoneId()));
        }

        String misfireInstructionId = triggerModel.getMisfireInstructionId();
        if (misfireInstructionId != null) {
            CronTriggerMisfireInstruction misfireInstruction = CronTriggerMisfireInstruction.fromId(misfireInstructionId);
            if (misfireInstruction == null) {
                logMissingMisfireInstruction(misfireInstructionId);
            } else {
                misfireInstruction.applyInstruction(cronScheduleBuilder);
            }
        }

        return cronScheduleBuilder;
    }

    protected SimpleScheduleBuilder buildSimpleSchedule(TriggerModel triggerModel) {
        Long repeatInterval = triggerModel.getRepeatInterval();
        if (Objects.isNull(repeatInterval)) {
            repeatInterval = DEFAULT_REPEAT_INTERVAL_MILLIS;
        }

        SimpleScheduleBuilder simpleScheduleBuilder = simpleSchedule()
                .withIntervalInMilliseconds(repeatInterval);

        String misfireInstructionId = triggerModel.getMisfireInstructionId();
        if (misfireInstructionId != null) {
            SimpleTriggerMisfireInstruction misfireInstruction = SimpleTriggerMisfireInstruction.fromId(misfireInstructionId);
            if (misfireInstruction == null) {
                logMissingMisfireInstruction(misfireInstructionId);
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

        return simpleScheduleBuilder;
    }

    protected void logMissingMisfireInstruction(String misfireInstructionId) {
        log.warn("No misfire instruction has been found for id '{}'. Default one will be used", misfireInstructionId);
    }
}
