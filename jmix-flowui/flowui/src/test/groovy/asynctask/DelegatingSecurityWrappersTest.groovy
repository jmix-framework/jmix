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

package asynctask

import io.jmix.flowui.asynctask.DelegatingSecurityRunnable
import io.jmix.flowui.asynctask.DelegatingSecuritySupplier
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DelegatingSecurityWrappersTest extends Specification {

    Authentication userAuth = new TestingAuthenticationToken('user', 'pw')
    Authentication otherAuth = new TestingAuthenticationToken('other', 'pw')
    SecurityContext callerContext = new SecurityContextImpl(userAuth)
    ExecutorService worker = Executors.newSingleThreadExecutor()

    def setup() {
        SecurityContextHolder.setContext(callerContext)
    }

    def cleanup() {
        worker.shutdownNow()
        SecurityContextHolder.clearContext()
    }

    def "runnable runs with the caller's authentication but changes to the context stay on the worker thread"() {
        given:
        Authentication seen = null
        def runnable = new DelegatingSecurityRunnable({
            seen = SecurityContextHolder.getContext().getAuthentication()
            SecurityContextHolder.getContext().setAuthentication(otherAuth)
        })

        when:
        worker.submit(runnable).get(5, TimeUnit.SECONDS)

        then:
        seen.is(userAuth)
        callerContext.getAuthentication().is(userAuth)
    }

    def "supplier runs with the caller's authentication but changes to the context stay on the worker thread"() {
        given:
        def supplier = new DelegatingSecuritySupplier({
            Authentication seen = SecurityContextHolder.getContext().getAuthentication()
            SecurityContextHolder.getContext().setAuthentication(otherAuth)
            return seen
        })

        when:
        Authentication seen = worker.submit({ supplier.get() } as java.util.concurrent.Callable).get(5, TimeUnit.SECONDS)

        then:
        seen.is(userAuth)
        callerContext.getAuthentication().is(userAuth)
    }
}
