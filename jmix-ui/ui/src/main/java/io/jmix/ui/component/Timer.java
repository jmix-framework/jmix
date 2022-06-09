/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.PositiveOrZero;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Client-side timer component that fires events at fixed intervals.
 */
@StudioFacet(
        xmlElement = "timer",
        caption = "Timer",
        description = "Fires events at fixed intervals",
        defaultProperty = "id",
        category = "Facets",
        icon = "io/jmix/ui/icon/facet/timer.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/facets/timer.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", type = PropertyType.COMPONENT_ID, required = true),
                @StudioProperty(name = "autostart", type = PropertyType.BOOLEAN, defaultValue = "false")
        }
)
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
    @StudioProperty(defaultValue = "false")
    void setRepeating(boolean repeating);

    /**
     * @return delay in milliseconds.
     */
    int getDelay();

    /**
     * @param delayMs delay in milliseconds.
     */
    @StudioProperty(required = true)
    @PositiveOrZero
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
     * @param listener listener
     * @return subscription
     */
    Subscription addTimerActionListener(Consumer<TimerActionEvent> listener);

    /**
     * Adds {@link TimerStopEvent} listener.
     *
     * @param listener listener
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