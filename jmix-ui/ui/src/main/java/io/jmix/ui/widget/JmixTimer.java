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

package io.jmix.ui.widget;

import io.jmix.ui.widget.client.timer.JmixTimerClientRpc;
import io.jmix.ui.widget.client.timer.JmixTimerServerRpc;
import io.jmix.ui.widget.client.timer.JmixTimerState;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JmixTimer extends AbstractExtension implements JmixTimerServerRpc {

    private static final Logger log = LoggerFactory.getLogger(JmixTimer.class);

    protected List<Consumer<JmixTimer>> actionListeners = new ArrayList<>(2);
    protected List<Consumer<JmixTimer>> stopListeners; // lazily initialized

    public JmixTimer() {
        registerRpc(this);
    }

    public void extend(AbstractComponent component) {
        super.extend(component);
    }

    @Override
    public JmixTimerState getState() {
        return (JmixTimerState) super.getState();
    }

    @Override
    protected JmixTimerState getState(boolean markAsDirty) {
        return (JmixTimerState) super.getState(markAsDirty);
    }

    public void setRepeating(boolean repeating) {
        getState().repeating = repeating;
    }

    public boolean isRepeating() {
        return getState(false).repeating;
    }

    public int getDelay() {
        return getState(false).delay;
    }

    public void setDelay(int delay) {
        getState().delay = delay;
    }

    public void start() {
        if (getDelay() <= 0) {
            throw new IllegalStateException("Undefined delay for timer");
        }

        if (!getState(false).running) {
            getRpcProxy(JmixTimerClientRpc.class).setRunning(true);

            getState().running = true;
        }
    }

    @SuppressWarnings("unchecked")
    public void stop() {
        if (getState(false).running) {
            getRpcProxy(JmixTimerClientRpc.class).setRunning(false);

            if (stopListeners != null) {
                for (Object listener : stopListeners.toArray()) {
                    ((Consumer<JmixTimer>) listener).accept(this);
                }
            }
            getState().running = false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onTimer() {
        try {
            long startTime = System.currentTimeMillis();

            for (Object listener : actionListeners.toArray()) {
                ((Consumer<JmixTimer>) listener).accept(this);
            }

            long endTime = System.currentTimeMillis();
            if (System.currentTimeMillis() - startTime > 2000) {
                long duration = endTime - startTime;
                log.warn("Too long timer {} processing: {} ms ", getLoggingTimerId(), duration);
            }
        } finally {
            getRpcProxy(JmixTimerClientRpc.class).requestCompleted();
        }
    }

    protected String getLoggingTimerId() {
        String timerId = "<noid>";
        if (getState(false).timerId != null) {
            timerId = getState(false).timerId;
        }
        return timerId;
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        getState().listeners = actionListeners.size() > 0 || (stopListeners != null && stopListeners.size() > 0);
    }

    public void setTimerId(@Nullable String id) {
        getState().timerId = id;
    }

    public void addActionListener(Consumer<JmixTimer> listener) {
        if (!actionListeners.contains(listener)) {
            actionListeners.add(listener);

            markAsDirty();
        }
    }

    public void removeActionListener(Consumer<JmixTimer> listener) {
        if (actionListeners.remove(listener)) {
            markAsDirty();
        }
    }

    public void addStopListener(Consumer<JmixTimer> listener) {
        if (stopListeners == null) {
            stopListeners = new ArrayList<>();
        }
        if (!stopListeners.contains(listener)) {
            stopListeners.add(listener);

            markAsDirty();
        }
    }

    public void removeStopListeners(Consumer<JmixTimer> listener) {
        if (stopListeners != null) {
            if (stopListeners.remove(listener)) {
                markAsDirty();
            }
        }
    }
}