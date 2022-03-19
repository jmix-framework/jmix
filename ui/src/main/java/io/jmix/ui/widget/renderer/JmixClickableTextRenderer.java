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

package io.jmix.ui.widget.renderer;

import com.vaadin.ui.renderers.ClickableRenderer;

public class JmixClickableTextRenderer<T> extends ClickableRenderer<T, String> {

    /**
     * Creates a new clickable text renderer.
     */
    public JmixClickableTextRenderer() {
        this("");
    }

    /**
     * Creates a new clickable text renderer.
     *
     * @param nullRepresentation the textual representation of {@code null} value
     */
    public JmixClickableTextRenderer(String nullRepresentation) {
        super(String.class, nullRepresentation);
    }

    /**
     * Creates a new clickable text renderer and adds the given click listener to it.
     *
     * @param listener the click listener to register
     */
    public JmixClickableTextRenderer(RendererClickListener<T> listener) {
        this(listener, "");
    }

    /**
     * Creates a new clickable text renderer and adds the given click listener to it.
     *
     * @param listener           the click listener to register
     * @param nullRepresentation the textual representation of {@code null} value
     */
    public JmixClickableTextRenderer(RendererClickListener<T> listener, String nullRepresentation) {
        this(nullRepresentation);
        addClickListener(listener);
    }
}