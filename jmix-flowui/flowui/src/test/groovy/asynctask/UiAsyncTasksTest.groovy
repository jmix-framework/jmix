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

import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.server.Command
import io.jmix.flowui.asynctask.UiAsyncTasks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@SpringBootTest
class UiAsyncTasksTest extends FlowuiTestSpecification {

    @Autowired
    UiAsyncTasks uiAsyncTasks

    def "supplier task future is cancelled when owner is detached and exception handler is invoked"() {
        setupSynchronousUi()

        def owner = new Div()
        def taskStarted = new CountDownLatch(1)
        def finishTask = new CountDownLatch(1)
        def exceptionHandled = new CountDownLatch(1)
        def handledException = new AtomicReference<Throwable>()

        when:
        CompletableFuture<Void> future = uiAsyncTasks
                .supplierConfigurer(() -> {
                    taskStarted.countDown()
                    finishTask.await(5, TimeUnit.SECONDS)
                    return "result"
                })
                .withExceptionHandler(throwable -> {
                    handledException.set(throwable)
                    exceptionHandled.countDown()
                })
                .withOwner(owner)
                .supplyAsync()

        then:
        taskStarted.await(5, TimeUnit.SECONDS)

        when:
        ComponentUtil.onComponentDetach(owner)

        then:
        future.isCancelled()
        exceptionHandled.await(5, TimeUnit.SECONDS)
        handledException.get() instanceof CancellationException

        cleanup:
        finishTask.countDown()
    }

    def "runnable task future is cancelled when owner is detached and exception handler is invoked"() {
        setupSynchronousUi()

        def owner = new Div()
        def taskStarted = new CountDownLatch(1)
        def finishTask = new CountDownLatch(1)
        def exceptionHandled = new CountDownLatch(1)
        def handledException = new AtomicReference<Throwable>()

        when:
        CompletableFuture<Void> future = uiAsyncTasks
                .runnableConfigurer(() -> {
                    taskStarted.countDown()
                    finishTask.await(5, TimeUnit.SECONDS)
                })
                .withExceptionHandler(throwable -> {
                    handledException.set(throwable)
                    exceptionHandled.countDown()
                })
                .withOwner(owner)
                .runAsync()

        then:
        taskStarted.await(5, TimeUnit.SECONDS)

        when:
        ComponentUtil.onComponentDetach(owner)

        then:
        future.isCancelled()
        exceptionHandled.await(5, TimeUnit.SECONDS)
        handledException.get() instanceof CancellationException

        cleanup:
        finishTask.countDown()
    }

    protected void setupSynchronousUi() {
        def syncUi = new SynchronousUi()
        syncUi.getInternals().setSession(vaadinSession)
        UI.setCurrent(syncUi)
    }

    static class SynchronousUi extends UI {
        @Override
        Future<Void> access(Command command) {
            command.execute()
            return CompletableFuture.completedFuture(null)
        }
    }
}
