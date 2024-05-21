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

package io.jmix.flowui.component.composite;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.UiComponents;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * A composite encapsulates a {@link Component} tree to allow creation of new
 * components by composing existing components. By encapsulating the component,
 * its API can be hidden or presented in a different way for the user of the
 * composite.
 * <p>
 * The encapsulated component tree is available through {@link #getContent()}.
 * Composite will by default look at the generic type declaration of its subclass
 * to find the content type and create an instance using {@link UiComponents}.
 * You can also override {@link #initContent()} to manually create the component
 * tree or define components tree using XML markup and bind it to a composite
 * component using {@link CompositeDescriptor}.
 * <p>
 * Composite is a way to hide API on the server side. It does not contribute any
 * element to the {@link Element} tree.
 *
 * @param <T> the type of the content
 */
public abstract class CompositeComponent<T extends Component> extends Composite<T> {

    protected UiComponents uiComponents;

    protected CompositeComponentActions compositeComponentActions;

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    protected CompositeComponentActions getActions() {
        return compositeComponentActions;
    }

    protected void setActions(CompositeComponentActions compositeComponentActions) {
        this.compositeComponentActions = compositeComponentActions;
    }

    @SuppressWarnings("unchecked")
    protected T initContent() {
        Class<? extends Component> type = CompositeComponentUtils
                .findContentType((Class<? extends CompositeComponent<?>>) getClass());
        return ((T) uiComponents.create(type));
    }

    /**
     * Returns the inner component with given composite id.
     *
     * @param id  composite id
     * @param <C> component type
     * @return the inner component with given composite id
     * @throws IllegalArgumentException if an inner component with given
     *                                  composite id is not found
     */
    @SuppressWarnings("unchecked")
    protected <C extends Component> C getInnerComponent(String id) {
        return (C) findInnerComponent(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Not found component with id '%s'", id)));
    }

    /**
     * Returns an {@link Optional} describing the inner component with given
     * composite id, or an empty {@link Optional}.
     *
     * @param id  composite id
     * @param <C> component type
     * @return an {@link Optional} describing the found component,
     * or an empty {@link Optional}
     */
    @SuppressWarnings("unchecked")
    protected <C extends Component> Optional<C> findInnerComponent(String id) {
        return (Optional<C>) CompositeComponentUtils.findComponent(this, id);
    }

    /**
     * Adds {@link PostInitEvent} listener.
     *
     * @param listener the listener to add, not {@code null}
     * @return a registration object that can be used for removing the listener
     */
    protected Registration addPostInitListener(ComponentEventListener<PostInitEvent> listener) {
        return getEventBus().addListener(PostInitEvent.class, listener);
    }

    /**
     * The event that is fired after the composite component and all its
     * declaratively defined inner components are created and fully
     * initialized.
     * <p>
     * In this event listener, you can make final configuration of the
     * composite component and its inner components, e.g. obtain inner
     * components using {@link #getInnerComponent(String)} and add their
     * specific event listeners. For example:
     * <pre>
     *     public StepperField() {
     *         addPostInitListener(this::onPostInit);
     *     }
     *
     *     private void onPostInit(PostInitEvent postInitEvent) {
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
     * @apiNote {@link PostInitEvent} is not fired if components tree is
     * created without using XML descriptor, i.e. by overriding
     * {@link #initContent()} to manually create the component tree
     * @see #addPostInitListener(ComponentEventListener)
     */
    public static class PostInitEvent extends ComponentEvent<CompositeComponent<?>> {

        public PostInitEvent(CompositeComponent<?> source) {
            super(source, false);
        }
    }
}
