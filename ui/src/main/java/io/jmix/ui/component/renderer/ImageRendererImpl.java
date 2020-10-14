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
import io.jmix.ui.widget.renderer.JmixImageRenderer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * A renderer for presenting images. The value of the corresponding property
 * is used as the image location. Location can be a theme resource or URL.
 */
@Component(DataGrid.ImageRenderer.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImageRendererImpl<T>
        extends AbstractClickableRenderer<T, String>
        implements DataGrid.ImageRenderer<T> {

    public ImageRendererImpl() {
    }

    public ImageRendererImpl(Consumer<DataGrid.RendererClickEvent<T>> listener) {
        super(listener);
    }

    @Override
    protected Renderer<String> createImplementation() {
        if (listener != null) {
            return new JmixImageRenderer<>(createClickListenerWrapper(listener));
        } else {
            return new JmixImageRenderer<>();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void copy(DataGrid.Renderer existingRenderer) {
        if (existingRenderer instanceof ImageRendererImpl) {
            setRendererClickListener(((ImageRendererImpl) existingRenderer).listener);
            setNullRepresentation(((ImageRendererImpl) existingRenderer).getNullRepresentation());
        }
    }
}
