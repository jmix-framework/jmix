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
import io.jmix.sessions.SessionsConfiguration;
import non_persistent_attributes.NonPersistentAttributesLifecycleTest.TestVaadinSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.jmix.sessions.validators.VaadinSessionAttributesValidator.VAADIN_SESSION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Checks that the session repository wrapper created by {@link SessionsConfiguration} hears of ended sessions.
 */
class SessionRepositoryFilterWiringTest {

    GenericApplicationContext context;
    Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        context = new GenericApplicationContext();
        context.refresh();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void sessionExpiredEvent_unbindsVaadinSession() {
        SessionRepositoryWrapper<MapSession> repository = createRepository();
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(repository, vaadinSession);

        context.publishEvent(new SessionExpiredEvent(this, sessionMap.get(id)));

        assertThat(vaadinSession.unboundCount).isEqualTo(1);
    }

    @Test
    void sessionExpiredEvent_failingListenerRegisteredEarlier_stillUnbindsVaadinSession() {
        context.addApplicationListener((SessionExpiredEvent event) -> {
            throw new IllegalStateException("Listener failure");
        });
        SessionRepositoryWrapper<MapSession> repository = createRepository();
        TestVaadinSession vaadinSession = new TestVaadinSession();
        String id = saveSession(repository, vaadinSession);

        assertThatThrownBy(() -> context.publishEvent(new SessionExpiredEvent(this, sessionMap.get(id))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(vaadinSession.unboundCount).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    private SessionRepositoryWrapper<MapSession> createRepository() {
        SessionsConfiguration<MapSession> configuration = new SessionsConfiguration<>();
        ReflectionTestUtils.setField(configuration, "sessionIdResolver", new CookieHttpSessionIdResolver());
        ReflectionTestUtils.setField(configuration, "applicationEventPublisher", context);
        ReflectionTestUtils.setField(configuration, "sessionRegistry", new SessionRegistryImpl());
        ReflectionTestUtils.setField(configuration, "applicationContext", context);

        SessionRepositoryFilter<SessionRepositoryWrapper<MapSession>.SessionWrapper> filter =
                configuration.jmixSessionRepositoryFilter(new MapSessionRepository(sessionMap));
        return (SessionRepositoryWrapper<MapSession>) ReflectionTestUtils.getField(filter, "sessionRepository");
    }

    private String saveSession(SessionRepositoryWrapper<MapSession> repository, TestVaadinSession vaadinSession) {
        SessionRepositoryWrapper<MapSession>.SessionWrapper session = repository.createSession();
        session.setAttribute(VAADIN_SESSION_KEY, vaadinSession);
        repository.save(session);
        return session.getId();
    }
}
