/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.builder;

import io.jmix.flowui.view.CloseAction;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;

import java.util.EventObject;

/**
 * Application event that encapsulates information about a view that has been closed.
 * It contains information about the source view and the associated close action.
 *
 * @param <V> the type of the view that is the source of this event
 */
public class ViewAfterCloseEvent<V extends View<?>> extends EventObject {

    protected final CloseAction closeAction;

    public ViewAfterCloseEvent(V source, CloseAction closeAction) {
        super(source);

        this.closeAction = closeAction;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getSource() {
        return ((V) super.getSource());
    }

    /**
     * Returns the close action associated with the event.
     *
     * @return the close action associated with the event
     */
    public CloseAction getCloseAction() {
        return closeAction;
    }

    /**
     * Returns whether the view was closed with the specified close action.
     *
     * @param outcome the close action to check against the close action associated with the event
     * @return whether the view was closed with the specified close action
     */
    public boolean closedWith(StandardOutcome outcome) {
        return outcome.getCloseAction().equals(closeAction);
    }
}
