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
        given: "a detached component with no parent in the UI tree"
        def orphan = new Div()

        when: "creating an action observation with this component as trigger"
        def obs = support.createActionExecutionObservation(new BaseAction("act"), orphan)

        then: "findView returns null and view.id tag is not added"
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
        given: "an action whose target lives in view-a, fired from a trigger in view-b"
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

        when: "creating an observation with both target and trigger present"
        def obs = support.createActionExecutionObservation(action, triggerInB)

        then: "view.id resolves from target (semantic owner), not from trigger"
        lowCardinalityValue(obs, "view.id") == "view-a"
        lowCardinalityValue(obs, "target.id") == "gridA"
    }

    def "TargetAction with non-component target falls back to trigger for view.id"() {
        given: "a TargetAction whose target is not a Vaadin Component (e.g. plain String)"
        def view = new TestView()
        view.setId("orders-view")
        def button = new Div()
        view.getContent().add(button)
        def action = new TestTargetAction("act", "not-a-component")

        when: "creating an observation with a valid trigger inside a view"
        def obs = support.createActionExecutionObservation(action, button)

        then: "target.id is omitted, but view.id is still resolved via the trigger fallback"
        lowCardinalityValue(obs, "view.id") == "orders-view"
        lowCardinalityValue(obs, "target.id") == null
    }

    def "view without explicit id produces no view.id tag"() {
        given: "a view with no setId() call — getId() returns an empty Optional"
        def view = new TestView()
        def button = new Div()
        view.getContent().add(button)

        when: "creating an observation with a trigger inside that view"
        def obs = support.createActionExecutionObservation(new BaseAction("act"), button)

        then: "view.id tag is not added — empty Optional is silently skipped"
        lowCardinalityValue(obs, "view.id") == null
    }

    def "action inside a fragment yields fragment.id and view.id"() {
        given: "a button inside a fragment which is inside a view"
        def view = new TestView()
        view.setId("orderDetail")
        def fragment = Mock(Fragment) {
            getId() >> Optional.of("billing")
            getParent() >> Optional.of(view)
        }
        def button = Mock(Component) {
            getParent() >> Optional.of(fragment)
        }

        when:
        def obs = support.createActionExecutionObservation(new BaseAction("save"), button)

        then: "both fragment.id and view.id are resolved via parent-chain walk"
        lowCardinalityValue(obs, "fragment.id") == "billing"
        lowCardinalityValue(obs, "view.id") == "orderDetail"
    }

    def "action outside any fragment omits fragment.id"() {
        given: "a button directly inside a view (no fragment in between)"
        def view = new TestView()
        view.setId("orderDetail")
        def button = new Div()
        view.getContent().add(button)

        when:
        def obs = support.createActionExecutionObservation(new BaseAction("save"), button)

        then: "view.id is present but fragment.id is silently omitted"
        lowCardinalityValue(obs, "view.id") == "orderDetail"
        lowCardinalityValue(obs, "fragment.id") == null
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
        given: "observationRegistry is left unset to simulate @Autowired(required = false) with no bean in the context"
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
        def info = new DataLoaderMonitoringInfo("orders-view", "ordersDl", null)

        when:
        def obs = support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD)

        then:
        lowCardinalityValue(obs, "lifecycle.name") == "load"
        lowCardinalityValue(obs, "loader.id") == "ordersDl"
        lowCardinalityValue(obs, "view.id") == "orders-view"
    }

    def "data loader lifecycle.name reflects the phase"() {
        given:
        def info = new DataLoaderMonitoringInfo("v", "dl", null)

        expect:
        lowCardinalityValue(support.createDataLoaderObservation(info, phase), "lifecycle.name") == name

        where:
        phase                         | name
        DataLoaderLifeCycle.PRE_LOAD  | "preLoad"
        DataLoaderLifeCycle.LOAD      | "load"
        DataLoaderLifeCycle.POST_LOAD | "postLoad"
    }

    def "data loader view.id falls back to N/A when viewId is #scenario"() {
        given: "monitoring info with #scenario viewId (loader outside any view, or custom monitoringInfoProvider)"
        def info = new DataLoaderMonitoringInfo(viewId, "dl", null)

        when: "creating a data loader observation"
        def obs = support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD)

        then: "view.id falls back to DATA_LOADER_EMPTY_VIEW_ID to keep the tag schema uniform"
        lowCardinalityValue(obs, "view.id") == "N/A"

        where:
        scenario | viewId
        "null"   | null
        "empty"  | ""
    }

    def "data loader observation skipped when loaderId is #scenario"() {
        given:
        def info = new DataLoaderMonitoringInfo("v", loaderId, null)

        expect:
        support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD) == Observation.NOOP

        where:
        scenario     | loaderId
        "null"       | null
        "empty"      | ""
        "whitespace" | "  "
    }

    def "generated loader id is aggregated under sentinel with original preserved as high cardinality"() {
        given: "a loader with an auto-generated id (prefix 'generated_')"
        def info = new DataLoaderMonitoringInfo("v", "generated_abc123", null)

        when:
        def obs = support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD)

        then: "loader.id collapses to a single low-cardinality bucket — keeps Prometheus cardinality bounded"
        lowCardinalityValue(obs, "loader.id") == "<generated>"

        and: "original id is preserved as high-cardinality attribute for trace search"
        highCardinalityValue(obs, "full_loader_id") == "generated_abc123"
    }

    def "regular loader id is mirrored verbatim into full_loader_id"() {
        given:
        def info = new DataLoaderMonitoringInfo("orderList", "ordersDl", null)

        when:
        def obs = support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD)

        then: "non-generated loader.id passes through unchanged"
        lowCardinalityValue(obs, "loader.id") == "ordersDl"

        and: "full_loader_id mirrors loader.id for non-generated loaders"
        highCardinalityValue(obs, "full_loader_id") == "ordersDl"
    }

    def "data loader observation NOOP when disabled"() {
        given:
        def disabled = createSupport(false)
        def info = new DataLoaderMonitoringInfo("v", "dl", null)

        expect:
        disabled.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD) == Observation.NOOP
    }

    def "data loader observation includes fragment.id when set"() {
        given: "monitoring info with both view and fragment ids (loader lives inside a fragment)"
        def info = new DataLoaderMonitoringInfo("orderDetail", "addressDl", "billing")

        when:
        def obs = support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD)

        then: "fragment.id is added alongside view.id, so the same loader.id can be attributed per fragment"
        lowCardinalityValue(obs, "view.id") == "orderDetail"
        lowCardinalityValue(obs, "fragment.id") == "billing"
    }

    def "data loader observation omits fragment.id when null"() {
        given: "monitoring info with no fragment context (loader belongs directly to a view)"
        def info = new DataLoaderMonitoringInfo("orderDetail", "ordersDl", null)

        when:
        def obs = support.createDataLoaderObservation(info, DataLoaderLifeCycle.LOAD)

        then: "fragment.id tag is silently omitted"
        lowCardinalityValue(obs, "fragment.id") == null
    }

    def "loader-based overload extracts info via monitoringInfoProvider"() {
        given:
        def info = new DataLoaderMonitoringInfo("orders-view", "ordersDl", null)
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
        given: "a supplier that throws a specific exception instance"
        def loader = mockLoader("v", "dl")
        def boom = new IllegalStateException("boom")

        when: "invoking observeDataLoader with that supplier"
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> throw boom } as Supplier)

        then: "the exact same instance bubbles up — observation must not swallow or wrap"
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
        given: "a view with an explicit id and access to the test observation registry"
        def view = new TestView()
        view.setId("orders-view")
        def registry = (TestObservationRegistry) support.observationRegistry

        when: "running a view lifecycle phase through observeViewLifecycle"
        support.observeViewLifecycle(view, ViewLifecycle.READY, { -> } as Runnable)

        then: "registry contains a started-and-stopped span with the expected tags"
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
        def info = new FragmentLifecycleObservationInfo("frag-id", "com.example.MyFragment", null)

        expect:
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE,
                { -> "fragment-result" } as Supplier) == "fragment-result"
    }

    def "observeFragmentLifecycle does not invoke LegacyUiTimerSupport"() {
        given: "fragment monitoring info"
        def info = new FragmentLifecycleObservationInfo("frag-id", "com.example.MyFragment", null)

        when: "running a fragment lifecycle phase through observeFragmentLifecycle"
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE, { -> "x" } as Supplier)

        then: "legacy bridge is never invoked — fragments historically had no Timer metric"
        0 * legacyUiTimerSupport._
    }

    def "observeFragmentLifecycle propagates supplier exception"() {
        given:
        def info = new FragmentLifecycleObservationInfo("frag-id", "com.example.MyFragment", null)

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
            getParent() >> Optional.empty()
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

    def "fragment observation includes view.id of the enclosing View"() {
        given: "a fragment attached to a view with an explicit id"
        def view = new TestView()
        view.setId("order-view")
        def fragment = Mock(Fragment) {
            getId() >> Optional.of("orderSummary")
            getParent() >> Optional.of(view)
        }
        def registry = (TestObservationRegistry) support.observationRegistry

        when: "running a fragment lifecycle phase"
        support.observeFragmentLifecycle(fragment, FragmentLifecycle.READY, { -> } as Runnable)

        then: "view.id is added so the same fragment class can be attributed per host view"
        TestObservationRegistryAssert.assertThat(registry)
                .hasObservationWithNameEqualTo("jmix.ui.fragments")
                .that()
                .hasLowCardinalityKeyValue("view.id", "order-view")
    }

    def "fragment observation omits view.id when the fragment is not attached to a view"() {
        given: "a fragment with no parent in the UI tree"
        def fragment = Mock(Fragment) {
            getId() >> Optional.of("orphan")
            getParent() >> Optional.empty()
        }

        when: "creating a fragment lifecycle observation directly"
        def obs = support.createFragmentLifecycleObservation(fragment, FragmentLifecycle.CREATE)

        then: "view.id tag is silently omitted — same conditional behaviour as fragment.id"
        lowCardinalityValue(obs, "view.id") == null
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
        def info = new DataLoaderMonitoringInfo(viewId, loaderId, null)
        return Mock(DataLoader) {
            getMonitoringInfoProvider() >> ({ DataLoader dl -> info } as Function)
        }
    }

    private static String lowCardinalityValue(Observation observation, String key) {
        def match = observation.getContextView().getLowCardinalityKeyValues().find { it.key == key }
        return match?.value
    }

    private static String highCardinalityValue(Observation observation, String key) {
        def match = observation.getContextView().getHighCardinalityKeyValues().find { it.key == key }
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
