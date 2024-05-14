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
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.UiComponents;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

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

    @SuppressWarnings("unchecked")
    protected <C extends Component> C getInnerComponent(String id) {
        return (C) findInnerComponent(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("Not found component with id '%s'", id)));
    }

    @SuppressWarnings("unchecked")
    protected <C extends Component> Optional<C> findInnerComponent(String id) {
        return (Optional<C>) CompositeComponentUtils.findComponent(this, id);
    }

    protected Registration addPostInitListener(ComponentEventListener<PostInitEvent> listener) {
        return getEventBus().addListener(PostInitEvent.class, listener);
    }

    public static class PostInitEvent extends ComponentEvent<CompositeComponent<?>> {

        public PostInitEvent(CompositeComponent<?> source) {
            super(source, false);
        }
    }
}
