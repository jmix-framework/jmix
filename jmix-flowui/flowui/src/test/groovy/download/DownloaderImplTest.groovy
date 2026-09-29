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

package download

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.server.Command
import io.jmix.flowui.asynctask.UiAsyncTasks
import io.jmix.flowui.download.DownloaderImpl
import io.jmix.flowui.download.SupportDownloadSuccessHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import test_support.spec.FlowuiTestSpecification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@SpringBootTest
class DownloaderImplTest extends FlowuiTestSpecification {

    @Autowired
    ApplicationContext applicationContext

    def "download cleanup task is cancelled when the download anchor is detached"() {
        setupSynchronousUi()

        DownloaderImpl downloader = applicationContext.getBean("flowui_Downloader", DownloaderImpl.class)

        def anchor = new Anchor()
        def context = new SupportDownloadSuccessHandler.DownloadSuccessContext(
                anchor, "report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

        def downloaderLogger = (Logger) LoggerFactory.getLogger(DownloaderImpl.class)
        def asyncTasksLogger = (Logger) LoggerFactory.getLogger(UiAsyncTasks.class)
        def downloaderAppender = new ListAppender<ILoggingEvent>()
        def asyncTasksAppender = new ListAppender<ILoggingEvent>()
        def originalDownloaderLevel = downloaderLogger.getLevel()
        def originalAsyncTasksLevel = asyncTasksLogger.getLevel()
        downloaderLogger.setLevel(Level.DEBUG)
        asyncTasksLogger.setLevel(Level.DEBUG)
        downloaderAppender.start()
        asyncTasksAppender.start()
        downloaderLogger.addAppender(downloaderAppender)
        asyncTasksLogger.addAppender(asyncTasksAppender)

        when: "a download has finished and its delayed cleanup task is scheduled"
        invokeFileDownloaderRemoveHandler(downloader, context)

        and: "the UI is detached before the 60 seconds cleanup delay elapses"
        ComponentUtil.onComponentDetach(anchor)

        then: "the cleanup task is cancelled via its owner and nothing is logged at error level"
        waitForLogEvents(asyncTasksAppender, "UI async task cancelled")
        downloaderAppender.list.every { it.level != Level.ERROR }
        asyncTasksAppender.list.every { it.level != Level.ERROR }

        cleanup:
        downloaderLogger.detachAppender(downloaderAppender)
        asyncTasksLogger.detachAppender(asyncTasksAppender)
        downloaderLogger.setLevel(originalDownloaderLevel)
        asyncTasksLogger.setLevel(originalAsyncTasksLevel)
    }

    protected void invokeFileDownloaderRemoveHandler(DownloaderImpl downloader,
                                                     SupportDownloadSuccessHandler.DownloadSuccessContext context) {
        def method = DownloaderImpl.class.getDeclaredMethod(
                "fileDownloaderRemoveHandler", SupportDownloadSuccessHandler.DownloadSuccessContext.class)
        method.accessible = true
        method.invoke(downloader, context)
    }

    protected void setupSynchronousUi() {
        def syncUi = new SynchronousUi()
        syncUi.getInternals().setSession(vaadinSession)
        UI.setCurrent(syncUi)
    }

    protected static boolean waitForLogEvents(ListAppender<ILoggingEvent> appender, String messagePart) {
        def deadline = System.currentTimeMillis() + 5000
        while (!appender.list.any { it.formattedMessage.contains(messagePart) }
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        return appender.list.any { it.formattedMessage.contains(messagePart) }
    }

    static class SynchronousUi extends UI {
        @Override
        Future<Void> access(Command command) {
            command.execute()
            return CompletableFuture.completedFuture(null)
        }
    }
}
