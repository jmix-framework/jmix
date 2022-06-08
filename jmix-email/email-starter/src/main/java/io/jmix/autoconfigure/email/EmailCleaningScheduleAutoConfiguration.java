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

package io.jmix.autoconfigure.email;

import io.jmix.autoconfigure.email.job.EmailCleaningJob;
import io.jmix.email.EmailConfiguration;
import io.jmix.email.EmailerProperties;
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
@Import(EmailConfiguration.class)
@ConditionalOnClass(Job.class)
@ConditionalOnProperty(name = "jmix.email.use-default-email-cleaning-quartz-configuration")
public class EmailCleaningScheduleAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(EmailSendingScheduleAutoConfiguration.class);

    @Autowired
    private EmailerProperties emailerProperties;

    @Bean("email_EmailCleaningJob")
    JobDetail emailCleaningJob() {
        return JobBuilder.newJob()
                .ofType(EmailCleaningJob.class)
                .storeDurably()
                .withIdentity("emailCleaning")
                .build();
    }

    @Bean("email_EmailCleaningTrigger")
    Trigger emailCleaningTrigger(@Qualifier("email_EmailCleaningJob") JobDetail emailCleaningJob) {
        String cron = emailerProperties.getEmailCleaningCron();
        log.info("Schedule Email Cleaning using default configuration with CRON expression '{}'", cron);
        return TriggerBuilder.newTrigger()
                .withIdentity("emailCleaningCronTrigger")
                .forJob(emailCleaningJob)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
    }
}
