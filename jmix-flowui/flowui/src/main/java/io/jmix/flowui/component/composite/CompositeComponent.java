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

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;

import java.util.Optional;
import java.util.stream.Stream;

public abstract class CompositeComponent<T extends Component> extends Component {

    private transient boolean contentIsInitializing = false;

    protected T content;

    protected CompositeComponent() {
        super(null);
    }

    @SuppressWarnings("unchecked")
    protected T initContent() {
        return CompositeComponentUtils.createContent((Class<? extends CompositeComponent<T>>) getClass());
    }

    /**
     * Gets the content of the composite, i.e. the component the composite is
     * wrapping.
     *
     * @return the content for the composite, never {@code null}
     */
    public T getContent() {
//        Preconditions.checkState(content != null, "Composition content is not initialized");
        if (content == null) {
            try {
                if (contentIsInitializing) {
                    throw new IllegalStateException(
                            "The content is not yet initialized. "
                                    + "Detected direct or indirect call to 'getContent' from 'initContent'. "
                                    + "You may not call any framework method on a '"
                                    + CompositeComponent.class.getSimpleName()
                                    + "' instance before 'initContent' has completed initializing the component.");
                }
                contentIsInitializing = true;
                T newContent = initContent();
                if (newContent == null) {
                    throw new IllegalStateException("'initContent' returned null instead of a component");
                }
                setContent(newContent);
            } finally {
                contentIsInitializing = false;
            }
        }
        return content;
    }

    protected void setContent(T content) {
        io.jmix.core.common.util.Preconditions.checkNotNullArgument(content, "Composition content cannot be 'null'");
        Preconditions.checkState(content.getElement().getComponent().isPresent(),
                "Composite should never be attached to an element which is not attached to a component");
        Preconditions.checkState(this.content == null, "Composition content has already been initialized");
        this.content = content;
        Element element = content.getElement();
        // Always replace the composite reference as this will be called from
        // inside out, so the end result is that the element refers to the
        // outermost composite in the probably rare case that multiple
        // composites are nested
        // TODO: gg, replace somehow
//        CompositeComponentUtils.setComponent(element, this);
    }

    /**
     * Gets the root element of this composite.
     * <p>
     * For a composite, the root element is the same as the root element of the
     * content of the composite.
     *
     * @return the root element of this component
     */
    @Override
    public Element getElement() {
        return getContent().getElement();
    }

    /**
     * Gets the child components of this composite.
     * <p>
     * A composite always has one child component, returned by
     * {@link #getContent()}.
     *
     * @return the child component of this composite
     */
    @Override
    public Stream<Component> getChildren() {
        return Stream.of(getContent());
    }

    @SuppressWarnings("unchecked")
    protected <C extends Component> C getInnerComponent(String id) {
        return (C) findInnerComponent(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Not found component with id '%s'", id)));
    }

    @SuppressWarnings("unchecked")
    protected <C extends Component> Optional<C> findInnerComponent(String id) {
        return (Optional<C>) CompositeComponentUtils.findComponent(this, id);
    }
}
