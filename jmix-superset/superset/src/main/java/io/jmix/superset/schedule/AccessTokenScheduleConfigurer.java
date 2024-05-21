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

package io.jmix.superset.schedule;

import io.jmix.superset.SupersetProperties;
import io.jmix.superset.schedule.impl.AccessTokenManagerImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component("superset_AccessTokenScheduleConfigurer")
public class AccessTokenScheduleConfigurer {

    private final TaskScheduler taskScheduler;
    private final SupersetProperties supersetProperties;
    private final AccessTokenManagerImpl accessTokenManager;

    public AccessTokenScheduleConfigurer(@Qualifier("superset_ThreadPoolTaskScheduler") TaskScheduler taskScheduler,
                                         SupersetProperties supersetProperties,
                                         AccessTokenManagerImpl accessTokenManager) {
        this.taskScheduler = taskScheduler;
        this.supersetProperties = supersetProperties;
        this.accessTokenManager = accessTokenManager;
    }

    @EventListener
    public void onContextRefreshedEvent(ContextRefreshedEvent event) {
        taskScheduler.scheduleWithFixedDelay(
                accessTokenManager::updateAccessToken,
                supersetProperties.getRefreshAccessTokenScheduler());
    }
}
