/*
 * Copyright 2025 Haulmont.
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

package io.jmix.sessions.impl;

import io.jmix.sessions.SessionsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Map} implementation for {@link MapSessionRepository} that periodically removes expired sessions.
 */
public class JmixExpiringSessionMap extends ConcurrentHashMap<String, Session> implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(JmixExpiringSessionMap.class);

    private ThreadPoolTaskScheduler taskScheduler;

    protected Boolean cleanupEnabled;

    protected Duration cleanupTimeout;

    protected ApplicationEventPublisher applicationEventPublisher;

    public JmixExpiringSessionMap(ApplicationEventPublisher applicationEventPublisher, SessionsProperties properties) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.cleanupEnabled = properties.getExpiringMap().getCleanupEnabled();
        this.cleanupTimeout = properties.getExpiringMap().getCleanupTimeout();
    }

    public void setCleanupTimeout(Duration cleanupTimeout) {
        Assert.notNull(cleanupTimeout, "cleanupTimeout must not be null");
        this.cleanupTimeout = cleanupTimeout;
    }

    public void cleanUpExpiredSessions() {
        log.debug("Session cleanup started");
        for (Map.Entry<String, Session> entry : entrySet()) {
            computeIfPresent(entry.getKey(), (key, session) -> {
                if (session.isExpired()) {
                    log.debug("Session {} expired. Sending SessionExpiredEvent and removing from {}", session.getId(), this.getClass().getName());
                    applicationEventPublisher.publishEvent(new SessionExpiredEvent(this, session));

                    return null;
                }
                return session;
            });
        }
    }


    private static ThreadPoolTaskScheduler createTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setThreadNamePrefix("jmix-sessions-");
        return taskScheduler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Boolean.TRUE.equals(cleanupEnabled)) {
            this.taskScheduler = createTaskScheduler();
            this.taskScheduler.initialize();
            this.taskScheduler.schedule(this::cleanUpExpiredSessions, new PeriodicTrigger(cleanupTimeout));
        }
    }

    @Override
    public void destroy() throws Exception {
        if (this.taskScheduler != null) {
            this.taskScheduler.destroy();
        }
    }
}
