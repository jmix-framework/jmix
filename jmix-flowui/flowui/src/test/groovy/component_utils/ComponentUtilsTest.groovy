/*
 * Copyright 2024 Haulmont.
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

package component_utils

import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.shared.SlotUtils
import io.jmix.flowui.kit.component.ComponentUtils
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ComponentUtilsTest extends Specification {

    def "Check isAutoSize()"(String size, boolean expected) {
        expect:
        ComponentUtils.isAutoSize(size) == expected

        where:
        size        | expected
        null        | true
        ""          | true
        "auto"      | true
        "AuTo"      | true
        "12.12"     | true // Incorrect CSS value, default 'auto' will be used
        "px"        | true // Incorrect CSS value, default 'auto' will be used
        "12.12asdf" | true // Incorrect CSS value, default 'auto' will be used
        "12.12px"   | false
        "-12em"     | false
        "-1px"      | false
    }

    def "copyIcon does not create a tooltip on the source icon"() {
        given: "A cold icon whose tooltip has not been created yet"
        Icon source = VaadinIcon.HOME.create()

        when: "The icon is copied"
        ComponentUtils.copyIcon(source)

        then: "The source icon is left untouched, no tooltip element is attached to it"
        SlotUtils.getElementsInSlot(source, "tooltip").findAny().isEmpty()
    }

    def "copyIcon preserves the plain text tooltip of the source icon"() {
        given: "An icon with a plain text tooltip"
        Icon source = VaadinIcon.HOME.create()
        source.setTooltipText("My tooltip")

        when: "The icon is copied"
        Icon copy = (Icon) ComponentUtils.copyIcon(source)

        then: "The copy has the same content and is not treated as Markdown"
        def tooltip = SlotUtils.getElementsInSlot(copy, "tooltip").findFirst().orElse(null)
        tooltip != null
        tooltip.getProperty("text") == "My tooltip"
        !tooltip.getProperty("markdown", false)
    }

    def "copyIcon preserves the Markdown tooltip of the source icon"() {
        given: "An icon with a Markdown tooltip"
        Icon source = VaadinIcon.HOME.create()
        source.setTooltipMarkdown("**bold**")

        when: "The icon is copied"
        Icon copy = (Icon) ComponentUtils.copyIcon(source)

        then: "The copy keeps the content and the Markdown flag"
        def tooltip = SlotUtils.getElementsInSlot(copy, "tooltip").findFirst().orElse(null)
        tooltip != null
        tooltip.getProperty("text") == "**bold**"
        tooltip.getProperty("markdown", false)
    }

    def "copyIcon does not mutate the shared source icon under concurrency"() {
        given: "A thread pool that repeatedly copies a freshly created shared icon"
        int threads = 16
        int rounds = 500
        ExecutorService executor = Executors.newFixedThreadPool(threads)
        AtomicReference<Throwable> failure = new AtomicReference<>()

        when: "All threads copy the same cold icon simultaneously in each round"
        try {
            for (int round = 0; round < rounds && failure.get() == null; round++) {
                Icon shared = VaadinIcon.HOME.create()
                CyclicBarrier barrier = new CyclicBarrier(threads)
                CountDownLatch done = new CountDownLatch(threads)
                threads.times {
                    executor.execute {
                        try {
                            barrier.await()
                            ComponentUtils.copyIcon(shared)
                        } catch (Throwable ex) {
                            failure.compareAndSet(null, ex)
                        } finally {
                            done.countDown()
                        }
                    }
                }
                done.await(30, TimeUnit.SECONDS)
            }
        } finally {
            executor.shutdownNow()
        }

        Throwable t = failure.get()
        if (t != null) {
            throw t
        }

        then: "Copying never mutates the shared icon, so no exception is thrown"
        noExceptionThrown()
    }
}
