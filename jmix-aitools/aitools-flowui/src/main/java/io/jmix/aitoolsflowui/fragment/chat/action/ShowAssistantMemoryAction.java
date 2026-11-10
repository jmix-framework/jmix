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

package io.jmix.aitoolsflowui.fragment.chat.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import org.jspecify.annotations.NullMarked;

/**
 * Shows the messages the assistant keeps in memory for the conversation. The action's text carries the number of
 * kept messages, so the user sees the memory filling up before it is full. The chat updates it through
 * {@link #setKeptCount(int)}. A listener added to the action replaces the default behaviour of showing the
 * memory.
 */
@NullMarked
public class ShowAssistantMemoryAction extends BaseAction<ShowAssistantMemoryAction> {

    public static final String ID = "aitls_showAssistantMemory";

    protected final SerializableFunction<Integer, String> textProvider;
    protected final SerializableRunnable showHandler;

    /**
     * @param id           the action id
     * @param textProvider builds the action's text from the number of kept messages
     * @param showHandler  shows the assistant memory
     */
    public ShowAssistantMemoryAction(String id, SerializableFunction<Integer, String> textProvider,
                                     SerializableRunnable showHandler) {
        super(id);
        this.textProvider = textProvider;
        this.showHandler = showHandler;

        this.text = textProvider.apply(0);
    }

    /**
     * Sets the number of kept messages and updates the action's text.
     *
     * @param keptCount the number of messages kept in the assistant memory
     */
    public void setKeptCount(int keptCount) {
        setText(textProvider.apply(keptCount));
    }

    @Override
    public void actionPerform(Component component) {
        if (!hasListener(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    protected void execute() {
        if (isEnabled() && isVisible()) {
            showHandler.run();
        }
    }
}
