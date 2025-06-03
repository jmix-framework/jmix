/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.sys.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.sys.autowire.EventListenerDependencyInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.context.event.GenericApplicationListenerAdapter;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @see UiEventPublisher
 * @see EventListenerDependencyInjector
 * @see UiEventListenerMethodAdapter
 */
@Internal
public class UiEventsManager {

    private static final Logger log = LoggerFactory.getLogger(UiEventsManager.class);

    protected Set<ComponentListeners> listeners = ConcurrentHashMap.newKeySet();

    /**
     * Adds application listener that should be invoked when corresponding application event is fired.
     *
     * @param component the component that contains listener definition.
     * @param listener  application listener
     */
    public void addApplicationListener(Component component, ApplicationListener<?> listener) {
        log.debug("Adding application listeners for '{}'", component);

        ComponentListeners componentListeners = listeners.stream()
                .filter(item -> item.isSameComponent(component))
                .findFirst()
                .orElseGet(() -> {
                    ComponentListeners newItem = new ComponentListeners(component, item -> listeners.remove(item));
                    newItem.addListener(listener);
                    listeners.add(newItem);
                    return newItem;
                });

        if (!componentListeners.contains(listener)) {
            componentListeners.addListener(listener);
        }
    }

    /**
     * Removes application listener.
     *
     * @param listener listener to remove
     */
    public void removeApplicationListener(ApplicationListener<?> listener) {
        listeners.stream()
                .filter(componentListeners -> componentListeners.contains(listener))
                .findFirst().ifPresent(componentListeners -> {
                    log.debug("Removing application listeners for '{}'", componentListeners.getComponent());

                    componentListeners.removeListener(listener);

                    if (componentListeners.isEmpty()) {
                        listeners.remove(componentListeners);
                    }
                });
    }

    /**
     * Disables all application listeners for the provided component. These listeners can be enabled later.
     *
     * @param component the component that contains listener definitions to disable
     */
    public void disableApplicationListenersFor(Component component) {
        listeners.stream()
                .filter(cs -> cs.getComponent().equals(component))
                .findFirst()
                .ifPresent(cs -> cs.setEnabled(false));
    }

    /**
     * Enables all application listeners for the provided component.
     *
     * @param component the component that contains listener definitions to enable
     */
    public void enableApplicationListenersFor(Component component) {
        listeners.stream()
                .filter(cs -> cs.getComponent().equals(component))
                .findFirst()
                .ifPresent(cs -> cs.setEnabled(true));
    }

    /**
     * Removes all application listeners that have definition in the given component.
     *
     * @param component component
     */
    public void removeApplicationListeners(Component component) {
        log.debug("Removing application listeners for '{}'", component);

        listeners.stream().filter(componentListeners -> componentListeners.isSameComponent(component))
                .findFirst()
                .ifPresent(componentListeners -> listeners.remove(componentListeners));
    }

    /**
     * Removes all application listeners for all components.
     */
    public void removeAllApplicationListeners() {
        listeners.clear();
    }

    /**
     * Publishes application event for provided UIs. Empty collection of UIs means iterating all existing UIs in the
     * current session.
     *
     * @param uis   collection of UIs
     * @param event event to publish
     */
    public void publish(Collection<UI> uis, ApplicationEvent event) {
        Object source = event.getSource();
        Class<?> sourceType = (source != null ? source.getClass() : null);

        ResolvableType type = resolveDefaultEventType(event);
        for (ApplicationListener<?> listener : retrieveApplicationListeners(uis, type, sourceType)) {
            invokeListener(listener, event);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void invokeListener(ApplicationListener listener, ApplicationEvent event) {
        try {
            listener.onApplicationEvent(event);
        } catch (ClassCastException ex) {
            String msg = ex.getMessage();
            if (msg == null || msg.startsWith(event.getClass().getName())) {
                // Possibly a lambda-defined listener which we could not resolve the generic event type for
                log.debug("Non-matching event type for listener: {}", listener, ex);
            } else {
                throw ex;
            }
        }
    }

    protected ResolvableType resolveDefaultEventType(ApplicationEvent event) {
        return ResolvableType.forInstance(event);
    }

    /**
     * Retrieves application listeners from UIs that support provided event type. Empty collection of UIs means
     * iterating all existing UIs in the current session.
     *
     * @param uis        collection of UIs
     * @param eventType  type of event
     * @param sourceType type of source
     * @return collection of application listeners that supports provided event type
     */
    protected Collection<ApplicationListener<?>> retrieveApplicationListeners(Collection<UI> uis,
                                                                              ResolvableType eventType,
                                                                              @Nullable Class<?> sourceType) {
        List<ApplicationListener<?>> filtered = new ArrayList<>();

        for (ComponentListeners componentListeners : listeners) {
            if (!componentListeners.isEnabled()) {
                continue;
            }

            UI ui = componentListeners.getComponent().getUI().orElse(null);
            if (uis.isEmpty()
                    || (ui != null && uis.contains(ui))) {
                filtered.addAll(filterComponentListeners(componentListeners, eventType, sourceType));
            }
        }
        return filtered;
    }

    protected Collection<ApplicationListener<?>> filterComponentListeners(ComponentListeners componentListeners,
                                                                          ResolvableType eventType,
                                                                          @Nullable Class<?> sourceType) {
        return componentListeners.getListeners()
                .stream()
                .filter(listener -> supportsEvent(listener, eventType, sourceType))
                .collect(Collectors.toList());
    }

    protected boolean supportsEvent(ApplicationListener<?> listener, ResolvableType eventType,
                                    @Nullable Class<?> sourceType) {
        GenericApplicationListener smartListener = (listener instanceof GenericApplicationListener ?
                (GenericApplicationListener) listener : new GenericApplicationListenerAdapter(listener));
        return (smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType));
    }

    protected static class ComponentListeners {

        protected boolean enabled = true;
        protected Component component;
        protected List<ApplicationListener<?>> listeners;

        public ComponentListeners(Component component, Consumer<ComponentListeners> uiDetachHandler) {
            this.component = component;

            listeners = new ArrayList<>();

            component.getUI().ifPresent(ui ->
                    ui.addDetachListener(event -> uiDetachHandler.accept(ComponentListeners.this)));
        }

        public Component getComponent() {
            return component;
        }

        public boolean isSameComponent(Component component) {
            return this.component.equals(component);
        }

        public void removeListener(ApplicationListener<?> listener) {
            listeners.remove(listener);
        }

        public void addListener(ApplicationListener<?> listener) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        public boolean contains(ApplicationListener<?> listener) {
            return listeners.contains(listener);
        }

        public boolean isEmpty() {
            return listeners.isEmpty();
        }

        public List<ApplicationListener<?>> getListeners() {
            return listeners;
        }

        public boolean isEnabled() {
            return enabled && component.getUI().isPresent();
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
