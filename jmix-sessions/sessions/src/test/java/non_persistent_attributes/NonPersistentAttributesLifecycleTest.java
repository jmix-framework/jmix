/*
 * Copyright 2026 Haulmont.
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

package non_persistent_attributes;

import io.jmix.sessions.SessionRepositoryWrapper;
import io.jmix.sessions.SessionsProperties;
import io.jmix.sessions.impl.JmixExpiringSessionMap;
import io.jmix.sessions.validators.VaadinSessionAttributesValidator;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.jmix.sessions.validators.VaadinSessionAttributesValidator.VAADIN_SESSION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Checks that the non-persistent attributes of a session (the Vaadin session) are dropped and the Vaadin session is
 * unbound however the session ends, so that they do not outlive it.
 */
class NonPersistentAttributesLifecycleTest {

    GenericApplicationContext context;
    JmixExpiringSessionMap sessionMap;
    SessionRepositoryWrapper<MapSession> repository;
    List<SessionExpiredEvent> expiredEvents = new ArrayList<>();

    @BeforeEach
    void setUp() {
        context = new GenericApplicationContext();
        context.refresh();
        context.addApplicationListener((SessionExpiredEvent event) -> expiredEvents.add(event));

        SessionsProperties properties = new SessionsProperties(
                new SessionsProperties.ExpiringMap(false, Duration.ofMinutes(5)));
        sessionMap = new JmixExpiringSessionMap(context, properties);

        repository = new SessionRepositoryWrapper<>(new SessionRegistryImpl(), context,
                new MapSessionRepository(sessionMap));
        repository.addAttributePersistenceValidators(new VaadinSessionAttributesValidator());
        context.addApplicationListener(repository);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void cleanUpExpiredSessions_expiredSession_unbindsVaadinSessionAndForgetsIt() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(vaadinSession, true);

        sessionMap.cleanUpExpiredSessions();

        assertThat(sessionMap).doesNotContainKey(id);
        assertThat(vaadinSession.unboundCount).isEqualTo(1);
        assertThat(nonPersistentAttributes()).isEmpty();
    }

    @Test
    void cleanUpExpiredSessions_liveSession_keepsVaadinSession() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(vaadinSession, false);

        sessionMap.cleanUpExpiredSessions();

