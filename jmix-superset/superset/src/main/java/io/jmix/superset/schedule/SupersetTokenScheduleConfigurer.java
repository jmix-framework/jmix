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
import io.jmix.superset.SupersetTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;

/**
 * The class starts schedule tasks for getting/refreshing access token and CSRF token. The availability of this class
 * is managed by {@link SupersetProperties#isTokensRefreshEnabled()} property in autoconfiguration.
 */
public class SupersetTokenScheduleConfigurer {
    private static final Logger log = LoggerFactory.getLogger(SupersetTokenScheduleConfigurer.class);

    private final TaskScheduler accessTokenTaskScheduler;
    private final TaskScheduler csrfTokenTaskScheduler;
    private final SupersetProperties supersetProperties;
    private final SupersetTokenManager accessTokenManager;

    public SupersetTokenScheduleConfigurer(TaskScheduler accessTokenTaskScheduler,
                                           TaskScheduler csrfTokenTaskScheduler,
                                           SupersetProperties supersetProperties,
                                           SupersetTokenManager accessTokenManager) {
        this.accessTokenTaskScheduler = accessTokenTaskScheduler;
        this.csrfTokenTaskScheduler = csrfTokenTaskScheduler;
        this.supersetProperties = supersetProperties;
        this.accessTokenManager = accessTokenManager;
    }

    @EventListener
    public void onContextRefreshedEvent(ContextRefreshedEvent event) {
        startAccessTokenScheduler();
        startCsrfTokenScheduler();
    }

    protected void startAccessTokenScheduler() {
        // Schedule refreshing an access token
        accessTokenTaskScheduler.scheduleWithFixedDelay(
                accessTokenManager::refreshAccessToken,
                supersetProperties.getAccessTokenRefreshSchedule());

        log.debug("AccessToken scheduler started");
    }

    protected void startCsrfTokenScheduler() {
        if (!supersetProperties.isCsrfProtectionEnabled()) {
            return;
        }

        // Schedule refreshing a CSRF token
        csrfTokenTaskScheduler.scheduleWithFixedDelay(
                accessTokenManager::refreshCsrfToken,
                supersetProperties.getCsrfTokenRefreshSchedule());

        log.debug("CSRF token scheduler started");
    }
}
