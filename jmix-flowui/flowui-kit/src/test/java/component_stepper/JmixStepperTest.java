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

package component_stepper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import io.jmix.flowui.kit.component.stepper.StepperStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JmixStepperTest {

    @Test
    void add_firstVisibleStep_isSelected() {
        TestStepper stepper = new TestStepper();
        StepperStep hidden = step("hidden");
        hidden.setVisible(false);
        StepperStep first = step("first");

        stepper.add(hidden);
        stepper.add(first);

        assertSame(first, stepper.getSelectedStep());
    }

    @Test
    void add_atIndex_keepsStepElementsInStepOrder() {
        TestStepper stepper = new TestStepper();
        StepperStep a = step("a");
        StepperStep c = step("c");
        StepperStep b = step("b");
        stepper.add(a);
        stepper.add(c);

        stepper.add(b, 1);

        assertEquals(List.of(a, b, c), stepper.getSteps());
        List<Element> stepElements = stepper.getElement().getChildren()
                .filter(element -> "jmix-step".equals(element.getTag()))
                .toList();
        assertEquals(List.of(a.getElement(), b.getElement(), c.getElement()), stepElements);
    }

    @Test
    void stepStates_followSelectionAndFurthestReachedStep() {
        TestStepper stepper = new TestStepper();
        StepperStep a = step("a");
        StepperStep b = step("b");
        StepperStep c = step("c");
        StepperStep d = step("d");
        stepper.add(a);
        stepper.add(b);
        stepper.add(c);
        stepper.add(d);

        stepper.select(c);
        stepper.select(b);

        assertEquals("completed", state(a));
        assertEquals("current", state(b));
        assertEquals("visited", state(c));
        assertEquals("unavailable", state(d));
    }

    @Test
    void numberAndLast_countVisibleStepsOnly() {
        TestStepper stepper = new TestStepper();
        StepperStep a = step("a");
        StepperStep b = step("b");
        StepperStep c = step("c");
        stepper.add(a);
        stepper.add(b);
        stepper.add(c);

        b.setVisible(false);

        assertEquals(1, a.getElement().getProperty("number", 0));
        assertEquals(2, c.getElement().getProperty("number", 0));
        assertTrue(c.getElement().getProperty("last", false));

        c.setVisible(false);

        assertTrue(a.getElement().getProperty("last", false));
    }

    @Test
    void hidingSelectedStep_selectsNearestVisibleStepBefore() {
        TestStepper stepper = new TestStepper();
        StepperStep a = step("a");
        StepperStep b = step("b");
        stepper.add(a);
        stepper.add(b);
        stepper.select(b);

        b.setVisible(false);

        assertSame(a, stepper.getSelectedStep());
    }

    @Test
    void hidingSelectedFirstStep_selectsNearestVisibleStepAfter() {
        TestStepper stepper = new TestStepper();
        StepperStep a = step("a");
        StepperStep b = step("b");
        stepper.add(a);
        stepper.add(b);

        a.setVisible(false);

        assertSame(b, stepper.getSelectedStep());
    }

    @Test
    void removingSelectedStep_selectsNearestVisibleStepBefore() {
        TestStepper stepper = new TestStepper();
        StepperStep a = step("a");
        StepperStep b = step("b");
        stepper.add(a);
        stepper.add(b);
        stepper.select(b);

        stepper.remove(b);

        assertSame(a, stepper.getSelectedStep());
        assertFalse(stepper.getElement().getChildren().anyMatch(b.getElement()::equals));
    }

    @Test
    void content_onlySelectedStepContentIsRenderedAndEnabled() {
        TestStepper stepper = new TestStepper();
        StepperStep a = stepWithContent("a");
        StepperStep b = stepWithContent("b");
        stepper.add(a);
        stepper.add(b);

        assertActiveContent(a);
        assertInactiveContent(b);

        stepper.select(b);

        assertInactiveContent(a);
        assertActiveContent(b);
    }

    private static StepperStep step(String id) {
        StepperStep step = new StepperStep();
        step.setId(id);
        return step;
    }

    private static StepperStep stepWithContent(String id) {
        StepperStep step = step(id);
        step.setContent(new Div());
        return step;
    }

    private static String state(StepperStep step) {
        return step.getElement().getProperty("state");
    }

    private static void assertActiveContent(StepperStep step) {
        Component content = step.getContent();
        assertNotNull(content);
        assertFalse(content.getElement().hasAttribute("slot"));
        assertTrue(content.getElement().getNode().isEnabledSelf());
    }

    private static void assertInactiveContent(StepperStep step) {
        Component content = step.getContent();
        assertNotNull(content);
        assertEquals("inactive", content.getElement().getAttribute("slot"));
        assertFalse(content.getElement().getNode().isEnabledSelf());
    }
}
