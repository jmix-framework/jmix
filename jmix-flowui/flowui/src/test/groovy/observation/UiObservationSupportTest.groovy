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
import io.jmix.flowui.monitoring.LegacyUiTimerSupport
import io.jmix.flowui.observation.FragmentLifecycle
import io.jmix.flowui.observation.FragmentLifecycleObservationInfo
import io.jmix.flowui.observation.UiObservationSupport
import io.jmix.flowui.observation.ViewLifecycle
import io.jmix.flowui.view.View
import io.micrometer.observation.Observation
import io.micrometer.observation.tck.TestObservationRegistry
import io.micrometer.observation.tck.TestObservationRegistryAssert
import spock.lang.Specification

import java.util.function.Function
import java.util.function.Supplier

@SuppressWarnings("deprecation")
class UiObservationSupportTest extends Specification {

    UiObservationSupport support
    LegacyUiTimerSupport legacyUiTimerSupport

    def setup() {
        legacyUiTimerSupport = Mock(LegacyUiTimerSupport)
        // Mock just delegates to the supplied action — Timer behaviour is covered by LegacyUiTimerSupportTest.
        legacyUiTimerSupport.recordDataLoaderTimer(_, _, _ as Supplier) >> { args -> ((Supplier) args[2]).get() }
        legacyUiTimerSupport.recordDataLoaderTimer(_, _, _ as Runnable) >> { args -> ((Runnable) args[2]).run() }
        legacyUiTimerSupport.recordViewTimer(_, _, _ as Supplier) >> { args -> ((Supplier) args[2]).get() }
        legacyUiTimerSupport.recordViewTimer(_, _, _ as Runnable) >> { args -> ((Runnable) args[2]).run() }

        support = createSupport(true)
    }

    // -------- action observation --------

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

    // -------- data loader observation factory --------

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

    // -------- observeDataLoader / observeViewLifecycle / observeFragmentLifecycle --------

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

    def "observeDataLoader rethrows the exact exception object from supplier"() {
        given:
        def loader = mockLoader("v", "dl")
        def boom = new IllegalStateException("boom")

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> throw boom } as Supplier)

        then: "the exact same exception instance bubbles up — no swallowing, no wrapping"
        def caught = thrown(IllegalStateException)
        caught.is(boom)
    }

    def "observeDataLoader delegates the work to LegacyUiTimerSupport"() {
        given:
        def loader = mockLoader("v", "dl")

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> "x" } as Supplier)

        then:
        1 * legacyUiTimerSupport.recordDataLoaderTimer(loader, DataLoaderLifeCycle.LOAD, _ as Supplier) >>
                { args -> ((Supplier) args[2]).get() }
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

    def "observeViewLifecycle delegates the work to LegacyUiTimerSupport"() {
        given:
        def view = new TestView()
        view.setId("orders-view")

        when:
        support.observeViewLifecycle(view, ViewLifecycle.READY, { -> "x" } as Supplier)

        then:
        1 * legacyUiTimerSupport.recordViewTimer(view, ViewLifecycle.READY, _ as Supplier) >>
                { args -> ((Supplier) args[2]).get() }
    }

    def "observeFragmentLifecycle returns supplier value"() {
        given:
        def info = new FragmentLifecycleObservationInfo("frag-id", "com.example.MyFragment")

        expect:
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE,
                { -> "fragment-result" } as Supplier) == "fragment-result"
    }

    def "observeFragmentLifecycle does not invoke LegacyUiTimerSupport"() {
        given:
        def info = new FragmentLifecycleObservationInfo("frag-id", "com.example.MyFragment")

        when:
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE, { -> "x" } as Supplier)

        then: "fragments never had a legacy Timer historically — must not delegate to the legacy bridge"
        0 * legacyUiTimerSupport._
    }

    def "observeFragmentLifecycle propagates supplier exception"() {
        given:
        def info = new FragmentLifecycleObservationInfo("frag-id", "com.example.MyFragment")

        when:
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE,
                { -> throw new RuntimeException("boom") } as Supplier)

        then:
        thrown(RuntimeException)
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

    // -------- helpers --------

    private UiObservationSupport createSupport(boolean observationEnabled) {
        def props = Mock(UiProperties) {
            isUiObservationEnabled() >> observationEnabled
        }
        def support = new UiObservationSupport(props)
        support.observationRegistry = TestObservationRegistry.create()
        support.legacyUiTimerSupport = legacyUiTimerSupport
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
