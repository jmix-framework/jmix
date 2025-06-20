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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;

import org.springframework.boot.autoconfigure.session.HazelcastSessionProperties;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.session.*;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.session.hazelcast.HazelcastIndexedSessionRepository;
import org.springframework.session.hazelcast.config.annotation.SpringSessionHazelcastInstance;
import org.springframework.session.hazelcast.config.annotation.web.http.HazelcastHttpSessionConfiguration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Hazelcast sessions autoconfiguration. Copied from {@link HazelcastHttpSessionConfiguration}
 * and {@code org.springframework.boot.autoconfigure.session.HazelcastSessionConfiguration}.
 * <p>
 * {@link SpringHttpSessionConfiguration} import avoided in order to use Jmix bean implementations which add Vaadin compatibility.
 */
@AutoConfiguration(after = HazelcastAutoConfiguration.class)
@ConditionalOnClass({HazelcastInstance.class, HazelcastIndexedSessionRepository.class})
@ConditionalOnSingleCandidate(HazelcastInstance.class)
@ConditionalOnMissingBean(SessionRepository.class)
@EnableConfigurationProperties(HazelcastSessionProperties.class)
public class JmixHazelcastSessionsAutoConfiguration {

    private HazelcastInstance hazelcastInstance;
    private ApplicationEventPublisher applicationEventPublisher;
    private IndexResolver<Session> indexResolver;
    private List<SessionRepositoryCustomizer<HazelcastIndexedSessionRepository>> sessionRepositoryCustomizers;
    private SessionIdGenerator sessionIdGenerator = UuidSessionIdGenerator.getInstance();

    private SessionProperties sessionProperties;
    private HazelcastSessionProperties hazelcastSessionProperties;
    private ServerProperties serverProperties;

    @Bean
    public FindByIndexNameSessionRepository<?> sessionRepository() {
        HazelcastIndexedSessionRepository sessionRepository = createHazelcastIndexedSessionRepository();

        applyProperties(sessionRepository);

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

        return sessionRepository;
    }

    private void applyProperties(HazelcastIndexedSessionRepository sessionRepository) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(sessionProperties.determineTimeout(() -> serverProperties.getServlet().getSession().getTimeout()))
                .to(sessionRepository::setDefaultMaxInactiveInterval);
        map.from(hazelcastSessionProperties::getMapName).to(sessionRepository::setSessionMapName);
        map.from(hazelcastSessionProperties::getFlushMode).to(sessionRepository::setFlushMode);
        map.from(hazelcastSessionProperties::getSaveMode).to(sessionRepository::setSaveMode);
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

    @Autowired
    public void setServerProperties(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Autowired
    public void setHazelcastSessionProperties(HazelcastSessionProperties hazelcastSessionProperties) {
        this.hazelcastSessionProperties = hazelcastSessionProperties;
    }

    @Autowired
    public void setSessionProperties(SessionProperties sessionProperties) {
        this.sessionProperties = sessionProperties;
    }

    @Autowired(required = false)
    public void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {
        this.sessionIdGenerator = sessionIdGenerator;
    }
}