        assertThat(vaadinSession.unboundCount).isZero();
        assertThat(repository.findById(id).<Object>getAttribute(VAADIN_SESSION_KEY)).isSameAs(vaadinSession);
    }

    @Test
    void findById_expiredSession_publishesExpiredEventAndUnbindsVaadinSession() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(vaadinSession, true);

        assertThat(repository.findById(id)).isNull();

        assertThat(expiredEvents).extracting(SessionExpiredEvent::getSessionId).containsExactly(id);
        assertThat(vaadinSession.unboundCount).isEqualTo(1);
        assertThat(nonPersistentAttributes()).isEmpty();
    }

    @Test
    void findById_afterCleanUp_doesNotUnbindVaadinSessionAgain() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(vaadinSession, true);

        sessionMap.cleanUpExpiredSessions();
        repository.findById(id);

        assertThat(expiredEvents).hasSize(1);
        assertThat(vaadinSession.unboundCount).isEqualTo(1);
    }

    @Test
    void save_afterChangeSessionId_dropsOldIdAndKeepsVaadinSession() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String oldId = saveSession(vaadinSession, false);

        SessionRepositoryWrapper<MapSession>.SessionWrapper session = repository.findById(oldId);
        String newId = session.changeSessionId();
        repository.save(session);

        assertThat(nonPersistentAttributes()).containsOnlyKeys(newId);
        assertThat(vaadinSession.unboundCount).isZero();
        assertThat(expiredEvents).isEmpty();
        assertThat(repository.findById(newId).<Object>getAttribute(VAADIN_SESSION_KEY)).isSameAs(vaadinSession);
    }

    @Test
    void save_concurrentRequestWithOriginalId_doesNotKeepVaadinSessionUnderOriginalId() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String oldId = saveSession(vaadinSession, false);

        // Another request with the old cookie loads the session before the id change and saves it afterwards
        SessionRepositoryWrapper<MapSession>.SessionWrapper concurrentSession = repository.findById(oldId);
        SessionRepositoryWrapper<MapSession>.SessionWrapper session = repository.findById(oldId);
        String newId = session.changeSessionId();
        repository.save(session);
        repository.save(concurrentSession);

        assertThat(nonPersistentAttributes()).containsOnlyKeys(newId);

        // The default store has brought the original id back; its expiry must not touch the Vaadin session
        sessionMap.get(oldId).setLastAccessedTime(Instant.now().minus(Duration.ofHours(1)));
        sessionMap.cleanUpExpiredSessions();

        assertThat(vaadinSession.unboundCount).isZero();
        assertThat(repository.findById(newId).<Object>getAttribute(VAADIN_SESSION_KEY)).isSameAs(vaadinSession);
    }

    @Test
    void save_sessionEndedDuringRequest_doesNotBringEntryBack() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(vaadinSession, false);

        SessionRepositoryWrapper<MapSession>.SessionWrapper session = repository.findById(id);
        // A store that reports expiry itself (e.g. Hazelcast) expires the session while the request is running
        context.publishEvent(new SessionExpiredEvent(this, sessionMap.get(id)));
        repository.save(session);

        assertThat(vaadinSession.unboundCount).isEqualTo(1);
        assertThat(nonPersistentAttributes()).isEmpty();
    }

    @Test
    void deleteById_afterChangeSessionId_leavesNothingBehind() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String oldId = saveSession(vaadinSession, false);

        SessionRepositoryWrapper<MapSession>.SessionWrapper session = repository.findById(oldId);
        String newId = session.changeSessionId();
        repository.save(session);
        repository.deleteById(newId);

        assertThat(sessionMap).isEmpty();
        assertThat(nonPersistentAttributes()).isEmpty();
        assertThat(vaadinSession.unboundCount).isEqualTo(1);
    }

    @Test
    void deleteById_liveSession_unbindsVaadinSessionWithoutExpiredEvent() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(vaadinSession, false);

        repository.deleteById(id);

        assertThat(expiredEvents).isEmpty();
        assertThat(vaadinSession.unboundCount).isEqualTo(1);
        assertThat(nonPersistentAttributes()).isEmpty();
    }

    @Test
    void deleteById_followedBySessionDeletedEvent_unbindsVaadinSessionOnce() {
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(vaadinSession, false);
        Session storedSession = sessionMap.get(id);

        // A store that reports deletions itself (e.g. Hazelcast) publishes the event after the deletion
        repository.deleteById(id);
        context.publishEvent(new SessionDeletedEvent(this, storedSession));

        assertThat(vaadinSession.unboundCount).isEqualTo(1);
        assertThat(nonPersistentAttributes()).isEmpty();
    }

    @Test
    void deleteById_storeReportsDeletionFromAnotherThread_unbindsVaadinSessionInCallingThread() throws Exception {
        // A store that reports deletions from its own thread (e.g. Hazelcast) may report it before deleteById returns
        SessionRepositoryWrapper<MapSession> reportingRepository = new SessionRepositoryWrapper<>(
                new SessionRegistryImpl(), context, new MapSessionRepository(sessionMap) {
            @Override
            public void deleteById(String id) {
                Session deleted = sessionMap.get(id);
                super.deleteById(id);
                Thread storeThread = new Thread(() -> context.publishEvent(new SessionDeletedEvent(this, deleted)));
                storeThread.start();
                try {
                    storeThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        reportingRepository.addAttributePersistenceValidators(new VaadinSessionAttributesValidator());
        context.addApplicationListener(reportingRepository);
        TestVaadinSession vaadinSession = new TestVaadinSession();
        SessionRepositoryWrapper<MapSession>.SessionWrapper session = reportingRepository.createSession();
        session.setAttribute(VAADIN_SESSION_KEY, vaadinSession);
        reportingRepository.save(session);

        reportingRepository.deleteById(session.getId());

        // Vaadin keeps a Vaadin session moved to a new HTTP session on login only if it is unbound in the request
        assertThat(vaadinSession.unboundCount).isEqualTo(1);
        assertThat(vaadinSession.unboundThread).isSameAs(Thread.currentThread());
    }

    @Test
    void deleteById_unbindingFails_stillDeletesSession() {
        HttpSessionBindingListener failingVaadinSession = new HttpSessionBindingListener() {
            @Override
            public void valueUnbound(HttpSessionBindingEvent event) {
                throw new IllegalStateException("Unbinding failure");
            }
        };
        SessionRepositoryWrapper<MapSession>.SessionWrapper session = repository.createSession();
        session.setAttribute(VAADIN_SESSION_KEY, failingVaadinSession);
        repository.save(session);

        assertThatThrownBy(() -> repository.deleteById(session.getId()))
                .isInstanceOf(IllegalStateException.class);

        // An invalidated session must not stay usable
        assertThat(sessionMap).doesNotContainKey(session.getId());
        assertThat(nonPersistentAttributes()).isEmpty();
    }

    @Test
    void cleanUpExpiredSessions_listenerFails_unbindsVaadinSessionsOfAllSessions() {
        context.addApplicationListener((SessionExpiredEvent event) -> {
            throw new IllegalStateException("Listener failure");
        });
        TestVaadinSession vaadinSession1 = new TestVaadinSession();
        TestVaadinSession vaadinSession2 = new TestVaadinSession();
        saveSession(vaadinSession1, true);
        saveSession(vaadinSession2, true);

        sessionMap.cleanUpExpiredSessions();

        assertThat(sessionMap).isEmpty();
        assertThat(vaadinSession1.unboundCount).isEqualTo(1);
        assertThat(vaadinSession2.unboundCount).isEqualTo(1);
    }

    @Test
    void findById_expiredSessionAndListenerFails_returnsNullAndUnbindsVaadinSession() {
        context.addApplicationListener((SessionExpiredEvent event) -> {
            throw new IllegalStateException("Listener failure");
        });
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(vaadinSession, true);

        assertThat(repository.findById(id)).isNull();

        assertThat(vaadinSession.unboundCount).isEqualTo(1);
        assertThat(nonPersistentAttributes()).isEmpty();
    }

    private String saveSession(TestVaadinSession vaadinSession, boolean expired) {
        SessionRepositoryWrapper<MapSession>.SessionWrapper session = repository.createSession();
        session.setAttribute(VAADIN_SESSION_KEY, vaadinSession);
        if (expired) {
            session.setMaxInactiveInterval(Duration.ofMinutes(1));
            session.setLastAccessedTime(Instant.now().minus(Duration.ofHours(1)));
        }
        repository.save(session);
        return session.getId();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> nonPersistentAttributes() {
        return (Map<String, Map<String, Object>>) ReflectionTestUtils.getField(repository,
                "nonPersistentSessionAttributesMap");
    }

    static class TestVaadinSession implements HttpSessionBindingListener {

        int unboundCount;
        Thread unboundThread;

        @Override
        public void valueUnbound(HttpSessionBindingEvent event) {
            unboundCount++;
            unboundThread = Thread.currentThread();
        }
    }
}
