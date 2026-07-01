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

package monitoring

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import io.jmix.flowui.UiProperties
import io.jmix.flowui.model.DataLoader
import io.jmix.flowui.model.impl.CollectionLoaderImpl
import io.jmix.flowui.monitoring.DataLoaderLifeCycle
import io.jmix.flowui.monitoring.DataLoaderMonitoringInfo
import io.jmix.flowui.observation.DataLoaderObservationInfo
import io.jmix.flowui.monitoring.LegacyUiTimerSupport
import io.jmix.flowui.observation.ViewLifecycle
import io.jmix.flowui.view.View
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import spock.lang.Specification

import java.util.function.Function
import java.util.function.Supplier

class LegacyUiTimerSupportTest extends Specification {

    SimpleMeterRegistry meterRegistry

    def setup() {
        meterRegistry = new SimpleMeterRegistry()
    }

    // -------- recordDataLoaderTimer --------

    def "recordDataLoaderTimer returns supplier value"() {
        given:
        def support = createSupport(true)
        def loader = mockLoader("v", "dl")

        expect:
        support.recordDataLoaderTimer(loader, DataLoaderLifeCycle.LOAD, { -> "hello" } as Supplier) == "hello"
    }

    def "recordDataLoaderTimer records Timer with view, dataLoader and lifeCycle tags"() {
        given:
        def support = createSupport(true)
        def loader = mockLoader("orders-view", "ordersDl")

        when:
        support.recordDataLoaderTimer(loader, DataLoaderLifeCycle.LOAD, { -> "x" } as Supplier)

        then:
        def timer = meterRegistry.find("jmix.ui.data")
                .tag("view", "orders-view")
                .tag("dataLoader", "ordersDl")
                .tag("lifeCycle", "load")
                .timer()
        timer != null
        timer.count() == 1
    }

