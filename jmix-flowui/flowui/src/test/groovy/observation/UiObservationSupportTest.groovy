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

package observation

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import io.jmix.flowui.UiProperties
import io.jmix.flowui.action.TargetAction
import io.jmix.flowui.fragment.Fragment
import io.jmix.flowui.kit.action.BaseAction
import io.jmix.flowui.model.DataLoader
import io.jmix.flowui.monitoring.DataLoaderLifeCycle
import io.jmix.flowui.monitoring.DataLoaderMonitoringInfo
import io.jmix.flowui.observation.FragmentLifecycle
import io.jmix.flowui.observation.FragmentLifecycleObservationInfo
import io.jmix.flowui.observation.UiObservationSupport
import io.jmix.flowui.observation.ViewLifecycle
import io.jmix.flowui.view.View
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.micrometer.observation.Observation
import io.micrometer.observation.tck.TestObservationRegistry
import io.micrometer.observation.tck.TestObservationRegistryAssert
import spock.lang.Specification

import java.util.function.Function
import java.util.function.Supplier

class UiObservationSupportTest extends Specification {

    UiObservationSupport support
    SimpleMeterRegistry meterRegistry

    def setup() {
        meterRegistry = new SimpleMeterRegistry()
        support = createSupport(true)
    }

    def "standalone action without trigger has only action.id"() {
        given:
        def action = new BaseAction("create-order")

        when:
        def obs = support.createActionExecutionObservation(action)

        then:
        lowCardinalityValue(obs, "action.id") == "create-order"
        lowCardinalityValue(obs, "target.id") == null
        lowCardinalityValue(obs, "view.id") == null
    }

    def "trigger component attached to a view yields view.id"() {
        given:
        def view = new TestView()
        view.setId("orders-view")
        def button = new Div()
        view.getContent().add(button)

        when:
        def obs = support.createActionExecutionObservation(new BaseAction("act"), button)

        then:
        lowCardinalityValue(obs, "view.id") == "orders-view"
    }

    def "trigger component being a view itself yields view.id"() {
        given:
        def view = new TestView()
        view.setId("main")

        when:
        def obs = support.createActionExecutionObservation(new BaseAction("act"), view)

        then:
        lowCardinalityValue(obs, "view.id") == "main"
    }

    def "trigger component not attached to any view yields no view.id"() {
        given:
        def orphan = new Div()

        when:
        def obs = support.createActionExecutionObservation(new BaseAction("act"), orphan)

        then:
        lowCardinalityValue(obs, "view.id") == null
    }

    def "TargetAction with component target attached to view yields target.id and view.id"() {
        given:
        def view = new TestView()
        view.setId("orders-view")
        def grid = new Div()
        grid.setId("ordersGrid")
        view.getContent().add(grid)
        def action = new TestTargetAction("act", grid)

        when:
        def obs = support.createActionExecutionObservation(action)

        then:
        lowCardinalityValue(obs, "target.id") == "ordersGrid"
        lowCardinalityValue(obs, "view.id") == "orders-view"
    }

    def "target view wins over trigger view"() {
        given:
        def viewA = new TestView()
        viewA.setId("view-a")
        def gridInA = new Div()
        gridInA.setId("gridA")
        viewA.getContent().add(gridInA)

        def viewB = new TestView()
        viewB.setId("view-b")
        def triggerInB = new Div()
        viewB.getContent().add(triggerInB)

        def action = new TestTargetAction("act", gridInA)

        when:
        def obs = support.createActionExecutionObservation(action, triggerInB)

        then:
        lowCardinalityValue(obs, "view.id") == "view-a"
        lowCardinalityValue(obs, "target.id") == "gridA"
    }

    def "TargetAction with non-component target falls back to trigger for view.id"() {
        given:
        def view = new TestView()
        view.setId("orders-view")
        def button = new Div()
        view.getContent().add(button)
        def action = new TestTargetAction("act", "not-a-component")

        when:
        def obs = support.createActionExecutionObservation(action, button)

        then:
        lowCardinalityValue(obs, "view.id") == "orders-view"
        lowCardinalityValue(obs, "target.id") == null
    }

