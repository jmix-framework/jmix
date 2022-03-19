/*
 * Copyright 2020 Haulmont.
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

package io.jmix.sessions;

import io.jmix.core.annotation.Internal;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.sessions.events.JmixSessionCreatedEvent;
import io.jmix.sessions.events.JmixSessionDestroyedEvent;
import io.jmix.sessions.events.JmixSessionRestoredEvent;
import io.jmix.sessions.validators.SessionAttributePersistenceValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Internal
public class SessionRepositoryWrapper<S extends Session> implements FindByIndexNameSessionRepository<SessionRepositoryWrapper<S>.SessionWrapper> {

    private List<SessionAttributePersistenceValidator> attributePersistenceValidators = new ArrayList<>();

    protected Map<String, Map<String, Object>> nonPersistentSessionAttributesMap = new ConcurrentHashMap<>();

    protected SessionRepository<S> delegate;

    protected SessionRegistry sessionRegistry;

    protected ApplicationEventPublisher applicationEventPublisher;

    public List<SessionAttributePersistenceValidator> getAttributePersistenceValidators() {
        return attributePersistenceValidators;
    }

    public void addAttributePersistenceValidators(SessionAttributePersistenceValidator... validators) {
        attributePersistenceValidators.addAll(Arrays.asList(validators));
    }

    public void setAttributePersistenceValidators(List<SessionAttributePersistenceValidator> attributePersistenceValidators) {
        this.attributePersistenceValidators = attributePersistenceValidators;
    }

    public SessionRepositoryWrapper(SessionRegistry sessionRegistry, ApplicationEventPublisher applicationEventPublisher, SessionRepository<S> delegate) {
        this.delegate = delegate;
        this.sessionRegistry = sessionRegistry;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public SessionWrapper createSession() {
        SessionWrapper sessionWrapper = new SessionWrapper(delegate.createSession());
        applicationEventPublisher.publishEvent(new JmixSessionCreatedEvent<>(sessionWrapper));
        return sessionWrapper;
    }

    @Override
    public void save(SessionWrapper session) {
        saveNonPersistenceAttributes(session);
        delegate.save(session.getSession());
    }

    private void restoreNonPersistentAttributes(SessionWrapper session) {
        Map<String, Object> nonPersistentAttributes = nonPersistentSessionAttributesMap.get(session.getId());
        if (nonPersistentAttributes != null) {
            for (Map.Entry<String, Object> entry : nonPersistentAttributes.entrySet()) {
                session.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    private void saveNonPersistenceAttributes(SessionWrapper session) {
        Map<String, Object> nonPersistentAttributes = session.getNonPersistentAttributes();
        if (!nonPersistentAttributes.isEmpty()) {
            nonPersistentSessionAttributesMap.put(session.getId(), nonPersistentAttributes);
        }
    }

    @Override
    public SessionWrapper findById(String id) {
        S session = delegate.findById(id);
        if (session != null) {
            SessionWrapper sessionWrapper = new SessionWrapper(session);
            restoreNonPersistentAttributes(sessionWrapper);
            if (SecurityContextHelper.getAuthentication() != null) {
                Object principal = SecurityContextHelper.getAuthentication().getPrincipal();
                if (principal != null && sessionRegistry.getSessionInformation(id) == null) {
                    sessionRegistry.registerNewSession(id, principal);
                    applicationEventPublisher.publishEvent(new JmixSessionRestoredEvent<>(sessionWrapper));
                }
            }
            return sessionWrapper;
        }
        return null;
    }

    @Override
    public void deleteById(String id) {
        SessionWrapper session = findById(id);
        if (session != null) {
            applicationEventPublisher.publishEvent(new JmixSessionDestroyedEvent<>(session));
            delegate.deleteById(id);
        }
        nonPersistentSessionAttributesMap.remove(id);
    }

    @Override
    public Map<String, SessionWrapper> findByIndexNameAndIndexValue(String indexName, String indexValue) {
        if (delegate instanceof FindByIndexNameSessionRepository) {
            return wrapMap(((FindByIndexNameSessionRepository<S>) delegate).findByIndexNameAndIndexValue(indexName, indexValue));
        }
        return Collections.emptyMap();
    }

    private Map<String, SessionWrapper> wrapMap(Map<String, S> map) {
        return map.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), new SessionWrapper(e.getValue())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public final class SessionWrapper implements Session {
        private final S session;
        private final Map<String, Object> nonPersistentAttributes = new HashMap<>();

        public SessionWrapper(S session) {
            this.session = session;
        }

        public Map<String, Object> getNonPersistentAttributes() {
            return nonPersistentAttributes;
        }

        private boolean isNotPersistent(String attributeName, Object attributeValue) {
            for (SessionAttributePersistenceValidator validator : attributePersistenceValidators) {
                if (!validator.isPersistent(attributeName, attributeValue)) {
                    return true;
                }
            }
            return false;
        }

        public S getSession() {
            return session;
        }

        @Override
        public String getId() {
            return session.getId();
        }

        @Override
        public String changeSessionId() {
            return session.changeSessionId();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getAttribute(String attributeName) {
            return (T) nonPersistentAttributes.getOrDefault(attributeName, session.getAttribute(attributeName));
        }

        @Override
        public Set<String> getAttributeNames() {
            Set<String> sessionAttributes = new HashSet<>(session.getAttributeNames());
            sessionAttributes.addAll(nonPersistentAttributes.keySet());
            return sessionAttributes;
        }

        @Override
        public void setAttribute(String attributeName, Object attributeValue) {
            if (isNotPersistent(attributeName, attributeValue)) {
                nonPersistentAttributes.put(attributeName, attributeValue);
            } else {
                session.setAttribute(attributeName, attributeValue);
            }
        }

        @Override
        public void removeAttribute(String attributeName) {
            nonPersistentAttributes.remove(attributeName);
            session.removeAttribute(attributeName);
        }

        @Override
        public Instant getCreationTime() {
            return session.getCreationTime();
        }

        @Override
        public void setLastAccessedTime(Instant lastAccessedTime) {
            session.setLastAccessedTime(lastAccessedTime);
        }

        @Override
        public Instant getLastAccessedTime() {
            return session.getLastAccessedTime();
        }

        @Override
        public void setMaxInactiveInterval(Duration interval) {
            session.setMaxInactiveInterval(interval);
        }

        @Override
        public Duration getMaxInactiveInterval() {
            return session.getMaxInactiveInterval();
        }

        @Override
        public boolean isExpired() {
            return session.isExpired();
        }
    }
}
