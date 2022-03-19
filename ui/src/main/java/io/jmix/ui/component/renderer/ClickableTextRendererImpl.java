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

import com.vaadin.ui.renderers.Renderer;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.widget.renderer.JmixClickableTextRenderer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A renderer for presenting simple plain-text string values as a link with call back handler.
 */
@Component(DataGrid.ClickableTextRenderer.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ClickableTextRendererImpl<T>
        extends AbstractClickableRenderer<T, String>
        implements DataGrid.ClickableTextRenderer<T> {

    public ClickableTextRendererImpl() {
        this("");
    }

    public ClickableTextRendererImpl(String nullRepresentation) {
        this(null, nullRepresentation);
    }

    public ClickableTextRendererImpl(Consumer<DataGrid.RendererClickEvent<T>> listener) {
        this(listener, "");
    }

    public ClickableTextRendererImpl(@Nullable Consumer<DataGrid.RendererClickEvent<T>> listener, String nullRepresentation) {
        super(listener);
        this.nullRepresentation = nullRepresentation;
    }

    @Override
    protected Renderer<String> createImplementation() {
        if (listener != null) {
            return new JmixClickableTextRenderer<>(createClickListenerWrapper(listener), getNullRepresentation());
        } else {
            return new JmixClickableTextRenderer<>(getNullRepresentation());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void copy(DataGrid.Renderer existingRenderer) {
        if (existingRenderer instanceof ClickableTextRendererImpl) {
            setRendererClickListener(((ClickableTextRendererImpl) existingRenderer).listener);
            setNullRepresentation(((ClickableTextRendererImpl) existingRenderer).getNullRepresentation());
        }
    }

    @Override
    public String getNullRepresentation() {
        return super.getNullRepresentation();
    }

    @Override
    public void setNullRepresentation(String nullRepresentation) {
        super.setNullRepresentation(nullRepresentation);
    }
}
