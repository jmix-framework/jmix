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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;

@Tag("jmix-timer")
@JsModule("./src/timer/jmix-timer.js")
public class JmixTimer extends Component {

    protected static final String DELAY_PROPERTY_NAME = "delay";
    protected static final String REPEATING_PROPERTY_NAME = "repeating";
    protected static final String AUTOSTART_PROPERTY_NAME = "autostart";

    public void setRepeating(boolean repeating) {
        getElement().setProperty(REPEATING_PROPERTY_NAME, repeating);
    }

    public boolean isRepeating() {
        return getElement().getProperty(REPEATING_PROPERTY_NAME, false);
    }

    public int getDelay() {
        return getElement().getProperty(DELAY_PROPERTY_NAME, 0);
    }

    public void setDelay(int delay) {
        getElement().setProperty(DELAY_PROPERTY_NAME, delay);
    }

    public void setAutostart(boolean autostart) {
        getElement().setProperty(AUTOSTART_PROPERTY_NAME, autostart);
    }

    public boolean isAutostart() {
        return getElement().getProperty(AUTOSTART_PROPERTY_NAME, false);
    }

    public void start() {
        if (getDelay() <= 0) {
            throw new IllegalStateException("Undefined delay for timer");
        }
        getElement().callJsFunction("start");
    }

    public void stop() {
        getElement().callJsFunction("stop");
    }

    public Registration addActionListener(ComponentEventListener<JmixTimerTickEvent> listener) {
        return getEventBus().addListener(JmixTimerTickEvent.class, listener);
    }

    public Registration addStopListener(ComponentEventListener<JmixTimerStopEvent> listener) {
        return getEventBus().addListener(JmixTimerStopEvent.class, listener);
    }

    @DomEvent("jmix-timer-tick")
    public static class JmixTimerTickEvent extends ComponentEvent<JmixTimer> {

        public JmixTimerTickEvent(JmixTimer source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("jmix-timer-stop")
    public static class JmixTimerStopEvent extends ComponentEvent<JmixTimer> {

        public JmixTimerStopEvent(JmixTimer source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}