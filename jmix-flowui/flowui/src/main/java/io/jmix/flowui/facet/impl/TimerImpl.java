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

import com.vaadin.flow.component.AttachEvent;
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

    private static final Logger log = LoggerFactory.getLogger(TimerImpl.class);

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
        Consumer<JmixTimer> wrapper = new JmixTimerActionListenerWrapper(listener);
        timerImpl.addActionListener(wrapper);
        return () -> timerImpl.removeActionListener(wrapper);
    }

    @Override
    public Registration addTimerStopListener(Consumer<TimerStopEvent> listener) {
        Consumer<JmixTimer> wrapper = new JmixTimerStopListenerWrapper(listener);
        timerImpl.addStopListener(wrapper);
        return () -> timerImpl.removeStopListeners(wrapper);
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
            registerInUI(owner);
        }
    }

    protected void registerInUI(View<?> owner) {
        if (owner.isAttached()) {
            attachTimerToUi(owner);
        } else {
            registerOnAttach(owner);
        }

        addDetachListener(owner);
    }

    protected void attachTimerToUi(View<?> owner) {
        HasComponents content = (HasComponents) owner.getContent();
        content.add(timerImpl);

//        UI.getCurrent().add(timerImpl);

//        owner.getUI().get().add(timerImpl);
//        AppUI appUI = (AppUI) ownerComponent.getUI();
//        appUI.addTimer(timerImpl);

        log.trace("Timer '{}' registered in UI ", getId());
    }

    protected void registerOnAttach(View<?> owner) {
        Registration registration = null;
        registration = owner.addAttachListener(new ComponentEventListener<>() {
            @Override
            public void onComponentEvent(AttachEvent event) {
                attachTimerToUi(owner);
                // execute attach listener only once
//                finalRegistration.remove();
            }
        });
    }

    protected void addDetachListener(View<?> owner) {
        owner.addDetachListener(event -> detachTimer(owner));
    }

    protected void detachTimer(View<?> owner) {
        HasComponents content = (HasComponents) owner.getContent();
        content.remove(timerImpl);

//        UI.getCurrent().remove(timerImpl);
//        if (timerImpl.getParent().isPresent()) {
//            timerImpl.remove();
//        }
        log.trace("Timer '{}' unregistered from UI ", TimerImpl.this.getId());
    }

    protected class JmixTimerActionListenerWrapper implements Consumer<JmixTimer> {

        private final Consumer<TimerActionEvent> listener;

        public JmixTimerActionListenerWrapper(Consumer<TimerActionEvent> listener) {
            this.listener = listener;
        }

        @Override
        public void accept(JmixTimer sender) {
            try {
                listener.accept(new TimerActionEvent(TimerImpl.this));
            } catch (RuntimeException e) {
                throw new RuntimeException("Exception on timer action", e);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }

            JmixTimerActionListenerWrapper that = (JmixTimerActionListenerWrapper) obj;

            return this.listener.equals(that.listener);
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    protected class JmixTimerStopListenerWrapper implements Consumer<JmixTimer> {

        private final Consumer<TimerStopEvent> listener;

        public JmixTimerStopListenerWrapper(Consumer<TimerStopEvent> listener) {
            this.listener = listener;
        }

        @Override
        public void accept(JmixTimer sender) {
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

            JmixTimerStopListenerWrapper that = (JmixTimerStopListenerWrapper) obj;

            return this.listener.equals(that.listener);
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }
}
