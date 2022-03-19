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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.event.TriggerOnce;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.impl.FrameImplementation;
import io.jmix.ui.sys.TestIdManager;
import io.jmix.ui.sys.event.UiEventsMulticaster;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CompositeComponent<T extends Component>
        implements Component, Component.BelongToFrame, AttachNotifier, HasDebugId, HasHtmlSanitizer {

    protected String id;
    protected T root;
    protected Frame frame;

    protected String prefixId;

    protected ApplicationContext applicationContext;

    // private, lazily initialized
    private EventHub eventHub = null;

    // Global event listeners
    private List<ApplicationListener> uiEventListeners;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected EventHub getEventHub() {
        if (eventHub == null) {
            eventHub = new EventHub();
        }
        return eventHub;
    }

    protected <E> void publish(Class<E> eventType, E event) {
        if (eventHub != null) {
            eventHub.publish(eventType, event);
        }
    }

    protected boolean hasSubscriptions(Class<?> eventClass) {
        return eventHub != null && eventHub.hasSubscriptions(eventClass);
    }

    protected <E> boolean unsubscribe(Class<E> eventType, Consumer<E> listener) {
        if (eventHub != null) {
            return eventHub.unsubscribe(eventType, listener);
        }
        return false;
    }

    /**
     * Returns the root component that represents the component tree of the composite component.
     *
     * @return the root component that represents the component tree of the composite component
     */
    public T getComposition() {
        Preconditions.checkState(root != null, "Composition root is not initialized");
        return root;
    }

    /**
     * Returns the root component that represents the component tree of the composite component.
     *
     * @return the root component that represents the component tree of the composite component
     * or {@code null} if not yet initialized
     * @see #getComposition()
     */
    @Nullable
    protected T getCompositionOrNull() {
        return root;
    }

    /**
     * Returns an inner component belonging to the whole components tree below this composition.
     *
     * @param id the id of a component
     * @return found component
     * @throws IllegalArgumentException if no component is found with given id
     * @throws IllegalStateException    if the root component can't contain inner components
     * @see #getInnerComponentOptional(String)
     */
    @SuppressWarnings("unchecked")
    protected <C> C getInnerComponent(String id) {
        return (C) getInnerComponentOptional(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Not found component with id '%s'", id)));
    }

    /**
     * Returns an inner component belonging to the whole components tree below this composition.
     *
     * @param id the id of a component
     * @return found component or {@code null} if no component is found
     * @throws IllegalStateException if the root component can't contain inner components
     * @see #getInnerComponent(String)
     */
    @SuppressWarnings("unchecked")
    protected <C> Optional<C> getInnerComponentOptional(String id) {
        Preconditions.checkState(getComposition() instanceof HasComponents,
                "Composition can't contain inner components");

        String fullId = getFullId(id);
        return (Optional<C>) Optional.ofNullable(((HasComponents) getComposition())
                .getComponent(fullId));
    }

    /**
     * Sets the root component that represents the component hierarchy of the composite component.
     *
     * @param composition a component to set as the root
     * @throws IllegalStateException if the root component is already set
     */
    protected void setComposition(T composition) {
        Preconditions.checkState(root == null, "Composition root has already been initialized");
        this.root = composition;

        // Add id prefix to root and all nested component id to uniquely identify them
        updateComponentIds(composition);
    }

    protected void updateComponentIds(T composition) {
        updateIdIfNeeded(composition);

        if (composition instanceof HasComponents) {
            for (Component component : ((HasComponents) composition).getComponents()) {
                updateIdIfNeeded(component);
            }
        }
    }

    protected void updateIdIfNeeded(Component component) {
        final String id = component.getId();
        if (!Strings.isNullOrEmpty(id)) {
            component.setId(getFullId(id));
        }
    }

    protected String getFullId(String id) {
        return String.format("%s_%s", getPrefixId(), id);
    }

    protected String getPrefixId() {
        if (prefixId == null) {
            prefixId = RandomStringUtils.randomAlphabetic(10);
        }
        return prefixId;
    }

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(@Nullable String id) {
        if (!Objects.equals(this.id, id)) {
            if (frame != null) {
                ((FrameImplementation) frame).unregisterComponent(this);
            }

            this.id = id;

            AppUI ui = AppUI.getCurrent();
            if (ui != null) {
                if (root != null && ui.isTestMode()) {
                    com.vaadin.ui.Component vComponent = root.unwrapOrNull(com.vaadin.ui.Component.class);
                    if (vComponent != null) {
                        vComponent.setJTestId(id);
                    }
                }
            }

            assignDebugId();

            if (frame != null) {
                ((FrameImplementation) frame).registerComponent(this);
            }
        }
    }

    protected void assignDebugId() {
        AppUI ui = AppUI.getCurrent();
        if (ui == null) {
            return;
        }

        if (root == null
                || frame == null
                || StringUtils.isEmpty(frame.getId())) {
            return;
        }

        if (ui.isPerformanceTestMode() && getDebugId() == null) {
            String fullFrameId = ComponentsHelper.getFullFrameId(frame);
            TestIdManager testIdManager = ui.getTestIdManager();

            String alternativeId = id != null ? id : getClass().getSimpleName();
            String candidateId = fullFrameId + "." + alternativeId;

            setDebugId(testIdManager.getTestId(candidateId));
        }
    }

    @Nullable
    @Override
    public String getDebugId() {
        return ((HasDebugId) getComposition()).getDebugId();
    }

    @Override
    public void setDebugId(@Nullable String id) {
        ((HasDebugId) getComposition()).setDebugId(id);
    }

    @Nullable
    @Override
    public Component getParent() {
        return getComposition().getParent();
    }

    @Override
    public void setParent(@Nullable Component parent) {
        if (getComposition().getParent() != parent) {
            if (isAttached()) {
                detached();
            }

            getComposition().setParent(parent);

            if (isAttached()) {
                attached();
            }
        }
    }

    @Override
    public boolean isAttached() {
        Component current = getComposition().getParent();
        while (current != null) {
            if (current instanceof Window) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    @Override
    public void attached() {
        ((AttachNotifier) getComposition()).attached();
        enableEventListeners();

        if (hasSubscriptions(AttachEvent.class)) {
            publish(AttachEvent.class, new AttachEvent(this));
        }
    }

    @Override
    public void detached() {
        ((AttachNotifier) getComposition()).detached();
        disableEventListeners();

        if (hasSubscriptions(DetachEvent.class)) {
            publish(DetachEvent.class, new DetachEvent(this));
        }
    }

    @Override
    public Subscription addAttachListener(Consumer<AttachEvent> listener) {
        return getEventHub().subscribe(AttachEvent.class, listener);
    }

    @Override
    public Subscription addDetachListener(Consumer<DetachEvent> listener) {
        return getEventHub().subscribe(DetachEvent.class, listener);
    }

    @Override
    public boolean isEnabled() {
        return getComposition().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        getComposition().setEnabled(enabled);
    }

    @Override
    public boolean isResponsive() {
        return getComposition().isResponsive();
    }

    @Override
    public void setResponsive(boolean responsive) {
        getComposition().setResponsive(responsive);
    }

    @Override
    public boolean isVisible() {
        return getComposition().isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        getComposition().setVisible(visible);
    }

    @Override
    public boolean isVisibleRecursive() {
        return getComposition().isVisibleRecursive();
    }

    @Override
    public boolean isEnabledRecursive() {
        return getComposition().isEnabledRecursive();
    }

    @Override
    public float getHeight() {
        return getComposition().getHeight();
    }

    @Override
    public SizeUnit getHeightSizeUnit() {
        return getComposition().getHeightSizeUnit();
    }

    @Override
    public void setHeight(@Nullable String height) {
        getComposition().setHeight(height);
    }

    @Override
    public float getWidth() {
        return getComposition().getWidth();
    }

    @Override
    public SizeUnit getWidthSizeUnit() {
        return getComposition().getWidthSizeUnit();
    }

    @Override
    public void setWidth(@Nullable String width) {
        getComposition().setWidth(width);
    }

    @Override
    public Alignment getAlignment() {
        return getComposition().getAlignment();
    }

    @Override
    public void setAlignment(Alignment alignment) {
        getComposition().setAlignment(alignment);
    }

    @Override
    public String getStyleName() {
        return getComposition().getStyleName();
    }

    @Override
    public void setStyleName(@Nullable String styleName) {
        getComposition().setStyleName(styleName);
    }

    @Override
    public void addStyleName(String styleName) {
        getComposition().addStyleName(styleName);
    }

    @Override
    public void removeStyleName(String styleName) {
        getComposition().removeStyleName(styleName);
    }

    @Override
    public <X> X unwrap(Class<X> internalComponentClass) {
        return getComposition().unwrap(internalComponentClass);
    }

    @Nullable
    @Override
    public <X> X unwrapOrNull(Class<X> internalComponentClass) {
        return getComposition().unwrapOrNull(internalComponentClass);
    }

    @Override
    public <X> void withUnwrapped(Class<X> internalComponentClass, Consumer<X> action) {
        getComposition().withUnwrapped(internalComponentClass, action);
    }

    @Override
    public <X> X unwrapComposition(Class<X> internalCompositionClass) {
        return getComposition().unwrapComposition(internalCompositionClass);
    }

    @Nullable
    @Override
    public <X> X unwrapCompositionOrNull(Class<X> internalCompositionClass) {
        return getComposition().unwrapCompositionOrNull(internalCompositionClass);
    }

    @Override
    public <X> void withUnwrappedComposition(Class<X> internalCompositionClass, Consumer<X> action) {
        getComposition().withUnwrappedComposition(internalCompositionClass, action);
    }

    @Nullable
    @Override
    public Frame getFrame() {
        return frame;
    }

    @Override
    public void setFrame(@Nullable Frame frame) {
        this.frame = frame;

        if (frame instanceof FrameImplementation) {
            ((FrameImplementation) frame).registerComponent(this);
        }

        if (getComposition() instanceof BelongToFrame) {
            ((BelongToFrame) getComposition()).setFrame(frame);
        }

        if (getDebugId() == null) {
            assignDebugId();
        }
    }

    @Override
    public boolean isHtmlSanitizerEnabled() {
        return ((HasHtmlSanitizer) getComposition()).isHtmlSanitizerEnabled();
    }

    @Override
    public void setHtmlSanitizerEnabled(boolean htmlSanitizerEnabled) {
        ((HasHtmlSanitizer) getComposition()).setHtmlSanitizerEnabled(htmlSanitizerEnabled);
    }

    /**
     * An Event that is fired right before the composite component instance is returned
     * by {@link io.jmix.ui.UiComponents} bean. By this time a composite component
     * is created, has context, all beans are injected and composition is loaded from descriptor if present.
     */
    @TriggerOnce
    public static class CreateEvent extends EventObject {

        public CreateEvent(CompositeComponent source) {
            super(source);
        }

        @Override
        public CompositeComponent getSource() {
            return (CompositeComponent) super.getSource();
        }
    }

    /**
     * Registers a new {@link CreateEvent} listener.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener added to a source
     */
    protected Subscription addCreateListener(Consumer<CreateEvent> listener) {
        return getEventHub().subscribe(CreateEvent.class, listener);
    }

    @Internal
    protected List<ApplicationListener> getUiEventListeners() {
        return uiEventListeners == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(uiEventListeners);
    }

    @Internal
    protected void setUiEventListeners(List<ApplicationListener> uiEventListeners) {
        this.uiEventListeners = uiEventListeners;
    }

    @Internal
    protected void enableEventListeners() {
        List<ApplicationListener> listeners = getUiEventListeners();
        if (CollectionUtils.isNotEmpty(listeners)) {
            AppUI ui = AppUI.getCurrent();
            UiEventsMulticaster multicaster = ui.getUiEventsMulticaster();

            for (ApplicationListener listener : uiEventListeners) {
                multicaster.addApplicationListener(listener);
            }
        }
    }

    @Internal
    protected void disableEventListeners() {
        List<ApplicationListener> listeners = getUiEventListeners();
        if (CollectionUtils.isNotEmpty(listeners)) {
            AppUI ui = AppUI.getCurrent();
            UiEventsMulticaster multicaster = ui.getUiEventsMulticaster();

            for (ApplicationListener listener : uiEventListeners) {
                multicaster.removeApplicationListener(listener);
            }
        }
    }
}
