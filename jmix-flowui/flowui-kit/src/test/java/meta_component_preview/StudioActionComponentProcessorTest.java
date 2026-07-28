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

package meta_component_preview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.meta.component.preview.processor.StudioActionComponentProcessor;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudioActionComponentProcessorTest {

    final StudioActionComponentProcessor processor = new StudioActionComponentProcessor();

    @Test
    void testAddActionAppendsWhenIndexNegativeAndInsertsAtIndexOtherwise() {
        ActionsHolder holder = new ActionsHolder();
        Action first = new BaseAction<>("first");
        Action second = new BaseAction<>("second");

        assertTrue(processor.addAction(holder, first, -1));
        assertTrue(processor.addAction(holder, second, 0));

        assertEquals(List.of(second, first), List.copyOf(holder.getActions()));
    }

    @Test
    void testRemoveActionDetachesByIdentity() {
        ActionsHolder holder = new ActionsHolder();
        Action action = new BaseAction<>("first");
        holder.addAction(action, 0);

        assertTrue(processor.removeAction(holder, action));

        assertNull(holder.getAction("first"));
    }

    @Test
    void testAddAndRemoveActionReturnFalseForNonHasActionsParent() {
        Component parent = new VerticalLayout();
        Action action = new BaseAction<>("id");

        assertFalse(processor.addAction(parent, action, -1));
        assertFalse(processor.removeAction(parent, action));
    }

    /**
     * Minimal {@link HasActions} test double: isolates these tests from a real implementer's
     * (e.g. {@code JmixGrid}) unrelated context-menu wiring.
     */
    private static class ActionsHolder extends Div implements HasActions {

        private final List<Action> actions = new ArrayList<>();

        @Override
        public void addAction(Action action, int index) {
            actions.add(index, action);
        }

        @Override
        public void removeAction(Action action) {
            actions.remove(action);
        }

        @Override
        public Collection<Action> getActions() {
            return List.copyOf(actions);
        }

        @Nullable
        @Override
        public Action getAction(String id) {
            return actions.stream()
                    .filter(action -> id.equals(action.getId()))
                    .findFirst()
                    .orElse(null);
        }
    }
}
