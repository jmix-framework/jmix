/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.components.compatibility.TimerActionListenerWrapper;
import com.haulmont.cuba.gui.components.compatibility.TimerStopListenerWrapper;

import java.util.function.Consumer;

@Deprecated
public interface Timer extends io.jmix.ui.component.Timer {

    /**
     * Removes {@link TimerActionEvent} listener.
     *
     * @param listener listener
     * @deprecated Use subscription object instead
     */
    @Deprecated
    void removeTimerActionListener(Consumer<TimerActionEvent> listener);

    /**
     * Removes {@link TimerStopEvent} listener.
     *
     * @param listener listener
     * @deprecated Use subscription object instead
     */
    @Deprecated
    void removeTimerStopListener(Consumer<TimerStopEvent> listener);

    /**
     * Listener for timer events.
     *
     * @deprecated Use {@link #addTimerActionListener(Consumer)} with lambda instead.
     */
    @Deprecated
    interface ActionListener {
        void timerAction(Timer timer);
    }

    /**
     * Listener for timer stop event.
     *
     * @deprecated Use {@link #addTimerStopListener(Consumer)} with lambda instead.
     */
    @Deprecated
    interface StopListener {
        void timerStopped(Timer timer);
    }

    /**
     * @deprecated Use {@link #addTimerActionListener(Consumer)} instead
     */
    @Deprecated
    default void addActionListener(ActionListener listener) {
        addTimerActionListener(new TimerActionListenerWrapper(listener));
    }

    /**
     * @deprecated Use {@link #removeTimerActionListener(Consumer)} instead
     */
    @Deprecated
    default void removeActionListener(ActionListener listener) {
        removeTimerActionListener(new TimerActionListenerWrapper(listener));
    }

    /**
     * @deprecated Use {@link #addTimerStopListener(Consumer)} instead
     */
    @Deprecated
    default void addStopListener(StopListener listener) {
        addTimerStopListener(new TimerStopListenerWrapper(listener));
    }

    /**
     * @deprecated Use {@link #removeTimerStopListener(Consumer)} instead
     */
    @Deprecated
    default void removeStopListener(StopListener listener) {
        removeTimerStopListener(new TimerStopListenerWrapper(listener));
    }
}