    def "view without explicit id produces no view.id tag"() {
        given:
        def view = new TestView()
        def button = new Div()
        view.getContent().add(button)

        when:
        def obs = support.createActionExecutionObservation(new BaseAction("act"), button)

        then:
        lowCardinalityValue(obs, "view.id") == null
    }

    def "legacy single-arg method delegates to overload"() {
        given:
        def view = new TestView()
        view.setId("orders-view")
        def grid = new Div()
        grid.setId("gridA")
        view.getContent().add(grid)
        def action = new TestTargetAction("act", grid)

        when:
        def obs = support.createActionExecutionObservation(action)

        then: "target-derived view.id is still set even without trigger component"
        lowCardinalityValue(obs, "view.id") == "orders-view"
        lowCardinalityValue(obs, "target.id") == "gridA"
    }

    def "disabled observation returns NOOP"() {
        given:
        def disabled = createSupport(false)

        when:
        def obs = disabled.createActionExecutionObservation(new BaseAction("act"), new Div())

        then:
        obs == Observation.NOOP
    }

    def "null registry returns NOOP even when enabled"() {
        given:
        def props = Mock(UiProperties) { isUiObservationEnabled() >> true }
        def supportNoRegistry = new UiObservationSupport(props)

        when:
        def obs = supportNoRegistry.createActionExecutionObservation(new BaseAction("act"), null)

        then:
        obs == Observation.NOOP
    }

    def "data loader observation has lifecycle.name, loader.id and view.id tags"() {
        given:
        def info = new DataLoaderMonitoringInfo("orders-view", "ordersDl")

        when:
        def obs = support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD)

