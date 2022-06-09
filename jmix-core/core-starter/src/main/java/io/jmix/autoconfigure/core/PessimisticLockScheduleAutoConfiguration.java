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

package io.jmix.autoconfigure.core;

import io.jmix.autoconfigure.core.job.PessimisticLockExpiringJob;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.CoreProperties;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(CoreConfiguration.class)
@ConditionalOnClass(Job.class)
@ConditionalOnProperty(name = "jmix.pessimistic-lock.use-default-quartz-configuration", matchIfMissing = true)
public class PessimisticLockScheduleAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PessimisticLockScheduleAutoConfiguration.class);

    private CoreProperties coreProperties;

    public PessimisticLockScheduleAutoConfiguration(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Bean("core_PessimisticLockExpiringJob")
    JobDetail pessimisticLockExpiringJob() {
        return JobBuilder.newJob()
                .ofType(PessimisticLockExpiringJob.class)
                .storeDurably()
                .withIdentity("pessimisticLockExpiring")
                .build();
    }

    @Bean("core_PessimisticLockExpiringTrigger")
    Trigger emailSendingTrigger(@Qualifier("core_PessimisticLockExpiringJob") JobDetail pessimisticLockExpiringJob) {
        String cron = coreProperties.getPessimisticLock().getExpirationCron();
        log.info("Schedule pessimistic lock expiring using default configuration with CRON expression '{}'", cron);
        return TriggerBuilder.newTrigger()
                .withIdentity("pessimisticLockExpiringTrigger")
                .forJob(pessimisticLockExpiringJob)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
    }
}