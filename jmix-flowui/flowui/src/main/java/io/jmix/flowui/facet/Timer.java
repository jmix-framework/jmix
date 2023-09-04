/*
 * Copyright 2023 Haulmont.
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
package io.jmix.flowui.facet;

import io.jmix.core.common.event.Subscription;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Client-side timer component that fires events at fixed intervals.
 */
public interface Timer extends Facet {

    String NAME = "timer";

    /**
     * @return true if timer action is repetitive
     */
    boolean isRepeating();

    /**
     * Sets repetitive mode for timer action.
     *
     * @param repeating repeating flag
     */
    void setRepeating(boolean repeating);

    /**
     * @return delay in milliseconds.
     */
    int getDelay();

    /**
     * @param delayMs delay in milliseconds.
     */
    void setDelay(int delayMs);

    /**
     * Starts timer. If timer is already started call will be ignored.
     */
    void start();

    /**
     * Stops timer if it is running.
     */
    void stop();

    /**
     * Adds {@link TimerActionEvent} listener.
     *
     * @param listener {@link TimerActionEvent} listener
     * @return subscription
     */
    Subscription addTimerActionListener(Consumer<TimerActionEvent> listener);

    /**
     * Adds {@link TimerStopEvent} listener.
     *
     * @param listener {@link TimerStopEvent} listener
     * @return subscription
     */
    Subscription addTimerStopListener(Consumer<TimerStopEvent> listener);

    /**
     * Event fired on timer tick.
     *
     * @see #addTimerActionListener(Consumer)
     */
    class TimerActionEvent extends EventObject {

        public TimerActionEvent(Timer source) {
            super(source);
        }

        @Override
        public Timer getSource() {
            return (Timer) super.getSource();
        }
    }

    /**
     * Event fired on timer stop after {@link #stop()} call.
     *
     * @see #addTimerStopListener(Consumer)
     */
    class TimerStopEvent extends EventObject {

        public TimerStopEvent(Timer source) {
            super(source);
        }

        @Override
        public Timer getSource() {
            return (Timer) super.getSource();
        }
    }
}