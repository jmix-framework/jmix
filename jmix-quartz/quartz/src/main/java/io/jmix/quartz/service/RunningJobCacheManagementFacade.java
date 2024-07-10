/*
 * Copyright 2024 Haulmont.
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

import io.jmix.quartz.QuartzProperties;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Component;

@ManagedResource(description = "Manages running quartz jobs cache", objectName = "jmix.quartz:type=RunningJobsCache")
@Component("quartz_RunningJobCacheManagementFacade")
public class RunningJobCacheManagementFacade {

    private static final Logger log = LoggerFactory.getLogger(RunningJobCacheManagementFacade.class);

    @Autowired
    protected RunningJobsCache runningJobsCache;
    @Autowired
    protected QuartzProperties quartzProperties;

    @ManagedAttribute(description = "If cache usage is enabled")
    public boolean isCacheEnabled() {
        return quartzProperties.isRunningJobsCacheUsageEnabled();
    }

    @ManagedOperation(description = "Discard all cached jobs")
    public String evictAll() {
        if (!quartzProperties.isRunningJobsCacheUsageEnabled()) {
            return "Cache usage is disabled";
        }

        runningJobsCache.invalidateAll();
        return "Done";
    }

    @ManagedOperation(description = "Discard all cached triggers for job")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "jobName", description = "Name of the job"),
            @ManagedOperationParameter(name = "jobGroup", description = "Group of the job. Can be empty if it uses DEFAULT group")
    })
    public String evictJob(String jobName, String jobGroup) {
        if (!quartzProperties.isRunningJobsCacheUsageEnabled()) {
            return "Cache usage is disabled";
        }

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        runningJobsCache.invalidate(jobKey);
        return "Done";
    }

    @ManagedOperation(description = "Discard specific cached triggers for job")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "jobName", description = "Name of the job"),
            @ManagedOperationParameter(name = "jobGroup", description = "Group of the job. Can be empty if it uses DEFAULT group"),
            @ManagedOperationParameter(name = "jobName", description = "Name of the trigger"),
            @ManagedOperationParameter(name = "jobGroup", description = "Group of the trigger. Can be empty if it uses DEFAULT group")
    })
    public String evictTrigger(String jobName, String jobGroup, String triggerName, String triggerGroup) {
        if (!quartzProperties.isRunningJobsCacheUsageEnabled()) {
            return "Cache usage is disabled";
        }

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        runningJobsCache.invalidate(jobKey, triggerKey);
        return "Done";
    }
}
