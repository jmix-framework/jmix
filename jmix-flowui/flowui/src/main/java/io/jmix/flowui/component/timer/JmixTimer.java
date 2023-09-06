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

package io.jmix.flowui.component.timer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Tag("jmix-timer")
@JsModule("./src/timer/jmix-timer.js")
public class JmixTimer extends Component {

    private static final Logger log = LoggerFactory.getLogger(JmixTimer.class);

    protected static final int EVENT_PROCESSING_WARNING_TIME_MS = 2000;
    protected static final String DELAY_PROPERTY_NAME = "delay";
    protected static final String REPEATING_PROPERTY_NAME = "repeating";

    protected List<Consumer<JmixTimer>> actionListeners = new ArrayList<>(1);
    protected List<Consumer<JmixTimer>> stopListeners; // lazily initialized

    public JmixTimer() {
        ComponentUtil.addListener(this, JmixTimerTickEvent.class, this::onTimerTick);
        ComponentUtil.addListener(this, JmixTimerStopEvent.class, this::onTimerStop);
    }

    protected void onTimerTick(JmixTimerTickEvent event) {
        long startTime = System.currentTimeMillis();

        for (Consumer<JmixTimer> listener : actionListeners) {
            listener.accept(this);
        }

        long duration = System.currentTimeMillis() - startTime;
        if (duration > EVENT_PROCESSING_WARNING_TIME_MS) {
            log.warn("Too long timer {} processing: {} ms ", getTimerIdToLog(), duration);
        }
    }

    protected String getTimerIdToLog() {
        return getId().orElse("<noid>");
    }

    protected void onTimerStop(JmixTimerStopEvent event) {
        if (stopListeners != null) {
            for (Consumer<JmixTimer> listener : stopListeners) {
                listener.accept(this);
            }
        }
    }

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

    public void start() {
        if (getDelay() <= 0) {
            throw new IllegalStateException("Undefined delay for timer");
        }
        getElement().callJsFunction("start");
    }

    public void stop() {
        getElement().callJsFunction("stop");
    }

    public void addActionListener(Consumer<JmixTimer> listener) {
        if (!actionListeners.contains(listener)) {
            actionListeners.add(listener);
        }
    }

    public void removeActionListener(Consumer<JmixTimer> listener) {
        actionListeners.remove(listener);
    }

    public void addStopListener(Consumer<JmixTimer> listener) {
        if (stopListeners == null) {
            stopListeners = new ArrayList<>();
        }
        if (!stopListeners.contains(listener)) {
            stopListeners.add(listener);
        }
    }

    public void removeStopListeners(Consumer<JmixTimer> listener) {
        if (stopListeners != null) {
            stopListeners.remove(listener);
        }
    }

    @DomEvent("jmix-timer-tick")
    protected static class JmixTimerTickEvent extends ComponentEvent<JmixTimer> {

        public JmixTimerTickEvent(JmixTimer source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("jmix-timer-stop")
    protected static class JmixTimerStopEvent extends ComponentEvent<JmixTimer> {

        public JmixTimerStopEvent(JmixTimer source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}