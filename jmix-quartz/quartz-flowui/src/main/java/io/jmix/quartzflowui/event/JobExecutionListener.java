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

package io.jmix.quartzflowui.event;

import io.jmix.flowui.UiEventPublisher;
import io.jmix.quartz.service.RunningJobsCache;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component("quartz_JobExecutionListener")
public class JobExecutionListener extends JobListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionListener.class);

    public static final String QUARTZ_JOB_EXECUTION_LISTENER = "quartz_JobExecutionListener";

    @Autowired
    private Scheduler scheduler;

    @Autowired
    protected UiEventPublisher uiEventPublisher;

    @Autowired
    protected RunningJobsCache runningJobCache;

    @Override
    public String getName() {
        return QUARTZ_JOB_EXECUTION_LISTENER;
    }

    @EventListener(ApplicationStartedEvent.class)
    private void registerListener() {
        try {
            scheduler.getListenerManager().addJobListener(this);
        } catch (SchedulerException e) {
            log.error("Cannot register job listener", e);
        }
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        log.debug("jobToBeExecuted: {}", context);
        uiEventPublisher.publishEventForUsers(new QuartzJobStartExecutionEvent(context), null);
        runningJobCache.put(context.getJobDetail().getKey(), context.getJobDetail());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context,
                               JobExecutionException jobException) {
        log.debug("jobWasExecuted: {}", context);
        uiEventPublisher.publishEventForUsers(new QuartzJobEndExecutionEvent(context, jobException), null);
        runningJobCache.invalidate(context.getJobDetail().getKey());
    }
}