    def "legacy `view` tag is whatever the monitoring info viewId provides"() {
        given: "the (already-folded) legacy 2-tuple is passed straight through to the `view` tag"
        def info = new DataLoaderMonitoringInfo("billing", "addressDl")
        def loader = Mock(DataLoader) {
            getMonitoringInfoProvider() >> ({ DataLoader dl -> info } as Function)
        }
        def support = createSupport(true)

        when:
        support.recordDataLoaderTimer(loader, DataLoaderLifeCycle.LOAD, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.data").tag("view", "billing").timer() != null
    }

    def "recordDataLoaderTimer records lifeCycle tag for each phase"() {
        given:
        def support = createSupport(true)
        def loader = mockLoader("v", "dl")

        when:
        support.recordDataLoaderTimer(loader, phase, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.data").tag("lifeCycle", expectedTag).timer() != null

        where:
        phase                         | expectedTag
        DataLoaderLifeCycle.PRE_LOAD  | "preLoad"
        DataLoaderLifeCycle.LOAD      | "load"
        DataLoaderLifeCycle.POST_LOAD | "postLoad"
    }

    def "recordDataLoaderTimer does not record when disabled"() {
        given:
        def support = createSupport(false)
        def loader = mockLoader("v", "dl")

        when:
        support.recordDataLoaderTimer(loader, DataLoaderLifeCycle.LOAD, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.data").timer() == null
    }

    def "recordDataLoaderTimer records Timer even when supplier throws"() {
        given:
        def support = createSupport(true)
        def loader = mockLoader("v", "dl")

        when:
        support.recordDataLoaderTimer(loader, DataLoaderLifeCycle.LOAD,
                { -> throw new RuntimeException("boom") } as Supplier)

        then:
        thrown(RuntimeException)
        meterRegistry.find("jmix.ui.data").timer()?.count() == 1
    }

    def "recordDataLoaderTimer rethrows the exact exception instance"() {
        given:
        def support = createSupport(true)
        def loader = mockLoader("v", "dl")
        def boom = new IllegalStateException("boom")

        when:
        support.recordDataLoaderTimer(loader, DataLoaderLifeCycle.LOAD, { -> throw boom } as Supplier)

        then:
        def caught = thrown(IllegalStateException)
        caught.is(boom)
    }

    def "recordDataLoaderTimer skips Timer when loaderId is blank"() {
        given: "loader with blank loaderId — UiMonitoring.canDataLoaderBeMonitored returns false"
        def support = createSupport(true)
        def loader = mockLoader("v", "")

        when:
        support.recordDataLoaderTimer(loader, DataLoaderLifeCycle.LOAD, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.data").timer() == null
    }

    // -------- getMonitoringInfoProvider derivation (DataLoader default) --------

    def "deprecated getMonitoringInfoProvider folds fragmentId into the legacy view slot"() {
        given: "a real loader carrying a modern observation provider with a fragment id"
        def loader = new CollectionLoaderImpl()
        loader.setObservationInfoProvider({ DataLoader dl ->
            new DataLoaderObservationInfo("orderDetail", "addressDl", "billing")
        } as Function)

        when: "reading the deprecated legacy provider"
        def legacy = loader.getMonitoringInfoProvider().apply(loader)

        then: "fragmentId becomes the legacy viewId; loaderId passes through"
        legacy.viewId() == "billing"
        legacy.loaderId() == "addressDl"
    }

    def "deprecated getMonitoringInfoProvider uses viewId when there is no fragment"() {
        given: "a real loader whose observation provider has no fragment id"
        def loader = new CollectionLoaderImpl()
        loader.setObservationInfoProvider({ DataLoader dl ->
            new DataLoaderObservationInfo("orders-view", "ordersDl", null)
        } as Function)

        when:
        def legacy = loader.getMonitoringInfoProvider().apply(loader)

        then: "viewId passes through unchanged"
        legacy.viewId() == "orders-view"
        legacy.loaderId() == "ordersDl"
    }

    // -------- recordViewTimer --------

    def "recordViewTimer returns supplier value"() {
        given:
        def support = createSupport(true)
        def view = new TestView()
        view.setId("orders-view")

        expect:
        support.recordViewTimer(view, ViewLifecycle.READY, { -> "hello" } as Supplier) == "hello"
    }

    def "recordViewTimer records Timer with view and lifeCycle tags"() {
        given:
        def support = createSupport(true)
        def view = new TestView()
        view.setId("orders-view")

        when:
        support.recordViewTimer(view, ViewLifecycle.READY, { -> "x" } as Supplier)

        then:
        def timer = meterRegistry.find("jmix.ui.views")
                .tag("view", "orders-view")
                .tag("lifeCycle", "ready")
                .timer()
        timer != null
        timer.count() == 1
    }

    def "recordViewTimer records lifeCycle tag for each phase"() {
        given:
        def support = createSupport(true)
        def view = new TestView()
        view.setId("orders-view")

        when:
        support.recordViewTimer(view, phase, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.views").tag("lifeCycle", expectedTag).timer() != null

        where:
        phase                      | expectedTag
        ViewLifecycle.CREATE       | "create"
        ViewLifecycle.LOAD         | "load"
        ViewLifecycle.INIT         | "init"
        ViewLifecycle.INJECT       | "inject"
        ViewLifecycle.BEFORE_SHOW  | "beforeShow"
        ViewLifecycle.READY        | "ready"
        ViewLifecycle.BEFORE_CLOSE | "beforeClose"
        ViewLifecycle.AFTER_CLOSE  | "afterClose"
    }

    def "recordViewTimer does not record when disabled"() {
        given:
        def support = createSupport(false)
        def view = new TestView()
        view.setId("orders-view")

        when:
        support.recordViewTimer(view, ViewLifecycle.READY, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.views").timer() == null
    }

    def "recordViewTimer records Timer even when supplier throws"() {
        given:
        def support = createSupport(true)
        def view = new TestView()
        view.setId("orders-view")

        when:
        support.recordViewTimer(view, ViewLifecycle.READY,
                { -> throw new RuntimeException("boom") } as Supplier)

        then:
        thrown(RuntimeException)
        meterRegistry.find("jmix.ui.views").timer()?.count() == 1
    }

    def "recordViewTimer skips Timer when view has no id"() {
        given: "view without explicit id — UiMonitoring.canViewBeMonitored returns false"
        def support = createSupport(true)
        def view = new TestView()

        when:
        support.recordViewTimer(view, ViewLifecycle.READY, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.views").timer() == null
    }

    // -------- helpers --------

    private LegacyUiTimerSupport createSupport(boolean enabled) {
        def support = new LegacyUiTimerSupport()
        support.uiProperties = Mock(UiProperties) { isLegacyMonitoringEnabled() >> enabled }
        support.meterRegistry = meterRegistry
        return support
    }

    private DataLoader mockLoader(String viewId, String loaderId) {
        def info = new DataLoaderMonitoringInfo(viewId, loaderId)
        return Mock(DataLoader) {
            getMonitoringInfoProvider() >> ({ DataLoader dl -> info } as Function)
        }
    }

    static class TestView extends View<VerticalLayout> {
    }
}
