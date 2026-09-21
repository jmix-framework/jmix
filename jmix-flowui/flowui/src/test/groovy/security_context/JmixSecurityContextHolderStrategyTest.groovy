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

import com.vaadin.flow.server.VaadinSession
import io.jmix.core.security.SystemAuthenticationToken
import io.jmix.flowui.UiEventPublisher
import io.jmix.flowui.asynctask.DelegatingSecurityRunnable
import io.jmix.flowui.asynctask.DelegatingSecuritySupplier
import io.jmix.flowui.sys.event.UiEventsManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.userdetails.User
import ui_events.TestUiEvent

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
class JmixSecurityContextHolderStrategyTest extends SessionSecurityContextSpecification {

    @Autowired
    UiEventPublisher uiEventPublisher

    def "on a UI thread the session context is current"() {
        expect:
        VaadinSession.getCurrent().is(vaadinSession)
        SecurityContextHolder.getContext().is(sessionContext)
    }

    def "withSystem takes effect on a UI thread and leaves the session context untouched"() {
        when:
        Authentication inside = systemAuthenticator.withSystem {
            SecurityContextHolder.getContext().getAuthentication()
        }

        then:
        inside instanceof SystemAuthenticationToken
        inside.getName() == 'system'
        sessionContext.getAuthentication().is(adminAuth)
        SecurityContextHolder.getContext().is(sessionContext)
    }

    def "begin on a UI thread is not visible to another thread of the same session"() {
        given:
        ExecutorService otherThread = Executors.newSingleThreadExecutor()

        when:
        systemAuthenticator.begin()
        Authentication seenByOtherThread = otherThread.submit {
            VaadinSession.setCurrent(vaadinSession)
            try {
                return SecurityContextHolder.getContext().getAuthentication()
            } finally {
                VaadinSession.setCurrent(null)
            }
        }.get(5, TimeUnit.SECONDS)

        then:
        seenByOtherThread.is(adminAuth)

        cleanup:
        systemAuthenticator.end()
        otherThread.shutdownNow()
    }

    def "clearContext removes an override left by begin without end"() {
        when:
        systemAuthenticator.begin()
        SecurityContextHolder.clearContext()

        then:
        SecurityContextHolder.getContext().getAuthentication().is(adminAuth)

        when: "the late end does not throw"
        systemAuthenticator.end()

        then:
        SecurityContextHolder.getContext().getAuthentication().is(adminAuth)
    }

    def "without a Vaadin session the strategy behaves as a thread-local strategy"() {
        given:
        VaadinSession.setCurrent(null)
        SecurityContext threadContext = new SecurityContextImpl(adminAuth)
        SecurityContextHolder.setContext(threadContext)

        when:
        Authentication inside = systemAuthenticator.withSystem {
            SecurityContextHolder.getContext().getAuthentication()
        }

        then:
        inside instanceof SystemAuthenticationToken
        SecurityContextHolder.getContext().is(threadContext)
        threadContext.getAuthentication().is(adminAuth)
    }

    def "UI event handlers of a recipient session run under that session's own authentication"() {
        given:
        def recipient = recipientSession()
        def event = new TestUiEvent(this, "eventMessage")

        when: "the event is sent from the admin session"
        uiEventPublisher.sendEventToUserSessions(event, ['recipient': [recipient.session]])

        then: "the handler ran under the recipient's own session authentication"
        recipient.handlerAuthentication.is(recipient.auth)

        and: "the sender's context was not modified"
        sessionContext.getAuthentication().is(adminAuth)
        SecurityContextHolder.getContext().is(sessionContext)

        cleanup:
        userRepository.removeUser(recipient.user)
    }

    def "UI event handlers of a recipient session run under that session's own authentication when sent from withSystem"() {
        given:
        def recipient = recipientSession()
        def event = new TestUiEvent(this, "eventMessage")

        when: "the event is sent from a system block, as a scheduled job would do"
        systemAuthenticator.runWithSystem {
            uiEventPublisher.sendEventToUserSessions(event, ['recipient': [recipient.session]])
        }

        then:
        recipient.handlerAuthentication.is(recipient.auth)
        SecurityContextHolder.getContext().is(sessionContext)

        cleanup:
        userRepository.removeUser(recipient.user)
    }

    def "test nested #wrapper preserves system authentication and restores the caller (UI session: #withSession)"() {
        given:
        if (!withSession) {
            VaadinSession.setCurrent(null)
            SecurityContextHolder.setContext(sessionContext)
        }
        Authentication inside = null
        Authentication afterWrapper = null

        when:
        systemAuthenticator.runWithSystem {
            Runnable action = { inside = SecurityContextHolder.getContext().authentication }
            switch (wrapper) {
                case 'runnable':
                    new DelegatingSecurityRunnable(action).run()
                    break
                case 'supplier':
                    new DelegatingSecuritySupplier({ action.run() }).get()
                    break
                case 'Spring runnable':
                    new DelegatingSecurityContextRunnable(action).run()
                    break
            }
            afterWrapper = SecurityContextHolder.getContext().authentication
        }

        then:
        inside instanceof SystemAuthenticationToken
        afterWrapper.is(inside)
        SecurityContextHolder.getContext().is(sessionContext)
        sessionContext.authentication.is(adminAuth)

        where:
        [wrapper, withSession] << [['runnable', 'supplier', 'Spring runnable'], [true, false]].combinations()
    }

