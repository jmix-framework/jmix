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

import com.vaadin.ui.renderers.ClickableRenderer;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.impl.AbstractDataGrid;
import io.jmix.ui.component.impl.WrapperUtils;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class AbstractClickableRenderer<T, V>
        extends AbstractDataGrid.AbstractRenderer<T, V>
        implements DataGrid.HasRendererClickListener<T> {

    protected Consumer<DataGrid.RendererClickEvent<T>> listener;

    public AbstractClickableRenderer() {
        this(null);
    }

    public AbstractClickableRenderer(@Nullable Consumer<DataGrid.RendererClickEvent<T>> listener) {
        super("");
        this.listener = listener;
    }

    protected ClickableRenderer.RendererClickListener<T> createClickListenerWrapper(Consumer<DataGrid.RendererClickEvent<T>> listener) {
        return (ClickableRenderer.RendererClickListener<T>) e -> {
            if (getDataGrid() != null) {
                DataGrid.Column column = getColumnByGridColumn(e.getColumn());
                DataGrid.RendererClickEvent<T> event = new DataGrid.RendererClickEvent<>(getDataGrid(),
                        WrapperUtils.toMouseEventDetails(e), e.getItem(), column.getId());
                listener.accept(event);
            }
        };
    }

    @Override
    public void setRendererClickListener(Consumer<DataGrid.RendererClickEvent<T>> listener) {
        checkRendererNotSet();
        this.listener = listener;
    }
}
