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
import com.vaadin.flow.server.VaadinSession
import io.jmix.core.security.SystemAuthenticationToken
import io.jmix.flowui.backgroundtask.BackgroundTask
import io.jmix.flowui.backgroundtask.BackgroundWorker
import io.jmix.flowui.backgroundtask.TaskLifeCycle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Background tasks are started from a UI thread of the 'admin' session. The task runs on a worker thread,
 * while progress and done handlers run through {@code ui.access()} inside the session.
 */
@SpringBootTest
class BackgroundTaskSecurityContextTest extends SessionSecurityContextSpecification {

    @Autowired
    BackgroundWorker backgroundWorker

    VaadinSession taskSession
    UI taskUi

    void setup() {
        taskSession = createSessionWithImmediateAccess(sessionContext)
        taskUi = createUi(taskSession, 3)
        VaadinSession.setCurrent(taskSession)
        UI.setCurrent(taskUi)
    }

    def "task body runs under the user's authentication and withSystem inside the task does not affect the session"() {
        given:
        def bodyAuth = new AtomicReference<Authentication>()
        def insideSystemAuth = new AtomicReference<Authentication>()
        def sessionAuthDuringSystem = new AtomicReference<Authentication>()
        def done = new CountDownLatch(1)
        def task = new BackgroundTask<Integer, Void>(10) {
            @Override
            Void run(TaskLifeCycle<Integer> lifeCycle) {
                bodyAuth.set(SecurityContextHolder.getContext().getAuthentication())
                systemAuthenticator.runWithSystem {
                    insideSystemAuth.set(SecurityContextHolder.getContext().getAuthentication())
                    sessionAuthDuringSystem.set(sessionContext.getAuthentication())
                }
                return null
            }

            @Override
            void done(Void result) {
                done.countDown()
            }
        }

        when:
        backgroundWorker.handle(task).execute()

        then:
        done.await(10, TimeUnit.SECONDS)
        bodyAuth.get().is(adminAuth)
        insideSystemAuth.get() instanceof SystemAuthenticationToken
        sessionAuthDuringSystem.get().is(adminAuth)
    }

    def "progress handler runs under the user's authentication even when published from inside withSystem"() {
        given:
        def progressAuth = new AtomicReference<Authentication>()
        def done = new CountDownLatch(1)
        def task = new BackgroundTask<Integer, Void>(10) {
            @Override
            Void run(TaskLifeCycle<Integer> lifeCycle) {
                systemAuthenticator.runWithSystem {
                    lifeCycle.publish(1)
                }
                return null
            }

            @Override
            void progress(List<Integer> changes) {
                progressAuth.set(SecurityContextHolder.getContext().getAuthentication())
            }

            @Override
            void done(Void result) {
                done.countDown()
            }
        }

        when:
        backgroundWorker.handle(task).execute()

        then:
        done.await(10, TimeUnit.SECONDS)
        progressAuth.get().is(adminAuth)
    }

    def "done handler does not modify the session security context when the task was started under withSystem"() {
        given:
        def doneAuth = new AtomicReference<Authentication>()
        def sessionAuthDuringDone = new AtomicReference<Authentication>()
        def done = new CountDownLatch(1)
        def task = new BackgroundTask<Integer, Void>(10) {
            @Override
            Void run(TaskLifeCycle<Integer> lifeCycle) {
                return null
            }

            @Override
            void done(Void result) {
                doneAuth.set(SecurityContextHolder.getContext().getAuthentication())
                sessionAuthDuringDone.set(sessionContext.getAuthentication())
                done.countDown()
            }
        }

        when: "the task is started from a system block on the UI thread"
        systemAuthenticator.runWithSystem {
            backgroundWorker.handle(task).execute()
        }

        then: "the done handler runs under the authentication the task was started with"
        done.await(10, TimeUnit.SECONDS)
        doneAuth.get() instanceof SystemAuthenticationToken

        and: "the session's shared context is never modified"
        sessionAuthDuringDone.get().is(adminAuth)
        sessionContext.getAuthentication().is(adminAuth)
    }
}
