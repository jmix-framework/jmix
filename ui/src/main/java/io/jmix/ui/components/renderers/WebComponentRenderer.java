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

package io.jmix.ui.components.renderers;

import com.vaadin.data.ValueProvider;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.renderers.Renderer;
import io.jmix.core.Entity;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.DataGrid;
import io.jmix.ui.components.impl.WebAbstractDataGrid;

/**
 * A renderer for UI components.
 */
public class WebComponentRenderer<T extends Entity> extends WebAbstractDataGrid.AbstractRenderer<T, com.vaadin.ui.Component>
        implements DataGrid.ComponentRenderer {

    @Override
    protected Renderer<com.vaadin.ui.Component> createImplementation() {
        return new ComponentRenderer();
    }

    @Override
    public ValueProvider<Component, com.vaadin.ui.Component> getPresentationValueProvider() {
        return (ValueProvider<Component, com.vaadin.ui.Component>) value ->
                value.unwrap(com.vaadin.ui.Component.class);
    }
}
