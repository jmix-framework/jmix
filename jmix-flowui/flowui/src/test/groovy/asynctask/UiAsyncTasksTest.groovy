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

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.server.Command
import io.jmix.flowui.asynctask.UiAsyncTasks
import org.slf4j.LoggerFactory
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

    def "default exception handler logs UIDetachedException at debug instead of error"() {
        setupDetachedUi()

        def resultHandlerInvoked = new CountDownLatch(1)

        def logger = (Logger) LoggerFactory.getLogger(UiAsyncTasks.class)
        def appender = new ListAppender<ILoggingEvent>()
        def originalLevel = logger.getLevel()
        logger.setLevel(Level.DEBUG)
        appender.start()
        logger.addAppender(appender)

        when: "a task without custom exception handler completes after the UI was detached"
        uiAsyncTasks.runnableConfigurer(() -> { })
                .withResultHandler({ resultHandlerInvoked.countDown() })
                .runAsync()

        then: "the expected lifecycle event is logged at debug, and no error is logged"
        waitForLogEvents(appender)
        resultHandlerInvoked.count == 1L
        appender.list.any { it.level == Level.DEBUG && it.formattedMessage.contains("UI was detached") }
        appender.list.every { it.level != Level.ERROR }

        cleanup:
        logger.detachAppender(appender)
        logger.setLevel(originalLevel)
    }

    def "default exception handler logs cancellation at debug instead of error"() {
        setupSynchronousUi()

        def owner = new Div()
        def taskStarted = new CountDownLatch(1)
        def finishTask = new CountDownLatch(1)

        def logger = (Logger) LoggerFactory.getLogger(UiAsyncTasks.class)
        def appender = new ListAppender<ILoggingEvent>()
        def originalLevel = logger.getLevel()
        logger.setLevel(Level.DEBUG)
        appender.start()
        logger.addAppender(appender)

        when: "a running task without custom exception handler is cancelled via owner detach"
        uiAsyncTasks.runnableConfigurer(() -> {
            taskStarted.countDown()
            finishTask.await(5, TimeUnit.SECONDS)
        })
                .withOwner(owner)
                .runAsync()

        and:
        taskStarted.await(5, TimeUnit.SECONDS)
        ComponentUtil.onComponentDetach(owner)

        then: "the cancellation is logged at debug, and no error is logged"
        waitForLogEvents(appender)
        appender.list.any { it.level == Level.DEBUG && it.formattedMessage.contains("cancelled") }
        appender.list.every { it.level != Level.ERROR }

        cleanup:
        finishTask.countDown()
        logger.detachAppender(appender)
        logger.setLevel(originalLevel)
    }

    protected void setupSynchronousUi() {
        def syncUi = new SynchronousUi()
        syncUi.getInternals().setSession(vaadinSession)
        UI.setCurrent(syncUi)
    }

    protected void setupDetachedUi() {
        def detachedUi = new UI()
        detachedUi.getInternals().setSession(vaadinSession)
        detachedUi.getInternals().setSession(null)
        UI.setCurrent(detachedUi)
    }

    protected static boolean waitForLogEvents(ListAppender<ILoggingEvent> appender) {
        def deadline = System.currentTimeMillis() + 5000
        while (appender.list.isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        return !appender.list.isEmpty()
    }

    static class SynchronousUi extends UI {
        @Override
        Future<Void> access(Command command) {
            command.execute()
            return CompletableFuture.completedFuture(null)
        }
    }
}
