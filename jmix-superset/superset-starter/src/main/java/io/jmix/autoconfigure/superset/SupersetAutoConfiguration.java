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

package io.jmix.autoconfigure.superset;

import io.jmix.superset.SupersetConfiguration;
import io.jmix.superset.SupersetProperties;
import io.jmix.superset.SupersetTokenManager;
import io.jmix.superset.client.cookie.SupersetCookieManager;
import io.jmix.superset.schedule.SupersetTokenScheduleConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@AutoConfiguration
@Import(SupersetConfiguration.class)
public class SupersetAutoConfiguration {

    @Bean("sprset_ThreadPoolAccessTokenTaskScheduler")
    public TaskScheduler threadPoolAccessTokenTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix("sprset_AccessTokenScheduler-");
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setDaemon(true);
        return threadPoolTaskScheduler;
    }

    @Bean("sprset_ThreadPoolCsrfTokenTaskScheduler")
    @ConditionalOnProperty(value = {"jmix.superset.csrf-protection-enabled",
                                    "jmix.superset.tokens-refresh-enabled"}, matchIfMissing = true)
    public TaskScheduler threadPoolTasCsrfTokenkScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix("sprset_CsrfTokenScheduler-");
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setDaemon(true);
        return threadPoolTaskScheduler;
    }

    @Bean("sprset_SupersetTokenScheduleConfigurer")
    @ConditionalOnProperty(value = "jmix.superset.tokens-refresh-enabled", matchIfMissing = true)
    public SupersetTokenScheduleConfigurer supersetTokenScheduleConfigurer(
            @Qualifier("sprset_ThreadPoolAccessTokenTaskScheduler")
            TaskScheduler taskScheduler,
            @Autowired(required = false)
            @Qualifier("sprset_ThreadPoolCsrfTokenTaskScheduler")
            TaskScheduler csrfTaskScheduler,
            SupersetProperties supersetProperties,
            SupersetTokenManager accessTokenManager) {
        return new SupersetTokenScheduleConfigurer(taskScheduler, csrfTaskScheduler, supersetProperties,
                accessTokenManager);
    }

    @Bean("sprset_SupersetCookieManager")
    @ConditionalOnMissingBean
    public SupersetCookieManager supersetCookieManager() {
        return new SupersetCookieManager();
    }
}
