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
import io.jmix.flowui.sys.event.UiEventsManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
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

    /**
     * A recipient session whose HTTP session holds the recipient's security context, with one UI and an event
     * listener that records the authentication it ran under.
     */
    private RecipientSession recipientSession() {
        def result = new RecipientSession()
        result.user = User.builder().username('recipient').password('').authorities(Collections.emptyList()).build()
        result.auth = new UsernamePasswordAuthenticationToken(result.user, null, result.user.authorities)
        userRepository.addUser(result.user)

        result.session = createSessionWithImmediateAccess(new SecurityContextImpl(result.auth))
        def recipientUi = createUi(result.session, 2)

        result.session.getAttribute(UiEventsManager).addApplicationListener(recipientUi, { e ->
            result.handlerAuthentication = SecurityContextHolder.getContext().getAuthentication()
        } as ApplicationListener<TestUiEvent>)
        return result
    }

    private static class RecipientSession {
        VaadinSession session
        User user
        Authentication auth
        Authentication handlerAuthentication
    }
}
