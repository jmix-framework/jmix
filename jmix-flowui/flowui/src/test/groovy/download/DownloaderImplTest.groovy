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
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Anchor
import io.jmix.flowui.download.Downloader
import io.jmix.flowui.download.DownloaderImpl
import io.jmix.flowui.download.SupportDownloadSuccessHandler.DownloadSuccessContext
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.TaskScheduler
import test_support.spec.FlowuiTestSpecification

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ScheduledFuture

@SpringBootTest
class DownloaderImplTest extends FlowuiTestSpecification {

    static final Duration REMOVAL_DELAY = Duration.ofSeconds(60)

    TaskScheduler taskScheduler = Mock()
    ScheduledFuture<?> removal = Mock()

    DownloaderImpl downloader
    Runnable scheduledRemoval
    Instant scheduledAt

    @Override
    void setup() {
        downloader = applicationContext.getBean(Downloader) as DownloaderImpl
        downloader.setTaskScheduler(taskScheduler)
    }

    def "successful download schedules the anchor removal in 60 seconds"() {
        def anchor = new Anchor()
        ui.add(anchor)
        def startedAt = Instant.now()

        when:
        downloader.fileDownloaderRemoveHandler(createSuccessContext(anchor))

        then: "the removal is scheduled once, and the anchor stays until it runs"
        1 * taskScheduler.schedule(_ as Runnable, _ as Instant) >> { Runnable task, Instant startTime ->
            scheduledRemoval = task
            scheduledAt = startTime
            removal
        }
        !scheduledAt.isBefore(startedAt.plus(REMOVAL_DELAY))
        !scheduledAt.isAfter(Instant.now().plus(REMOVAL_DELAY))
        anchor.getParent().isPresent()
    }

    def "scheduled removal removes the anchor from the UI"() {
        def anchor = new Anchor()
        ui.add(anchor)

        when:
        downloader.fileDownloaderRemoveHandler(createSuccessContext(anchor))
        runScheduledRemoval()

        then:
        1 * taskScheduler.schedule(_ as Runnable, _ as Instant) >> { Runnable task, Instant startTime ->
            scheduledRemoval = task
            removal
        }
        !anchor.getParent().isPresent()
    }

    def "detaching the anchor before the delay ends cancels the scheduled removal"() {
        def anchor = new Anchor()
        ui.add(anchor)

        when:
        downloader.fileDownloaderRemoveHandler(createSuccessContext(anchor))
        ui.remove(anchor)

        then:
        1 * taskScheduler.schedule(_ as Runnable, _ as Instant) >> removal
        1 * removal.cancel(false)
    }

    def "scheduled removal after the UI is detached logs no error"() {
        def detachedUi = new UI()
        detachedUi.getInternals().setSession(vaadinSession)
        def anchor = new Anchor()
        detachedUi.add(anchor)

        def logger = (Logger) LoggerFactory.getLogger(DownloaderImpl)
        def appender = new ListAppender<ILoggingEvent>()
        def originalLevel = logger.getLevel()
        logger.setLevel(Level.DEBUG)
        appender.start()
        logger.addAppender(appender)

        when: "the UI is closed while the removal is scheduled, and the removal runs anyway"
        downloader.fileDownloaderRemoveHandler(createSuccessContext(anchor))
        detachedUi.getInternals().setSession(null)
        runScheduledRemoval()

        then: "the closed UI is logged at debug, and nothing is thrown or logged as an error"
        1 * taskScheduler.schedule(_ as Runnable, _ as Instant) >> { Runnable task, Instant startTime ->
            scheduledRemoval = task
            removal
        }
        noExceptionThrown()
        appender.list.any { it.level == Level.DEBUG && it.formattedMessage.contains("UI is detached") }
        appender.list.every { it.level != Level.ERROR }

        cleanup:
        logger.detachAppender(appender)
        logger.setLevel(originalLevel)
    }

    def "detached anchor schedules nothing"() {
        def anchor = new Anchor()

        when:
        downloader.fileDownloaderRemoveHandler(createSuccessContext(anchor))

        then:
        0 * taskScheduler.schedule(*_)
    }

    protected static DownloadSuccessContext createSuccessContext(Anchor anchor) {
        return new DownloadSuccessContext(anchor, "file.txt", "text/plain")
    }

    protected void runScheduledRemoval() {
        scheduledRemoval.run()

        // TestVaadinSession never releases a real lock, so the access tasks it queues are run here
        vaadinSession.getService().runPendingAccessTasks(vaadinSession)
    }
}
