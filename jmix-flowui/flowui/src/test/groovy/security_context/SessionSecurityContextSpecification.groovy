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

package security_context

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.server.WrappedHttpSession
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.flowui.backgroundtask.BackgroundTaskManager
import io.jmix.flowui.sys.JmixSecurityContextHolderStrategy
import io.jmix.flowui.sys.event.UiEventsManager
import io.jmix.flowui.testassist.vaadin.TestUI
import io.jmix.flowui.testassist.vaadin.TestVaadinRequest
import io.jmix.flowui.testassist.vaadin.TestVaadinSession
import org.apache.commons.lang3.reflect.FieldUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import test_support.spec.FlowuiTestSpecification

/**
 * Runs with {@link JmixSecurityContextHolderStrategy} installed, as in a FlowUI application with Spring Security,
 * and with a Vaadin session whose HTTP session holds the security context of the logged-in user 'admin'.
 */
abstract class SessionSecurityContextSpecification extends FlowuiTestSpecification {

    @Autowired
    InMemoryUserRepository userRepository

    User admin
    Authentication adminAuth
    SecurityContext sessionContext

    @Override
    protected void setupAuthentication() {
        SecurityContextHolder.setContextHolderStrategy(new JmixSecurityContextHolderStrategy())
    }

    @Override
    protected void removeAuthentication() {
        SecurityContextHolder.clearContext()
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL)
    }

    void setup() {
        admin = User.builder().username('admin').password('').authorities(Collections.emptyList()).build()
        userRepository.addUser(admin)
        adminAuth = new UsernamePasswordAuthenticationToken(admin, null, admin.authorities)
        sessionContext = new SecurityContextImpl(adminAuth)
        bindHttpSession(vaadinSession, sessionContext)
    }

    void cleanup() {
        userRepository.removeUser(admin)
        VaadinSession.setCurrent(vaadinSession)
        UI.setCurrent(ui)
    }

    /**
     * Gives the Vaadin session an HTTP session that holds the given security context under the key used by
     * Spring Security, so that the Vaadin-aware strategy finds it.
     */
    protected static void bindHttpSession(VaadinSession session, SecurityContext securityContext) {
        def httpSession = new MockHttpSession()
        httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext)
        // VaadinSession.refreshTransients() asserts that the session lock is held, which the test session
        // does not model, so the wrapped session is set directly.
        FieldUtils.writeField(session, "session", new WrappedHttpSession(httpSession), true)
    }

    /**
     * Creates a Vaadin session that runs tasks queued by {@code session.access()} as soon as the lock is released,
     * as a real session does. {@link TestVaadinSession#unlock()} is a no-op, so such tasks would never run there.
     */
    protected VaadinSession createSessionWithImmediateAccess(SecurityContext securityContext) {
        VaadinService service = vaadinSession.getService()
        VaadinSession session = new TestVaadinSession(service) {
            @Override
            void unlock() {
                service.runPendingAccessTasks(this)
            }
        }
        bindHttpSession(session, securityContext)
        session.setAttribute(BackgroundTaskManager, new BackgroundTaskManager())
        session.setAttribute(UiEventsManager, new UiEventsManager())
        return session
    }

    protected UI createUi(VaadinSession session, int uiId) {
        def newUi = new TestUI()
        newUi.getInternals().setSession(session)
        newUi.doInit(new TestVaadinRequest(session.getService()), uiId, "testAppId" + uiId)
        session.addUI(newUi)
        return newUi
    }
}
