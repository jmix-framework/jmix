/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.fragment;

import com.vaadin.flow.component.*;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A fragment encapsulates a {@link Component} tree to allow creation of new
 * components by composing existing components. The main purpose of fragments
 * is to be used as a part of views and other fragments. By encapsulating the
 * component, its API can be hidden or presented in a different way for the
 * user of the fragment.
 * <p>
 * The encapsulated component tree is available through {@link #getContent()}.
 * Fragment will by default look at the generic type declaration of its subclass
 * to find the content type and create an instance using {@link UiComponents}.
 * You can also override {@link #initContent()} to manually create the component
 * tree or define components tree using XML markup and bind it to a fragment
 * using {@link FragmentDescriptor}.
 * <p>
 * Fragment is a way to hide API on the server side. It does not contribute any
 * element to the {@link Element} tree.
 *
 * @param <T> the type of the content
 */
public abstract class Fragment<T extends Component> extends Composite<T> implements FragmentOwner {

    protected UiComponents uiComponents;

    protected FragmentData fragmentData;
    protected FragmentActions fragmentActions;

    protected FragmentOwner parentController;

    private List<ApplicationListener<?>> applicationEventListeners;     // Global event listeners

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    protected FragmentData getFragmentData() {
        return fragmentData;
    }

    protected void setFragmentData(FragmentData fragmentData) {
        this.fragmentData = fragmentData;
        fragmentData.setFragmentId(getId().orElse(getClass().getSimpleName()));
    }

    protected FragmentActions getFragmentActions() {
        return fragmentActions;
    }

    protected void setFragmentActions(FragmentActions fragmentActions) {
        this.fragmentActions = fragmentActions;
    }

    protected FragmentOwner getParentController() {
        return parentController;
    }

    protected void setParentController(FragmentOwner parentController) {
        this.parentController = parentController;
    }

    @SuppressWarnings("unchecked")
    protected T initContent() {
        Class<? extends Component> type = FragmentUtils
                .findContentType((Class<? extends Fragment<?>>) getClass());
        return ((T) uiComponents.create(type));
    }

    /**
     * Returns the inner component with given id.
     *
     * @param id  id of the component
     * @param <C> component type
     * @return the inner component with given id
     * @throws IllegalArgumentException if an inner component with given id is not found
     */
    @SuppressWarnings("unchecked")
    protected <C extends Component> C getInnerComponent(String id) {
        return (C) findInnerComponent(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Not found component with id '%s'", id)));
    }

    /**
     * Returns an {@link Optional} describing the inner component with given
     * id, or an empty {@link Optional}.
     *
     * @param id  id of the component
     * @param <C> component type
     * @return an {@link Optional} describing the found component,
     * or an empty {@link Optional}
     */
    @SuppressWarnings("unchecked")
    protected <C extends Component> Optional<C> findInnerComponent(String id) {
        return (Optional<C>) FragmentUtils.findComponent(this, id);
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    private void onHostReadyInternal(final View.ReadyEvent event) {
        // We cannot refresh the state of actions when an EnableRule is added
        // because the logic of EnableRule may rely on something that will be
        // initialized in a screen event. To prevent breaking changes, it is
        // more robust to refresh actions' states in the host view's 'ReadyEvent'
        // listener
        UiComponentUtils.refreshActionsState(getFragmentActions());
        UiComponentUtils.refreshActionsState(getContent());
    }

    /**
     * Adds {@link ReadyEvent} listener.
     *
     * @param listener the listener to add, not {@code null}
     * @return a registration object that can be used for removing the listener
     */
    protected Registration addReadyListener(ComponentEventListener<ReadyEvent> listener) {
        return getEventBus().addListener(ReadyEvent.class, listener);
    }

    List<ApplicationListener<?>> getApplicationEventListeners() {
        return applicationEventListeners != null
                ? Collections.unmodifiableList(applicationEventListeners)
                : Collections.emptyList();
    }

    void setApplicationEventListeners(@Nullable List<ApplicationListener<?>> listeners) {
        this.applicationEventListeners = listeners;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        addApplicationListeners();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        removeApplicationListeners();
    }

    private void addApplicationListeners() {
        uiEventsManager().ifPresent(uiEventsManager -> {
            uiEventsManager.removeApplicationListeners(this);
            List<ApplicationListener<?>> listeners = getApplicationEventListeners();
            for (ApplicationListener<?> listener : listeners) {
                uiEventsManager.addApplicationListener(this, listener);
            }
        });
    }

    private void removeApplicationListeners() {
        uiEventsManager().ifPresent(uiEventsManager ->
                uiEventsManager.removeApplicationListeners(this));
    }

    private Optional<UiEventsManager> uiEventsManager() {
        VaadinSession session = VaadinSession.getCurrent();
        return session != null
                ? Optional.ofNullable(session.getAttribute(UiEventsManager.class))
                : Optional.empty();
    }

    /**
     * The event that is fired after the fragment and all its declaratively
     * defined inner components are created and fully initialized.
     * <p>
     * In this event listener, you can make final configuration of the
     * fragment and its inner components, e.g. obtain inner components
     * using {@link #getInnerComponent(String)} and add their specific
     * event listeners. For example:
     * <pre>
     *     public StepperField() {
     *         addReadyListener(this::onReady);
     *     }
     *
     *     private void onReady(ReadyEvent readyEvent) {
     *         valueField = getInnerComponent("valueField");
     *         setValue(0);
     *
     *         upBtn = getInnerComponent("upBtn");
     *         upBtn.addClickListener(__ -> updateValue(1));
     *
     *         downBtn = getInnerComponent("downBtn");
     *         downBtn.addClickListener(__ -> updateValue(-1));
     *     }
     * </pre>
     *
     * @see #addReadyListener(ComponentEventListener)
     */
    public static class ReadyEvent extends ComponentEvent<Fragment<?>> {

        public ReadyEvent(Fragment<?> source) {
            super(source, false);
        }
    }
}
