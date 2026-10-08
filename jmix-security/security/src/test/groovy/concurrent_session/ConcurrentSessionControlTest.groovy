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

package concurrent_session

import io.jmix.core.session.SessionProperties
import io.jmix.security.SecurityConfiguration
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class ConcurrentSessionControlTest extends Specification {

    def "logging in with maximum-sessions-per-user=1 keeps the new session and expires only the previous one"() {
        given: "a session control strategy limited to a single session per user"
        def sessionRegistry = new SessionRegistryImpl()
        def strategy = sessionControlStrategy(sessionRegistry, 1)

        and: "a user who already has an active session"
        def principal = "user1"
        def authentication = new UsernamePasswordAuthenticationToken(principal, "password")
        sessionRegistry.registerNewSession("previous", principal)

        and: "the same user logging in from a new session"
        def request = requestWithSession("new")
        def response = new MockHttpServletResponse()

        when: "the session authentication strategy is applied for the new login"
        strategy.onAuthentication(authentication, request, response)

        then: "the previous session is expired"
        sessionRegistry.getSessionInformation("previous")?.expired

        and: "the new session stays active"
        def newSession = sessionRegistry.getSessionInformation("new")
        newSession != null
        !newSession.expired
    }

    def "logging in above the limit expires only the oldest sessions and keeps the new one"() {
        given: "a session control strategy limited to two sessions per user"
        def sessionRegistry = new SessionRegistryImpl()
        def strategy = sessionControlStrategy(sessionRegistry, 2)

        and: "a user who already has two active sessions"
        def principal = "user1"
        def authentication = new UsernamePasswordAuthenticationToken(principal, "password")
        sessionRegistry.registerNewSession("oldest", principal)
        sessionRegistry.registerNewSession("newer", principal)

        and: "the same user logging in from a new session"
        def request = requestWithSession("new")
        def response = new MockHttpServletResponse()

        when: "the session authentication strategy is applied for the new login"
        strategy.onAuthentication(authentication, request, response)

        then: "only the single oldest session is expired"
        sessionRegistry.getSessionInformation("oldest")?.expired
        !sessionRegistry.getSessionInformation("newer").expired

        and: "the new session stays active"
        def newSession = sessionRegistry.getSessionInformation("new")
        newSession != null
        !newSession.expired
    }

    private static SessionAuthenticationStrategy sessionControlStrategy(SessionRegistry sessionRegistry, int maxSessions) {
        def configuration = new SecurityConfiguration()
        ReflectionTestUtils.setField(configuration, "sessionProperties", new SessionProperties(maxSessions))
        return configuration.sessionControlAuthenticationStrategy(sessionRegistry)
    }

    private static MockHttpServletRequest requestWithSession(String sessionId) {
        def request = new MockHttpServletRequest()
        request.setSession(new MockHttpSession(null, sessionId))
        return request
    }
}
