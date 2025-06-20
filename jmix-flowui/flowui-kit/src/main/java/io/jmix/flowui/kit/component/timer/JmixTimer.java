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

package io.jmix.flowui.kit.component.timer;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;

/**
 * The JmixTimer class provides a server-side component for managing timer-based
 * execution. It enables configuration of delayed actions, optionally repeating
 * at defined intervals.
 * <p>
 * This component is designed to handle timed tasks, which can be triggered
 * either once after a specified delay or periodically when repeating is enabled.
 */
@Tag("jmix-timer")
@JsModule("./src/timer/jmix-timer.js")
public class JmixTimer extends Component {

    protected static final String DELAY_PROPERTY_NAME = "delay";
    protected static final String REPEATING_PROPERTY_NAME = "repeating";
    protected static final String AUTOSTART_PROPERTY_NAME = "autostart";

    /**
     * @return {@code true} if repeated executions are enabled, {@code false} otherwise
     */
    public boolean isRepeating() {
        return getElement().getProperty(REPEATING_PROPERTY_NAME, false);
    }

    /**
     * Sets whether repeated executions of the timer are turned on. If the attribute
     * is set to {@code true}, the timer runs in cycles at equal intervals defined
     * in the {@code delay} attribute. Otherwise, the timer runs only once after the
     * timeout specified in the {@code delay} attribute after the timer start.
     * The Default value is {@code false}.
     *
     * @param repeating {@code true} to enable repeated executions, {@code false} otherwise
     */
    public void setRepeating(boolean repeating) {
        getElement().setProperty(REPEATING_PROPERTY_NAME, repeating);
    }

    /**
     * @return timer interval in milliseconds
     */
    public int getDelay() {
        return getElement().getProperty(DELAY_PROPERTY_NAME, 0);
    }

    /**
     * Sets timer interval in milliseconds.
     *
     * @param delay timer interval in milliseconds
     */
    public void setDelay(int delay) {
        getElement().setProperty(DELAY_PROPERTY_NAME, delay);
    }

    /**
     * @return whether the timer starts automatically
     */
    public boolean isAutostart() {
        return getElement().getProperty(AUTOSTART_PROPERTY_NAME, false);
    }

    /**
     * Sets whether to start the timer automatically. When it is set to {@code true},
     * the timer starts immediately after the view opening. The default value is
     * {@code false}, which means that the timer will start only when its {@link #start()}
     * method is invoked.
     *
     * @param autostart whether to start the timer automatically
     */
    public void setAutostart(boolean autostart) {
        getElement().setProperty(AUTOSTART_PROPERTY_NAME, autostart);
    }

    /**
     * Starts the timer if it is not running.
     */
    public void start() {
        if (getDelay() <= 0) {
            throw new IllegalStateException("Undefined delay for timer");
        }
        getElement().callJsFunction("start");
    }

    /**
     * Stops timer if it is running.
     */
    public void stop() {
        getElement().callJsFunction("stop");
    }

    /**
     * Adds an action listener.
     *
     * @param listener a listener to add
     * @return a registration handle to remove the listener
     */
    public Registration addActionListener(ComponentEventListener<JmixTimerTickEvent> listener) {
        return getEventBus().addListener(JmixTimerTickEvent.class, listener);
    }

    /**
     * Adds timer stop listener.
     *
     * @param listener a listener to add
     * @return a registration handle to remove the listener
     */
    public Registration addStopListener(ComponentEventListener<JmixTimerStopEvent> listener) {
        return getEventBus().addListener(JmixTimerStopEvent.class, listener);
    }

    /**
     * Event that is sent after the specified time interval in the {@code delay} attribute
     * has passed since the timer started. If the {@code repeating} attribute is set
     * to {@code true}, this event is sent periodically, until the timer is stopped.
     */
    @DomEvent("jmix-timer-tick")
    public static class JmixTimerTickEvent extends ComponentEvent<JmixTimer> {

        public JmixTimerTickEvent(JmixTimer source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * Event that is sent when the timer is stopped by invoking the {@link #stop()} method.
     */
    @DomEvent("jmix-timer-stop")
    public static class JmixTimerStopEvent extends ComponentEvent<JmixTimer> {

        public JmixTimerStopEvent(JmixTimer source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}