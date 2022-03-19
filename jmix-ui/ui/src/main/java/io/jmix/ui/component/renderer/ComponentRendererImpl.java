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

package io.jmix.ui.component.renderer;

import com.vaadin.data.ValueProvider;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.renderers.Renderer;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.impl.AbstractDataGrid;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;

/**
 * A renderer for UI components.
 */
@org.springframework.stereotype.Component(DataGrid.ComponentRenderer.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ComponentRendererImpl<T>
        extends AbstractDataGrid.AbstractRenderer<T, com.vaadin.ui.Component>
        implements DataGrid.ComponentRenderer {

    @Override
    protected Renderer<com.vaadin.ui.Component> createImplementation() {
        return new ComponentRenderer();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void copy(DataGrid.Renderer existingRenderer) {
        if (existingRenderer instanceof ComponentRendererImpl) {
            setNullRepresentation(((ComponentRendererImpl) existingRenderer).getNullRepresentation());
        }
    }

    @Nullable
    @Override
    public ValueProvider<Component, com.vaadin.ui.Component> getPresentationValueProvider() {
        return (ValueProvider<Component, com.vaadin.ui.Component>) value ->
                value.unwrap(com.vaadin.ui.Component.class);
    }
}
