/*
 * Copyright 2021 Haulmont.
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

package io.jmix.autoconfigure.search;

import io.jmix.autoconfigure.search.job.EnqueueingSessionProcessingJob;
import io.jmix.search.SearchConfiguration;
import io.jmix.search.SearchProperties;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

@AutoConfiguration
@Import(SearchConfiguration.class)
@ConditionalOnClass(Job.class)
public class EnqueueingSessionProcessingScheduleAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EnqueueingSessionProcessingScheduleAutoConfiguration.class);

    public static final String JOB_NAME = "EnqueueingSessionProcessing";

    public static final String JOB_TRIGGER_NAME = "EnqueueingSessionProcessingTrigger";

    @Autowired
    protected SearchProperties searchProperties;

    @Autowired
    protected ApplicationContext applicationContext;

    @Bean("search_EnqueueingSessionProcessingJob")
    @ConditionalOnProperty(name = "jmix.search.use-default-enqueueing-session-processing-quartz-configuration", matchIfMissing = true)
    JobDetail enqueueingSessionProcessingJob() {
        return JobBuilder.newJob()
                .ofType(EnqueueingSessionProcessingJob.class)
                .storeDurably()
                .withIdentity(JOB_NAME)
                .build();
    }

    @Bean("search_EnqueueingSessionProcessingTrigger")
    @ConditionalOnProperty(name = "jmix.search.use-default-enqueueing-session-processing-quartz-configuration", matchIfMissing = true)
    Trigger enqueueingSessionProcessingTrigger(@Qualifier("search_EnqueueingSessionProcessingJob") JobDetail enqueueingSessionProcessingJob) {
        String cron = searchProperties.getEnqueueingSessionProcessingCron();
        log.info("Schedule Enqueueing Session processing using default configuration with CRON expression '{}'", cron);
        return TriggerBuilder.newTrigger()
                .withIdentity(JOB_TRIGGER_NAME)
                .forJob(enqueueingSessionProcessingJob)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
    }

    @EventListener(ApplicationStartedEvent.class)
    void cleanJob() {
        if (!searchProperties.isUseDefaultEnqueueingSessionProcessingQuartzConfiguration()) {
            JobKey jobKey = JobKey.jobKey(JOB_NAME);
            Scheduler scheduler = applicationContext.getBean(Scheduler.class);
            try {
                if (scheduler.checkExists(jobKey)) {
                    scheduler.deleteJob(jobKey);
                }
            } catch (SchedulerException e) {
                throw new RuntimeException("Error cleaning disabled EnqueueingSessionProcessing quartz job", e);
            }
        }
    }
}
