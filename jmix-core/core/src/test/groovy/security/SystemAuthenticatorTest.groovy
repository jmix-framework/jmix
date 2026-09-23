/*
 * Copyright 2019 Haulmont.
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

package security

import io.jmix.core.CoreConfiguration
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticationToken
import io.jmix.core.security.SystemAuthenticator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import org.springframework.security.web.context.HttpRequestResponseHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import test_support.base.TestBaseConfiguration

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
class SystemAuthenticatorTest extends Specification {

    @Autowired
    SystemAuthenticator authenticator

    @Autowired
    InMemoryUserRepository userRepository

    UserDetails admin

    def setup() {
        admin = User.builder()
                .username('admin')
                .password('{noop}admin123')
                .authorities(Collections.emptyList())
                .build()
        userRepository.addUser(admin)
    }

    def cleanup() {
        userRepository.removeUser(admin)
    }


    def "authenticate as system"() {
        when:

        authenticator.begin()

        then:

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        authentication instanceof SystemAuthenticationToken
        authentication.principal instanceof UserDetails
        ((UserDetails) authentication.principal).username == 'system'

        when:

        authenticator.end()

        then:

        SecurityContextHolder.getContext().getAuthentication() == null
    }

    def "authenticate as admin"() {
        when:

        authenticator.begin('admin')

        then:

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        authentication instanceof SystemAuthenticationToken
        authentication.principal instanceof UserDetails
        ((UserDetails) authentication.principal).username == 'admin'

        when:

        authenticator.end()

        then:

        SecurityContextHolder.getContext().getAuthentication() == null
    }

    def "nested authentication"() {

        when: "outer auth"

        authenticator.begin()

        then:

        Authentication outerAuth = SecurityContextHolder.getContext().getAuthentication()
        outerAuth instanceof SystemAuthenticationToken
        outerAuth.principal instanceof UserDetails
        ((UserDetails) outerAuth.principal).username == 'system'

        when: "inner auth"

        authenticator.begin('admin')

        then:

        Authentication innerAuth = SecurityContextHolder.getContext().getAuthentication()
        innerAuth instanceof SystemAuthenticationToken
        innerAuth.principal instanceof UserDetails
        ((UserDetails) innerAuth.principal).username == 'admin'

        when: "end inner"

        authenticator.end()

        then:

        Authentication outerAuth1 = SecurityContextHolder.getContext().getAuthentication()
        outerAuth1 instanceof SystemAuthenticationToken
        outerAuth1.principal instanceof UserDetails
        ((UserDetails) outerAuth1.principal).username == 'system'

        when: "end outer"

        authenticator.end()

        then:

        SecurityContextHolder.getContext().getAuthentication() == null
    }

    def "begin and end do not modify the previously current SecurityContext instance"() {
        given: "a context of a logged-in user is current, as on a UI request thread"
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(admin, null, admin.authorities)
        SecurityContext original = SecurityContextHolder.createEmptyContext()
        original.setAuthentication(adminAuth)
        SecurityContextHolder.setContext(original)

        when:
        authenticator.begin()

        then: "the current thread sees system, but the original context object is untouched"
        SecurityContextHolder.getContext().getAuthentication() instanceof SystemAuthenticationToken
        original.getAuthentication().is(adminAuth)

        when:
        authenticator.end()

        then: "the original context instance is current again"
        SecurityContextHolder.getContext().is(original)
        SecurityContextHolder.getContext().getAuthentication().is(adminAuth)

        cleanup:
        SecurityContextHolder.clearContext()
    }

    def "begin on one thread is not visible to another thread sharing the same SecurityContext instance"() {
        given: "two threads hold the same context instance, as a UI thread and an async task do"
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(admin, null, admin.authorities)
        SecurityContext shared = SecurityContextHolder.createEmptyContext()
        shared.setAuthentication(adminAuth)
        SecurityContextHolder.setContext(shared)
        ExecutorService otherThread = Executors.newSingleThreadExecutor()
        otherThread.submit { SecurityContextHolder.setContext(shared) }.get(5, TimeUnit.SECONDS)

        when:
        authenticator.begin()
        Authentication seenByOtherThread = otherThread.submit({
            SecurityContextHolder.getContext().getAuthentication()
        } as Callable<Authentication>).get(5, TimeUnit.SECONDS)

        then:
        seenByOtherThread.is(adminAuth)

        cleanup:
        authenticator.end()
        otherThread.shutdownNow()
        SecurityContextHolder.clearContext()
    }

    def "overlapping begin and end on two threads leave the shared SecurityContext unchanged"() {
        given:
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(admin, null, admin.authorities)
        SecurityContext shared = SecurityContextHolder.createEmptyContext()
        shared.setAuthentication(adminAuth)
        SecurityContextHolder.setContext(shared)
        ExecutorService otherThread = Executors.newSingleThreadExecutor()
        otherThread.submit { SecurityContextHolder.setContext(shared) }.get(5, TimeUnit.SECONDS)

        when: "T1 begins, T2 begins, T1 ends, T2 ends"
        authenticator.begin()
        otherThread.submit { authenticator.begin() }.get(5, TimeUnit.SECONDS)
        authenticator.end()
        otherThread.submit { authenticator.end() }.get(5, TimeUnit.SECONDS)

        Authentication seenByOtherThread = otherThread.submit({
            SecurityContextHolder.getContext().getAuthentication()
        } as Callable<Authentication>).get(5, TimeUnit.SECONDS)

        then:
        shared.getAuthentication().is(adminAuth)
        SecurityContextHolder.getContext().getAuthentication().is(adminAuth)
        seenByOtherThread.is(adminAuth)

        cleanup:
        otherThread.shutdownNow()
        SecurityContextHolder.clearContext()
    }

    def "context installed by begin is not saved to the HTTP session when the response is committed"() {
        given: "the HTTP session holds the context of a logged-in user, loaded as the security filter does it"
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(admin, null, admin.authorities)
        SecurityContext sessionContext = SecurityContextHolder.createEmptyContext()
        sessionContext.setAuthentication(adminAuth)

        MockHttpServletRequest request = new MockHttpServletRequest()
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sessionContext)

        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository()
        HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, new MockHttpServletResponse())
        SecurityContextHolder.setContext(repository.loadContext(holder))

        when: "the response is committed inside a begin/end block"
        authenticator.begin()
        holder.getResponse().sendRedirect("/")

        then: "the session still holds the user's context, so other requests of the session do not get system"
        SecurityContext stored = request.getSession(false)
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY) as SecurityContext
        stored.is(sessionContext)
        stored.getAuthentication().is(adminAuth)

        cleanup:
        authenticator.end()
        SecurityContextHolder.clearContext()
    }
}
