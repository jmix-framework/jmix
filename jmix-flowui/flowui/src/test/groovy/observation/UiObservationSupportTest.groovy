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
import io.jmix.flowui.kit.action.BaseAction
import io.jmix.flowui.observation.UiObservationSupport
import io.jmix.flowui.view.View
import io.micrometer.observation.Observation
import io.micrometer.observation.tck.TestObservationRegistry
import spock.lang.Specification

class UiObservationSupportTest extends Specification {

    UiObservationSupport support

    def setup() {
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

    private UiObservationSupport createSupport(boolean enabled) {
        def props = Mock(UiProperties) { isUiObservationEnabled() >> enabled }
        def support = new UiObservationSupport(props)
        support.observationRegistry = TestObservationRegistry.create()
        return support
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