        then:
        lowCardinalityValue(obs, "lifecycle.name") == "load"
        lowCardinalityValue(obs, "loader.id") == "ordersDl"
        lowCardinalityValue(obs, "view.id") == "orders-view"
    }

    def "data loader lifecycle.name reflects the phase"() {
        given:
        def info = new DataLoaderMonitoringInfo("v", "dl")

        expect:
        lowCardinalityValue(support.createDataLoaderObservation(info, phase), "lifecycle.name") == name

        where:
        phase                         | name
        DataLoaderLifeCycle.PRE_LOAD  | "preLoad"
        DataLoaderLifeCycle.LOAD      | "load"
        DataLoaderLifeCycle.POST_LOAD | "postLoad"
    }

    def "data loader view.id falls back to N/A when viewId is #scenario"() {
        given:
        def info = new DataLoaderMonitoringInfo(viewId, "dl")

        when:
        def obs = support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD)

        then:
        lowCardinalityValue(obs, "view.id") == "N/A"

        where:
        scenario | viewId
        "null"   | null
        "empty"  | ""
    }

    def "data loader observation skipped when loaderId is #scenario"() {
        given:
        def info = new DataLoaderMonitoringInfo("v", loaderId)

        expect:
        support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD) == Observation.NOOP

        where:
        scenario     | loaderId
        "null"       | null
        "empty"      | ""
        "whitespace" | "  "
    }

    def "data loader observation skipped for generated loader id"() {
        given:
        def info = new DataLoaderMonitoringInfo("v", "generated_abc123")

        expect:
        support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD) == Observation.NOOP
    }

    def "data loader observation NOOP when disabled"() {
        given:
        def disabled = createSupport(false)
        def info = new DataLoaderMonitoringInfo("v", "dl")

        expect:
        disabled.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD) == Observation.NOOP
    }

    def "loader-based overload extracts info via monitoringInfoProvider"() {
        given:
        def info = new DataLoaderMonitoringInfo("orders-view", "ordersDl")
        def loader = Mock(DataLoader) {
            getMonitoringInfoProvider() >> ({ DataLoader dl -> info } as Function)
        }

        when:
        def obs = support.createDataLoaderObservation(loader, DataLoaderLifeCycle.LOAD)

        then:
        lowCardinalityValue(obs, "loader.id") == "ordersDl"
        lowCardinalityValue(obs, "view.id") == "orders-view"
    }

    def "observeDataLoader returns supplier value"() {
        given:
        def loader = mockLoader("v", "dl")

        expect:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> "hello" } as Supplier) == "hello"
    }

    def "observeDataLoader runs runnable"() {
        given:
        def loader = mockLoader("v", "dl")
        def executed = false

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> executed = true } as Runnable)

        then:
        executed
    }

    def "legacy Timer recorded with old tags when legacy flag is on"() {
        given:
        def loader = mockLoader("orders-view", "ordersDl")

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> "x" } as Supplier)

        then:
        def timer = meterRegistry.find("jmix.ui.data")
                .tag("view", "orders-view")
                .tag("dataLoader", "ordersDl")
                .tag("lifeCycle", "load")
                .timer()
        timer != null
        timer.count() == 1
    }

    def "legacy Timer is not recorded when legacy flag is off"() {
        given:
        def s = createSupport(true, false)
        def loader = mockLoader("v", "dl")

        when:
        s.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.data").timer() == null
    }

    def "legacy Timer recorded even when supplier throws"() {
        given:
        def loader = mockLoader("v", "dl")

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> throw new RuntimeException("boom") } as Supplier)

        then:
        thrown(RuntimeException)
        meterRegistry.find("jmix.ui.data").timer()?.count() == 1
    }

    def "observeDataLoader rethrows the exact exception object from supplier (legacyTimer=#legacyTimer)"() {
        given:
        def s = createSupport(true, legacyTimer)
        def loader = mockLoader("v", "dl")
        def boom = new IllegalStateException("boom")

        when:
        s.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> throw boom } as Supplier)

        then: "the exact same exception instance bubbles up — no swallowing, no wrapping"
        def caught = thrown(IllegalStateException)
        caught.is(boom)

        where:
        legacyTimer << [true, false]
    }

    def "observeDataLoader runs supplier even when both observation and legacy timer are off"() {
        given:
        def s = createSupport(false, false)
        def loader = mockLoader("v", "dl")

        expect:
        s.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> "result" } as Supplier) == "result"
        meterRegistry.find("jmix.ui.data").timer() == null
    }

    def "observeViewLifecycle returns supplier value"() {
        given:
        def view = new TestView()
        view.setId("orders-view")

        expect:
        support.observeViewLifecycle(view, ViewLifecycle.READY, { -> "hello" } as Supplier) == "hello"
    }

    def "observeViewLifecycle runs runnable"() {
        given:
        def view = new TestView()
        view.setId("orders-view")
        def executed = false

        when:
        support.observeViewLifecycle(view, ViewLifecycle.READY, { -> executed = true } as Runnable)

        then:
        executed
    }

    def "view legacy Timer recorded with old tags when legacy flag is on"() {
        given:
        def view = new TestView()
        view.setId("orders-view")

        when:
        support.observeViewLifecycle(view, ViewLifecycle.READY, { -> "x" } as Supplier)

        then:
        def timer = meterRegistry.find("jmix.ui.views")
                .tag("view", "orders-view")
                .tag("lifeCycle", "ready")
                .timer()
        timer != null
        timer.count() == 1
    }

    def "view legacy Timer not recorded when legacy flag is off"() {
        given:
        def s = createSupport(true, false)
        def view = new TestView()
        view.setId("orders-view")

        when:
        s.observeViewLifecycle(view, ViewLifecycle.READY, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.views").timer() == null
    }

    def "view legacy Timer recorded even when supplier throws"() {
        given:
        def view = new TestView()
        view.setId("orders-view")

        when:
        support.observeViewLifecycle(view, ViewLifecycle.READY,
                { -> throw new RuntimeException("boom") } as Supplier)

        then:
        thrown(RuntimeException)
        meterRegistry.find("jmix.ui.views").timer()?.count() == 1
    }

    def "observeFragmentLifecycle returns supplier value"() {
        given:
        def info = new FragmentLifecycleObservationInfo("frag-id", "com.example.MyFragment")

        expect:
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE,
                { -> "fragment-result" } as Supplier) == "fragment-result"
    }

    def "fragment legacy Timer is NEVER recorded regardless of legacy flag"() {
        given:
        def s = createSupport(true, legacyTimerEnabled)
        def info = new FragmentLifecycleObservationInfo("frag-id", "com.example.MyFragment")

        when:
        s.observeFragmentLifecycle(info, FragmentLifecycle.CREATE, { -> "x" } as Supplier)

        then: "no legacy fragment Timer is created — fragments never had legacy Timer historically"
        meterRegistry.find("jmix.ui.fragments").timer() == null

        where:
        legacyTimerEnabled << [true, false]
    }

    def "observeFragmentLifecycle propagates supplier exception"() {
        given:
        def info = new FragmentLifecycleObservationInfo("frag-id", "com.example.MyFragment")

        when:
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE,
                { -> throw new RuntimeException("boom") } as Supplier)

        then:
        thrown(RuntimeException)
        meterRegistry.find("jmix.ui.fragments").timer() == null
    }

    def "view legacy Timer tag lifeCycle reflects the phase #phase"() {
        given:
        def view = new TestView()
        view.setId("orders-view")

        when:
        support.observeViewLifecycle(view, phase, { -> "x" } as Supplier)

        then:
        meterRegistry.find("jmix.ui.views")
                .tag("view", "orders-view")
                .tag("lifeCycle", expectedTag)
                .timer() != null

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

    def "view legacy Timer is skipped when view has no id"() {
        given: "a view without explicit id"
        def view = new TestView()

        when:
        support.observeViewLifecycle(view, ViewLifecycle.READY, { -> "x" } as Supplier)

        then: "UiMonitoring.canViewBeMonitored skips recording for blank viewId — no Timer is created"
        meterRegistry.find("jmix.ui.views").timer() == null
    }

    def "observeViewLifecycle records modern Observation with view.id and lifecycle.name tags"() {
        given:
        def view = new TestView()
        view.setId("orders-view")
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeViewLifecycle(view, ViewLifecycle.READY, { -> } as Runnable)

        then:
        TestObservationRegistryAssert.assertThat(registry)
                .hasObservationWithNameEqualTo("jmix.ui.views")
                .that()
                .hasLowCardinalityKeyValue("lifecycle.name", "ready")
                .hasLowCardinalityKeyValue("view.id", "orders-view")
    }

    def "observeFragmentLifecycle Fragment-based overload records modern Observation"() {
        given:
        def fragment = Mock(Fragment) {
            getId() >> Optional.of("my-frag")
        }
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeFragmentLifecycle(fragment, FragmentLifecycle.READY, { -> } as Runnable)

        then:
        TestObservationRegistryAssert.assertThat(registry)
                .hasObservationWithNameEqualTo("jmix.ui.fragments")
                .that()
                .hasLowCardinalityKeyValue("lifecycle.name", "ready")
                .hasLowCardinalityKeyValue("fragment.id", "my-frag")
    }

    private UiObservationSupport createSupport(boolean observationEnabled, boolean legacyTimerEnabled = true) {
        def props = Mock(UiProperties) {
            isUiObservationEnabled() >> observationEnabled
            isLegacyTimerEnabled() >> legacyTimerEnabled
        }
        def support = new UiObservationSupport(props)
        support.observationRegistry = TestObservationRegistry.create()
        support.meterRegistry = meterRegistry
        return support
    }

    private DataLoader mockLoader(String viewId, String loaderId) {
        def info = new DataLoaderMonitoringInfo(viewId, loaderId)
        return Mock(DataLoader) {
            getMonitoringInfoProvider() >> ({ DataLoader dl -> info } as Function)
        }
    }

    private static String lowCardinalityValue(Observation observation, String key) {
        def match = observation.getContextView().getLowCardinalityKeyValues().find { it.key == key }
        return match?.value
    }

    static class TestView extends View<VerticalLayout> {
    }

    static class TestTargetAction extends BaseAction implements TargetAction<Object> {
        private Object target

        TestTargetAction(String id, Object target) {
            super(id)
            this.target = target
        }

        @Override
        Object getTarget() {
            return target
        }

        @Override
        void setTarget(Object target) {
            this.target = target
        }
    }
}