    def "test queued UI event uses recipient authentication when processed inside withSystem"() {
        given:
        def recipient = recipientSession(false)
        def worker = Executors.newSingleThreadExecutor()
        VaadinSession.setCurrent(null)
        recipient.session.lock()

        when:
        worker.submit {
            uiEventPublisher.sendEventToUserSessions(new TestUiEvent(this, 'queued'),
                    ['recipient': [recipient.session]])
        }.get(5, TimeUnit.SECONDS)

        then:
        recipient.handlerAuthentication == null

        when:
        systemAuthenticator.runWithSystem {
            recipient.session.unlock()
            assert SecurityContextHolder.getContext().authentication instanceof SystemAuthenticationToken
        }

        then:
        recipient.handlerAuthentication.is(recipient.auth)

        cleanup:
        if (recipient.session.hasLock()) {
            recipient.session.unlock()
        }
        worker.shutdownNow()
        userRepository.removeUser(recipient.user)
    }

    def "test explicit wrapper context restores nested authentication after an exception"() {
        when:
        systemAuthenticator.runWithSystem {
            SecurityContext systemContext = SecurityContextHolder.getContext()
            systemAuthenticator.runWithUser('admin') {
                SecurityContext userContext = SecurityContextHolder.getContext()
                try {
                    new DelegatingSecurityRunnable({
                        assert SecurityContextHolder.getContext().is(sessionContext)
                        throw new IllegalStateException('task failed')
                    }, sessionContext).run()
                    assert false: 'The task must throw'
                } catch (IllegalStateException ignored) {
                    assert SecurityContextHolder.getContext().is(userContext)
                }
            }
            assert SecurityContextHolder.getContext().is(systemContext)
        }

        then:
        SecurityContextHolder.getContext().is(sessionContext)
        sessionContext.authentication.is(adminAuth)
    }

    def "test deferred context replacement preserves the enclosing authentication scope"() {
        given:
        VaadinSession.setCurrent(null)
        SecurityContextHolder.setContext(sessionContext)

        when:
        systemAuthenticator.runWithSystem {
            def previous = SecurityContextHolder.getDeferredContext()
            try {
                SecurityContextHolder.setDeferredContext { sessionContext }
                assert SecurityContextHolder.getContext().is(sessionContext)
            } finally {
                SecurityContextHolder.setDeferredContext(previous)
            }
            assert SecurityContextHolder.getContext().authentication instanceof SystemAuthenticationToken
        }

        then:
        SecurityContextHolder.getContext().is(sessionContext)
    }

    def "test #wrapper clears unfinished system scopes on a pooled thread"() {
        given:
        ExecutorService worker = Executors.newSingleThreadExecutor()
        Runnable action = {
            systemAuthenticator.begin()
            systemAuthenticator.begin()
        }
        def task = wrapper == 'runnable' ? new DelegatingSecurityRunnable(action) :
                new DelegatingSecuritySupplier({ action.run() })

        when:
        worker.submit {
            if (task instanceof Runnable) {
                task.run()
            } else {
                task.get()
            }
        }.get(5, TimeUnit.SECONDS)
        Authentication afterLateEnd = worker.submit {
            systemAuthenticator.end()
            return SecurityContextHolder.getContext().authentication
        }.get(5, TimeUnit.SECONDS)

        then:
        afterLateEnd == null
        sessionContext.authentication.is(adminAuth)

        cleanup:
        worker.submit {
            systemAuthenticator.end()
            SecurityContextHolder.clearContext()
        }.get(5, TimeUnit.SECONDS)
        worker.shutdownNow()

        where:
        wrapper << ['runnable', 'supplier']
    }

    /**
     * A recipient session whose HTTP session holds the recipient's security context, with one UI and an event
     * listener that records the authentication it ran under.
     */
    private RecipientSession recipientSession(boolean immediateAccess = true) {
        def result = new RecipientSession()
        result.user = User.builder().username('recipient').password('').authorities(Collections.emptyList()).build()
        result.auth = new UsernamePasswordAuthenticationToken(result.user, null, result.user.authorities)
        userRepository.addUser(result.user)

        def context = new SecurityContextImpl(result.auth)
        result.session = immediateAccess ? createSessionWithImmediateAccess(context) : createSessionWithLock(context)
        result.session.lock()
        try {
            result.session.setAttribute(UiEventsManager, new UiEventsManager())
            def recipientUi = createUi(result.session, 2)
            result.session.getAttribute(UiEventsManager).addApplicationListener(recipientUi, { e ->
                result.handlerAuthentication = SecurityContextHolder.getContext().getAuthentication()
            } as ApplicationListener<TestUiEvent>)
        } finally {
            result.session.unlock()
        }
        return result
    }

    private static class RecipientSession {
        VaadinSession session
        User user
        Authentication auth
        Authentication handlerAuthentication
    }
}
