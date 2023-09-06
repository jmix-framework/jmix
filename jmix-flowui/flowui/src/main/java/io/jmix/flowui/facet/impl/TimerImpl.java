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

package io.jmix.flowui.facet.impl;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.timer.JmixTimer;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class TimerImpl extends AbstractFacet implements Timer {

    protected JmixTimer timerImpl;

    public TimerImpl() {
        timerImpl = createComponent();
    }

    protected JmixTimer createComponent() {
        return new JmixTimer();
    }

    @Override
    public void start() {
        timerImpl.start();
    }

    @Override
    public void stop() {
        timerImpl.stop();
    }

    @Override
    public boolean isRepeating() {
        return timerImpl.isRepeating();
    }

    @Override
    public void setRepeating(boolean repeating) {
        timerImpl.setRepeating(repeating);
    }

    @Override
    public int getDelay() {
        return timerImpl.getDelay();
    }

    @Override
    public void setDelay(int delay) {
        timerImpl.setDelay(delay);
    }

    @Override
    public Registration addTimerActionListener(Consumer<TimerActionEvent> listener) {
        ComponentEventListener<JmixTimer.JmixTimerTickEvent> adapter = new JmixTimerActionListenerAdapter(listener);
        return timerImpl.addActionListener(adapter);
    }

    @Override
    public Registration addTimerStopListener(Consumer<TimerStopEvent> listener) {
        ComponentEventListener<JmixTimer.JmixTimerStopEvent> adapter = new JmixTimerStopListenerAdapter(listener);
        return timerImpl.addStopListener(adapter);
    }

    @Override
    public void setId(@Nullable String id) {
        super.setId(id);
        timerImpl.setId(id);
    }

    @Override
    public void setOwner(@Nullable View<?> owner) {
        super.setOwner(owner);

        if (owner != null) {
            if (owner.getContent() instanceof HasComponents) {
                //noinspection unchecked
                registerInView((View<? extends HasComponents>) owner);
            }
        }
    }

    protected void registerInView(View<? extends HasComponents> owner) {
        if (owner.isAttached()) {
            attachTimer(owner);
        } else {
            registerOnAttach(owner);
        }
        addDetachListener(owner);
    }

    protected void attachTimer(View<? extends HasComponents> owner) {
        owner.getContent().add(timerImpl);
    }

    protected void registerOnAttach(View<? extends HasComponents> owner) {
        owner.addAttachListener(e -> attachTimer(owner));
    }

    protected void addDetachListener(View<? extends HasComponents> owner) {
        owner.addDetachListener(e -> owner.getContent().remove(timerImpl));
    }

    protected class JmixTimerActionListenerAdapter implements ComponentEventListener<JmixTimer.JmixTimerTickEvent> {

        private static final Logger log = LoggerFactory.getLogger(JmixTimerActionListenerAdapter.class);

        protected static final int EVENT_PROCESSING_WARNING_TIME_MS = 2000;

        protected Consumer<TimerActionEvent> listener;

        public JmixTimerActionListenerAdapter(Consumer<TimerActionEvent> listener) {
            this.listener = listener;
        }

        @Override
        public void onComponentEvent(JmixTimer.JmixTimerTickEvent event) {
            long startTime = System.currentTimeMillis();

            listener.accept(new TimerActionEvent(TimerImpl.this));

            long duration = System.currentTimeMillis() - startTime;
            if (duration > EVENT_PROCESSING_WARNING_TIME_MS) {
                log.warn("Too long timer {} processing: {} ms ", getTimerIdToLog(event.getSource()), duration);
            }
        }

        protected String getTimerIdToLog(JmixTimer timer) {
            return timer.getId().orElse("<noid>");
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }

            JmixTimerActionListenerAdapter that = (JmixTimerActionListenerAdapter) obj;

            return this.listener.equals(that.listener);
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    protected class JmixTimerStopListenerAdapter implements ComponentEventListener<JmixTimer.JmixTimerStopEvent> {

        private final Consumer<TimerStopEvent> listener;

        public JmixTimerStopListenerAdapter(Consumer<TimerStopEvent> listener) {
            this.listener = listener;
        }

        @Override
        public void onComponentEvent(JmixTimer.JmixTimerStopEvent event) {
            listener.accept(new TimerStopEvent(TimerImpl.this));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }

            JmixTimerStopListenerAdapter that = (JmixTimerStopListenerAdapter) obj;

            return this.listener.equals(that.listener);
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }
}
