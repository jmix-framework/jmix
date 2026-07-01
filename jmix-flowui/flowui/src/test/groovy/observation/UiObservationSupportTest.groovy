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
import com.vaadin.flow.router.QueryParameters
import io.jmix.flowui.UiProperties
import io.jmix.flowui.action.TargetAction
import io.jmix.flowui.fragment.Fragment
import io.jmix.flowui.kit.action.BaseAction
import io.jmix.flowui.model.DataLoader
import io.jmix.flowui.monitoring.DataLoaderLifeCycle
import io.jmix.flowui.observation.DataLoaderObservationInfo
import io.jmix.flowui.monitoring.LegacyUiTimerSupport
import io.jmix.flowui.observation.FragmentLifecycle
import io.jmix.flowui.observation.FragmentObservationInfo
import io.jmix.flowui.observation.UiObservationSupport
import io.jmix.flowui.observation.ViewLifecycle
import io.jmix.flowui.sys.ViewSupport
import io.jmix.flowui.view.StandardCloseAction
import io.jmix.flowui.view.View
import io.jmix.flowui.view.ViewActions
import io.jmix.flowui.view.ViewControllerUtils
import io.micrometer.observation.Observation
import io.micrometer.observation.tck.TestObservationRegistry
import io.micrometer.observation.tck.TestObservationRegistryAssert
import spock.lang.Specification

