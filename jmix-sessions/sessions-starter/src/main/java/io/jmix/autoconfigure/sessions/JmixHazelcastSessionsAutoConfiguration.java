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

package io.jmix.autoconfigure.sessions;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.session.HazelcastIndexedSessionRepository;
import com.hazelcast.spring.session.SessionMapCustomizer;
import com.hazelcast.spring.session.config.annotation.SpringSessionHazelcastInstance;
import com.hazelcast.spring.session.config.annotation.web.http.HazelcastHttpSessionConfiguration;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.session.autoconfigure.SessionAutoConfiguration;
import org.springframework.boot.session.autoconfigure.SessionTimeout;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.IndexResolver;
import org.springframework.session.Session;
import org.springframework.session.SessionIdGenerator;
import org.springframework.session.SessionRepository;
import org.springframework.session.UuidSessionIdGenerator;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hazelcast sessions autoconfiguration. Copied from {@link HazelcastHttpSessionConfiguration}.
 * <p>
 * {@link SpringHttpSessionConfiguration} import avoided in order to use Jmix bean implementations which add Vaadin compatibility.
 */
@AutoConfiguration(after = SessionAutoConfiguration.class,
        afterName = "org.springframework.boot.hazelcast.autoconfigure.HazelcastAutoConfiguration")
@ConditionalOnClass({HazelcastInstance.class, HazelcastIndexedSessionRepository.class})
@ConditionalOnSingleCandidate(HazelcastInstance.class)
@ConditionalOnMissingBean(SessionRepository.class)
public class JmixHazelcastSessionsAutoConfiguration {

    private HazelcastInstance hazelcastInstance;
    private ApplicationEventPublisher applicationEventPublisher;
    private IndexResolver<Session> indexResolver;
    private List<SessionRepositoryCustomizer<HazelcastIndexedSessionRepository>> sessionRepositoryCustomizers = Collections.emptyList();
    private SessionMapCustomizer sessionMapCustomizer;
    private SessionIdGenerator sessionIdGenerator = UuidSessionIdGenerator.getInstance();

    private ObjectProvider<SessionTimeout> sessionTimeout;
    private ServletContext servletContext;

    @Bean
    public FindByIndexNameSessionRepository<?> sessionRepository() {
        HazelcastIndexedSessionRepository sessionRepository = createHazelcastIndexedSessionRepository();

        applyTimeout(sessionRepository);

        this.sessionRepositoryCustomizers
                .forEach((sessionRepositoryCustomizer) -> sessionRepositoryCustomizer.customize(sessionRepository));
        return sessionRepository;
    }

    private HazelcastIndexedSessionRepository createHazelcastIndexedSessionRepository() {
        HazelcastIndexedSessionRepository sessionRepository = new HazelcastIndexedSessionRepository(
                this.hazelcastInstance);
        sessionRepository.setApplicationEventPublisher(this.applicationEventPublisher);
        if (this.indexResolver != null) {
            sessionRepository.setIndexResolver(this.indexResolver);
        }

        sessionRepository.setSessionIdGenerator(this.sessionIdGenerator);
        if (this.sessionMapCustomizer != null) {
            sessionRepository.setSessionMapConfigCustomizer(this.sessionMapCustomizer);
        }

        return sessionRepository;
    }

    private void applyTimeout(HazelcastIndexedSessionRepository sessionRepository) {
        sessionRepository.setDefaultMaxInactiveInterval(determineTimeout());
    }

    private Duration determineTimeout() {
        SessionTimeout sessionTimeout = this.sessionTimeout.getIfAvailable();
        Duration timeout = sessionTimeout != null ? sessionTimeout.getTimeout() : null;
        return timeout != null ? timeout : Duration.ofMinutes(this.servletContext.getSessionTimeout());
    }

    @Autowired
    public void setHazelcastInstance(
            @SpringSessionHazelcastInstance ObjectProvider<HazelcastInstance> springSessionHazelcastInstance,
            ObjectProvider<HazelcastInstance> hazelcastInstance) {
        HazelcastInstance hazelcastInstanceToUse = springSessionHazelcastInstance.getIfAvailable();
        if (hazelcastInstanceToUse == null) {
            hazelcastInstanceToUse = hazelcastInstance.getObject();
        }
        this.hazelcastInstance = hazelcastInstanceToUse;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired(required = false)
    public void setIndexResolver(IndexResolver<Session> indexResolver) {
        this.indexResolver = indexResolver;
    }

    @Autowired(required = false)
    public void setSessionRepositoryCustomizer(
            ObjectProvider<SessionRepositoryCustomizer<HazelcastIndexedSessionRepository>> sessionRepositoryCustomizers) {
        this.sessionRepositoryCustomizers = sessionRepositoryCustomizers.orderedStream().collect(Collectors.toList());
    }

    @Autowired(required = false)
    public void setSessionMapCustomizer(ObjectProvider<SessionMapCustomizer> sessionMapCustomizers) {
        this.sessionMapCustomizer = sessionMapCustomizers.orderedStream()
                .reduce(SessionMapCustomizer::andThen)
                .orElse(SessionMapCustomizer.noop());
    }

    @Autowired
    public void setSessionTimeout(ObjectProvider<SessionTimeout> sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Autowired(required = false)
    public void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {
        this.sessionIdGenerator = sessionIdGenerator;
    }
}
