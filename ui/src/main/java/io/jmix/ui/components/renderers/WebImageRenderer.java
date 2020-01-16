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

import com.vaadin.ui.renderers.Renderer;
import io.jmix.core.entity.Entity;
import io.jmix.ui.components.DataGrid;
import io.jmix.ui.widgets.renderers.CubaImageRenderer;

import java.util.function.Consumer;

/**
 * A renderer for presenting images. The value of the corresponding property
 * is used as the image location. Location can be a theme resource or URL.
 */
public class WebImageRenderer<T extends Entity>
        extends WebAbstractClickableRenderer<T, String>
        implements DataGrid.ImageRenderer<T> {

    public WebImageRenderer() {
    }

    public WebImageRenderer(Consumer<DataGrid.RendererClickEvent<T>> listener) {
        super(listener);
    }

    @Override
    protected Renderer<String> createImplementation() {
        if (listener != null) {
            return new CubaImageRenderer<>(createClickListenerWrapper(listener));
        } else {
            return new CubaImageRenderer<>();
        }
    }
}
