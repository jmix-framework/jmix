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

package action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ActionPerformTest {

    @Test
    void perform_baseActionWithListener_firesEventWithContextComponent() {
        Span span = new Span();
        BaseAction<?> action = new BaseAction<>("a");
        AtomicReference<ActionPerformedEvent> captured = new AtomicReference<>();
        action.addActionPerformedListener(captured::set);

        action.perform(new Action.Context(span));

        assertSame(span, captured.get().getComponent());
        assertSame(action, captured.get().getSource());
    }

    @Test
    void perform_legacyOverride_isInvokedWithContextComponent() {
        Span span = new Span();
        LegacyOverrideAction action = new LegacyOverrideAction();

        action.perform(new Action.Context(span));

        assertSame(span, action.performedWith);
    }

    @Test
    void perform_overrideCallingSuper_firesEvent() {
        Span span = new Span();
        Action.Context context = new Action.Context(span);
        PerformOverrideAction action = new PerformOverrideAction();
        AtomicReference<ActionPerformedEvent> captured = new AtomicReference<>();
        action.addActionPerformedListener(captured::set);

        action.perform(context);

        assertSame(context, action.performedWith);
        assertSame(span, captured.get().getComponent());
    }

    @Test
    void perform_overrideOfBoth_runsEachOnce() {
        BothOverrideAction action = new BothOverrideAction();

        action.perform(new Action.Context(new Span()));

        assertEquals(1, action.performCalls);
        assertEquals(1, action.actionPerformCalls);
    }

    @Test
    void buttonClick_performOverride_receivesButtonAsComponent() {
        PerformOverrideAction action = new PerformOverrideAction();
        JmixButton button = new JmixButton();
        button.setAction(action);

        button.click();

        assertSame(button, action.performedWith.getComponent());
    }

    @Test
    void buttonClick_performOverrideWithoutSuper_firesNoEvent() {
        PerformOverrideWithoutSuperAction action = new PerformOverrideWithoutSuperAction();
        AtomicReference<ActionPerformedEvent> captured = new AtomicReference<>();
        action.addActionPerformedListener(captured::set);
        JmixButton button = new JmixButton();
        button.setAction(action);

        button.click();

        assertNotNull(action.performedWith);
        assertNull(captured.get());
    }

    static class LegacyOverrideAction extends BaseAction<LegacyOverrideAction> {

        Component performedWith;

        LegacyOverrideAction() {
            super("legacy");
        }

        @Override
        public void actionPerform(Component component) {
            performedWith = component;
        }
    }

    static class PerformOverrideAction extends BaseAction<PerformOverrideAction> {

        Action.Context performedWith;

        PerformOverrideAction() {
            super("perform");
        }

        @Override
        public void perform(Context context) {
            performedWith = context;
            super.perform(context);
        }
    }

    static class PerformOverrideWithoutSuperAction extends BaseAction<PerformOverrideWithoutSuperAction> {

        Action.Context performedWith;

        PerformOverrideWithoutSuperAction() {
            super("performWithoutSuper");
        }

        @Override
        public void perform(Context context) {
            performedWith = context;
        }
    }

    static class BothOverrideAction extends BaseAction<BothOverrideAction> {

        int performCalls;
        int actionPerformCalls;

        BothOverrideAction() {
            super("both");
        }

        @Override
        public void perform(Context context) {
            performCalls++;
            super.perform(context);
        }

        @Override
        public void actionPerform(Component component) {
            actionPerformCalls++;
            super.actionPerform(component);
        }
    }
}
