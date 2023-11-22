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

package io.jmix.autoconfigure.pessimisticlock;

import io.jmix.autoconfigure.pessimisticlock.job.PessimisticLockExpiringJob;
import io.jmix.pessimisticlock.PessimisticLockConfiguration;
import io.jmix.pessimisticlock.PessimisticLockProperties;
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
@Import(PessimisticLockConfiguration.class)
@ConditionalOnClass(Job.class)
@ConditionalOnProperty(name = "jmix.pslock.use-default-quartz-configuration", matchIfMissing = true)
public class PessimisticLockScheduleAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PessimisticLockScheduleAutoConfiguration.class);

    private final PessimisticLockProperties pessimisticLockProperties;

    public PessimisticLockScheduleAutoConfiguration(PessimisticLockProperties pessimisticLockProperties) {
        this.pessimisticLockProperties = pessimisticLockProperties;
    }

    @Bean("pslock_PessimisticLockExpiringJob")
    JobDetail pessimisticLockExpiringJob() {
        return JobBuilder.newJob()
                .ofType(PessimisticLockExpiringJob.class)
                .storeDurably()
                .withIdentity("pessimisticLockExpiring")
                .build();
    }

    @Bean("pslock_PessimisticLockExpiringTrigger")
    Trigger pessimisticLockExpiringTrigger(
            @Qualifier("pslock_PessimisticLockExpiringJob") JobDetail pessimisticLockExpiringJob) {
        String cron = pessimisticLockProperties.getExpirationCron();
        log.info("Schedule pessimistic lock expiring using default configuration with CRON expression '{}'", cron);
        return TriggerBuilder.newTrigger()
                .withIdentity("pessimisticLockExpiringTrigger")
                .forJob(pessimisticLockExpiringJob)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
    }
}
