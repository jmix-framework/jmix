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
import org.quartz.spi.OperableTrigger;
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
     * Saves the job in the Quartz engine. Job data parameters and triggers are taken from the {@link JobModel}
     * collections. Existing triggers of the job are synchronized with the provided ones: unchanged triggers are
     * kept with their execution state and pause state, changed ones are rescheduled keeping their last fire time
     * and pause state, obsolete ones are removed, an empty collection removes all triggers of the job. A save never
     * starts a job that is not running: triggers added to an existing job without active triggers are paused.
     * <p>
     * The behavior is defined by the original job key of the context
     * (see {@link JobSaveContext#setOriginalJobKey(JobKey)}) and the target key built from the job model name
     * and group (an empty group means the default group):
     * <ul>
     *     <li>the original key is not set — the job is created under the target key; if a job with this key
     *     already exists, {@link QuartzJobSaveException} is thrown and the existing job is not touched;</li>
     *     <li>the original key equals the target key — the existing job is updated in place (or created,
     *     if it no longer exists in the engine);</li>
     *     <li>the original key differs from the target key — the job is recreated under the target key and
     *     removed under the original one; its triggers are created anew, since a trigger cannot be moved to
     *     another job, so their execution state is not kept, but they are paused if the original job had no
     *     active triggers; if a job with the target key already exists, {@link QuartzJobSaveException} is thrown
     *     and nothing is changed.</li>
     * </ul>
     *
     * @param context the job model to save and the save options
     * @throws QuartzJobSaveException if the job cannot be saved; with a JDBC job store all performed operations
     *                                are rolled back, so a failed rename leaves the original job intact
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveJob(JobSaveContext context) {
        JobModel jobModel = context.getJobModel();
        List<JobDataParameterModel> jobDataParameterModels = jobModel.getJobDataParameters();
        List<TriggerModel> triggerModels = jobModel.getTriggers();
        JobKey originalJobKey = context.getOriginalJobKey();

        JobKey jobKey = JobKey.jobKey(jobModel.getJobName(), Strings.emptyToNull(jobModel.getJobGroup()));
        if (originalJobKey == null || originalJobKey.equals(jobKey)) {
            updateQuartzJob(jobModel, jobDataParameterModels, triggerModels, originalJobKey != null);
            return;
        }

        log.debug("moving job {} to new key {}", originalJobKey, jobKey);
        try {
            JobDetail jobDetail = buildJobDetail(jobModel, scheduler.getJobDetail(originalJobKey), jobDataParameterModels)
                    .getJobBuilder()
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();
            scheduler.addJob(jobDetail, false);

            //unschedule triggers of the original job before scheduling new ones - they usually share trigger keys
            List<? extends Trigger> originalTriggers = scheduler.getTriggersOfJob(originalJobKey);
            boolean paused = !hasActiveTrigger(originalTriggers);
            for (Trigger trigger : originalTriggers) {
                scheduler.unscheduleJob(trigger.getKey());
            }
            if (!CollectionUtils.isEmpty(triggerModels)) {
                for (TriggerModel triggerModel : triggerModels) {
                    //the save must not start a job that was not running
                    scheduleTrigger(buildTrigger(jobDetail, triggerModel), paused);
                }
            }

            //delete the original job last, so that it survives any failure above even on a non-transactional job store
            scheduler.deleteJob(originalJobKey);
        } catch (SchedulerException e) {
            log.warn("Unable to save job {} under new key {}", originalJobKey, jobKey, e);
            throw new QuartzJobSaveException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.warn("Unable to find job class {}", jobModel.getJobClass());
            throw new QuartzJobSaveException("Job class " + jobModel.getJobClass() + " not found");
        }
    }

    /**
     * Updates job in the Quartz engine
     *
     * @param jobModel               job to edit
     * @param jobDataParameterModels parameters for job
     * @param triggerModels          triggers for job; existing triggers of the job are synchronized with provided
     *                               ones: unchanged triggers are kept with their execution state and pause state,
     *                               changed ones are rescheduled keeping their last fire time and pause state,
     *                               obsolete ones are removed, empty list removes all triggers; triggers added to
     *                               an existing job without active triggers are paused
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
            JobDetail existingJobDetail = scheduler.getJobDetail(jobKey);
            JobDetail jobDetail = buildJobDetail(jobModel, existingJobDetail, jobDataParameterModels);
            scheduler.addJob(jobDetail, replaceJobIfExists);

            updateTriggers(jobDetail, triggerModels, existingJobDetail != null);
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
     * keep their execution state and pause state, changed ones are rescheduled keeping their last fire time and
     * pause state, missing ones are scheduled and obsolete ones are unscheduled. Triggers added to an existing job
     * that has no active triggers are scheduled paused, so that the save does not start a job that is not running.
     *
     * @param existingJob whether the job existed in the scheduler before this save
     */
    protected void updateTriggers(JobDetail jobDetail, List<TriggerModel> triggerModels, boolean existingJob)
            throws SchedulerException {
        List<? extends Trigger> jobTriggers = scheduler.getTriggersOfJob(jobDetail.getKey());
        Map<TriggerKey, Trigger> existingTriggers = new HashMap<>();
        for (Trigger trigger : jobTriggers) {
            existingTriggers.put(trigger.getKey(), trigger);
        }
        /*
        A paused job and a job without triggers (shown as paused) must stay paused after the save,
        so their new triggers are paused as well. A new job starts active.
        */
        boolean pauseAddedTriggers = existingJob && !hasActiveTrigger(jobTriggers);

        Set<TriggerKey> keysToKeep = new HashSet<>();
        if (!CollectionUtils.isEmpty(triggerModels)) {
            for (TriggerModel triggerModel : triggerModels) {
                Trigger newTrigger = buildTrigger(jobDetail, triggerModel);
                Trigger existingTrigger = existingTriggers.get(newTrigger.getKey());
                if (existingTrigger == null) {
                    scheduleTrigger(newTrigger, pauseAddedTriggers);
                } else {
                    keysToKeep.add(newTrigger.getKey());
                    if (isTriggerChanged(existingTrigger, newTrigger, triggerModel)) {
                        Trigger.TriggerState oldTriggerState = scheduler.getTriggerState(existingTrigger.getKey());
                        copyPreviousFireTime(existingTrigger, newTrigger);
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
     * Defines whether the trigger registered in the scheduler differs from the trigger built from the submitted
     * model, which means the registered trigger must be replaced. The built trigger is compared instead of the raw
     * model values, so that the defaults and the normalization applied on build (default time zone and repeat
     * interval, start time rounding of cron triggers) do not count as changes.
     */
    protected boolean isTriggerChanged(Trigger existingTrigger, Trigger newTrigger, TriggerModel triggerModel) {
        if (isScheduleChanged(existingTrigger, newTrigger)) {
            return true;
        }
        if (triggerModel.getStartDate() == null) {
            /*
            Null start date in the model matches a start time in the past because past start times are cleared
            when the trigger is loaded into the model (see createTriggerModel), while the built trigger starts now.
            */
            if (existingTrigger.getStartTime() != null && existingTrigger.getStartTime().after(new Date())) {
                return true;
            }
        } else if (!Objects.equals(existingTrigger.getStartTime(), newTrigger.getStartTime())) {
            return true;
        }
        return !Objects.equals(existingTrigger.getEndTime(), newTrigger.getEndTime())
                || existingTrigger.getMisfireInstruction() != newTrigger.getMisfireInstruction();
    }

    protected boolean isScheduleChanged(Trigger existingTrigger, Trigger newTrigger) {
        if (existingTrigger instanceof CronTrigger existingCronTrigger
                && newTrigger instanceof CronTrigger newCronTrigger) {
            return !Objects.equals(existingCronTrigger.getCronExpression(), newCronTrigger.getCronExpression())
                    || !existingCronTrigger.getTimeZone().getID().equals(newCronTrigger.getTimeZone().getID());
        }
        if (existingTrigger instanceof SimpleTrigger existingSimpleTrigger
                && newTrigger instanceof SimpleTrigger newSimpleTrigger) {
            return existingSimpleTrigger.getRepeatInterval() != newSimpleTrigger.getRepeatInterval()
                    || existingSimpleTrigger.getRepeatCount() != newSimpleTrigger.getRepeatCount();
        }
        //different or unsupported trigger types - replace to be safe
        return true;
    }

    /**
     * Copies the last fire time of the trigger being replaced to its replacement, so that a reschedule keeps the
     * execution history visible. Quartz reads the previous fire time only for informational purposes (trigger
     * listing, {@code JobExecutionContext}): the next fire time and the misfire handling do not depend on it.
     * The fire counter of a simple trigger is not copied, the replacement starts its repeat count anew.
     */
    protected void copyPreviousFireTime(Trigger existingTrigger, Trigger newTrigger) {
        if (newTrigger instanceof OperableTrigger operableTrigger) {
            operableTrigger.setPreviousFireTime(existingTrigger.getPreviousFireTime());
        }
    }

    /**
     * Schedules the trigger, paused if requested.
     */
    protected void scheduleTrigger(Trigger trigger, boolean paused) throws SchedulerException {
        scheduler.scheduleJob(trigger);
        if (paused) {
            scheduler.pauseTrigger(trigger.getKey());
        }
    }

    /**
     * Defines whether any of the triggers is active, which means the job they belong to is waiting to be fired
     * or running.
     */
    protected boolean hasActiveTrigger(Collection<? extends Trigger> triggers) throws SchedulerException {
        for (Trigger trigger : triggers) {
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            if (triggerState == Trigger.TriggerState.NORMAL || triggerState == Trigger.TriggerState.BLOCKED) {
                return true;
            }
        }
        return false;
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