import java.lang.reflect.Field
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

    def "standalone action without trigger falls back to N/A sentinels"() {
        given:
        def action = new BaseAction("create-order")

        when:
        def obs = support.createActionExecutionObservation(action, null)

        then: "all optional tags are still present with the N/A sentinel so the tag-key schema stays stable"
        lowCardinalityValue(obs, "action.id") == "create-order"
        lowCardinalityValue(obs, "target.id") == "N/A"
        lowCardinalityValue(obs, "view.id") == "N/A"
        lowCardinalityValue(obs, "fragment.id") == "N/A"
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

    def "trigger component not attached to any view falls back to N/A view.id"() {
        given: "a detached component with no parent in the UI tree"
        def orphan = new Div()

        when: "creating an action observation with this component as trigger"
        def obs = support.createActionExecutionObservation(new BaseAction("act"), orphan)

        then: "findView returns null and view.id is filled with the sentinel"
        lowCardinalityValue(obs, "view.id") == "N/A"
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
        def obs = support.createActionExecutionObservation(action, null)

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

        then: "target.id falls back to the N/A sentinel, view.id is resolved via the trigger fallback"
        lowCardinalityValue(obs, "view.id") == "orders-view"
        lowCardinalityValue(obs, "target.id") == "N/A"
    }

    def "view without explicit id falls back to N/A view.id"() {
        given: "a view with no setId() call — getId() returns an empty Optional"
        def view = new TestView()
        def button = new Div()
        view.getContent().add(button)

        when: "creating an observation with a trigger inside that view"
        def obs = support.createActionExecutionObservation(new BaseAction("act"), button)

        then: "view.id falls back to the sentinel rather than being omitted"
        lowCardinalityValue(obs, "view.id") == "N/A"
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

    def "action outside any fragment uses N/A sentinel for fragment.id"() {
        given: "a button directly inside a view (no fragment in between)"
        def view = new TestView()
        view.setId("orderDetail")
        def button = new Div()
        view.getContent().add(button)

        when:
        def obs = support.createActionExecutionObservation(new BaseAction("save"), button)

        then: "view.id is resolved, fragment.id falls back to the N/A sentinel to keep tag-key schema stable"
        lowCardinalityValue(obs, "view.id") == "orderDetail"
        lowCardinalityValue(obs, "fragment.id") == "N/A"
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

    // -------- data loader observation --------

    def "data loader observation has lifecycle.name, loader.id and view.id tags"() {
        given:
        def loader = mockLoader("orders-view", "ordersDl")
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> } as Runnable)

        then:
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.data")
                .that()
                .hasLowCardinalityKeyValue("lifecycle.name", "load")
                .hasLowCardinalityKeyValue("loader.id", "ordersDl")
                .hasLowCardinalityKeyValue("view.id", "orders-view")
    }

    def "data loader observation lifecycle.name reflects the phase #lifecycleName"() {
        given:
        def loader = mockLoader("v", "dl")
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeDataLoader(loader, phase, { -> } as Runnable)

        then:
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.data")
                .that()
                .hasLowCardinalityKeyValue("lifecycle.name", lifecycleName)

        where:
        phase                         | lifecycleName
        DataLoaderLifeCycle.PRE_LOAD  | "preLoad"
        DataLoaderLifeCycle.LOAD      | "load"
        DataLoaderLifeCycle.POST_LOAD | "postLoad"
    }

    def "data loader view.id falls back to N/A when viewId is #scenario"() {
        given: "a loader whose info provider returns #scenario viewId (loader outside any view, or custom provider)"
        def loader = mockLoader(viewId, "dl")
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> } as Runnable)

        then: "view.id falls back to the N/A sentinel to keep the tag-key schema uniform"
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.data")
                .that()
                .hasLowCardinalityKeyValue("view.id", "N/A")

        where:
        scenario | viewId
        "null"   | null
        "empty"  | ""
    }

    def "data loader observation skipped when loaderId is #scenario"() {
        given:
        def loader = mockLoader("v", loaderId)
        def registry = (TestObservationRegistry) support.observationRegistry

        when: "observing a phase with blank loaderId — buildDataLoaderObservation short-circuits to NOOP"
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> } as Runnable)

        then: "no observation is recorded"
        TestObservationRegistryAssert.assertThat(registry).hasNumberOfObservationsEqualTo(0)

        where:
        scenario     | loaderId
        "null"       | null
        "empty"      | ""
        "whitespace" | "  "
    }

    def "generated loader id is aggregated under sentinel with original preserved as high cardinality"() {
        given: "a loader with an auto-generated id (prefix 'generated_')"
        def loader = mockLoader("v", "generated_abc123")
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> } as Runnable)

        then: "loader.id collapses to a single low-cardinality bucket — keeps Prometheus cardinality bounded; original id is preserved as high-cardinality attribute for trace search"
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.data")
                .that()
                .hasLowCardinalityKeyValue("loader.id", "<generated>")
                .hasHighCardinalityKeyValue("full_loader_id", "generated_abc123")
    }

    def "regular loader id is mirrored verbatim into full_loader_id"() {
        given:
        def loader = mockLoader("orderList", "ordersDl")
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> } as Runnable)

        then: "non-generated loader.id passes through unchanged; full_loader_id mirrors it"
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.data")
                .that()
                .hasLowCardinalityKeyValue("loader.id", "ordersDl")
                .hasHighCardinalityKeyValue("full_loader_id", "ordersDl")
    }

    def "data loader observation NOOP when disabled"() {
        given:
        def disabled = createSupport(false)
        def loader = mockLoader("v", "dl")
        def registry = (TestObservationRegistry) disabled.observationRegistry

        when:
        disabled.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> } as Runnable)

        then: "no observation is recorded — observation is disabled"
        TestObservationRegistryAssert.assertThat(registry).hasNumberOfObservationsEqualTo(0)
    }

    def "data loader observation includes fragment.id when set"() {
        given: "loader info with both view and fragment ids (loader lives inside a fragment)"
        def loader = mockLoader("orderDetail", "addressDl", "billing")
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> } as Runnable)

        then: "fragment.id is added alongside view.id, so the same loader.id can be attributed per fragment"
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.data")
                .that()
                .hasLowCardinalityKeyValue("view.id", "orderDetail")
                .hasLowCardinalityKeyValue("fragment.id", "billing")
    }

    def "data loader observation uses N/A sentinel for fragment.id when null"() {
        given: "loader info with no fragment context (loader belongs directly to a view)"
        def loader = mockLoader("orderDetail", "ordersDl", null)
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> } as Runnable)

        then: "fragment.id falls back to the N/A sentinel so the tag-key schema for jmix.ui.data stays stable"
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.data")
                .that()
                .hasLowCardinalityKeyValue("fragment.id", "N/A")
    }

    def "observeDataLoader extracts info via observationInfoProvider"() {
        given:
        def info = new DataLoaderObservationInfo("orders-view", "ordersDl", null)
        def loader = Mock(DataLoader) {
            getObservationInfoProvider() >> ({ DataLoader dl -> info } as Function)
        }
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> } as Runnable)

        then: "tags are populated from the info returned by the provider"
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.data")
                .that()
                .hasLowCardinalityKeyValue("loader.id", "ordersDl")
                .hasLowCardinalityKeyValue("view.id", "orders-view")
    }

    def "loader-based overload does not invoke observationInfoProvider when observation is disabled"() {
        given: "observation disabled and a loader whose provider records whether it was consulted"
        def disabled = createSupport(false)
        def applied = false
        def loader = Mock(DataLoader) {
            getObservationInfoProvider() >> ({ DataLoader dl ->
                applied = true
                new DataLoaderObservationInfo("v", "dl", null)
            } as Function)
        }

        when: "observing a data loader phase with observation turned off"
        def result = disabled.observeDataLoader(loader, DataLoaderLifeCycle.LOAD, { -> "result" } as Supplier)

        then: "the provider is never applied — the info extraction (a UI-tree walk for fragment loaders) is short-circuited"
        !applied

        and: "the supplied work still runs — legacy path is unaffected by the short-circuit"
        result == "result"
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

    def "observeViewLifecycle records exactly one Observation for #lifecycleName phase"() {
        given: """
            Each observeViewLifecycle call must produce exactly one Observation, regardless of
            phase. Covers all eight ViewLifecycle values:
              - non-event phases (CREATE/LOAD/INJECT) — invoked directly from ViewSupport.initView
                per D2; R5 requires exactly one wrap per phase per occurrence;
              - event phases (INIT/BEFORE_SHOW/READY/BEFORE_CLOSE/AFTER_CLOSE) — also reached
                through observeViewLifecycle from drivers that fire via Composite.fireEvent
                (View.afterNavigation, View.beforeEnter, View.close, StandardDetailView), per D1.
            Guards against accidental double-wrapping at this layer.
        """
        def view = new TestView()
        view.setId("orders-view")
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeViewLifecycle(view, phase, { -> } as Runnable)

        then:
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.views")
                .that()
                .hasLowCardinalityKeyValue("lifecycle.name", lifecycleName)
                .hasLowCardinalityKeyValue("view.id", "orders-view")

        where:
        phase                       | lifecycleName
        ViewLifecycle.CREATE        | "create"
        ViewLifecycle.LOAD          | "load"
        ViewLifecycle.INJECT        | "inject"
        ViewLifecycle.INIT          | "init"
        ViewLifecycle.BEFORE_SHOW   | "beforeShow"
        ViewLifecycle.READY         | "ready"
        ViewLifecycle.BEFORE_CLOSE  | "beforeClose"
        ViewLifecycle.AFTER_CLOSE   | "afterClose"
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

    def "ViewSupport.fireViewInitEvent records exactly one INIT observation"() {
        given: """
            A test view wired to the test UiObservationSupport (so ViewControllerUtils.fireEvent
            inside fireViewInitEvent resolves to our test registry) and a real ViewSupport exposing
            the protected fireViewInitEvent method.
        """
        def view = new TestView()
        view.setId("orders-view")
        injectUiObservationSupport(view, support)
        def viewSupport = new TestableViewSupport()
        def registry = (TestObservationRegistry) support.observationRegistry

        when: """
            Reproduces ViewSupport.initView's INIT step. Per D1: initView calls fireViewInitEvent
            directly, without an outer observeViewLifecycle wrap. The fire method itself goes
            through ViewControllerUtils.fireEvent, which auto-observes the INIT phase. If an outer
            wrap is reintroduced, this test catches the resulting double observation.
        """
        viewSupport.invokeFireViewInitEvent(view)

        then:
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.views")
                .that()
                .hasLowCardinalityKeyValue("lifecycle.name", "init")
                .hasLowCardinalityKeyValue("view.id", "orders-view")
    }

    def "ViewControllerUtils.fireEvent auto-observes lifecycle event #lifecycleName exactly once"() {
        given: """
            ViewControllerUtils.fireEvent must produce exactly one observation per lifecycle event
            it dispatches — guards against both (a) someone removing the auto-observation (would
            yield 0) and (b) someone wrapping it in an external observeViewLifecycle (would yield
            2). Covers all five lifecycle events the contract recognises.

            ReadyEvent additionally triggers View's internal refreshActionsState listener, which
            walks getViewActions().getActions(); stub an empty ViewActions so it iterates nothing
            instead of NPEing.
        """
        def view = new TestView()
        view.setId("orders-view")
        ViewControllerUtils.setViewActions(view, Mock(ViewActions) { getActions() >> [] })
        injectUiObservationSupport(view, support)
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        ViewControllerUtils.fireEvent(view, eventFactory(view))

        then:
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.views")
                .that()
                .hasLowCardinalityKeyValue("lifecycle.name", lifecycleName)
                .hasLowCardinalityKeyValue("view.id", "orders-view")

        where:
        lifecycleName | eventFactory
        "init"        | { View v -> new View.InitEvent(v) }
        "beforeShow"  | { View v -> new View.BeforeShowEvent(v) }
        "ready"       | { View v -> new View.ReadyEvent(v) }
        "beforeClose" | { View v -> new View.BeforeCloseEvent(v, new StandardCloseAction("close")) }
        "afterClose"  | { View v -> new View.AfterCloseEvent(v, new StandardCloseAction("close")) }
    }

    def "ViewControllerUtils.fireEvent on a non-lifecycle event creates no observation"() {
        given:
        def view = new TestView()
        view.setId("orders-view")
        injectUiObservationSupport(view, support)
        def registry = (TestObservationRegistry) support.observationRegistry

        when: "firing an event that is not one of the five lifecycle events"
        ViewControllerUtils.fireEvent(view,
                new View.QueryParametersChangeEvent(view, QueryParameters.empty()))

        then: "no observation is recorded — non-lifecycle events bypass the observation path"
        TestObservationRegistryAssert.assertThat(registry).hasNumberOfObservationsEqualTo(0)
    }

    private static void injectUiObservationSupport(View<?> view, UiObservationSupport support) {
        Field field = View.class.getDeclaredField("uiObservationSupport")
        field.setAccessible(true)
        field.set(view, support)
    }

    /**
     * Subclass that exposes the protected {@code fireViewInitEvent} for direct invocation from the
     * test. {@code fireViewInitEvent} itself does not touch any of the dependencies passed to the
     * constructor, so {@code null} arguments are safe.
     */
    private static class TestableViewSupport extends ViewSupport {
        TestableViewSupport() {
            super(null, null, null, null, null, null, null, null)
        }

        void invokeFireViewInitEvent(View<?> view) {
            fireViewInitEvent(view)
        }
    }

    def "observeFragmentLifecycle returns supplier value"() {
        given:
        def info = new FragmentObservationInfo("frag-id", "com.example.MyFragment", null)

        expect:
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE,
                { -> "fragment-result" } as Supplier) == "fragment-result"
    }

    def "observeFragmentLifecycle does not invoke LegacyUiTimerSupport"() {
        given: "fragment monitoring info"
        def info = new FragmentObservationInfo("frag-id", "com.example.MyFragment", null)

        when: "running a fragment lifecycle phase through observeFragmentLifecycle"
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE, { -> "x" } as Supplier)

        then: "legacy bridge is never invoked — fragments historically had no Timer metric"
        0 * legacyUiTimerSupport._
    }

    def "observeFragmentLifecycle propagates supplier exception"() {
        given:
        def info = new FragmentObservationInfo("frag-id", "com.example.MyFragment", null)

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

    def "observeFragmentLifecycle records exactly one Observation for #lifecycleName phase"() {
        given: """
            Per R3: each observeFragmentLifecycle call must produce exactly one Observation. D3
            states fragments cannot double-observe by construction (single driver FragmentsImpl,
            raw ComponentUtil.fireEvent for READY) — this test guards the invariant at the
            method level, symmetric to view/loader coverage.
        """
        def fragment = Mock(Fragment) {
            getId() >> Optional.of("my-frag")
            getParent() >> Optional.empty()
        }
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeFragmentLifecycle(fragment, phase, { -> } as Runnable)

        then:
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.fragments")
                .that()
                .hasLowCardinalityKeyValue("lifecycle.name", lifecycleName)
                .hasLowCardinalityKeyValue("fragment.id", "my-frag")

        where:
        phase                    | lifecycleName
        FragmentLifecycle.CREATE | "create"
        FragmentLifecycle.READY  | "ready"
    }

    def "observeFragmentLifecycle(Info, ...) records exactly one Observation for CREATE phase"() {
        given: """
            Info-based observe is the production path for the CREATE phase (FragmentsImpl.create
            uses it when the Fragment instance does not yet exist and is materialised inside the
            observe block). Per R3 the call must produce exactly one Observation.
        """
        def info = new FragmentObservationInfo("frag-id", "com.example.MyFragment", null)
        def registry = (TestObservationRegistry) support.observationRegistry

        when:
        support.observeFragmentLifecycle(info, FragmentLifecycle.CREATE, { -> "x" } as Supplier)

        then:
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.fragments")
                .that()
                .hasLowCardinalityKeyValue("lifecycle.name", "create")
                .hasLowCardinalityKeyValue("fragment.id", "frag-id")
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

    def "fragment observation falls back to N/A view.id when the fragment is not attached to a view"() {
        given: "a fragment with no parent in the UI tree"
        def fragment = Mock(Fragment) {
            getId() >> Optional.of("orphan")
            getParent() >> Optional.empty()
        }
        def registry = (TestObservationRegistry) support.observationRegistry

        when: "running a fragment lifecycle phase for a detached fragment"
        support.observeFragmentLifecycle(fragment, FragmentLifecycle.CREATE, { -> } as Runnable)

        then: "view.id falls back to the sentinel — uniform tag schema across all observations"
        TestObservationRegistryAssert.assertThat(registry)
                .hasNumberOfObservationsEqualTo(1)
                .hasObservationWithNameEqualTo("jmix.ui.fragments")
                .that()
                .hasLowCardinalityKeyValue("view.id", "N/A")
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

    private DataLoader mockLoader(String viewId, String loaderId, String fragmentId = null) {
        def info = new DataLoaderObservationInfo(viewId, loaderId, fragmentId)
        return Mock(DataLoader) {
            getObservationInfoProvider() >> ({ DataLoader dl -> info } as Function)
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
