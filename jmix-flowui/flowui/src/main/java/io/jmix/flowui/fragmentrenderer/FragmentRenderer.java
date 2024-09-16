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

package io.jmix.flowui.fragmentrenderer;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentData;
import io.jmix.flowui.model.InstanceContainer;

/**
 * Base class for {@link Fragment} components that will be used as {@link ComponentRenderer} for components
 * that support renderers.
 *
 * @param <T> the type of the content
 * @param <E> the type of the data item
 */
public abstract class FragmentRenderer<T extends Component, E> extends Fragment<T> {

    /**
     * Sets the corresponding item for the rendered fragment component.
     *
     * @param item the item for the rendered fragment component
     */
    public void setItem(E item) {
        getItemRendererContainer().setItem(item);
    }

    /**
     * @return instance container for the rendering item
     * @throws IllegalStateException         if {@link RendererItemContainer} annotation is not declared
     * @throws UnsupportedOperationException if the renderer item container ID is incorrect
     */
    protected InstanceContainer<E> getItemRendererContainer() {
        RendererItemContainer annotation = getClass().getAnnotation(RendererItemContainer.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            throw new IllegalStateException(
                    "'%s' does not declare @%s"
                            .formatted(getClass().getSimpleName(), RendererItemContainer.class.getSimpleName())
            );
        }

        String[] parts = annotation.value().split("\\.");
        FragmentData fragmentData;
        if (parts.length == 1) {
            fragmentData = getFragmentData();
        } else {
            throw new UnsupportedOperationException(
                    String.format("Can't obtain renderer item container with id: '%s'", annotation.value()));
        }

        return fragmentData.getContainer(parts[parts.length - 1]);
    }
}