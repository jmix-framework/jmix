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

package io.jmix.autoconfigure.imap;

import io.jmix.autoconfigure.imap.job.ImapSyncJob;
import io.jmix.imap.ImapConfiguration;
import io.jmix.imap.ImapProperties;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ImapConfiguration.class)
@ConditionalOnClass(Job.class)
@ConditionalOnProperty(name = "jmix.imap.use-default-quartz-configuration", matchIfMissing = true)
public class ImapSyncScheduleAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ImapSyncScheduleAutoConfiguration.class);

    @Autowired
    private ImapProperties imapProperties;

    @Bean("imap_ImapSyncJob")
    JobDetail imapSyncJob() {
        return JobBuilder.newJob()
                .ofType(ImapSyncJob.class)
                .storeDurably()
                .withIdentity("imapSync")
                .build();
    }

    @Bean("imap_EmailSendingTrigger")
    Trigger emailSendingTrigger(@Qualifier("imap_ImapSyncJob") JobDetail imapSyncJob) {
        String cron = imapProperties.getImapSyncCron();
        log.info("Schedule Imap Sync using default configuration with CRON expression '{}'", cron);
        return TriggerBuilder.newTrigger()
                .withIdentity("imapSyncCronTrigger")
                .forJob(imapSyncJob)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
    }
}
