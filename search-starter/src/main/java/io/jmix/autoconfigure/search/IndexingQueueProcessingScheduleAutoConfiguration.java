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

import io.jmix.autoconfigure.search.job.IndexingQueueProcessingJob;
import io.jmix.search.SearchConfiguration;
import io.jmix.search.SearchProperties;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SearchConfiguration.class)
@ConditionalOnClass(Job.class)
@ConditionalOnProperty(name = "jmix.search.useDefaultIndexingQueueProcessingQuartzConfiguration", matchIfMissing = true)
public class IndexingQueueProcessingScheduleAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IndexingQueueProcessingScheduleAutoConfiguration.class);

    @Autowired
    protected SearchProperties searchProperties;

    @Bean("search_IndexingQueueProcessingJob")
    JobDetail indexingQueueProcessingJob() {
        return JobBuilder.newJob()
                .ofType(IndexingQueueProcessingJob.class)
                .storeDurably()
                .withIdentity("IndexingQueueProcessing")
                .build();
    }

    @Bean("search_IndexingQueueProcessingTrigger")
    Trigger indexingQueueProcessingTrigger() {
        String cron = searchProperties.getIndexingQueueProcessingCron();
        log.info("Schedule Indexing Queue processing using default configuration with CRON expression '{}'", cron);
        return TriggerBuilder.newTrigger()
                .forJob(indexingQueueProcessingJob())
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
    }
}
